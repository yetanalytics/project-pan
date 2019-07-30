(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.graph :as graph]))

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

;; Check if primary is true or false

(s/def ::is-primary-true
  (fn [p] (:primary p)))

(s/def ::is-primary-false
  (fn [p] (not (:primary p))))

;; Ensure that only one of the five regex properties are included in pattern.
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

(s/def ::primary-pattern
  (s/and (s/keys :req-un [::id ::type ::prefLabel ::definition ::primary]
                 :opt-un [::inScheme ::deprecated ::alternates ::optional
                          ::oneOrMore ::sequence ::zeroOrMore])
         ::pattern-clause
         ::is-primary-true))

(s/def ::reg-pattern
  (s/and (s/keys :req-un [::id ::type]
                 :opt-un [::primary ::inScheme ::prefLabel ::definition
                          ::deprecated ::alternates ::optional ::oneOrMore
                          ::sequence ::zeroOrMore])
         ::pattern-clause
         ::is-primary-false))

(s/def ::pattern
  (s/or :no-primary ::reg-pattern
        :primary ::primary-pattern))

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

;; "sequence" is already used by clojure; cannot be used as a symbol when
;; disassociating maps
(defmethod get-pattern-edges '(:sequence) [pattern]
  (mapv #(vector (:id pattern) % {:type :sequence}) (:sequence pattern)))

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

;; Create a pattern graph from its constitutent templates and patterns
(defn create-graph [templates patterns]
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

;; Dissassociate a graph into its edges, in the form of attribute maps
(defn get-edges
  [pgraph]
  (let [edges (uber/edges pgraph)]
    (mapv (fn [edge]
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

(defmulti valid-edge? #(:type %))

;; MUST NOT include optional or zeroOrMore directly inside alternates
(defmethod valid-edge? :alternates
  [{:keys [src-type dest-type dest-property]}]
  (and (#{"Pattern"} src-type)
       (or (and (#{"Pattern"} dest-type)
                (not (#{:optional :zeroOrMore} dest-property)))
           (#{"StatementTemplate"} dest-type))))

;; MUST include at least two members in sequence, unless:
;; 1. sequence is a primary pattern not used elsewhere, and
;; 2. sequence member is a single StatementTemplate
(defmethod valid-edge? :sequence
  [{:keys [src-type dest-type src-indegree src-outdegree src-primary]}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)
       (or (<= 2 src-outdegree)
           (and (#{"StatementTemplate"} dest-type)
                (true? src-primary)
                (= 0 src-indegree)))))

;; Other regex properties: all MUST contain patterns or templates

(defmethod valid-edge? :optional
  [{:keys [src-type dest-type]}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :oneOrMore
  [{:keys [src-type dest-type]}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :zeroOrMore
  [{:keys [src-type dest-type]}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :default [_] false)

(s/def ::valid-edge valid-edge?)

(s/def ::valid-edges (s/coll-of ::valid-edge))

;; MUST NOT include any Pattern within itself, at any depth
;; In other words, no cycles
(s/def ::acyclic-graph alg/dag?)

(s/def ::pattern-graph
  (fn [pgraph] (and (s/valid? ::valid-edges (get-edges pgraph))
                    (s/valid? ::acyclic-graph pgraph))))

(defn explain-graph [pgraph]
  (concat (s/explain-data ::valid-edges (get-edges pgraph))
          (s/explain-data ::acyclic-graph pgraph)))

;; TODO: MAY re-use Statement Templates and Patterns from other Profiles
