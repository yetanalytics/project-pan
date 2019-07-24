(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [camel-snake-kebab.core :as kebab]
            [cheshire.core :as cheshire]
            [ubergraph.core :as uber]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn only-ids
  "Return a collection of IDs from a map of objects."
  [obj-coll] (mapv (fn [{:keys [id]}] id) obj-coll))

(defn id-map [map-with-ids]
  "Create a map of IDs to the objects that they identify."
  (dissoc (zipmap (only-ids map-with-ids) map-with-ids) nil))

(defn combine-args
  "Return a vector of maps that each include an object and additional
  arguments."
  [obj-vec args]
  (mapv #(conj args [:object %]) obj-vec))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inline-schema)]
      (not (and schema? inline-schema?)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-chars
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  [s] (string/replace s #"@|\s" ""))

(defn convert-json
  "Convert a JSON string into an edn data structure."
  [json]
  (cheshire/parse-string
   json (fn [k] (-> k remove-chars kebab/->kebab-case-keyword))))
;; ^ example usage of ->

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a node and its attributes
(defmulti node-with-attrs #(:type %))

(defmethod node-with-attrs :default [node]
  (let [node-name (:id node)
        node-attrs {:type (:type node)
                    :in-scheme (:in-scheme node)}]
    (vector node-name node-attrs)))

;; Return a vector of all outgoing edges
(defmulti edges-with-attrs #(:type %))

(defmethod edges-with-attrs :default [_] [])

;; Flatten a collection of edges so that it is a 1D vector of [src dest attrs]
;; vectors
(defn collect-edges [attr-edges]
  (reduce concat attr-edges))
