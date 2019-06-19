(ns com.yetanalytics.concepts.extensions
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.concept :as concept]))

;; Basic properties


(s/def ::id ::ax/iri)
(s/def ::type #{"ContextExtension" "ResultExtension" "ActivityExtension"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

;; Recommended types properties
;; TODO Fill in placeholders and perform validation on Concept types
(s/def ::recommended-activity-types (s/and ::boolean true ;; TODO Placeholder
                                           (s/coll-of ::ax/uri :type vector?)))
(s/def ::recommended-verbs (s/and ::boolean true ;; TODO Placeholder
                                  (s/coll-of ::ax/uri :type vector?)))

(s/def ::context ::ax/iri)
; (s/def ::schema ::ax/iri)
; (s/def ::inline-schema ::ax/json-schema)

;; TODO Account for "MUST use at least one of ::schema and ::inline-schema" req
; (s/def ::extension (s/keys :req [::id
;                                  ::type
;                                  ::in-scheme
;                                  ::pref-label
;                                  ::definition
;                                  (s/or ::schema
;                                        ::inline-schema)]
;                            :opt [::deprecated
;                                  ::recommended-activity-types
;                                  ::recommended-verbs
;                                  ::context]))

(s/def ::activity-extension
  (s/merge ::concept/common
           (s/keys :req-un [(or ::concept/schema
                                ::concept/inline-schema)]
                   :opt-un [::recommended-activity-types
                            ::context])))

(s/def ::verb-extension
  (s/merge ::concept/common
           (s/keys :req-un [(or ::concept/schema
                                ::concept/inline-schema)]
                   :opt-un [::extension/recommended-verb
                            ::extension/context])))

; (defn extension-xor? [ext]
;   (let [schema? (contains? ext :schema)
;         inline-schema? (contains? ext :inline-schema)]
;     (or (and schema? (not inline-schema?))
;         (and inline-schema? (not inline-schema?)))))

(defmethod concept/concept? "ContextExtension" [_]
  (s/and :verb-extension
         concept/extension-xor?))
(defmethod concept/concept? "ResultExtension" [_]
  (s/and :concept/verb-extension
         concept/extension-xor?))
(defmethod concept/concept? "ActivityExtension" [_]
  (s/and :concept/activity-extension
         concept/extension-xor?))
