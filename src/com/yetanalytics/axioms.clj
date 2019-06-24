(ns com.yetanalytics.axioms
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [xapi-schema.spec :as xs]
            [json-schema.core :as js]))

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
  (s/map-of (s/or :string ::xs/language-tag :keyword keyword?)
            (s/or :not-empty ::xs/string-not-empty
                  :maybe-empty string?)
            :min-count 1))

;; JSONPath strings
;; Implementation of the spec is mostly based off of the Jayway implementation
;; of JSONPath, particularly handling of special characters and other edge
;; cases. The main exception is with the pipe character '|', which, if placed
;; before the '$' character separates two JSONPath strings (unless escaped).
;; See: regexr.com/4fk7v

(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[\s*(('([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\s*\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")

(def JSONPathSplitRegEx #"\s*(?<!\\)\|\s*(?=\$)")

;; TODO: Current generator is very bad; will need to improve in order to
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
;; TODO We are currently using the xapi-schema specs as substitutes for the
;; real thing (currently they do not correctly differentiate between IRIs,
;; which accept non-ASCII chars, and URIs, which do not).
(s/def ::iri ::xs/iri)
(s/def ::irl ::xs/irl)
(s/def ::uri ::xs/iri)
(s/def ::url ::xs/irl)

;; Array of iris
(s/def ::array-of-iri (s/coll-of ::iri :type vector? :min-count 1))
