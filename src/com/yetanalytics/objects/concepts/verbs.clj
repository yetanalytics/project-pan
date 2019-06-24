(ns com.yetanalytics.objects.concepts.verbs
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Verb
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(s/def ::verb
  (s/keys
   :req-un [::id ::type ::in-scheme ::pref-label ::definition]
   :opt-un [::deprecated ::broader ::broad-match ::narrower
            ::narrow-match ::related ::related-match ::exact-match]))

(s/def ::verbs (s/coll-of ::verb :type vector?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::in-scheme-strict-scalar
  (fn [{:keys [in-scheme profile]}]
    (u/in-scheme? in-scheme profile)))

(s/def ::deprecated-strict-scalar
  (fn [{:keys [iri profile]}]
    (cu/iri-in-profile-concepts?
     {:iri iri
      :target-type-str "Verb"
      :profile profile
      :?deprecated true})))

(s/def ::in-profile-strict-scalar
  (fn [{:keys [iri profile]}]
    (cu/iri-in-profile-concepts?
     {:iri iri
      :target-type-str "Verb"
      :profile profile})))

(s/def ::valid-boolean-coll
  (fn [coll] u/valid-boolean-cool? coll))

(s/def ::verb-in-profile-strict
  (fn [{:keys [verb profile]}]
    (let [{:keys [in-scheme broader narrower related]} verb]
      (s/and (s/valid? ::verb verb)
             (s/valid? ::in-scheme-strict-scalar {:in-scheme in-scheme
                                                  :profile profile})
             (if (not-empty broader)
               (s/valid? ::valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::in-profile-strict-scalar
                                           {:iri iri :profile profile})) broader))
               true)
             (if (not-empty narrower)
               (s/valid? ::valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::in-profile-strict-scalar
                                           {:iri iri :profile profile})) narrower))
               true)
             (if (not-empty related)
               (s/valid? ::valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::deprecated-strict-scalar
                                           {:iri iri :profile profile})) related))
               true)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::verb-complete-validation
  (fn [{:keys [verb profile]}]
    (let [{:keys [in-scheme broad-match narrow-match related-match exact-match]} verb]
      (s/valid? ::verb-in-profile-strict {:verb verb :profile profile})
      ;; TODO: outside of profile validation
      ;; - broad-match
      ;; - narrow-match
      ;; - related-match
      ;; - exact-match
      ;; TODO: related-match should
      ;; TODO: exact-match should
      )))





