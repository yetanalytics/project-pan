(ns com.yetanalytics.objects.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [clojure.set :refer [rename-keys]]
            [clojure.walk :refer [stringify-keys]]
            [camel-snake-kebab.core :as csk]
            [xapi-schema.spec]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.graph :as graph]
            [com.yetanalytics.util :as util]))

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

;; Turn language map keys back into strings
(defn stringify-lang-keys
  [kmap] (-> kmap
             (update :name stringify-keys)
             (update :description stringify-keys)))

;; Need to use this function instead of s/merge because of restrict-keys in
;; xapi-schema function.
(s/def ::activityDefinition
  (s/and (s/keys :req-un [::_context])
         (fn [adef]
           (s/valid? :activity/definition
                     (stringify-lang-keys (dissoc adef :_context))))))

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