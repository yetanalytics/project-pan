(ns com.yetanalytics.project-pan
  (:require [clojure.string :as string]
            [cheshire.core :as cheshire]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn to-kebab-case
  "Convert strings (particularly camelCase strings) to kebab-case."  
  [s]
  (string/lower-case (string/replace s #"([a-z])([A-Z])" "$1-$2")))

(defn remove-chars [s]
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  (string/replace s #"@|\s" ""))

(defn convert-json [json]
  (cheshire/parse-string json (fn [k] (-> k remove-chars to-kebab-case keyword))))
;; ^ example usage of ->

(defn convert-type [json-map]
  (update-in json-map [:type] keyword))

(convert-type {:type "Profile" :foo {:type "Bar"}})
