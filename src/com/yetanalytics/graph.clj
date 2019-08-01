(ns com.yetanalytics.graph
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.identifiers :as id]
            [com.yetanalytics.util :as u]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a node and its attributes
(defmulti node-with-attrs #(:type %))

;; Default node-returing function
(defmethod node-with-attrs :default [node]
  (let [node-name (:id node)
        node-attrs {:type (:type node)
                    :inScheme (:inScheme node)}]
    (vector node-name node-attrs)))

;; Return a vector of all outgoing edges
(defmulti edges-with-attrs #(:type %))

;; Default edge returning function (returns nothing)
(defmethod edges-with-attrs :default [_] [])

(defn collect-edges
  "Flatten a collection of edges (ie. vectors of form [src dest attrs] such
  that it is a 1D vector of vectors."
  [attr-edges]
  (reduce concat attr-edges))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::not-self-loop (fn not-self-loop?
                         [edge] (not= (uber/src edge) (uber/dest edge))))
