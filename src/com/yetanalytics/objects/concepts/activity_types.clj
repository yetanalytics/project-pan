(ns com.yetanalytics.objects.concepts.activity-types
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity Type
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ActivityType"})
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

(s/def ::activity-type
  (s/keys
   :req-un [::id ::type ::in-scheme ::pref-label ::definition]
   :opt-un [::deprecated ::broader ::broad-match ::narrower
            ::narrow-match ::related ::related-match ::exact-match]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::deprecated-strict-scalar
  (fn [{:keys [iri profile]}]
    (cu/iri-in-profile-concepts?
     {:iri iri
      :target-type-str "ActivityType"
      :profile profile
      :?deprecated true})))

(s/def ::in-profile-strict-scalar
  (fn [{:keys [iri profile]}]
    (cu/iri-in-profile-concepts?
     {:iri iri
      :target-type-str "ActivityType"
      :profile profile})))

(s/def ::activity-type-in-profile-strict
  (fn [{:keys [activity-type profile]}]
    (let [{:keys [in-scheme broader narrower related]} activity-type]
      (s/and (s/valid? ::activity-type activity-type)
             (s/valid? ::u/in-scheme-strict-scalar {:in-scheme in-scheme
                                                    :profile profile})
             (if (not-empty broader)
               (s/valid? ::u/valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::in-profile-strict-scalar
                                           {:iri iri :profile profile})) broader))
               true)
             (if (not-empty narrower)
               (s/valid? ::u/valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::in-profile-strict-scalar
                                           {:iri iri :profile profile})) narrower))
               true)
             (if (not-empty related)
               (s/valid? ::u/valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::deprecated-strict-scalar
                                           {:iri iri :profile profile})) related))
               true)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::activity-type-complete-validation
  (fn [{:keys [activity-type profile]}]
    (let [{:keys [in-scheme broad-match narrow-match related-match exact-match]} activity-type]
      (s/valid? ::activity-type-in-profile-strict
                {:activity-type activity-type
                 :profile profile})
      ;; TODO: outside of profile validation
      ;; - broad-match
      ;; - narrow-match
      ;; - related-match
      ;; - exact-match
      ;; TODO: related-match should
      ;; TODO: exact-match should
      )))
