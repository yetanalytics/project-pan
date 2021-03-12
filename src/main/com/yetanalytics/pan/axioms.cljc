(ns com.yetanalytics.pan.axioms
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [xapi-schema.spec.regex :as xsr]
            #?(:clj [json-schema.core :as jschema]
               :cljs [jsonschema]))
  #?(:clj (:require
           [com.yetanalytics.pan.utils.resources
            :refer [read-resource read-edn-resource]])
     :cljs (:require-macros
            [com.yetanalytics.pan.utils.resources
             :refer [read-resource read-edn-resource]])))

;; Booleans
;; (Useless wrapper, but exists for consistency)
(s/def ::boolean boolean?)

;; Arbitrary strings
;; By the xAPI-Profile specification, they cannot be empty (except Lang Maps)
(defn- non-empty-string? [s]
  (and (string? s) (not-empty s)))

(s/def ::string non-empty-string?)
(s/def ::lang-map-string string?) ;; Language maps only

;; Timestamps
;; Example: "2010-01-14T12:13:14Z"
(s/def ::timestamp (s/and ::string (partial re-matches xsr/TimestampRegEx)))

;; Language Maps
;; Example: {"en" "Hello World"} or {:en "Hello World"}
;; TODO Should revise language maps such that keys like :foo are not counted
(s/def ::language-tag
  (fn language-tag? [t]
    (or (and (keyword? t)
             (re-matches xsr/LanguageTagRegEx (name t)))
        (and (non-empty-string? t)
             (re-matches xsr/LanguageTagRegEx t)))))

(s/def ::language-map
  (s/map-of ::language-tag ::lang-map-string :min-count 1))

;; RFC 2046 media types, as defined by the IANA.
;; Example: "application/json"
;; 
;; A full list of media types can be found at: 
;; https://www.iana.org/assignments/media-types/media-types.xml
;; Currently only the five discrete top-level media type values are supported:
;; application, audio, image, text and video.
(def media-types (read-edn-resource "media_types.edn"))

(defn- media-type? [s]
  (let [regexp  #?(:clj #"\/" :cljs #"/")
        substrs (string/split s regexp 2)]
    (contains? (get media-types (first substrs)) (second substrs))))

(s/def ::media-type (s/and ::string media-type?))

;; JSONPath strings
;; Example: "$.store.book"
;; 
;; Implementation of the spec is mostly based off of the Jayway implementation
;; of JSONPath, particularly handling of special characters and other edge
;; cases. The main exception is with the pipe character '|', which, if placed
;; before the '$' character separates two JSONPath strings (unless escaped).
;; See: regexr.com/4fk7v

;; JSONPath regexes
(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[\s*(('([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\s*\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")
(def JSONPathSplitRegEx #"\s*\|\s*(?!([^\[]*\]))")

(defn- json-path? [paths]
  (every? some?
          (map (partial re-matches JSONPathRegEx)
               (filterv some? ; Filter out nils from JS regexes
                        (string/split paths JSONPathSplitRegEx)))))

(s/def ::json-path
  (s/and ::string json-path?))

;; JSON Schema
;; Example: "{\"type\":\"array\", \"uniqueItems\":true}"

(defn- validate-schema [json-schema json]
  #?(:clj
     (try (let [vres (jschema/validate json-schema json)]
            (some? vres))
          (catch Exception _ false))
     :cljs
     (try (let [vres (.validate jsonschema
                                (.parse js/JSON json)
                                (.parse js/JSON json-schema))]
            (. vres -valid))
          (catch js/Error _ false))))

;; TODO: dynamic var for json schema version
;; TODO: test newest schema: version 08
(def meta-schema-7 (read-resource "json/schema-07.json"))

(s/def ::json-schema (s/and ::string (partial validate-schema meta-schema-7)))

;; IRIs/IRLs/URIs/URLs
;; Example: "https://yetanalytics.io"

;; TODO We are currently using the xapi-schema specs as substitutes for the
;; real thing (currently they do not correctly differentiate between IRIs,
;; which accept non-ASCII chars, and URIs, which do not).
(s/def ::iri (s/and ::string (partial re-matches xsr/AbsoluteIRIRegEx)))
(s/def ::irl (s/and ::string (partial re-matches xsr/AbsoluteIRIRegEx)))
(s/def ::uri (s/and ::string (partial re-matches xsr/AbsoluteIRIRegEx)))
(s/def ::url (s/and ::string (partial re-matches xsr/AbsoluteIRIRegEx)))

; (s/def ::iri ::xs/iri)
; (s/def ::irl ::xs/irl)
; (s/def ::uri ::xs/iri)
; (s/def ::url ::xs/irl)

;; Array of identifiers
;; Example: ["https://foo.org" "https://bar.org"]
(s/def ::array-of-iri (s/coll-of ::iri :type vector? :min-count 1))
(s/def ::array-of-uri (s/coll-of ::uri :type vector? :min-count 1))
