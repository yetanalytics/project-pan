(ns com.yetanalytics.pan.utils.json
  (:require [clojure.string :as string]
            #?(:clj [clojure.data.json :as json]
               :cljs [clojure.walk :as w])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- remove-spaces
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  [s] (string/replace s #"\s" ""))

(defn- replace-at
  "Replace any @ symbols with the replacement arg (as @ cannot be used in
  keywords)."
  [s replacement] (string/replace s #"@" replacement))

#?(:clj
   (defn- convert-json-java
     ([json-str at-replacement rm-spaces]
      (letfn [(rm-sp-fn [k] (if rm-spaces (remove-spaces k) k))
              (key-fn [k] (-> k (replace-at at-replacement) rm-sp-fn keyword))]
        (json/read-str json-str :key-fn key-fn)))))

#?(:cljs
   (defn- convert-json-js
     [json-str at-replacement rm-spaces]
     (letfn [(rm-sp-fn [k] (if rm-spaces (remove-spaces k) k))
             (key-fn [k] (-> k (replace-at at-replacement) rm-sp-fn keyword))
             (kv-fn [acc k v] (assoc acc (key-fn k) v))
             (map-fn [x] (if (map? x) (reduce-kv kv-fn {} x) x))
             (tree-fn [m] (w/postwalk map-fn m))]
       (->> json-str (.parse js/JSON) js->clj tree-fn))))

(defn convert-json
  "Convert a JSON string into an EDN data structure.
   Optional args:
   - :at-replacement is the string that should replace the \"@\" character (the
     empty string by default).
   - :remove-spaces is true if spaces are to be removed in keys (default), false
     otherwise."
  [json-str & {:keys [at-replacement remove-spaces]
               :or {at-replacement "" remove-spaces true}}]
  #?(:clj (convert-json-java json-str at-replacement remove-spaces)
     :cljs (convert-json-js json-str at-replacement remove-spaces)))
