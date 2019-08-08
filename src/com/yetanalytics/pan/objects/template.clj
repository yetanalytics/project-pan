(ns com.yetanalytics.pan.objects.template
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.util :as util]
            [com.yetanalytics.pan.objects.templates.rules :as rules]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/uri)
(s/def ::type #{"StatementTemplate"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::verb ::ax/iri)
(s/def ::objectActivityType ::ax/iri)
(s/def ::contextGroupingActivityType
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::contextParentActivityType
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::contextOtherActivityType
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::contextCategoryActivityType
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::attachmentUsageType
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::objectStatementRefTemplate
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::contextStatementRefTemplate
  (s/coll-of ::ax/iri :type vector? :min-count 1))

;; A StatementTemplate MUST NOT have both objectStatementRefTemplate and
;; objectActivityType at the same time.
(s/def ::type-or-reference
  (fn [st]
    (let [otype? (contains? st :objectActivityType)
          otemp? (contains? st :objectStatementRefTemplate)]
      (not (and otype? otemp?)))))

(s/def ::rules (s/coll-of ::rules/rule :type vector?))

(s/def ::template
  (s/and
   (s/keys :req-un [::id ::type ::inScheme ::prefLabel ::definition]
           :opt-un [::deprecated ::rules
                    ::verb
                    ::objectActivityType
                    ::contextGroupingActivityType
                    ::contextParentActivityType
                    ::contextOtherActivityType
                    ::contextCategoryActivityType
                    ::attachmentUsageType
                    ::objectStatementRefTemplate
                    ::contextStatementRefTemplate])
   ::type-or-reference))

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Graph creation functions

;; From a single StatementTemplate, return a 1D vector of edge vectors of 
;; form [src dest {:type type-kword}]
(defmethod graph/edges-with-attrs "StatementTemplate"
  [{:keys [id
           verb
           objectActivityType
           contextGroupingActivityType
           contextParentActivityType
           contextOtherActivityType
           contextCategoryActivityType
           attachmentUsageType
           objectStatementRefTemplate
           contextStatementRefTemplate]}]
  (into [] (filter #(some? (second %))
                   (concat
                    (vector (vector id verb {:type :verb}))
                    (vector (vector id objectActivityType
                                    {:type :objectActivityType}))
                    (map #(vector id % {:type :contextGroupingActivityType})
                         contextGroupingActivityType)
                    (map #(vector id % {:type :contextParentActivityType})
                         contextParentActivityType)
                    (map #(vector id % {:type :contextOtherActivityType})
                         contextOtherActivityType)
                    (map #(vector id % {:type :contextCategoryActivityType})
                         contextCategoryActivityType)
                    (map #(vector id % {:type :attachmentUsageType})
                         attachmentUsageType)
                    (map #(vector id % {:type :objectStatementRefTemplate})
                         objectStatementRefTemplate)
                    (map #(vector id % {:type :contextStatementRefTemplate})
                         contextStatementRefTemplate)))))

(defn create-graph
  "Create a template graph from its constituent concepts and templates.
  Returns a digraph with connections between templates to concepts (and each
  other)."
  [concepts templates]
  (let [tgraph (uber/digraph)
        ;; Nodes
        cnodes (mapv (partial graph/node-with-attrs) concepts)
        tnodes (mapv (partial graph/node-with-attrs) templates)
        ;; Edges
        tedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) templates))]
    (-> tgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-nodes-with-attrs* tnodes)
        (uber/add-directed-edges* tedges))))

(defn get-edges
  "Return a sequence of edge maps, with the following keys:
  - src: source node ID
  - dest: destination node ID
  - src-type: source node 'type' property
  - dest-type: destination node 'type' property
  - src-version: source node 'inScheme' property
  - dest-version: destination node 'inScheme property
  - type: corresponding property in the source node (ie. the Determining
  Property or Statement Ref Template type)"
  [tgraph]
  (let [edges (uber/edges tgraph)]
    (map (fn [edge]
           (let [src (uber/src edge) dest (uber/dest edge)]
             {:src src
              :src-type (uber/attr tgraph src :type)
              :src-version (uber/attr tgraph src :inScheme)
              :dest dest
              :dest-type (uber/attr tgraph dest :type)
              :dest-version (uber/attr tgraph dest :inScheme)
              :type (uber/attr tgraph edge :type)}))
         edges)))

;; Edge property specs

;; Is the source a Statement Template?
(s/def ::template-src
  (fn [{:keys [src-type]}]
    (contains? #{"StatementTemplate"} src-type)))

;; Is the destination not nil?
(s/def ::valid-dest
  (fn [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

;; Is the destination a Verb?
(s/def ::verb-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

;; Is the destination an Activity Type?
(s/def ::at-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

;; Is the destination an Attachment Usage Type?
(s/def ::aut-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"AttachmentUsageType"} dest-type)))

;; Is the destination another Statement Template?
(s/def ::template-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"StatementTemplate"} dest-type)))

;; Are both the source and destination in the same version?
(s/def ::same-version
  (fn [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Edge validation multimethod

(defmulti valid-edge? util/type-dispatch)

;; verb MUST point to a Verb Concept
(defmethod valid-edge? :verb [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::verb-dest))

;; objectActivityType MUST point to an object ActivityType
(defmethod valid-edge? :objectActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

;; contextGroupingActivityType MUST point to grouping ActivityTypes
(defmethod valid-edge? :contextGroupingActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

;; contextParentActivityType MUST point to parent ActivityTypes
(defmethod valid-edge? :contextParentActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

;; contextOtherActivityType MUST point to other ActivityTypes
(defmethod valid-edge? :contextOtherActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

;; contextCategoryActivityType MUST point to category ActivityTypes
(defmethod valid-edge? :contextCategoryActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

;; attachmentUsageType MUST point to AttachmentUsageType Concepts
(defmethod valid-edge? :attachmentUsageType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::aut-dest))

;; objectStatementRefTemplate MUST point to Statement Templates from this
;; profile version
(defmethod valid-edge? :contextStatementRefTemplate [_]
  (s/and ::template-src
         ::valid-dest
         ::template-dest
         ::same-version))

;; contextStatementRefTemplate MUST point to Statement Templates from this
;; profile version 
(defmethod valid-edge? :objectStatementRefTemplate [_]
  (s/and ::template-src
         ::valid-dest
         ::template-dest
         ::same-version))

;; Validate a single edge
(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))


;; Validate all the edges
(s/def ::valid-edges (s/coll-of ::valid-edge))

;; Putting it all together

(s/def ::template-graph
  (fn [tgraph] (s/valid? ::valid-edges (get-edges tgraph))))

(defn explain-graph [tgraph]
  (s/explain-data ::valid-edges (get-edges tgraph)))

;; TODO Validate links that are external to this Profile
