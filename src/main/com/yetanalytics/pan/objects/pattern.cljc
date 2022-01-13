(ns com.yetanalytics.pan.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.graph :as graph]))

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
(s/def ::optional ::ax/iri)
(s/def ::oneOrMore ::ax/iri)
(s/def ::sequence (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::zeroOrMore ::ax/iri)

;; Check if 'primary' property is true or false
(s/def ::is-primary-true (fn is-primary? [p] (:primary p)))
(s/def ::is-primary-false (fn is-not-primary? [p] (not (:primary p))))

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
(s/def ::primary-pattern-keys
  (s/keys :req-un [::id ::type ::prefLabel ::definition ::primary]
          :opt-un [::inScheme ::deprecated ::alternates ::optional
                   ::oneOrMore ::sequence ::zeroOrMore]))

(s/def ::primary-pattern
  (s/and ::primary-pattern-keys
         ::pattern-clause
         ::is-primary-true))

;; Spec for a non-primary pattern ('primary' is set to false).
(s/def ::non-primary-pattern-keys
  (s/keys :req-un [::id ::type]
          :opt-un [::primary ::inScheme ::prefLabel ::definition
                   ::deprecated ::alternates ::optional ::oneOrMore
                   ::sequence ::zeroOrMore]))

(s/def ::non-primary-pattern
  (s/and ::non-primary-pattern-keys
         ::pattern-clause
         ::is-primary-false))

(defmulti pattern? #(:primary %))
(defmethod pattern? true [_] ::primary-pattern)
(defmethod pattern? :default [_] ::non-primary-pattern)

;; Spec for a generic pattern.
(s/def ::pattern (s/multi-spec pattern? #(:primary %)))

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

;; Use non-terse destructuring syntax because "sequence" is already a Clojure
;; core function
(defmethod get-pattern-edges '(:sequence) [{:keys [id] :as pattern}]
  (mapv #(vector id % {:type :sequence}) (:sequence pattern)))

(defmethod get-pattern-edges '(:alternates) [{:keys [id alternates]}]
  (mapv #(vector id % {:type :alternates}) alternates))

(defmethod get-pattern-edges '(:optional) [{:keys [id optional]}]
  (vector (vector id optional {:type :optional})))

(defmethod get-pattern-edges '(:oneOrMore) [{:keys [id oneOrMore]}]
  (vector (vector id oneOrMore {:type :oneOrMore})))

(defmethod get-pattern-edges '(:zeroOrMore) [{:keys [id zeroOrMore]}]
  (vector (vector id zeroOrMore {:type :zeroOrMore})))

(defmethod get-pattern-edges :default [_] nil)

;; Return a vector of nodes of the form [id attribute-map]
(defmethod graph/node-with-attrs "Pattern" [{id :id :as pattern}]
  (let [attrs {:type "Pattern"
               :primary (get pattern :primary false)
               :property (first (dispatch-on-pattern pattern))}]
    (vector id attrs)))

;; Return a vector of pattern edges in the form [src dest {:type kword}] 
(defmethod graph/edges-with-attrs "Pattern" [pattern]
  (get-pattern-edges pattern))

(def pattern-ext-keys [:sequence :alternates :optional :oneOrMore :zeroOrMore])

(defn get-graph-templates-patterns
  [profile extra-profiles]
  (let [patterns  (:patterns profile)
        ext-ids   (set (reduce
                        (fn [acc pat]
                          (-> pat
                              (select-keys pattern-ext-keys)
                              vals
                              flatten
                              (concat acc)))
                        []
                        patterns))
        ext-pats  (->> extra-profiles
                       (mapcat :patterns)
                       (filter (fn [{id :id}] (contains? ext-ids id))))
        templates (->> profile
                       (mapcat :templates)
                       (filter (fn [{id :id}] (contains? ext-ids id))))
        ext-tmps  (->> extra-profiles
                       (mapcat :templates)
                       (filter (fn [{id :id}] (contains? ext-ids id))))]
    {:templates    (concat templates ext-tmps)
     :patterns     patterns
     :ext-patterns ext-pats}))

(defn create-graph
  "Create a graph of links between Patterns and other Patterns and Templates."
  [templates patterns]
  (let [pgraph (graph/new-digraph)
        ;; Nodes
        tnodes (mapv (partial graph/node-with-attrs) templates)
        pnodes (mapv (partial graph/node-with-attrs) patterns)
        ;; Edges 
        pedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) patterns))]
    (-> pgraph
        (graph/add-nodes tnodes)
        (graph/add-nodes pnodes)
        (graph/add-edges pedges))))

(defn create-graph-2
  [profile extra-profiles]
  (let [{:keys [templates
                patterns
                ext-patterns]} (get-graph-templates-patterns
                                profile
                                extra-profiles)
        pgraph (graph/new-digraph)
        pnodes (->> (concat templates patterns ext-patterns)
                    (mapv graph/node-with-attrs))
        pedges (->> patterns
                    (mapv graph/edges-with-attrs)
                    graph/collect-edges)]
    (-> pgraph
        (graph/add-nodes pnodes)
        (graph/add-edges pedges))))

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
  (map (fn [edge]
         (let [src  (graph/src edge)
               dest (graph/dest edge)]
           {:src           src
            :src-type      (graph/attr pgraph src :type)
            :src-primary   (graph/attr pgraph src :primary)
            :src-indegree  (graph/in-degree pgraph src)
            :src-outdegree (graph/out-degree pgraph src)
            :dest          dest
            :dest-type     (graph/attr pgraph dest :type)
            :dest-property (graph/attr pgraph dest :property)
            :type          (graph/attr pgraph edge :type)}))
       (graph/edges pgraph)))

(comment
  (get-edges
   (create-graph-2
    {:patterns [{:id "https://foo.org/pattern1" :type "Pattern"
                 :inScheme "https://foo.org/v1" :primary true
                 :alternates ["https://foo.org/pattern2"]}
                {:id "https://foo.org/pattern2" :type "Pattern"
                 :inScheme "https://foo.org/v1" :primary true
                 :sequence ["https://foo.org/pattern3"
                            "https://foo.org/template1"]}]}
    [{:templates [{:id "https://foo.org/template1"
                   :type "StatementTemplate" :inScheme "https://foo.org/v1"}]
      :patterns [{:id "https://foo.org/pattern3" :type "Pattern"
                  :inScheme "https://foo.org/v1" :primary true
                  :optional "https://foo.org/template1"}]}])))

;; Edge property specs

;; Is the destination not nil?
(s/def ::valid-dest
  (fn valid-dest? [{:keys [dest-type]}]
    (some? dest-type)))

;; Is the source a Pattern?
(s/def ::pattern-src
  (fn pattern-src? [{:keys [src-type]}]
    (contains? #{"Pattern"} src-type)))

;; Is the destination a Pattern?
(s/def ::pattern-dest
  (fn pattern-dest? [{:keys [dest-type]}]
    (contains? #{"Pattern"} dest-type)))

;; Is the destination a Template?
(s/def ::template-dest
  (fn template-dest? [{:keys [dest-type]}]
    (contains? #{"StatementTemplate"} dest-type)))

;; Unique to alternates patterns

;; Is the destination not 'optional' or 'zeroOrMore'?
(s/def ::non-opt-dest
  (fn non-opt-dest? [{:keys [dest-property]}]
    (not (contains? #{:optional :zeroOrMore} dest-property))))

;; Unique to sequence patterns

;; Does the source only have one outgoing connection?
(s/def ::singleton-src
  (fn singleton-src? [{:keys [src-outdegree]}]
    (= 1 src-outdegree)))

;; Does the source have two or more outgoing connections?
(s/def ::not-singleton-src
  (fn not-singleton-src? [{:keys [src-outdegree]}]
    (<= 2 src-outdegree)))

;; Is the source node a primary Pattern?
(s/def ::primary-src
  (fn primary-src? [{:keys [src-primary]}]
    (true? src-primary)))

;; Does the source node have zero incoming connections? In other words, is it
;; used nowhere else in the Profile?
(s/def ::zero-indegree-src
  (fn zero-indegree-src? [{:keys [src-indegree]}]
    (= 0 src-indegree)))

;; Edge validation multimethod

(defmulti valid-edge? :type)

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
(s/def ::pattern-edge (s/multi-spec valid-edge? :type))

;; Are all the edges valid?
(s/def ::pattern-edges (s/coll-of ::pattern-edge))

;; Edge validation
(defn validate-pattern-edges [pgraph]
  (s/explain-data ::pattern-edges (get-edges pgraph)))

;; MUST NOT include any Pattern within itself, at any depth.
;; In other words, no cycles (including self loops)
(defn validate-pattern-tree [pgraph]
  (s/explain-data ::graph/singleton-sccs (graph/scc pgraph)))

;; TODO: MAY re-use Statement Templates and Patterns from other Profiles

(defn- pattern-children
  [patterns-m {pat-type :type :as pat}]
  (when (= "Pattern" pat-type)
    (let [child-iris (or (-> pat :sequence)
                         (-> pat :alternates)
                         (-> pat :optional vector)
                         (-> pat :oneOrMore vector)
                         (-> pat :zeroOrMore vector))]
      (map #(get patterns-m %) child-iris))))

;; Normal loop-recur-based DFS cannot record path traversed due to its
;; iterative nature, so we roll our own recursion-based solution.
(defn- pattern-dfs*
  [pat-children-fn pattern pat-visit pat-path]
  (let [{pat-id :id :as pat} pattern]
    (if (contains? pat-visit pat-id)
      [(conj pat-path pat-id)]
      (let [pat-visit' (conj pat-visit pat-id)
            pat-path'  (conj pat-path pat-id)]
        (if-some [children (-> pat pat-children-fn not-empty)]
          (mapcat (fn [child]
                    (pattern-dfs* pat-children-fn child pat-visit' pat-path'))
                  children)
          [pat-path'])))))

(defn pattern-dfs
  [patterns-m pattern]
  (pattern-dfs* (partial pattern-children patterns-m) pattern #{} []))

;; TODO: Move to util namespace
(defn- count-ids
  [ids-coll]
  (reduce (fn [accum id] (update accum id #(if (nil? %) 1 (inc %))))
          {}
          ids-coll))

(s/def ::non-cyclic-path
       (s/and (s/conformer count-ids)
              (s/map-of ::ax/iri #(= 1 %))))

(defn validate-pattern-tree-2
  [profile extra-profiles]
  (let [primaries (filter :primary (:patterns profile))
        all-pats  (mapcat :patterns (concat [profile] extra-profiles))
        pats-map  (zipmap (map :id all-pats) all-pats)
        pat-paths (mapcat (partial pattern-dfs pats-map) primaries)]
    (some #(s/explain-data ::non-cyclic-path %)
          pat-paths)))
