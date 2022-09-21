(ns com.yetanalytics.pan.objects.concepts.activity.definition
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [xapi-schema.spec :as xs]
            [com.yetanalytics.pan.axioms  :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.objects.concepts.activity.definition.interaction-type :as itype]))

;; Based off of the activityDefinition specs in xapi-schema:
;; https://github.com/yetanalytics/xapi-schema/blob/master/src/xapi_schema/spec.cljc

;; @context

(def context-url "https://w3id.org/xapi/profiles/activity-context")

(defn- has-context-url? [context-coll]
  (some #(= context-url %) context-coll))

(s/def ::_context
  (s/or :context-uri
        ::ax/uri
        :context-array
        (s/with-gen (s/and ::ax/array-of-uri has-context-url?)
          #(->> (s/gen ::ax/array-of-uri)
                (sgen/fmap (fn [v] (conj v context-url)))))))

;; Basics

(s/def ::name ::ax/language-map)
(s/def ::description ::ax/language-map)
(s/def ::type ::ax/iri)
(s/def ::moreInfo ::ax/irl)

;; Interaction Activities

(s/def ::interactionType
  #{"true-false" "choice" "fill-in" "long-fill-in" "matching" "performance"
    "sequencing" "likert" "numeric" "other"})

;; Technically there are additional requirements for each response string,
;; depending on interactionType (e.g. for "true-false" the string has to
;; either be "true" or "false") but since xapi-schema doesn't model these
;; (quite complex) requirements we don't do it here either.
(s/def ::correctResponsesPattern
  (s/coll-of ::ax/string :kind vector? :min-count 1))

(s/def ::choices itype/interaction-component-coll-spec)
(s/def ::scale itype/interaction-component-coll-spec)
(s/def ::source itype/interaction-component-coll-spec)
(s/def ::target itype/interaction-component-coll-spec)
(s/def ::steps itype/interaction-component-coll-spec)

;; Extensions

;; MUST include a JSON-LD @context in all top-level objects of extensions,
;; or in every top-level object if array-valued.
(s/def ::extension
  (s/or :object (s/keys :req-un [::ctx/_context])
        :array  (s/coll-of (s/or :object (s/keys :req-un [::ctx/_context])
                                 :non-object (s/and any? (comp not map?)))
                           :kind vector?)
        :scalar (s/and any? (comp not coll?))))

(s/def ::extensions
  (s/map-of ::ax/iri ::extension))

;; Putting it all together

(def base-activity-def-spec
  "Map spec for keys shared by all activity definitions."
  (s/keys :req-un [::_context]
          :opt-un [::name
                   ::description
                   ::type
                   ::moreInfo
                   ::extensions]))

(def base-activity-def-keys
  "Coll of keys shared by all activity definitions."
  [:_context
   :name
   :description
   :type
   :moreInfo
   :extensions])

(def itype-activity-def-spec
  (s/merge base-activity-def-spec
           (s/keys :req-un [::interactionType]
                   :opt-un [::correctResponsesPattern])))

(def itype-activity-def-keys
  (into base-activity-def-keys
        [:interactionType
         :correctResponsesPattern]))

(defmulti activity-definition-spec :interactionType)

(defmethod activity-definition-spec nil [_]
  (s/and base-activity-def-spec
         (apply xs/restrict-keys base-activity-def-keys)))

(defmethod activity-definition-spec "choice" [_]
  (s/and (s/merge itype-activity-def-spec
                  (s/keys :opt-un [::choices]))
         (apply xs/restrict-keys
                (into itype-activity-def-keys [:choices]))))

(defmethod activity-definition-spec "sequencing" [_]
  (s/and (s/merge itype-activity-def-spec
                  (s/keys :opt-un [::choices]))
         (apply xs/restrict-keys
                (into itype-activity-def-keys [:choices]))))

(defmethod activity-definition-spec "likert" [_]
  (s/and (s/merge itype-activity-def-spec
                  (s/keys :opt-un [::scale]))
         (apply xs/restrict-keys
                (into itype-activity-def-keys [:scale]))))

(defmethod activity-definition-spec "matching" [_]
  (s/and (s/merge itype-activity-def-spec
                  (s/keys :opt-un [::source ::target]))
         (apply xs/restrict-keys
                (into itype-activity-def-keys [:source :target]))))

(defmethod activity-definition-spec "performance" [_]
  (s/and (s/merge itype-activity-def-spec
                  (s/keys :opt-un [::steps]))
         (apply xs/restrict-keys
                (into itype-activity-def-keys [:steps]))))

(defmethod activity-definition-spec :default [_]
  (s/and itype-activity-def-spec
         (apply xs/restrict-keys itype-activity-def-keys)))
