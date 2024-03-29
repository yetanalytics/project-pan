(ns com.yetanalytics.pan.objects.concepts.extensions.activity
  (:require [clojure.spec.alpha                         :as s]
            [com.yetanalytics.pan.axioms                :as ax]
            [com.yetanalytics.pan.graph                 :as graph]
            [com.yetanalytics.pan.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity Extensions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ActivityExtension"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::recommendedActivityTypes ::ax/array-of-iri)
;; TODO Clarify on what it means to "not be used"
;; (s/def ::recommended-verbs (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inlineSchema ::ax/json-schema)

(s/def ::no-recommended-verbs
  (fn no-rec-ats? [ext] (not (contains? ext :recommendedVerbs))))

(s/def ::extension-keys
  (s/keys :req-un [::id ::type ::inScheme ::prefLabel ::definition]
          :opt-un [::deprecated ::recommendedActivityTypes ::context
                   ::schema ::inlineSchema]))

(s/def ::extension
  (s/and ::extension-keys
         ::cu/inline-or-iri
         ::no-recommended-verbs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a vector of edges in the form [src dest {:type kword}]
(defmethod graph/edges-with-attrs "ActivityExtension"
  [{:keys [id recommendedActivityTypes]}]
  (if (some? recommendedActivityTypes)
    (mapv #(vector id % {:type :recommendedActivityTypes})
          recommendedActivityTypes)
    []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: get string from iri
;; schema - json-schema string at other end of iri
