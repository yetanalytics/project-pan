(ns com.yetanalytics.objects.template
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.graph :as graph]
            [com.yetanalytics.objects.templates.rules :as rules]))

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

;; Create a template graph from its constitutent concepts and templates
(defn create-graph [concepts templates]
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

;; Dissassociate a graph into its edges, in the form of attribute maps
(defn get-edges
  [tgraph]
  (let [edges (uber/edges tgraph)]
    (mapv (fn [edge]
            (let [src (uber/src edge) dest (uber/dest edge)]
              {:src src
               :src-type (uber/attr tgraph src :type)
               :src-version (uber/attr tgraph src :inScheme)
               :dest dest
               :dest-type (uber/attr tgraph dest :type)
               :dest-version (uber/attr tgraph dest :inScheme)
               :type (uber/attr tgraph edge :type)}))
          edges)))

;; Validate edges

(s/def ::template-src
  (fn [{:keys [src-type]}]
    (contains? #{"StatementTemplate"} src-type)))

(s/def ::valid-dest
  (fn [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

(s/def ::verb-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

(s/def ::at-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

(s/def ::aut-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"AttachmentUsageType"} dest-type)))

(s/def ::template-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"StatementTemplate"} dest-type)))

(s/def ::same-version
  (fn [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

(defmulti valid-edge? util/type-dispatch)

(defmethod valid-edge? :verb [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::verb-dest))

(defmethod valid-edge? :objectActivityType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::at-dest))

(defmethod valid-edge? :contextGroupingActivityType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::at-dest))

(defmethod valid-edge? :contextParentActivityType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::at-dest))

(defmethod valid-edge? :contextOtherActivityType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::at-dest))

(defmethod valid-edge? :contextCategoryActivityType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::at-dest))

(defmethod valid-edge? :attachmentUsageType [_]
  (s/and ::graph/not-self-loop
         ::template-src
         ::valid-dest
         ::aut-dest))

(defmethod valid-edge? :contextStatementRefTemplate [_]
  (s/and ::template-src
         ::valid-dest
         ::template-dest
         ::same-version))

(defmethod valid-edge :objectStatementRefTemplate [_]
  (s/and ::template/src
         ::valid-dest
         ::template-dest
         ::same-version))

;; verb MUST point to a Verb Concept


; (defmethod valid-edge? :verb [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"Verb"} dest-type)))

;; objectActivityType MUST point to an object ActivityType
; (defmethod valid-edge? :objectActivityType [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"ActivityType"} dest-type)))

;; contextGroupingActivityType MUST point to grouping ActivityTypes
; (defmethod valid-edge? :contextGroupingActivityType
;   [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"ActivityType"} dest-type)))

;; contextParentActivityType MUST point to parent ActivityTypes
; (defmethod valid-edge? :contextParentActivityType
;   [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"ActivityType"} dest-type)))

;; contextOtherActivityType MUST point to other ActivityTypes
; (defmethod valid-edge? :contextOtherActivityType
;   [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"ActivityType"} dest-type)))

;; contextCategoryActivityType MUST point to category ActivityTypes
; (defmethod valid-edge? :contextCategoryActivityType
;   [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"ActivityType"} dest-type)))

;; attachmentUsageType MUST point to AttachmentUsageType Concepts
; (defmethod valid-edge? :attachmentUsageType
;   [{:keys [src-type dest-type]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"AttachmentUsageType"} dest-type)))

;; objectStatementRefTemplate MUST point to Statement Templates from this
;; profile version
; (defmethod valid-edge? :objectStatementRefTemplate
;   [{:keys [src-type dest-type src-version dest-version]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"StatementTemplate"} dest-type)
;        (= src-version dest-version)))

;; contextStatementRefTemplate MUST point to Statement Templates from this
;; profile version


; (defmethod valid-edge? :contextStatementRefTemplate
;   [{:keys [src-type dest-type src-version dest-version]}]
;   (and (#{"StatementTemplate"} src-type)
;        (#{"StatementTemplate"} dest-type)
;        (= src-version dest-version)))

;; If the source object is a Statement Template, then it did not satisfy any
;; of the other properties so we return false.
(defmethod valid-edge? :default [_] false)

(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))

(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::template-graph
  (fn [tgraph] (s/valid? ::valid-edges (get-edges tgraph))))

(defn explain-graph [tgraph]
  (s/explain-data ::valid-edges (get-edges tgraph)))

;; TODO Fix stuff in the rules
