(ns com.yetanalytics.pan.utils.json
  (:require [clojure.string :as cstr]
            #?(:clj [clojure.data.json :as json]
               :cljs [clojure.walk :as w])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- rm-spaces
  "Remove spaces, which are illegal in keywords."
  [s]
  (cstr/replace s #"\s" ""))

(defn- replace-at
  "Replace any @ symbols with the replacement arg (as @ cannot be used in
  keywords)."
  [s replacement]
  (cstr/replace s #"@" replacement))

(defn- has-colon?
  "Return true if `s` has a colon (as that signifies it's likely an IRI)."
  [s]
  (cstr/includes? s ":"))

(defn- keyword-fn
  [at-replacement k]
  (if (has-colon? k)
    k
    (-> k (replace-at at-replacement) rm-spaces keyword)))

#?(:clj
   (defn- convert-json-java
     [json-str at-replacement]
     (let [key-fn (partial keyword-fn at-replacement)]
       (json/read-str json-str :key-fn key-fn))))

#?(:cljs
   (defn- convert-json-js
     [json-str at-replacement]
     (let [kv-fn   (fn [acc k v] (assoc acc (keyword-fn at-replacement k) v))
           map-fn  (fn [x] (if (map? x) (reduce-kv kv-fn {} x) x))
           tree-fn (fn [m] (w/postwalk map-fn m))]
       (->> json-str (.parse js/JSON) js->clj tree-fn))))

(defn convert-json
  "Convert a JSON string into an EDN data structure.
   Optional `at-replacement` arg is the string that should replace the `@`
   character (`_` by default). Removes spaces in keywords."
  ([json-str] (convert-json json-str "_"))
  ([json-str at-replacement]
   #?(:clj (convert-json-java json-str at-replacement)
      :cljs (convert-json-js json-str at-replacement))))
