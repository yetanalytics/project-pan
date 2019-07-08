(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns + specs
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
