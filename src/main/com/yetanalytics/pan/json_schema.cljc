(ns com.yetanalytics.pan.json-schema
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as sgen]
            [xapi-schema.spec       :as xs]
            [xapi-schema.spec.regex :as xsr]
            #?(:cljs [clojure.test.check.generators])))

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

(def uri-reference-spec
  (s/with-gen
   (s/and string?
          (s/or :absolute-ref (partial re-matches xsr/AbsoluteURIRegEx)
                :relative-ref (partial re-matches xsr/RelativeURLRegEx)
                :same-doc-ref (partial re-matches (frag-spec))))
    #(s/gen ::xs/iri)))

;; URI references
(s/def ::$id uri-reference-spec) ; Just "id" in draft 4)
(s/def ::$ref uri-reference-spec)
(s/def ::$schema
  (s/with-gen (s/and string? (partial re-matches xsr/AbsoluteURIRegEx))
    #(s/gen ::xs/iri)))

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
(s/def ::items (s/or :schema ::schema
                     :array schema-array-spec))
(s/def ::maxItems nat-int?)
(s/def ::minItems nat-int?)
(s/def ::uniqueItems boolean?)
(s/def ::contains ::schema)
(s/def ::maxProperties nat-int?)
(s/def ::minProperties nat-int?)
(s/def ::required string-array-spec) ; draft 4 - required at least 1 string
(s/def ::additionalProperties ::schema)

;; Maps
(s/def ::definitions (s/map-of keyword? ::schema :gen-max 5))
(s/def ::properties (s/map-of keyword? ::schema :gen-max 5))
(s/def ::patternProperties (s/map-of (s/and keyword?
                                            (s/conformer name)
                                            regex?)
                                     ::schema
                                     :gen-max 5))
(s/def ::dependencies (s/map-of keyword?
                                (s/or :schema ::schema
                                      :string-array string-array-spec)
                                :gen-max 5))

;; More values
(s/def ::propertyNames ::schema) ; Added in draft 6
(s/def ::const any?) ; Added in draft 6
(s/def ::enum enum-spec)
(s/def ::type (s/or :string simple-types
                    :array type-array-spec))
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
  (s/with-gen
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
                                   ::not]))
    ;; These generators don't generate all possible JSON schemas, but
    ;; should be good enough for most purposes while not creating
    ;; humongous schemas
    #(sgen/one-of [;; string
                   (sgen/fmap (fn [m] (assoc m :type "string"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema
                                                      ::minLength
                                                      ::maxLength
                                                      ::pattern
                                                      ::format])))
                   ;; numeric
                   (sgen/fmap (fn [m] (assoc m :type "number"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema
                                                      ::multipleOf
                                                      ::minimum
                                                      ::maximum])))
                   ;; boolean
                   (sgen/fmap (fn [m] (assoc m :type "boolean"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema])))
                   ;; null
                   (sgen/fmap (fn [m] (assoc m :type "null"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema])))
                   ;; object
                   (sgen/fmap (fn [m] (assoc m :type "object"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema
                                                      ::properties
                                                      ::patternProperties
                                                      ::additionalProperties
                                                      ::required])))
                   ;; object (freeform)
                   (sgen/fmap (fn [m] (assoc m :type "object"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema
                                                      ::propertyNames
                                                      ::minProperties
                                                      ::maxProperties])))
                   ;; array
                   (sgen/fmap (fn [m] (assoc m :type "array"))
                              (s/gen (s/keys :opt-un [::$id
                                                      ::$schema
                                                      ::items])))])))
