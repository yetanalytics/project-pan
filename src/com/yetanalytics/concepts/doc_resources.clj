(ns com.yetanalytics.concepts.doc-resources
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.concept :as concept]))

;; Basic properties


; (s/def ::id ::ax/iri)
; (s/def ::type #{"StateResource" "AgentProfileResource"
;                 "ActivityProfileResource"})
; (s/def ::in-scheme ::ax/iri)
; (s/def ::pref-label ::ax/language-map)
; (s/def ::definition ::ax/language-map)
; (s/def ::deprecated ::ax/boolean)

;; Advanced properties

(s/def ::content-type ::ax/string)
(s/def ::context ::ax/iri)
; (s/def ::schema ::ax/iri)
; (s/def ::inline-schema ::ax/iri)

; (s/def ::doc-resource (s/keys :req [::id
;                                     ::type
;                                     ::in-scheme
;                                     ::pref-label
;                                     ::definition
;                                     ::content-type
;                                     (s/or ::schema
;                                           ::inline-schema)]
;                               :opt [::deprecated
;                                     ::context]))

(s/def ::document-resource
  (s/merge ::concept/common
           (s/keys :req-un [::content-type
                            (or :concept/schema
                                :concept/inline-schema)]
                   :opt-un [::context])))

(defmethod concept/concept? "StateResource" [_]
  (s/and ::document-resource
         concept/extension-xor?))
(defmethod concept/concept? "AgentProfileResource" [_]
  (s/and ::document-resource
         concept/extension-xor?))
(defmethod concept/concept? "ActivityProfileResource" [_]
  (s/and ::document-resource
         concept/extension-xor?:w))
