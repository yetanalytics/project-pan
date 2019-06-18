(ns com.yetanalytics.concepts.extensions
  (:require [clojure.spec.alpha :as s
             com.yetanalytics.axioms :as ax]))

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
(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

;; TODO Account for "MUST use at least one of ::schema and ::inline-schema" req
(s/def ::extension (s/keys :req [::id
                                 ::type
                                 ::in-scheme
                                 ::pref-label
                                 ::definition]
                           :opt [::deprecated
                                 ::recommended-activity-types
                                 ::recommended-verbs
                                 ::context
                                 ::schema
                                 ::inline-schema]))
