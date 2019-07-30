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
;; IRI validation (via graphs)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (defn evaluate-graphs*
;   "Given graph creation and explain functions, create and validate IRI specs"
;   [create-graph-fn explain-graph-fn coll1 & [coll2]]
;   (if (some? coll2)
;     (explain-graph-fn (create-graph-fn coll1 coll2))
;     (explain-graph-fn (create-graph-fn coll1))))

; (defn evaluate-graphs
;   "Given graph creation and explain functions, create and validate IRI specs.
;   Return graph errors (or nil on success) if we have distinct IDs.
;   Else we cannot create the graph; return ID error data."
;   [create-graph-fn explain-graph-fn coll1 & [coll2]]
;   (let [id-errors (->> (concat coll1 coll2) id/only-ids id/count-ids
;                        (s/explain-data ::id/distinct-ids))]
;     (if (empty? id-errors)
;       (evaluate-graphs* create-graph-fn explain-graph-fn coll1 coll2)
;       id-errors)))

;; TODO Take care of potentially duplicate IDs - may mess up graph functions
#_(defn validate-iris
    "Validate all profile IRIs by creating a graph data structure out of the
  Profile. Returns an empty sequence if validation is successful, else a 
  sequence of spec errors if validation fails.
  If this is false, we have to validate before graph evaluation."
    [profile]
    (let [profile (u/normalize-profile profile)
          {:keys [concepts templates patterns] :as profile} profile]
      {:concept-errors (-> concepts concept/create-graph concept/explain-graph)
       :template-errors (template/explain-graph
                         (template/create-graph concepts templates))
       :pattern-errors (pattern/explain-graph
                        (pattern/explain-graph templates patterns))}))
