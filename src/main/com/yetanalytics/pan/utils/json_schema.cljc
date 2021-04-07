(ns com.yetanalytics.pan.utils.json-schema
  (:require [clojure.spec.alpha :as s]
            [xapi-schema.spec.regex :as xsr]))

;; Based on the draft 7 meta-schema.
;; Most draft 6 schemas should be compatible as well, but future versions are
;; not considered.

(s/def ::schema (constantly false)) ; Forward declaration

(def simple-types
  #{"array" "boolean" "integer" "null" "number" "object" "string"})

(defn- regex? [s]
  (try (re-pattern s) true
       (catch #?(:clj Exception :cljs js/Error) _ false)))

(def string-array-spec
  (s/coll-of string? :kind vector? :distinct true :gen-max 5))

(def schema-array-spec
  (s/coll-of ::schema :kind vector? :min-count 1 :distinct true :gen-max 5))

(def enum-spec
  (s/coll-of any? :kind vector? :min-count 1 :distinct true :gen-max 5))

(def type-array-spec
  (s/coll-of simple-types :kind vector? :min-count 1 :distinct true :gen-max 5))

(defn- frag-spec []
  (let [fs #?(:clj "\\/" :cljs "/")
        frag-char (str "(?:"
                       "[\\w\\-\\.\\~]" "|"  ; unreserved 
                       "%[0-9A-Fa-f]{2}" "|" ; pct-encoded
                       "[!$&'()*+,;=]" "|"   ; sub-delims
                       "|:|@"                ; extra chars
                       ")")
        fragment  (str "^#(?:" frag-char "|" fs "|" "\\?" ")*$")]
    (re-pattern fragment)))

#_{:clj-kondo/ignore [:unresolved-var]}
(def uri-reference-spec
  (s/or :absolute-ref (partial re-matches xsr/AbsoluteURIRegEx)
        :relative-ref (partial re-matches xsr/RelativeURLRegEx)
        :same-doc-ref (partial re-matches (frag-spec))))

;; URI references
(s/def ::$id (s/and string? uri-reference-spec)) ; Just "id" in draft 4)
(s/def ::$ref (s/and string? uri-reference-spec))
#_{:clj-kondo/ignore [:unresolved-var]}
(s/def ::$schema (s/and string? (partial re-matches xsr/AbsoluteURIRegEx)))

(s/def ::$comment string?) ; Added in draft 7

;; Annotations
(s/def ::title string?)
(s/def ::description string?)
(s/def ::default any?)
(s/def ::readOnly boolean?)
(s/def ::examples vector?) ; Added in draft 6

;; Numeric
(s/def ::multipleOf (s/and number? pos?))
(s/def ::maximum number?)
(s/def ::exclusiveMaximum number?)
(s/def ::minimum number?)
(s/def ::exclusiveMinimum number?)
(s/def ::maxLength nat-int?)
(s/def ::minLength nat-int?)

;; Values and items
(s/def ::pattern (s/and string? regex?))
(s/def ::additionalItems ::schema)
(s/def ::items (s/or :schema ::schema :array schema-array-spec))
(s/def ::maxItems nat-int?)
(s/def ::minItems nat-int?)
(s/def ::uniqueItems boolean?)
(s/def ::contains ::schema)
(s/def ::maxProperties nat-int?)
(s/def ::minProperties nat-int?)
(s/def ::required string-array-spec) ; draft 4 - required at least 1 string
(s/def ::additionalProperties ::schema)

;; Maps
(s/def ::definitions (s/map-of keyword? ::schema))
(s/def ::properties (s/map-of keyword? ::schema))
(s/def ::patternProperties (s/map-of (s/and keyword?
                                            (s/conformer name)
                                            regex?)
                                     ::schema))
(s/def ::dependencies (s/map-of keyword?
                                (s/or :schema ::schema
                                      :string-array string-array-spec)))

;; More values
(s/def ::propertyNames ::schema) ; Added in draft 6
(s/def ::const any?) ; Added in draft 6
(s/def ::enum enum-spec)
(s/def ::type (s/or :string simple-types :array type-array-spec))
(s/def ::format string?)

;; Content encoding - added in draft 7
(s/def ::contentMediaType string?)
(s/def ::contentEncoding string?)

;; Conditionals
(s/def ::if ::schema)
(s/def ::then ::schema)
(s/def ::else ::schema)
(s/def ::allOf schema-array-spec)
(s/def ::anyOf schema-array-spec)
(s/def ::oneOf schema-array-spec)
(s/def ::not ::schema)

(s/def ::schema
  (s/or :boolean boolean?
        :object (s/keys :opt-un [::$id
                                 ::$schema
                                 ::$ref
                                 ::$comment
                                 ::title
                                 ::description
                                 ::default
                                 ::readOnly
                                 ::examples
                                 ::multipleOf
                                 ::maximum
                                 ::exclusiveMaximum
                                 ::minimum
                                 ::exclusiveMinimum
                                 ::maxLength
                                 ::minLength
                                 ::pattern
                                 ::additionalItems
                                 ::items
                                 ::maxItems
                                 ::minItems
                                 ::uniqueItems
                                 ::contains
                                 ::maxProperties
                                 ::minProperties
                                 ::required
                                 ::additionalProperties
                                 ::definitions
                                 ::properties
                                 ::patternProperties
                                 ::dependencies
                                 ::propertyNames
                                 ::const
                                 ::enum
                                 ::type
                                 ::format
                                 ::contentMediaType
                                 ::contentEncoding
                                 ::if
                                 ::then
                                 ::else
                                 ::allOf
                                 ::anyOf
                                 ::oneOf
                                 ::not])))
