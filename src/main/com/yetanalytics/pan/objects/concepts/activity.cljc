(ns com.yetanalytics.pan.objects.concepts.activity
  (:require [clojure.spec.alpha          :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.graph  :as graph]
            [com.yetanalytics.pan.objects.concepts.activity.definition :as adef]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Activity"})
(s/def ::inScheme ::ax/iri)
(s/def ::deprecated ::ax/boolean)

(s/def ::activityDefinition
  (s/multi-spec adef/activity-definition-spec (fn [gen-val _] gen-val)))

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
