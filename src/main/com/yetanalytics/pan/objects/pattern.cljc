(ns com.yetanalytics.pan.objects.pattern
  (:require [clojure.spec.alpha               :as s]
            [com.yetanalytics.pan.axioms      :as ax]
            [com.yetanalytics.pan.graph       :as graph]
            [com.yetanalytics.pan.identifiers :as ids]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pattern Specs
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

;; NOTE: The inScheme property is listed as Optional in the spec, but that
;; is actually an error.
;; See: https://github.com/adlnet/xapi-profiles/issues/245

;; Spec for a primary pattern ('primary' is set to true).
(s/def ::primary-pattern-keys
  (s/keys :req-un [::id ::type ::inScheme ::prefLabel ::definition ::primary]
          :opt-un [::deprecated ::alternates ::optional
                   ::oneOrMore ::sequence ::zeroOrMore]))

(s/def ::primary-pattern
  (s/and ::primary-pattern-keys
         ::pattern-clause
         ::is-primary-true))

;; Spec for a non-primary pattern ('primary' is set to false).
(s/def ::non-primary-pattern-keys
  (s/keys :req-un [::id ::type ::inScheme]
          :opt-un [::primary ::prefLabel ::definition
                   ::deprecated ::alternates ::optional ::oneOrMore
                   ::sequence ::zeroOrMore]))

(s/def ::non-primary-pattern
  (s/and ::non-primary-pattern-keys
         ::pattern-clause
         ::is-primary-false))

(defmulti pattern? :primary)
(defmethod pattern? true [_] ::primary-pattern)
(defmethod pattern? :default [_] ::non-primary-pattern)

;; Spec for a generic pattern.
;; The retag fn is so that both primary and non-primary patterns can be
;; generated.
(s/def ::pattern (s/multi-spec pattern? (fn [gen-v _] gen-v)))

;; Spec for a vector of patterns.
(s/def ::patterns
  (s/coll-of ::pattern :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pattern Graph Creation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def pattern-iri-keys
  [:sequence :alternates :optional :oneOrMore :zeroOrMore])

(defn get-external-iris
  "Return the external IRIs from the Patterns of `profile`."
  [profile]
  (let [{:keys [templates patterns]} profile
        id-filter-set (set (concat (ids/objs->ids templates)
                                   (ids/objs->ids patterns)))]
    (ids/objs->out-ids-map patterns pattern-iri-keys id-filter-set)))

;; ;;;;; Node and Edge Creation ;;;;;

;; Get the IRIs of a Pattern (within a sequence), depending on its property
(defn- dispatch-on-pattern [pattern]
  (keys (select-keys pattern pattern-iri-keys)))

;; Obtain a vector of edges originating from a pattern.
;; The multimethod dispatches on what regex property the pattern has.

(defmulti get-pattern-edges dispatch-on-pattern)

(defmethod get-pattern-edges '(:sequence) [{:keys [id] :as pattern}]
  ;; Use non-terse destructuring syntax because `sequence` is already a
  ;; Clojure core function
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
  (let [attrs {:type     "Pattern"
               :primary  (get pattern :primary false)
               :property (first (dispatch-on-pattern pattern))}]
    (vector id attrs)))

;; Return a vector of pattern edges in the form [src dest {:type kword}] 
(defmethod graph/edges-with-attrs "Pattern" [pattern]
  (get-pattern-edges pattern))

;; ;;;;; Graph Creation ;;;;;

(defn- empty-queue []
  #?(:clj clojure.lang.PersistentQueue/EMPTY
     :cljs cljs.core/PersistentQueue.EMPTY))

(defn- pattern-children
  "Return the children of the Pattern given by `pat-id`."
  [patterns-m pat-id]
  (let [{pat-type :type :as pat} (get patterns-m pat-id)]
    (when (= "Pattern" pat-type)
      (let [child-iris (or (some-> pat :sequence)
                           (some-> pat :alternates)
                           (some-> pat :optional vector)
                           (some-> pat :oneOrMore vector)
                           (some-> pat :zeroOrMore vector))]
        (map #(get patterns-m %) child-iris)))))

(defn- append-bfs
  "Perform a breadth-first traversal through the Profile Patterns
   such that all nodes and edges connected to those in the original
   Profile are found."
  [pat-map init-pnodes init-pedges queue-objs visit-objs]
  (loop [;; Start with the nodes and edges from the main Profile
         pnodes init-pnodes
         pedges init-pedges
         ;; Add to queue the Patterns/Templates adjacent to the main
         ;; Profile nodes
         pqueue (->> queue-objs (apply conj (empty-queue)))
         pvisit (->> visit-objs (map :id) set)]
    (if-some [{pat-tmp-id :id :as pat-tmp} (peek pqueue)]
      (if (contains? pvisit pat-tmp-id)
        ;; Already visited this node; skip adding it
        (recur pnodes pedges (pop pqueue) pvisit)
        ;; Visitng new node
        (let [;; Add the node and, if it's a Pattern, its outgoing edges
              new-pnode  (graph/node-with-attrs pat-tmp)
              new-pedges (when (= "Pattern" (:type pat-tmp))
                           (graph/edges-with-attrs pat-tmp))
              next-pats  (pattern-children pat-map pat-tmp-id)]
          (recur (conj pnodes new-pnode)
                 (concat pedges new-pedges)
                 (apply conj (pop pqueue) next-pats)
                 (conj pvisit pat-tmp-id))))
      [pnodes pedges])))

(defn- create-graph*
  [patterns templates ?ext-pats ?ext-tmps]
  (let [out-ids    (ids/objs->out-ids patterns pattern-iri-keys)
        templates* (ids/filter-by-ids out-ids templates)
        pnodes     (->> (concat patterns templates*)
                        (mapv graph/node-with-attrs))
        pedges     (->> patterns
                        (mapv graph/edges-with-attrs)
                        graph/collect-edges)]
    (if (or ?ext-tmps ?ext-pats)
      (let [pat-coll  (concat patterns templates ?ext-pats ?ext-tmps)
            pat-map   (zipmap (map :id pat-coll) pat-coll)
            init-exts (->> (concat ?ext-pats ?ext-tmps)
                           (ids/filter-by-ids out-ids))
            inits     (concat patterns templates*)
            [pn pe]   (append-bfs pat-map pnodes pedges init-exts inits)]
        (graph/create-graph* pn pe))
      (graph/create-graph* pnodes pedges))))

(defn create-graph
  "Create a graph of Pattern relations from `profile` and possibly
   `extra-profiles` that can then be used in validation. Relations
   can include those between Patterns and Statement Templates. If
   `extra-profiles` is provided, those profiles are traversed in order
   to add Patterns and Templates that are connected to the nodes
   of the main Profile's Pattern graph."
  ([profile]
   (let [{:keys [patterns templates]} profile]
     (create-graph* patterns templates nil nil)))
  ([profile extra-profiles]
   (let [{:keys [patterns templates]} profile
         ext-pats (mapcat :patterns extra-profiles)
         ext-tmps (mapcat :templates extra-profiles)]
     (create-graph* patterns templates ext-pats ext-tmps))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pattern Graph Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(s/def ::pattern-edge (s/multi-spec valid-edge? :type))

(s/def ::pattern-edges (s/coll-of ::pattern-edge))

(defn validate-pattern-edges
  "Given the Pattern graph `pgraph`, return spec error data if the
   graph edges are invalid according to the xAPI Profile spec, or
   `nil` otherwise."
  [pgraph]
  (s/explain-data ::pattern-edges (get-edges pgraph)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pattern Cycle Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; MUST NOT include any Pattern within itself, at any depth.
;; In other words, no cycles (including self loops)
(defn validate-pattern-tree
  "Given the Pattern graph `pgraph`, return spec error data if `pgraph`
   has non-singleton strongly connected components (indicating that
   a cycle was detected and `pgraph` is not a tree), `nil` otherwise."
  [pgraph]
  (s/explain-data ::graph/singleton-sccs (graph/scc pgraph)))
