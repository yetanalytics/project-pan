(ns com.yetanalytics.pan.axioms
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.string         :as cstr]
            [xapi-schema.spec       :as xs]
            [xapi-schema.spec.regex :as xsr]
            [com.yetanalytics.pan.json-schema :as jsn-schema]
            #?(:clj [clojure.data.json :as json]
               :cljs [clojure.test.check.generators]))
  #?(:clj (:require
           [com.yetanalytics.pan.utils.resources :refer [read-edn-resource]])
     :cljs (:require-macros
            [com.yetanalytics.pan.utils.resources :refer [read-edn-resource]])))

;; Booleans
;; (Useless wrapper, but exists for consistency)
(s/def ::boolean boolean?)

;; Arbitrary strings
;; By the xAPI-Profile specification, they cannot be empty (except Lang Maps)
(defn- non-empty-str? [s]
  (and (string? s) (not-empty s)))

(s/def ::string
  (s/and string? not-empty))

;; Timestamps
;; Example: "2010-01-14T12:13:14Z"
(s/def ::timestamp ::xs/timestamp)

;; Language Maps
;; Example: {"en" "Hello World"} or {:en "Hello World"}
(s/def ::language-tag
  (s/with-gen
    (s/or :keyword
          (s/and keyword? (s/conformer name) ::xs/language-tag)
          :string
          ::xs/language-tag)
    #(->> (sgen/one-of [(sgen/vector (sgen/char-alpha) 2 2)
                        (sgen/cat (sgen/vector (sgen/char-alpha) 2 2)
                                  (sgen/return [\-])
                                  (sgen/vector (sgen/char-alpha) 2 2))])
          (sgen/fmap (partial reduce str))
          (sgen/fmap keyword))))

(s/def ::language-map
  (s/map-of ::language-tag string? :min-count 1))

;; RFC 2046 media types, as defined by the IANA.
;; Example: "application/json"
;; 
;; A full list of media types can be found at: 
;; https://www.iana.org/assignments/media-types/media-types.xml
;; Currently only the five discrete top-level media type values are supported:
;; application, audio, image, text and video.
(def media-types (read-edn-resource "media_types.edn"))
(def app-types (get media-types "application"))
(def aud-types (get media-types "audio"))
(def img-types (get media-types "image"))
(def txt-types (get media-types "text"))
(def vid-types (get media-types "video"))

(defn- media-type? [s]
  (let [regexp  #?(:clj #"\/" :cljs #"/")
        substrs (cstr/split s regexp 2)]
    (contains? (get media-types (first substrs)) (second substrs))))

(s/def ::media-type
  (s/with-gen (s/and ::string media-type?)
    (sgen/generate
     (sgen/one-of
      [(sgen/fmap (partial str "application/")
                  (sgen/elements app-types))
       (sgen/fmap (partial str "audio/")
                  (sgen/elements aud-types))
       (sgen/fmap (partial str "image/")
                  (sgen/elements img-types))
       (sgen/fmap (partial str "text/")
                  (sgen/elements txt-types))
       (sgen/fmap (partial str "video/")
                  (sgen/elements vid-types))]))))

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
                        (cstr/split paths JSONPathSplitRegEx)))))

(s/def ::json-path
  (s/with-gen (s/and string? json-path?)
    (sgen/generate
     (->> (sgen/vector (sgen/string-alphanumeric) 1 5)
          (sgen/fmap (partial cstr/join "."))
          (sgen/fmap (partial str "$."))))))

;; JSON Schema
;; Example: "{\"type\":\"array\", \"uniqueItems\":true}"

;; TODO: Support for versions other than draft-07,
;; e.g. draft-04, draft-06, draft-2019-09, and draft-2020-12

(defn- str->jsn
  "Parses JSON string to EDN, returns nil on failure."
  [s]
  #?(:clj (try (json/read-str s :key-fn keyword)
               (catch Exception _ nil))
     :cljs (try (js->clj (.parse js/JSON s) :keywordize-keys true)
                (catch js/Error _ nil))))

(defn- json-schema?
  "Returns false if `s` is not parseable as JSON or not a valid
   JSON schema, true otherwise."
  [s]
  ;; TODO: Add generator
  (if-some [jsn (str->jsn s)]
    (s/valid? ::jsn-schema/schema jsn)
    false))

;; This could also be done with s/conformer, but doing so will mess
;; with expound.
(s/def ::json-schema
  (s/and string? json-schema?))

;; IRIs/IRLs/URIs/URLs
;; Example: "https://yetanalytics.io"

(defn- iri-str? [s]
  (and (non-empty-str? s) (re-matches xsr/AbsoluteIRIRegEx s)))

(defn- uri-str? [s]
  (and (non-empty-str? s) (re-matches xsr/AbsoluteURIRegEx s)))

(s/def ::iri (s/with-gen iri-str? #(s/gen ::xs/iri)))
(s/def ::irl (s/with-gen iri-str? #(s/gen ::xs/iri)))
(s/def ::uri (s/with-gen uri-str? #(s/gen ::xs/iri)))
(s/def ::url (s/with-gen uri-str? #(s/gen ::xs/iri)))

;; Array of identifiers
;; Example: ["https://foo.org" "https://bar.org"]

(s/def ::array-of-iri (s/coll-of ::iri :type vector? :min-count 1))
(s/def ::array-of-uri (s/coll-of ::uri :type vector? :min-count 1))
