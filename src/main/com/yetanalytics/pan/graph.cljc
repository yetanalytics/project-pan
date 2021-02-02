(ns com.yetanalytics.pan.graph
  (:require [clojure.spec.alpha :as s]
            [loom.graph]
            [loom.attr]
            [loom.alg]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Return a node and its attributes, in the form of [node attribute-map].
;; Only special implementation is in the pattern namespace
(defmulti node-with-attrs #(:type %))

;; Default node-creation function
(defmethod node-with-attrs :default [node]
  (let [node-name (:id node)
        node-attrs {:type (:type node)
                    :inScheme (:inScheme node)}]
    (vector node-name node-attrs)))

;; Return a vector of all outgoing edges, in the form [src dest attribute-map].
;; Special implementations are found for all concepts + patterns and templates.
(defmulti edges-with-attrs #(:type %))

;; Default edge creation function (returns an empty vector)
(defmethod edges-with-attrs :default [_] [])

(defn collect-edges
  "Flatten a collection of edges (ie. vectors of form [src dest attrs] such
  that it is a 1D vector of vectors."
  [attr-edges]
  (reduce concat attr-edges))

;; Thin wrappers for Loom functions

(defn new-digraph
  "Init a new directed graph."
  []
  (loom.graph/digraph))

(defn nodes
  "Given a graph, return its nodes."
  [g]
  (loom.graph/nodes g))

(defn edges
  "Given a graph, return its edges."
  [g]
  (loom.graph/edges g))

(defn add-nodes
  "Add a list or vector of nodes to a graph, where each node has the form
   [node attr-map]."
  [g nodes]
  (reduce (fn [g [node attrs]]
            (reduce-kv (fn [g k v] (loom.attr/add-attr g node k v))
                       (loom.graph/add-nodes g node)
                       attrs))
          g
          nodes))

(defn add-edges
  "Add a list or vector of directed edges to a graph, where each node has the
   form [src dest attr-map]."
  [g edges]
  (reduce (fn [g [src dest attrs]]
            (reduce-kv (fn [g k v] (loom.attr/add-attr g src dest k v))
                       (loom.graph/add-edges g [src dest])
                       attrs))
          g
          edges))

(defn src
  "Return the source node of a directed edge."
  [edge]
  (loom.graph/src edge))

(defn dest
  "Return the destination node of a directed edge."
  [edge]
  (loom.graph/dest edge))

(defn attr
  "Return the attribute of a particular node or edge in a graph."
  [g node-or-edge attr]
  (loom.attr/attr g node-or-edge attr))

(defn in-degree
  "Return the in-degree of a node in a digraph."
  [g node]
  (loom.graph/in-degree g node))

(defn out-degree
  "Return the out-degree of a node in a digraph."
  [g node]
  (loom.graph/out-degree g node))

;; Need to manually rewrite transpose and scc function due to Issue #131 in Loom

(defn- transpose [{in :in adj :adj :as g}] (assoc g :adj in :in adj))

(defn- scc* ;; Copy-paste of code from loom.alg namespace
  [g]
  (let [gt (transpose g)]
    (loop [stack (reverse (loom.alg/post-traverse g))
           seen  #{}
           cc    (transient [])]
      (if (empty? stack)
        (persistent! cc)
        (if (seen (first stack))
          (recur (rest stack) seen cc)
          (let [[c seen]
                (loom.alg/post-traverse gt (first stack)
                                        :seen seen
                                        :return-seen true)]
            (recur (rest stack)
                   seen
                   (conj! cc c))))))))

(defn scc
  "Return the strongly-connected components of a digraph as a vector of
   vectors. Uses Kosaraju's algorithm."
  [g]
  (scc* g))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::not-self-loop (fn not-self-loop? [edge] (not= (src edge) (dest edge))))
