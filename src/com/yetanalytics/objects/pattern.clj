(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as uber]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;; Basic properties


(s/def ::id ::ax/uri)
(s/def ::type #{"Pattern"})
(s/def ::primary ::ax/boolean)
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

(s/def ::alternates (s/coll-of ::ax/iri :type vector? :min-count 2))
(s/def ::optional (s/keys :req-un [::id]))
(s/def ::one-or-more (s/keys :req-un [::id]))
(s/def ::sequence (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::zero-or-more (s/keys :req-un [::id]))

;; Ensure that only one of the five regex properties are included in pattern.
;; Including two or more properties should fail the spec.

(s/def ::pattern-clause
  (fn [p]
    (let [alt? (contains? p :alternates)
          opt? (contains? p :optional)
          oom? (contains? p :one-or-more)
          sqn? (contains? p :sequence)
          zom? (contains? p :zero-or-more)]
      (cond
        alt? (not (or opt? oom? sqn? zom?))
        opt? (not (or alt? oom? sqn? zom?))
        oom? (not (or alt? opt? sqn? zom?))
        sqn? (not (or alt? opt? oom? zom?))
        zom? (not (or alt? opt? oom? sqn?))))))

(s/def ::primary-pattern
  (s/and (s/keys :req-un [::id ::type ::pref-label ::definition ::primary]
                 :opt-un [::in-scheme ::deprecated ::alternates ::optional
                          ::one-or-more ::sequence ::zero-or-more])
         ::pattern-clause))

(s/def ::pattern
  (s/and (s/keys :req-un [::id ::type]
                 :opt-un [::primary ::in-scheme ::pref-label ::definition
                          ::deprecated ::alternates ::optional ::one-or-more
                          ::sequence ::zero-or-more])
         ::pattern-clause))

(s/def ::patterns
  (s/coll-of (s/or :pattern ::pattern
                   :primary ::primary-pattern) :kind vector? :min-count 1))

;; TODO: MUST + MUST NOTS from Profile Authors: section
;; https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#90-patterns

;; Get the IRIs of a Pattern, depending on its property
(defn dispatch-on-pattern [pattern]
  (keys (dissoc pattern :id :type :pref-label :definition
                :primary :in-scheme :deprecated)))

(defmulti get-edges dispatch-on-pattern)

(defmethod get-edges '(:alternates) [{:keys [id alternates]}]
  (mapv #(vector id % {:type :alternates}) alternates))

(defmethod get-edges '(:sequence) [pattern]
  (mapv #(vector (:id pattern) % {:type :sequence}) (:sequence pattern)))

(defmethod get-edges '(:optional) [{:keys [id optional]}]
  (vector (vector id (:id optional) {:type :optional})))

(defmethod get-edges '(:one-or-more) [{:keys [id one-or-more]}]
  (vector (vector id (:id one-or-more) {:type :one-or-more})))

(defmethod get-edges '(:zero-or-more) [{:keys [id zero-or-more]}]
  (vector (vector id (:id zero-or-more) {:type :zero-or-more})))

(defmethod get-edges :default [_] nil)

(defmethod util/edges-with-attrs "Pattern" [pattern]
  (get-edges pattern))

(defmethod util/node-with-attrs [pattern]
  (let [id (:id pattern)
        attrs {:type "Pattern"
               :primary (:primary pattern)
               :property (dispatch-on-pattern pattern)}]
    (vector id attrs)))

(defn get-edges
  [pgraph node-map]
  (let [edges (uber/edges tgraph)]
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

;; MUST NOT include optional or zero-or-more directly inside alternates
(defmethod valid-edge? :alternates
  [{:keys src-type dest-type dest-property}]
  (and (#{"Pattern"} src-type)
       (or (and (#{"Pattern"} dest-type)
                (not (#{:optional :zero-or-more} dest-property)))
           (#{"StatementTemplate"} dest-type))))

;; MUST include at least two members in sequence, unless:
;; 1. sequence is a primary pattern not used elsewhere
;; 2. sequence member is a single StatementTemplate
(defmethod valid-edge? :sequence
  [{:keys src-type dest-type src-indegree src-outdegree}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)
       (or (<= 2 src-outdegree)
           (and (#{"StatementTemplate"} dest-type)
                (true? src-primary)
                (= 0 src-indegree)))))

(defmethod valid-edge? :optional
  [{:keys src-type dest-type}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :one-or-more
  [{:keys src-type dest-type}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :zero-or-more
  [{:keys src-type dest-type}]
  (and (#{"Pattern"} src-type)
       (#{"Pattern" "StatementTemplate"} dest-type)))

(defmethod valid-edge? :default [_] false)

(s/def ::pattern-edge valid-edge?)

;; MUST NOT include any Pattern within itself, at any depth
(s/def ::acyclic-graph alg/dag?)

(defn pattern-graph
  (fn [pgraph]
    (let [edges (get-edges pgraph)]
      (s/valid? (s/coll-of ::valid-edge edges)))))

(s/def ::pattern-edges
  (s/coll-of ::pattern-edge))

(s/def ::pattern-graph
  (fn [pgraph] (and (s/valid? ::pattern-edges (pattern/get-edges pgraph))
                    (s/valid? ::acyclic-graph pgraph))))

(defn explain-pattern-graph [pgraph]
  (concat (s/explain-data ::pattern-edges (pattern/get-edges pgraph))
          (s/explain-data ::acyclic-graph pgraph)))
