(ns com.yetanalytics.pan.objects.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :refer [stringify-keys]]
            [xapi-schema.spec]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.graph :as graph]))

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
  (s/or :context-uri ::ax/uri
        :context-array (s/and ::ax/array-of-uri
                              ::has-context-url)))

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
                                 :non-object (comp not map?))
                           :kind vector?)
        :scalar (comp not coll?)))

(s/def ::extensions
  (s/map-of ::ax/iri ::extension))

(s/def ::activityDefinition
  (s/and (s/nonconforming (s/keys :req-un [::_context]
                                  :opt-un [::extensions]))
         (s/conformer #(dissoc % :_context))
         (s/conformer stringify-submaps)
         :activity/definition))

(s/def ::activity
  (s/keys :req-un [::id ::type ::inScheme ::activityDefinition]
          :opt-un [::deprecated]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Currently does nothing
(defmethod graph/edges-with-attrs "Activity" [_] [])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; advanced processing for spec MUSTS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: spec MUSTs activity definition w/ extension
;; - MUST include JSON-LD @context in all top-level objects of extensions
;; - MUST ensure every extension @context is sufficient to guareantee all 
;;   properties in the extension expand to absolute IRIs during JSON-LD 
;;   processing
;; - see Profile Authors: bellow table at:
;; https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#74-activities
