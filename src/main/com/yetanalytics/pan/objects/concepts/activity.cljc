(ns com.yetanalytics.pan.objects.concepts.activity
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [xapi-schema.spec]
            [com.yetanalytics.pan.axioms  :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.graph   :as graph]
            #?(:cljs [clojure.spec.test.alpha])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Activity"})
(s/def ::inScheme ::ax/iri)
(s/def ::deprecated ::ax/boolean)

;; @context validation
(def context-url "https://w3id.org/xapi/profiles/activity-context")
(s/def ::has-context-url (partial some #(= % context-url)))
(s/def ::_context
  (s/or :context-uri
        ::ax/uri
        :context-array
        (s/with-gen (s/and ::ax/array-of-uri
                           ::has-context-url)
          #(->> (s/gen ::ax/array-of-uri)
                (sgen/fmap (fn [v] (conj v context-url)))))))

;; Important to stringify lang maps to work with xapi-schema.
;; Top-level keys don't have to be stringified, however.
(defn stringify-submaps
  "Stringify keys in maps that exist below the top level, i.e.
   `{:foo {:bar 1}}` becomes `{:foo {\"bar\" 1}}`."
  [m]
  (into {} (map (fn [[k v]] [k (stringify-keys v)]) m)))

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

(def activity-def-keys-spec
  (s/keys :req-un [::_context]
          :opt-un [::extensions]))

(s/def ::activityDefinition
  (s/with-gen
    (s/and (s/nonconforming activity-def-keys-spec)
           (s/conformer #(dissoc % :_context))
           (s/conformer stringify-submaps)
           :activity/definition)
   #(->> (sgen/tuple (s/gen :activity/definition)
                     (s/gen activity-def-keys-spec))
         (sgen/fmap (fn [[adef x]] (merge x (keywordize-keys adef)))))))

(s/def ::activity
  (s/keys :req-un [::id ::type ::inScheme ::activityDefinition]
          :opt-un [::deprecated]))

;; The following MUST is validated during context validation
;; MUST ensure every extension `@context` is sufficient to guarantee all
;; properties in the extension expand to absolute IRIs during JSON-LD
;; processing.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Currently does nothing
(defmethod graph/edges-with-attrs "Activity" [_] [])
