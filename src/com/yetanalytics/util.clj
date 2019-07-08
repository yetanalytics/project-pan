(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns + specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Returns a collection of ids
(defn only-ids [filtered-coll] (mapv (fn [{:keys [id]}] id) filtered-coll))

;; Create a map of ids to the objects they identify
(defn id-map [map-with-ids]
  (dissoc (zipmap (only-ids map-with-ids) map-with-ids) nil))

(defn combine-args
  "Return a vector of maps that each include an object and additional
  arguments."
  [obj-vec args]
  (mapv #(conj args [:object %]) obj-vec))

; (s/def ::in-scheme-valid?
;   (fn [{:keys [object vid-set]}]
;     (contains? vid-set (object :in-scheme))))

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
