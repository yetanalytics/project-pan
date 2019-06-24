(ns com.yetanalytics.objects.concepts.extensions.activity
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity Extensions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ActivityExtension"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::recommended-activity-types ::ax/array-of-iri)
(s/def ::recommended-verbs (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(s/def ::inline-or-iri
  (fn [m]
    (cond (contains? m :schema)
          (false? (contains? m :inline-schema))
          (contains? m :inline-schema)
          (false? (contains? m :schema))
          :else true)))

(s/def ::activity-extension
  (s/and (s/keys
          :req-un [::id ::type ::in-scheme ::pref-label ::definition]
          :opt-un [::deprecated ::recommended-activity-types ::recommended-verbs
                   ::context ::schema ::inline-schema])
         ::inline-or-iri))

(s/def ::activity-extensions (s/coll-of ::activity-extension :type vector?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::in-profile-strict-scalar
  (fn [{:keys [iri profile]}]
    (cu/iri-in-profile-concepts?
     {:iri iri
      :target-type-str "ActivityType"
      :profile profile})))

(s/def ::activity-extension-in-profile-strict
  (fn [{:keys [extension profile]}]
    (let [{:keys [in-scheme recommended-activity-types]} extension]
      (s/and (s/valid? ::activity-extension extension)
             (s/valid? ::u/in-scheme-strict-scalar {:in-scheme in-scheme
                                                    :profile profile})
             (if (not-empty recommended-activity-types)
               (s/valid? ::u/valid-boolean-coll
                         (mapv (fn [iri]
                                 (s/valid? ::in-profile-strict-scalar
                                           {:iri iri :profile profile})) recommended-activity-types))
               true)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::activity-extension-complete-validation
  (fn [{:keys [extension profile]}]
    (s/valid? ::activity-extension-in-profile-strict
              {:extension extension :profile profile})
    ;; TODO: json-ld context validation
    ;; context - valid json-ld context

    ;; TODO: get string from iri
    ;; schema - json-schema string at other end of iri
    ))




