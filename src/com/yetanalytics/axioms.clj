(ns com.yetanalytics.axioms
  (:require [com.yetanalytics.meta-schema :as ms]
            [com.yetanalytics.meta-schema-2 :as ms2]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [xapi-schema.spec :as xs]
            [json-schema.core :as js]
            [cheshire.core :as cheshire])
  (:import [org.json JSONObject]
           [org.everit.json.schema Schema]
           [org.everit.json.schema.loader SchemaLoader]
           [org.everit.json.schema SchemaException]
           [org.everit.json.schema ValidationException]))

;; Booleans
;; (Useless wrapper, but exists for consistency)


(s/def ::boolean boolean?)

;; Arbitrary strings
;; By the xAPI-Profile specification, they cannot be empty
(s/def ::string ::xs/string-not-empty)
; (s/valid? ::string "")
; (s/valid? ::string "Hello World")
; (s/valid? ::string "#$%^&!-_+=")

;; Timestamps
(s/def ::timestamp ::xs/timestamp)

;; Language Maps
;; Differs from xs/language-map in that empty strings are banned.
(s/def ::language-map
  (s/map-of ::xs/language-tag
            ::xs/string-not-empty
            :min-count 1))

;; JSONPath strings
;; Implementation of the spec is mostly based off of the Jayway implementation
;; of JSONPath, particularly handling of special characters and other edge
;; cases. The main exception is with the pipe character '|', which, if placed
;; before the '$' character separates two JSONPath strings (unless escaped).
;; See: regexr.com/4fk7v

(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[\s*(('([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\s*\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")

(def JSONPathSplitRegEx #"\s*(?<!\\)\|\s*(?=\$)")

;; TODO: Current generator is very shitty; will need to improve in order to
;; improve test coverage
; (s/def ::json-path-old
;   (s/with-gen
;     (s/and string?
;            (partial re-matches JSONPathRegEx))
;     #(sgen/fmap (fn [nodes] (str "$" nodes))
;                 (sgen/one-of [(sgen/elements [".*"])
;                               (sgen/fmap (fn [s] (str "." s)) (sgen/string-alphanumeric))
;                               (sgen/fmap (fn [s] (str "['" s "']")) (sgen/string-alphanumeric))]))))

(s/def ::json-path
  (s/with-gen
    (s/and string?
           (fn [paths]
             (every? some?
                     (map (partial re-matches JSONPathRegEx)
                          (#(string/split % JSONPathSplitRegEx) paths)))))
    #(sgen/elements ["$"
                     "$.store"
                     "$.store.book"
                     "$..book"
                     "$.*"])))

;; JSON Schema
;; Currently only draft-07 supported

(defn schema-validate [schema json]
  (try (do (js/validate schema json) true)
       (catch Exception e false)))

(def meta-schema (slurp "resources/json/schema-07.json"))

(s/def ::json-schema
  (s/and string?
         (partial schema-validate meta-schema)))

;; IRIs/IRLs/URIs/URLs

(s/valid? ::xs/iri "http://adlnet.gov/expapi/verbs/voided")
(s/valid? ::xs/iri "what the pineapple?")
(s/valid? ::xs/iri "whatThePineapple?")
(s/valid? ::xs/iri "mailto:kelvin@yetanalytics.com")
(s/valid? ::xs/iri "https://en.wikitionary.org/wiki/Ῥόδος")

;; xAPI Profile Type Keywords


(s/def ::typekey-profile #{"Profile"})
(s/def ::typekey-concept #{"Verb" "ActivityType" "AttachmentUsageType"})
(s/def ::typekey-extension #{"ContextExtension" "ResultExtension" "ActivityExtension"})
(s/def ::typekey-activity-ext #{"ActivityExtension"})
(s/def ::typekey-other-ext #{"ContextExtension" "ResultExtension"})
(s/def ::typekey-resource #{"StateResource" "AgentProfileResource" "ActivityProfileResource"})
(s/def ::typekey-activity #{"Activity"})
(s/def ::typekey-template #{"StatementTemplate"})
(s/def ::typekey-pattern #{"Pattern"})
(s/def ::typekey-presence #{"included" "excluded" "recommended"})
