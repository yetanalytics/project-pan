(ns com.yetanalytics.objects.concepts.verbs
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Verb
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(s/def ::id ::ax/iri)
(s/def ::type #{"Verb"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::broader ::ax/array-of-iri)
(s/def ::broadMatch ::ax/array-of-iri)
(s/def ::narrower ::ax/array-of-iri)
(s/def ::narrowMatch ::ax/array-of-iri)
(s/def ::related ::ax/array-of-iri)
(s/def ::relatedMatch ::ax/array-of-iri)
(s/def ::exactMatch ::ax/array-of-iri)

(s/def ::related-only-deprecated
  (fn [atype]
    (if (contains? atype :related)
      (true? (:deprecated atype))
      true)))

(s/def ::verb
  (s/and
   (s/keys
    :req-un [::id ::type ::inScheme ::prefLabel ::definition]
    :opt-un [::deprecated ::broader ::broadMatch ::narrower
             ::narrowMatch ::related ::relatedMatch ::exactMatch])
   ::related-only-deprecated))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a vector of edges in the form [src dest {:type kword}]
(defmethod util/edges-with-attrs "Verb"
  [{:keys [id broader broadMatch narrower narrowMatch related relatedMatch
           exactMatch]}]
  (into [] (filter #(some? (second %))
                   (concat
                    (map #(vector id % {:type :broader}) broader)
                    (map #(vector id % {:type :broadMatch}) broadMatch)
                    (map #(vector id % {:type :narrower}) narrower)
                    (map #(vector id % {:type :narrowMatch}) narrowMatch)
                    (map #(vector id % {:type :related}) related)
                    (map #(vector id % {:type :relatedMatch}) relatedMatch)
                    (map #(vector id % {:type :exactMatch}) exactMatch)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: outside of profile validation
;; - broadMatch
;; - narrowMatch
;; - relatedMatch
;; - exactMatch
;; TODO: relatedMatch should
;; TODO: exactMatch should
