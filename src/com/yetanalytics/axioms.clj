(ns com.yetanalytics.axioms
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [xapi-schema.spec :as xs]))

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

(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[((\s*'([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")

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

;; IRIs/IRLs/URIs/URLs

;; xAPI Profile Keywords
