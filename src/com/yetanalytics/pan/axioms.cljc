(ns com.yetanalytics.pan.axioms
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [xapi-schema.spec.regex :as xsr]
            #?(:clj [json-schema.core :as jschema]
               :cljs [jsonschema]))
  #?(:clj
     (:require [com.yetanalytics.pan.util
                :refer [read-json-res read-edn-res]])
     :cljs
     (:require-macros [com.yetanalytics.pan.util
                       :refer [read-json-res read-edn-res]])))

;; Booleans
;; (Useless wrapper, but exists for consistency)
(s/def ::boolean boolean?)

;; Arbitrary strings
;; By the xAPI-Profile specification, they cannot be empty (except Lang Maps)
(s/def ::string (s/and string? (complement empty?)))
(s/def ::lang-map-string string?) ;; Language maps only

;; Timestamps
;; Example: "2010-01-14T12:13:14Z"
(s/def ::timestamp (s/and ::string (partial re-matches xsr/TimestampRegEx)))

;; Language Maps
;; Example: {"en" "Hello World"} or {:en "Hello World"}
;; TODO Should revise language maps such that keys like :foo are not counted
(s/def ::language-tag
  (fn [t] (or (and (keyword? t) (->> t name (re-matches xsr/LanguageTagRegEx)))
              (and (string? t) (not-empty t)
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
(def media-types (read-edn-res "media_types.edn"))

(s/def ::media-type
  (s/and ::string
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

;; JSONPath regexes
(def JSONPathRegEx #"\$((((\.\.?)([^\[\]\.,\s]+|(?=\[)))|(\[\s*(('([^,']|(\\\,)|(\\\'))+'(,\s*('([^,']|(\\\,)|(\\\'))+'))*\s*)|\*)\s*\]))(\[((\d*(,\s*(\d*))*)|\*)\])?)*")
(def JSONPathSplitRegEx #"\s*\|\s*(?!([^\[]*\]))")

(s/def ::json-path
  (s/and ::string
         (fn [paths]
           (every? some?
                   (map (partial re-matches JSONPathRegEx)
                        (#(string/split % JSONPathSplitRegEx) paths))))))

;; JSON Schema
;; Example: "{\"type\":\"array\", \"uniqueItems\":true}"

(defn schema-validate [json-schema json]
  #?(:clj
     (try ((jschema/validate json-schema json) true)
          (catch Exception _ false))
     :cljs
     (.validate jsonschema json json-schema)))

;; TODO: dynamic var for json schema version
;; TODO: test newest schema: version 08
(def meta-schema (read-json-res "json/schema-07.json"))

(s/def ::json-schema
  (s/and ::string (partial schema-validate meta-schema)))

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
