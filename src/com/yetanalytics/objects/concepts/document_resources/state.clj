(ns com.yetanalytics.objects.concepts.document-resources.state
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State Resource
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"StateResource"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::content-type ::ax/string)
;; TODO: RFC 2046 valid content types
(s/def ::deprecated ::ax/boolean)
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(s/def ::document-resource
  (s/and (s/keys
          :req-un [::id ::type ::in-scheme ::pref-label ::definition ::content-type]
          :opt-un [::deprecated ::context ::schema ::inline-schema])
         ::cu/inline-or-iri))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (s/def ::document-resource-in-profile-strict
;   (fn [{:keys [document-resource profile]}]
;     (let [{:keys [in-scheme]} document-resource]
;       (s/and (s/valid? ::document-resource document-resource)
;              (s/valid? ::u/in-scheme-strict-scalar {:in-scheme in-scheme
;                                                     :profile profile})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (s/def ::document-resource-complete-validation
;   (fn [{:keys [document-resource profile]}]
;     (s/valid? ::document-resource-in-profile-strict
;               {:document-resource document-resource :profile profile})
;     ;; TODO: json-ld context validation
;     ;; context - valid json-ld context

;     ;; TODO: get string from iri
;     ;; schema - json-schema string at other end of iri
; ))

;; TODO: LRS clients sending Document resources checks required by the spec
;; - id
;; - content-type

(defmethod util/edges-with-attrs "StateResource" [_] nil)
