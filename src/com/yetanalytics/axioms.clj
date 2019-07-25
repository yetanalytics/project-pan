(ns com.yetanalytics.axioms
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
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

;; Timestamps
;; Example: "2010-01-14T12:13:14Z"
(s/def ::timestamp ::xs/timestamp)

;; Language Maps
;; Example: {"en" "Hello World"} or {:en "Hello World"}
(s/def ::language-map
  ;; TODO Should revise language maps such that keys like :foo are not counted
  (s/map-of (s/or :string ::xs/language-tag :keyword keyword?)
            (s/or :not-empty ::xs/string-not-empty
                  :maybe-empty string?)
            :min-count 1))

;; RFC 2046 media types, as defined by the IANA.
;; Example: "application/json"
;; 
;; A full list of media types can be found at: 
;; https://www.iana.org/assignments/media-types/media-types.xml
;; Currently only the five discrete top-level media type values are supported:
;; application, audio, image, text and video.
(def media-types (edn/read-string (slurp "resources/media_types.edn")))

(s/def ::media-type
  (s/and string?
         (fn [mt]
           (let [substrs (string/split mt #"\/" 2)]
             (contains? (get media-types (first substrs)) (second substrs))))))

;; JSONPath strings
;; Example: "$.store.book"
;; 
;; Implementation of the spec is mostly based off of the Jayway implementation
;; of JSONPath, particularly handling of special characters and other edge
;; cases. The main exception is with the pipe character '|', which, if placed
;; before the '$' character separates two JSONPath strings (unless escaped).
;; See: regexr.com/4fk7v
;;
;; JSONPath regexes
(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[\s*(('([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\s*\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")
(def JSONPathSplitRegEx #"\s*\|\s*(?!([^\[]*\]))")

(s/def ::json-path
  (s/and string?
         (fn [paths]
           (every? some?
                   (map (partial re-matches JSONPathRegEx)
                        (#(string/split % JSONPathSplitRegEx) paths))))))

;; JSON Schema
;; Example: "{\"type\":\"array\", \"uniqueItems\":true}"
;; 
;; Currently only draft-07 supported. TODO Should prev versions be supported? 
(defn schema-validate [schema json]
  (try (do (js/validate schema json) true)
       (catch Exception e false)))

(def meta-schema (slurp "resources/json/schema-07.json"))

(s/def ::json-schema
  (s/and string?
         (partial schema-validate meta-schema)))

;; IRIs/IRLs/URIs/URLs
;; Example: "https://yetanalytics.io"
;;
;; TODO We are currently using the xapi-schema specs as substitutes for the
;; real thing (currently they do not correctly differentiate between IRIs,
;; which accept non-ASCII chars, and URIs, which do not).
(s/def ::iri ::xs/iri)
(s/def ::irl ::xs/irl)
(s/def ::uri ::xs/iri)
(s/def ::url ::xs/irl)

;; Array of IRIs
;; Example: ["https://foo.org" "https://bar.org"]
(s/def ::array-of-iri (s/coll-of ::iri :type vector? :min-count 1))
