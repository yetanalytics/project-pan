(ns com.yetanalytics.objects.concepts.extensions.activity
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity Extensions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ActivityExtension"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::recommended-activity-types ::ax/array-of-iri)
;; TODO Clarify on what it means to "not be used"
;;(s/def ::recommended-verbs (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(s/def ::no-recommended-verbs
  (fn [ext] (not (contains? ext :recommended-verbs))))

(s/def ::extension
  (s/and (s/keys
          :req-un [::id ::type ::in-scheme ::pref-label ::definition]
          :opt-un [::deprecated ::recommended-activity-types ::context
                   ::schema ::inline-schema])
         ::util/inline-or-iri
         ::no-recommended-verbs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a vector of edges in the form [src dest {:type kword}]
(defmethod util/edges-with-attrs "ActivityExtension"
  [{:keys [id recommended-activity-types]}]
  (if (some? recommended-activity-types)
    (mapv #(vector id % {:type :recommended-activity-types})
          recommended-activity-types)
    []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: json-ld context validation
;; context - valid json-ld context

;; TODO: get string from iri

;; schema - json-schema string at other end of iri
