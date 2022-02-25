(ns com.yetanalytics.pan.objects.concepts.extensions.context
  (:require [clojure.spec.alpha                         :as s]
            [com.yetanalytics.pan.axioms                :as ax]
            [com.yetanalytics.pan.graph                 :as graph]
            [com.yetanalytics.pan.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context Extensions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ContextExtension"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
;; (s/def ::recommended-activity-types (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::recommendedVerbs ::ax/array-of-iri)
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inlineSchema ::ax/json-schema)

(s/def ::no-recommended-activity-types
  (fn no-rec-ats? [ext] (not (contains? ext :recommendedActivityTypes))))

(s/def ::extension-keys
  (s/keys :req-un [::id ::type ::inScheme ::prefLabel ::definition]
          :opt-un [::deprecated ::recommendedVerbs
                   ::context ::schema ::inlineSchema]))

(s/def ::extension
  (s/and ::extension-keys
         ::cu/inline-or-iri
         ::no-recommended-activity-types))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 

;; Return a vector of edges in the form [src dest {:type kword}]
(defmethod graph/edges-with-attrs "ContextExtension"
  [{:keys [id recommendedVerbs]}]
  (if (some? recommendedVerbs)
    (mapv #(vector id % {:type :recommendedVerbs})
          recommendedVerbs)
    []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: get string from iri
;; schema - json-schema string at other end of iri
