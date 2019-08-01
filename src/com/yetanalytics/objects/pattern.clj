(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.graph :as graph]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Patterns 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Basic properties
(s/def ::id ::ax/uri)
(s/def ::type #{"Pattern"})
(s/def ::primary ::ax/boolean)
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

;; Regex properties
;; MUST include at least two members in alternates
(s/def ::alternates (s/coll-of ::ax/iri :type vector? :min-count 2))
(s/def ::optional (s/keys :req-un [::id]))
(s/def ::oneOrMore (s/keys :req-un [::id]))
(s/def ::sequence (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::zeroOrMore (s/keys :req-un [::id]))

;; Check if 'primary' property is true or false
(s/def ::is-primary-true (fn [p] (:primary p)))
(s/def ::is-primary-false (fn [p] (not (:primary p))))

;; A Pattern MUST contain exactly one of 'sequence', 'alternates', 'optional',
;; 'oneOrMore' or 'zeroOrMore'.
;; Ensure that only one of those five regex properties are included in pattern.
;; Including two or more properties should fail the spec. 
(s/def ::pattern-clause
  (fn [p]
    (let [alt? (contains? p :alternates)
          opt? (contains? p :optional)
          oom? (contains? p :oneOrMore)
          sqn? (contains? p :sequence)
          zom? (contains? p :zeroOrMore)]
      (cond
        alt? (not (or opt? oom? sqn? zom?))
        opt? (not (or alt? oom? sqn? zom?))
        oom? (not (or alt? opt? sqn? zom?))
        sqn? (not (or alt? opt? oom? zom?))
        zom? (not (or alt? opt? oom? sqn?))))))

;; Spec for a primary pattern ('primary' is set to true).
(s/def ::primary-pattern
  (s/and (s/keys :req-un [::id ::type ::prefLabel ::definition ::primary]
                 :opt-un [::inScheme ::deprecated ::alternates ::optional
                          ::oneOrMore ::sequence ::zeroOrMore])
         ::pattern-clause
         ::is-primary-true))

;; Spec for a non-primary pattern ('primary' is set to false).
(s/def ::non-primary-pattern
  (s/and (s/keys :req-un [::id ::type]
                 :opt-un [::primary ::inScheme ::prefLabel ::definition
                          ::deprecated ::alternates ::optional ::oneOrMore
                          ::sequence ::zeroOrMore])
         ::pattern-clause
         ::is-primary-false))

;; Spec for a generic pattern.
(s/def ::pattern
  (s/or :non-primary ::non-primary-pattern
        :primary ::primary-pattern))

;; Spec for a vector of patterns.
(s/def ::patterns
  (s/coll-of ::pattern :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Graph creation functions

;; Get the IRIs of a Pattern (within a sequence), depending on its property
(defn dispatch-on-pattern [pattern]
  (keys (dissoc pattern :id :type :prefLabel :definition
                :primary :inScheme :deprecated)))

;; Obtain a vector of edges originating from a pattern.
;; The multimethod dispatches on what regex property the pattern has.

(defmulti get-pattern-edges dispatch-on-pattern)

(defmethod get-pattern-edges '(:alternates) [{:keys [id alternates]}]
  (mapv #(vector id % {:type :alternates}) alternates))

;; Use non-terse destructuring syntax because "sequence" is already a Clojure
;; core function
(defmethod get-pattern-edges '(:sequence) [{:keys [id] :as pattern}]
  (mapv #(vector id % {:type :sequence}) (:sequence pattern)))

(defmethod get-pattern-edges '(:optional) [{:keys [id optional]}]
  (vector (vector id (:id optional) {:type :optional})))

(defmethod get-pattern-edges '(:oneOrMore) [{:keys [id oneOrMore]}]
  (vector (vector id (:id oneOrMore) {:type :oneOrMore})))

(defmethod get-pattern-edges '(:zeroOrMore) [{:keys [id zeroOrMore]}]
  (vector (vector id (:id zeroOrMore) {:type :zeroOrMore})))

(defmethod get-pattern-edges :default [_] nil)

;; Return a vector of pattern edges in the form [src dest {:type kword}] 
(defmethod graph/edges-with-attrs "Pattern" [pattern]
  (get-pattern-edges pattern))

;; Return a vector of nodes of the form [id attribute-map]
(defmethod graph/node-with-attrs "Pattern" [pattern]
  (let [id (:id pattern)
        attrs {:type "Pattern"
               :primary (get pattern :primary false)
               :property (first (dispatch-on-pattern pattern))}]
    (vector id attrs)))

(defn create-graph
  "Create a graph of links between Patterns and other Patterns and Templates."
  [templates patterns]
  (let [pgraph (uber/digraph)
        ;; Nodes
        tnodes (mapv (partial graph/node-with-attrs) templates)
        pnodes (mapv (partial graph/node-with-attrs) patterns)
        ;; Edges 
        pedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) patterns))]
    (-> pgraph
        (uber/add-nodes-with-attrs* tnodes)
        (uber/add-nodes-with-attrs* pnodes)
        (uber/add-directed-edges* pedges))))

(defn get-edges
  "Return a sequence of edges in the form of maps, with the following keys:
  - src: source node ID value
  - dest: destination node ID value
  - src-primary: true if the source node is a primary pattern, false otherwise
  - src-indegree: how many other Patterns reference this node
  - src-outdegree: how mahy Patterns or Templates this node references
  - dest-type: 'type' property of destination node (Pattern or 
  StatementTemplate)
  - dest-property: the regex property of the destination node (sequence,
  alternates, etc.); nil if the destination is a Template
  - type: the regex property of this edge (sequence, alternates, etc.)"
  [pgraph]
  (let [edges (uber/edges pgraph)]
    (map (fn [edge]
           (let [src (uber/src edge) dest (uber/dest edge)]
             {:src src
              :src-type (uber/attr pgraph src :type)
              :src-primary (uber/attr pgraph src :primary)
              :src-indegree (uber/in-degree pgraph src)
              :src-outdegree (uber/out-degree pgraph src)
              :dest dest
              :dest-type (uber/attr pgraph dest :type)
              :dest-property (uber/attr pgraph dest :property)
              :type (uber/attr pgraph edge :type)}))
         edges)))

;; Edge property specs

;; Is the destination not nil?
(s/def ::valid-dest
  (fn [{:keys [dest-type]}]
    (some? dest-type)))

;; Is the source a Pattern?
(s/def ::pattern-src
  (fn [{:keys [src-type]}] (contains? #{"Pattern"} src-type)))

;; Is the destination a Pattern?
(s/def ::pattern-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"Pattern"} dest-type)))

;; Is the destination a Template?
(s/def ::template-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"StatementTemplate"} dest-type)))

;; Unique to alternates patterns

;; Is the destination not 'optional' or 'zeroOrMore'?
(s/def ::non-opt-dest
  (fn [{:keys [dest-property]}]
    (not (contains? #{:optional :zeroOrMore} dest-property))))

;; Unique to sequence patterns

;; Does the source only have one outgoing connection?
(s/def ::singleton-src
  (fn [{:keys [src-outdegree]}]
    (= 1 src-outdegree)))

;; Does the source have two or more outgoing connections?
(s/def ::not-singleton-src
  (fn [{:keys [src-outdegree]}]
    (<= 2 src-outdegree)))

;; Is the source node a primary Pattern?
(s/def ::primary-src
  (fn [{:keys [src-primary]}]
    (true? src-primary)))

;; Does the source node have zero incoming connections? In other words, is it
;; used nowhere else in the Profile?
(s/def ::zero-indegree-src
  (fn [{:keys [src-indegree]}]
    (= 0 src-indegree)))

;; Edge validation multimethod

(defmulti valid-edge? util/type-dispatch)

;; MUST NOT include optional or zeroOrMore directly inside alternates
(defmethod valid-edge? :alternates [_]
  (s/and ::pattern-src
         ::graph/not-self-loop
         ::valid-dest
         (s/or :pattern (s/and ::pattern-dest
                               ::non-opt-dest)
               :template ::template-dest)))

;; MUST include at least two members in sequence, unless:
;; 1. sequence is a primary pattern not used elsewhere, and
;; 2. sequence member is a single StatementTemplate 
(defmethod valid-edge? :sequence [_]
  (s/and ::pattern-src
         ::valid-dest
         ::graph/not-self-loop
         (s/or :two-or-more
               (s/and ::not-singleton-src
                      (s/or :pattern ::pattern-dest
                            :template ::template-dest))
               :one
               (s/and ::singleton-src
                      ::template-dest
                      ::primary-src
                      ::zero-indegree-src))))

;; Other regex properties: all MUST lead to a Pattern or Statement Template 

(defmethod valid-edge? :optional [_]
  (s/and ::pattern-src
         ::valid-dest
         ::graph/not-self-loop
         (s/or :pattern ::pattern-dest
               :template ::template-dest)))

(defmethod valid-edge? :oneOrMore [_]
  (s/and ::pattern-src
         ::valid-dest
         ::graph/not-self-loop
         (s/or :pattern ::pattern-dest
               :template ::template-dest)))

(defmethod valid-edge? :zeroOrMore [_]
  (s/and ::pattern-src
         ::valid-dest
         ::graph/not-self-loop
         (s/or :pattern ::pattern-dest
               :template ::template-dest)))

;; Is one edge valid?
(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))

;; Are all the edges valid?
(s/def ::valid-edges (s/coll-of ::valid-edge))

;; MUST NOT include any Pattern within itself, at any depth.
;; In other words, no cycles. We need to check for two things:
;;
;; 1. All strongly connected components (subgraphs where all nodes can be
;; reached from any other node in the subgraph) must be singletons. (Imagine
;; a SCC of two nodes - there must be a cycle present; induct from there.)
;; We find our SCCs using Kosaraju's Algorithm (which is what Ubergraph uses
;; in alg/scc), which has a time complexity of O(V+E); we then validate that 
;; they all only have one member node.
;;
;; 2. No self-loops exist. This condition is not caught by Kosaraju's Algorithm
;; (and thus not by our SCC specs) but is caught by our edge validation.
;;
;; Note that Ubergraph has a built-in function for DAG determination (which
;; does correctly identify self-loops), but we use this algorithm to make our 
;; spec errors cleaner.

;; Check that one SCC is a singleton
;; (Technically we can do this with s/cat, but this allows us to return the
;; entire vector as a value in the error map)
(s/def ::singleton-scc
  (s/coll-of any? :kind vector? :min-count 1 :max-count 1))

;; Check that all SCCs are singletons
(s/def ::singleton-sccs (s/coll-of ::singleton-scc :kind vector?))

(s/def ::pattern-graph
  (fn [pgraph] (and (s/valid? ::valid-edges (get-edges pgraph))
                    (s/valid? ::acyclic-graph pgraph))))

;; Edge validation
(defn explain-graph [pgraph]
  (s/explain-data ::valid-edges (get-edges pgraph)))

;; Cycle validation
(defn explain-graph-cycles [pgraph]
  (s/explain-data ::singleton-sccs (alg/scc pgraph)))

;; TODO: MAY re-use Statement Templates and Patterns from other Profiles
