(ns com.yetanalytics.concept
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.object.object :as object]))

(s/def ::type #{"Verb"
                "ActivityType"
                "AttachmentUsageType" 
                ; Extensions
                "ContextExtension"
                "ResultExtension"
                "ActivityExtension"
                ; Document Resources
                "StateResource"
                "AgentProfileResource"
                "ActivityProfileResource"
                ; Activities 
                "Activity"})

;; Properties in common with all concepts
;; Required: id, type, inScheme, prefLabel, definition
;; Optional: deprecated
(s/def ::common
  (s/merge ::object/common
           (s/keys :req-un [::object/id ::type])))

;; JSON Schema-related properties.
;; These are used in both Extensions and Document Resources.
;; Either a schema or an inlineSchema must be included (and only one).

(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

;; Ensure only a schema or an inline schema can be included, and not both.
(defn json-schema-xor? [ext]
  (let [schema? (contains? ext :schema)
        inline-schema? (contains? ext :inline-schema)]
    (or (and schema? (not inline-schema?))
        (and inline-schema? (not inline-schema?)))))

;; Include JSON Schema into the map
(s/def ::json-schema-common
  (s/keys :req-un [(or ::schema ::inline-schema)]))

;; "Relational" properties
;; ie. Verbs, ActivityTypes, and AttachmentUsageTypes properties

(s/def :verb/broader (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/broader-match (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/narrower (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/narrower-match (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/related (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/related-match (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def :verb/exact-match (s/coll-of ::ax/iri :type vector? :min-count 1))

(s/def ::verb-or-type-unique
  (s/keys opt-un [:verb/broader
                  :verb/broad-match
                  :verb/narrower
                  :verb/narrow-match
                  :verb/related
                  :verb/related-match
                  :verb/exact-match]))

;; Recommended types properties
;; TODO Fill in placeholders and perform validation on Concept types

(s/def :extension/recommended-activity-types
  (s/coll-of ::ax/uri :type vector?))
(s/def :extension/recommended-verbs
  (s/coll-of ::ax/uri :type vector?))

;; Extension context property
(s/def :extension/context ::ax/iri)

;; Document Resources properties

(s/def :resource/content-type ::ax/string)
(s/def :resource/context ::ax/iri)

;; Activity properties

(def activity-context-url "https://w3id.org/xapi/profiles/activity-context")
(s/def :activity/context
  (s/or :single-val ::ax/uri
        :array-val (s/and (s/coll-of ::ax/uri :type vector? :min-count 1)
                          (partial some #(= activity-context-url %)))))

;; TODO Include Activity definition from xapi-schema
(s/def ::activity-definition
  (s/keys :req [:activity/context]))

;;; CONCEPT SPEC

(defmulti concept? ::object/type)
 
;; Concepts that can be "related" to other concepts
(defmethod concept? "Verb" [_]
  (s/merge ::common
           ::verb-or-type-unique))

(defmethod concept? "ActivityType" [_]
  (s/merge ::common
           ::verb-or-type-unique))
(defmethod concept? "AttachmentUsageType" [_]
  (s/merge ::common
           ::verb-or-type-unique))

;; Extensions 
(defmethod concept? "ContextExtension" [_] 
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :opt-un [:extension/recommended-verbs
                              :extension/context]))
    json-schema-xor?))

(defmethod concept? "ResultExtension" [_]
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :opt-un [:extension.recommended-verbs
                              :extension/context]))
    json-schema-xor?))

(defmethod concept? "ActivityExtension" [_]
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :opt-un [:extension/recommended-activity-types
                              :extension/context]))
    json-schema-xor?))

;; Document Resources
(defmethod concept? "StateResource" [_]
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :req-un [:resource/content-type]
                     :opt-un [:resource/context]))
    json-schema-xor?))

(defmethod concept? "AgentProfileResource" [_]
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :req-un [:resource/content-type]
                     :opt-un [:resource/context]))
    json-schema-xor?))

(defmethod concept? "ActivityProfileResource" [_]
  (s/and
    (s/merge ::common
             ::json-schema-common
             (s/keys :req-un [:resource/content-type]
                     :opt-un [:resource/context]))
    json-schema-xor?))

;; Activities
(defmethod concept? "Activity" [_]
  (s/keys :req-un [::object/id
                   ::type
                   ::object/in-scheme
                   ::activity-definition]
          :opt-un [::object/deprecated]))

(s/def ::concept (s/multi-spec concept? ::type))
