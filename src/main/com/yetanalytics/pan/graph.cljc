(ns com.yetanalytics.pan.graph
  (:require [clojure.spec.alpha :as s]
            [clojure.set        :as cset]))

(s/def ::src any?)
(s/def ::dest any?)

(def node-spec
  (s/cat :node any? :attrs (s/? map?)))
(def edge-spec
  (s/cat :src any? :dest any? :attrs (s/? map?)))
(def edge-or-map-spec
  (s/or :vector edge-spec
        :map (s/keys :req-un [::src ::dest])))

(s/def ::type any?)
(def node-obj-spec (s/keys :opt-un [::type]))
(def edge-obj-spec (s/keys :opt-un [::type]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; For some mysterious reason these specs result in cljs test failures
#?(:clj
   (s/fdef node-with-attrs
     :args (s/cat :node node-obj-spec)
     :ret (s/tuple any? map?)))

;; Return a node and its attributes, in the form of [node attribute-map].
;; Only special implementation is in the pattern namespace
(defmulti node-with-attrs #(:type %))

;; Default node-creation function
(defmethod node-with-attrs :default [node]
  (let [node-name (:id node)
        node-attrs {:type (:type node)
                    :inScheme (:inScheme node)}]
    [node-name node-attrs]))

#?(:clj
   (s/fdef edge-with-attrs
     :args (s/cat :edges edge-obj-spec)
     :ret (s/tuple any? any? map?)))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Loom replacements
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Specs

(s/def ::nodeset set?)
(s/def ::adj (s/map-of any? set?))
(s/def ::in (s/map-of any? set?))
(s/def ::node-attrs (s/map-of any? map?))
(s/def ::edge-attrs (s/map-of any? (s/map-of any? map?)))

(def graph-spec
  (s/keys :req-un [::nodeset ::adj ::in]
          :opt-un [::node-attrs ::edge-attrs]))

;; Graph creation

(s/fdef new-digraph
  :args (s/cat)
  :ret graph-spec)

(defn new-digraph
  "Init a new, empty directed graph."
  []
  {:nodeset #{}
   :adj     {}
   :in      {}})

;; Node and edge get

(s/fdef nodes
  :args (s/cat :graph graph-spec)
  :ret set?)

(defn nodes
  "Given `graph`, return a set of its nodes."
  [graph]
  (:nodeset graph))

(s/fdef edges
  :args (s/cat :graph graph-spec)
  :ret (s/coll-of edge-spec))

(defn edges
  "Given `graph`, return a lazy seq of its edges."
  [graph]
  ;; Could be O(1) but changing this resulted in edge order not matching test
  ;; fixtures.
  (for [src (nodes graph)
        dst (get-in graph [:adj src])]
    [src dst]))

;; Node and edge add

(s/fdef add-nodes
  :args (s/cat :graph graph-spec
               :nodes (s/every node-spec))
  :ret graph-spec)

(defn- add-node-attr
  [g node k v]
  (assoc-in g [:node-attrs node k] v))

(defn add-nodes
  "Add a list or vector of nodes to `graph`, where each node has the form
   [node attr-map]."
  [graph nodes]
  (reduce (fn [g [node attrs]]
            (let [g* (update g :nodeset conj node)]
              (reduce-kv (fn [g k v] (add-node-attr g node k v))
                         g*
                         attrs)))
          graph
          nodes))

(s/fdef add-edges
  :args (s/cat :graph graph-spec
               :edges (s/every edge-spec))
  :ret graph-spec)

(defn- add-edge-attr
  [g src dst k v]
  (assoc-in g [:edge-attrs src dst k] v))

(defn add-edges
  "Add a list or vector of directed edges to `graph`, where each node has the
   form [src dest attr-map]."
  [graph edges]
  (reduce (fn [g [src dst attrs]]
            (let [g* (-> g
                         (update-in [:nodeset] conj src dst)
                         (update-in [:adj src] (fnil conj #{}) dst)
                         (update-in [:in dst] (fnil conj #{}) src))]
              (reduce-kv (fn [g k v]
                           (add-edge-attr g src dst k v))
                         g*
                         attrs)))
          graph
          edges))

;; Graph creation

(s/fdef create-graph*
  :args (s/cat :nodes (s/every node-spec)
               :edges (s/every edge-spec))
  :ret graph-spec)

(defn create-graph*
  "Create a graph with `nodes` and `edges`."
  [nodes edges]
  (-> (new-digraph)
      (add-nodes nodes)
      (add-edges edges)))

;; Args need to be nilable since they may be gotten directly from profile maps
(s/fdef create-graph
  :args (s/cat :node-objs (s/nilable (s/coll-of node-obj-spec))
               :edge-objs (s/nilable (s/coll-of edge-obj-spec)))
  :ret graph-spec)

(defn create-graph
  "Create a graph with `node-objs` and `edge-objs`, which should be
   coerceable by `node-with-attrs` and `edges-with-attrs`,
   respectively."
  [node-objs edge-objs]
  (let [cnodes (->> node-objs
                    (mapv node-with-attrs))
        cedges (->> edge-objs
                    (mapv edges-with-attrs)
                    collect-edges)]
    (create-graph* cnodes cedges)))

;; Edge src or dest

(s/fdef src
  :args (s/cat :edge edge-or-map-spec)
  :ret any?)

(defn src
  "Return the source node of a directed edge."
  [edge]
  (if (vector? edge)
    (get edge 0)
    (get edge :src)))

(s/fdef dest
  :args (s/cat :edge edge-or-map-spec)
  :ret any?)

(defn dest
  "Return the destination node of a directed edge."
  [edge]
  (if (vector? edge)
    (get edge 1)
    (get edge :dest)))

;; Node/edge properties

(s/fdef attr
  :args (s/cat :graph graph-spec
               :node-or-edge (s/or :node any? :edge edge-or-map-spec)
               :attr any?)
  :ret any?)

(defn attr
  "Return the attribute of a particular node or edge in `graph`."
  [g node-or-edge attr]
  (if (contains? (:nodeset g) node-or-edge)
    (get-in g [:node-attrs node-or-edge attr])
    (get-in g [:edge-attrs (src node-or-edge) (dest node-or-edge) attr])))

(s/fdef in-degree
  :args (s/cat :graph graph-spec :node any?)
  :ret int?)

(defn in-degree
  "Return the in-degree of `node` in `graph`."
  [g node]
  (count (get-in g [:in node])))

(s/fdef out-degree
  :args (s/cat :graph graph-spec :node any?)
  :ret int?)

(defn out-degree
  "Return the out-degree of `node` in `graph`."
  [g node]
  (count (get-in g [:adj node])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Kosaraju's Algorithm for Strongly Connected Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; We compute SCCs since each SCC is a cycle, which is important to avoid when
;; constructing xAPI Profile Patterns.

;; Kosaraju's Algorithm has three basic steps:
;; 1. Perform DFS to return a list of nodes in "finishing order," i.e. the
;; order in which we finish visiting a node after all its adjacent outgoing
;; nodes have been visited
;; 2. Compute the transpose of the graph.
;; 3. Perform DFS on the transpose in the order of the previously computed list.
;; Each start node on the list is the "root" of a new strongly connected
;; component.

;; Ideally we should have generic DFS/BFS functions here but implementing
;; them is a bit tricky.
(defn- scc-dfs*
  "Starting at the `start` node in `graph`, with `visited` a set of seen
   nodes from previous DFS iterations, perform a DFS. Return a pair of
   `result` and `visited`, where `result` is a vector of nodes in their
   \"finishing order\" (i.e. when all their outgoing nodes have been visited)
   and the new `visited` including all nodes seen in this DFS."
  [graph start visited]
  (loop [stack   [start]
         visited visited
         result  []]
    (if-some [n (peek stack)]
      (if (contains? visited n)
        (let [stack*  (pop stack)
              result* (cond-> result
                        (not (some #{n} result)) ; only record first visit
                        (conj n))]
          (recur stack* visited result*))
        (let [visited* (conj visited n)
              all-outs (get-in graph [:adj n])
              unv-outs (cset/difference all-outs visited*)
              stack*   (apply conj stack unv-outs)]
          (recur stack* visited* result)))
      [result visited])))

(defn- scc-forward-dfs
  "Perform DFS on `graph`, returning a vector of nodes in their \"finishing
   order\" (i.e. when all their outgoing nodes have been visited)."
  [graph]
  (loop [nodeset (nodes graph)
         visited #{}
         result  []]
    (if-some [n (first nodeset)]
      (let [[new-result visited*] (scc-dfs* graph n visited)
            nodeset* (cset/difference nodeset visited*)
            result*  (vec (concat result new-result))]
        (recur nodeset* visited* result*))
      result)))

(defn- scc-transpose-dfs
  "Perform DFS on `graph-trans`, where `node-vec` is the vector of nodes
   in \"finishing order\". Returns a vector of vector of nodes, where each
   inner vector is a strongly connected component."
  [graph-trans node-vec]
  (loop [nodes   node-vec
         visited #{}
         sccs    []]
    (if-some [n (peek nodes)]
      (let [[next-scc visited*] (scc-dfs* graph-trans n visited)
            nodes* (filterv #(not (contains? visited* %)) (pop nodes))
            sccs*  (conj sccs next-scc)]
        (recur nodes* visited* sccs*))
      sccs)))

;; Normally Kosaraju's algorithm isn't fast due to the need to transpose the
;; graph. But here the transpose computation is O(1) since we implicitly record
;; the transpose via the `:in` field during graph construction.
(defn- transpose
  "Compute the transpose of `graph`."
  [{:keys [in adj] :as graph}]
  (assoc graph :adj in :in adj))

(s/fdef scc
  :args (s/cat :graph graph-spec)
  :ret (s/coll-of (s/coll-of any?)))

(defn scc
  "Return the strongly connected components of `graph` as a vector of
   vectors. Uses Kosaraju's algorithm."
  [graph]
  (let [gtrans  (transpose graph)
        nodevec (scc-forward-dfs graph)]
    (scc-transpose-dfs gtrans nodevec)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::not-self-loop
  (fn not-self-loop? [edge] (not= (src edge) (dest edge))))

;; All strongly connected components (subgraphs where all nodes can be
;; reached from any other node in the subgraph) must be singletons. (Imagine
;; a SCC of two nodes - there must be a cycle present; induct from there.)
;; We find our SCCs using Kosaraju's Algorithm (which is what Loom uses in
;; alg/scc), which has a time complexity of O(V+E); we then validate that they
;; all only have one member node.
;; 
;; Note that Loom has a built-in function for DAG determination (which does
;; correctly identify self-loops), but we use this algorithm to make our spec
;; errors cleaner.
;; 
;; The following specs are to be used on the result of graph/scc.

(s/def ::singleton-scc
  (s/coll-of any? :kind vector? :min-count 1 :max-count 1))

(s/def ::singleton-sccs
  (s/coll-of ::singleton-scc :kind vector?))
 