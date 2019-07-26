(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [camel-snake-kebab.core :as kebab]
            [cheshire.core :as cheshire]
            [ubergraph.core :as uber]
            [com.yetanalytics.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn only-ids
  "Return a collection of IDs from a collection of objects."
  [obj-coll] (mapv (fn [{:keys [id]}] id) obj-coll))

(defn only-ids-multiple
  "Return a collection of all IDs from multiple collections of objects"
  [obj-colls]
  (flatten (mapv only-ids obj-colls)))

#_(defn id-map
    "Create a map of IDs to the objects that they identify."
    [map-with-ids]
    (dissoc (zipmap (only-ids map-with-ids) map-with-ids) nil))

#_(defn combine-args
    "Return a vector of maps that each include an object and additional
  arguments."
    [obj-vec args]
    (mapv #(conj args [:object %]) obj-vec))

(defn count-ids
  "Count the number of ID instances by creating a map between IDs and their
  respective counts. (Ideally theys should all be one, as IDs MUST be unique
  by definition.)"
  [ids-coll]
  (reduce (fn [accum id]
            (update accum id #(if (nil? %) 1 (inc %)))) {} ids-coll))

;; IDs MUST be distinct.
(defn one? [n] (= 1 n))

(s/def ::distinct-ids
  (s/map-of (s/or :iri ::ax/iri :irl ::ax/irl :uri ::ax/uri :url ::ax/url)
            #(= % 1)))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inlineSchema)]
      (not (and schema? inline-schema?)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-chars
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  [s] (string/replace s #"@|\s" ""))

(defn replace-at
  "Replace any @ symbols with 'at/' (so that they are all in their own pseudo-
  namespace."
  [s replacement] (string/replace s #"@" replacement))

(defn convert-json
  "Convert a JSON string into an edn data structure."
  [json at-replacement]
  (cheshire/parse-string
   json #(-> % (replace-at at-replacement) keyword)
   #_(fn [k] (-> k remove-chars kebab/->kebab-case-keyword))))
;; ^ example usage of ->

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a node and its attributes
(defmulti node-with-attrs #(:type %))

(defmethod node-with-attrs :default [node]
  (let [node-name (:id node)
        node-attrs {:type (:type node)
                    :inScheme (:inScheme node)}]
    (vector node-name node-attrs)))

;; Return a vector of all outgoing edges
(defmulti edges-with-attrs #(:type %))

(defmethod edges-with-attrs :default [_] [])

;; Flatten a collection of edges so that it is a 1D vector of [src dest attrs]
;; vectors
(defn collect-edges [attr-edges]
  (reduce concat attr-edges))
