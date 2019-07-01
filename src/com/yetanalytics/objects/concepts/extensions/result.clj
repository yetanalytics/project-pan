(ns com.yetanalytics.objects.concepts.extensions.result
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Result Extensions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ResultExtension"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
;; (s/def ::recommended-activity-types (fn [coll] (-> coll not-empty nil?))) ;; if present, it should be nil
(s/def ::recommended-verbs ::ax/array-of-iri)
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(s/def ::no-recommended-activity-types
  (fn [ext] (not (contains? ext :recommended-activity-types))))

(s/def ::extension
  (s/and (s/keys
          :req-un [::id ::type ::in-scheme ::pref-label ::definition]
          :opt-un [::deprecated ::recommended-verbs
                   ::context ::schema ::inline-schema])
         ::cu/inline-or-iri
         ::no-recommended-activity-types))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(s/def ::extension-basic
  (fn [{:keys [object]}] (s/valid? ::extension object)))

(s/def ::recommended-verbs-uris
  (fn [{:keys [object concepts-table]}]
    (let [uri-vec (:recommended-verbs object)]
      (cu/recommend-concepts "Verb" concepts-table uri-vec))))

(s/def ::extension+
  (s/and ::extension-basic
         ::u/in-scheme-valid?
         ::recommended-verbs-uris))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: json-ld context validation

;; context - valid json-ld context

;; TODO: get string from iri

;; schema - json-schema string at other end of iri
