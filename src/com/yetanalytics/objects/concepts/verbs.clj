(ns com.yetanalytics.objects.concepts.verbs
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
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

(s/def ::verb-basic
  (fn [{:keys [object]}] (s/valid? ::verb object)))

(s/def ::broader-concept-iris
  (fn [{:keys [object concepts-table]}]
    (let [version (:in-scheme object) iri-vec (:broader object)]
      (cu/relate-concepts "Verb" version concepts-table iri-vec))))

(s/def ::narrower-concept-iris
  (fn [{:keys [object concepts-table]}]
    (let [version (:in-scheme object) iri-vec (:narrower object)]
      (cu/relate-concepts "Verb" version concepts-table iri-vec))))

(s/def ::related-concept-iris
  (fn [{:keys [object concepts-table]}]
    (let [version (:in-scheme object) iri-vec (:related object)]
      (cu/relate-concepts "Verb" version concepts-table iri-vec))))

(s/def ::verb+
  (s/and ::verb-basic
         ::u/in-scheme-valid?
         ::broader-concept-iris
         ::narrower-concept-iris
         ::related-concept-iris))

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
