(ns com.yetanalytics.pan.utils.json
  (:require [clojure.string :as string]
            #?(:clj [clojure.data.json :as json]
               :cljs [clojure.walk :as w])))

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
