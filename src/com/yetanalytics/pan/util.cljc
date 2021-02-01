(ns com.yetanalytics.pan.util
  #?(:clj
     (:require [clojure.spec.alpha :as s]
               [clojure.string :as string]
               [clojure.data.json :as json])
     :cljs
     (:require [clojure.spec.alpha :as s]
               [clojure.string :as string]
               [clojure.walk :as w]
               [fs])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; File IO 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ClojureScript approach from:
;; https://stackoverflow.com/questions/38880796/how-to-load-a-local-file-for-a-clojurescript-test

(defn read-resource
  "Read a file in the \"resources\" dir, returning a string."
  [path]
  (let [path' (str "resources/" path)]
    #?(:clj (slurp path')
       :cljs (.readFileSync fs path' "utf8"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-nil
  "Turn a nil array into an empty array"
  [value]
  (if (nil? value) [] value))

(defn normalize-profile
  "Turn any nil top-level arrays in a profile (versions, concepts, templates
  and patterns) into empty arrays."
  [profile]
  (-> profile
      (update :versions normalize-nil)
      (update :concepts normalize-nil)
      (update :templates normalize-nil)
      (update :patterns normalize-nil)))

(defn type-dispatch
  "Dispatch on the type key of an object, eg. in a multimethod.
  Works for both Profile objects and graph edges with the type attribute"
  [object]
  (:type object))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inlineSchema)]
      (not (and schema? inline-schema?)))))

;; In concepts that may have the related property, it MUST only be used on a
;; deprecated property
(s/def ::related-only-deprecated
  (fn [{:keys [deprecated related]}]
    (if (some? related)
      (true? deprecated)
      ;; Ignore if related property is not present
      true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-chars
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  [s] (string/replace s #"@|\s" ""))

(defn replace-at
  "Replace any @ symbols with the replacement arg (as @ cannot be used in
  keywords)."
  [s replacement] (string/replace s #"@" replacement))

#?(:clj
   (defn- convert-json-java
     [json-str at-replacement]
     (letfn [(key-fn [k] (-> k (replace-at at-replacement) keyword))]
       (json/read-str json-str :key-fn key-fn))))

#?(:cljs
   (defn- convert-json-js
     [json-str at-replacement]
     (letfn [(key-fn [k] (-> k (replace-at at-replacement) keyword))
             (kv-fn [acc k v] (assoc acc (key-fn k) v))
             (map-fn [x] (if (map? x) (reduce-kv kv-fn {} x) x))
             (tree-fn [m] (w/postwalk map-fn m))]
       (->> json-str (.parse js/JSON) js->clj tree-fn))))

(defn convert-json
  "Convert a JSON string into an edn data structure.
  Second argument should be what string the @ char should be replaced by."
  [json-str at-replacement]
  #?(:clj (convert-json-java json-str at-replacement)
     :cljs (convert-json-js json-str at-replacement)))
