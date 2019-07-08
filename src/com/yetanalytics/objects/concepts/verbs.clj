(ns com.yetanalytics.objects.concepts.verbs
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Verb
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Verb"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::broader ::ax/array-of-iri)
(s/def ::broad-match ::ax/array-of-iri)
(s/def ::narrower ::ax/array-of-iri)
(s/def ::narrow-match ::ax/array-of-iri)
(s/def ::related ::ax/array-of-iri)
(s/def ::related-match ::ax/array-of-iri)
(s/def ::exact-match ::ax/array-of-iri)

(s/def ::related-only-deprecated
  (fn [atype]
    (if (contains? atype :related)
      (true? (:deprecated atype))
      true)))

(s/def ::verb
  (s/and
   (s/keys
    :req-un [::id ::type ::in-scheme ::pref-label ::definition]
    :opt-un [::deprecated ::broader ::broad-match ::narrower
             ::narrow-match ::related ::related-match ::exact-match])
   ::related-only-deprecated))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod util/edges-with-attrs "Verb"
  [{:keys [id broader broad-match narrower narrow-match related related-match
           exact-match]}]
  (into [] (filter #(some? (second %))
                   (concat
                    (map #(vector id % {:type :broader}) broader)
                    (map #(vector id % {:type :broad-match}) broad-match)
                    (map #(vector id % {:type :narrower}) narrower)
                    (map #(vector id % {:type :narrow-match}) narrow-match)
                    (map #(vector id % {:type :related}) related)
                    (map #(vector id % {:type :related-match}) related-match)
                    (map #(vector id % {:type :exact-match}) exact-match)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: outside of profile validation
;; - broad-match
;; - narrow-match
;; - related-match
;; - exact-match
;; TODO: related-match should
;; TODO: exact-match should
