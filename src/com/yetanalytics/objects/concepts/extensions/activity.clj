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
;; TODO Clarify on what it means to "not be used"
(s/def ::recommended-verbs (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(s/def ::extension
  (s/and (s/keys
          :req-un [::id ::type ::in-scheme ::pref-label ::definition]
          :opt-un [::deprecated ::recommended-activity-types ::context
                   ::schema ::inline-schema])
         ::cu/inline-or-iri))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::extension-basic
  (fn [{:keys [object]}] (s/valid? ::extension object)))

(s/def ::rec-activity-type-uris
  (fn [{:keys [object concepts-table]}]
    (let [uri-vec (:recommended-activity-types object)]
      (if (some? uri-vec)
        (every?
         (map (cu/recommended-concept "ActivityType" concepts-table) uri-vec))
        true))))

(s/def ::extension+
  (s/and ::extension-basic
         ::u/in-scheme-valid?
         ::rec-activity-type-uris))

; (s/def ::in-profile-strict-scalar
;   (fn [{:keys [iri profile]}]
;     (cu/iri-in-profile-concepts?
;      {:iri iri
;       :target-type-str "ActivityType"
;       :profile profile})))

; (s/def ::extension-in-profile-strict
;   (fn [{:keys [extension profile]}]
;     (let [{:keys [in-scheme recommended-activity-types]} extension]
;       (s/and (s/valid? ::extension extension)
;              (s/valid? ::u/in-scheme-strict-scalar {:in-scheme in-scheme
;                                                     :profile profile})
;              (if (not-empty recommended-activity-types)
;                (s/valid? ::u/valid-boolean-coll
;                          (mapv (fn [iri]
;                                  (s/valid? ::in-profile-strict-scalar
;                                            {:iri iri :profile profile})) recommended-activity-types))
;                true)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(s/def ::extension-complete-validation
  (fn [{:keys [extension profile]}]
    (s/valid? ::extension-in-profile-strict
              {:extension extension :profile profile})
    ;; TODO: json-ld context validation
    ;; context - valid json-ld context

    ;; TODO: get string from iri


;; schema - json-schema string at other end of iri
    ))
