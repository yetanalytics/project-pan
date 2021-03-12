(ns com.yetanalytics.pan.objects.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :refer [stringify-keys]]
            [xapi-schema.spec]
            [com.yetanalytics.pan.axioms :as ax]
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

;; Turn language map keys back into strings
(defn stringify-lang-keys
  [kmap]
  (as-> kmap m
    (if-some [_ (:name m)] (update m :name stringify-keys) m)
    (if-some [_ (:description m)] (update m :description stringify-keys) m)
    (if-some [_ (:scale m)] (update m :scale stringify-keys) m)
    (if-some [_ (:choices m)] (update m :choices stringify-keys) m)
    (if-some [_ (:source m)] (update m :source stringify-keys) m)
    (if-some [_ (:target m)] (update m :target stringify-keys) m)
    (if-some [_ (:steps m)] (update m :steps stringify-keys) m)))

;; Need to use this function instead of s/merge because of restrict-keys in
;; xapi-schema function.
(s/def ::activity-definition-keys
       (s/keys :req-un [::_context]))

(s/def ::activityDefinition
  (s/and ::activity-definition-keys
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
