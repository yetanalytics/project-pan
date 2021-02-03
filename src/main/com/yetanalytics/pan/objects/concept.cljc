(ns com.yetanalytics.pan.objects.concept
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.utils.spec :as util]
            [com.yetanalytics.pan.objects.concepts.verbs :as v]
            [com.yetanalytics.pan.objects.concepts.activities :as a]
            [com.yetanalytics.pan.objects.concepts.activity-types :as at]
            [com.yetanalytics.pan.objects.concepts.extensions.result :as re]
            [com.yetanalytics.pan.objects.concepts.extensions.context :as ce]
            [com.yetanalytics.pan.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.pan.objects.concepts.attachment-usage-types
             :as a-ut]
            [com.yetanalytics.pan.objects.concepts.document-resources.state
             :as s-pr]
            [com.yetanalytics.pan.objects.concepts.document-resources.agent-profile
             :as ag-pr]
            [com.yetanalytics.pan.objects.concepts.document-resources.activity-profile
             :as act-pr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concepts 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti concept? util/type-dispatch)

(defmethod concept? "Verb" [_] ::v/verb)
(defmethod concept? "Activity" [_] ::a/activity)
(defmethod concept? "ActivityType" [_] ::at/activity-type)
(defmethod concept? "AttachmentUsageType" [_] ::a-ut/attachment-usage-type)
(defmethod concept? "ActivityExtension" [_] ::ae/extension)
(defmethod concept? "ContextExtension" [_] ::ce/extension)
(defmethod concept? "ResultExtension" [_] ::re/extension)
(defmethod concept? "ActivityProfileResource" [_] ::act-pr/document-resource)
(defmethod concept? "AgentProfileResource" [_] ::ag-pr/document-resource)
(defmethod concept? "StateResource" [_] ::s-pr/document-resource)

(s/def ::concept (s/multi-spec concept? #(:type %)))

(s/def ::concepts (s/coll-of ::concept :type vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO make broadMatch, narrowMatch, relatedMatch and exactMatch work

;; Graph creation functions

(defn create-graph
  "Create a digraph out of the vector of concepts"
  [concepts]
  (let [cgraph (graph/new-digraph)
        cnodes (mapv (partial graph/node-with-attrs) concepts)
        cedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) concepts))]
    (-> cgraph
        (graph/add-nodes cnodes)
        (graph/add-edges cedges))))

(defn get-edges
  "Returns a sequence of edge maps, with the following keys:
  - src: source node ID
  - dest: destination node ID
  - src-type: source node 'type' property
  - dest-type: destination node 'type' property
  - src-version: source node 'inScheme' property
  - dest-version: destination node 'inScheme property
  - type: the property corresponding to this edge (eg. broader, narrower,
  related, etc.)"
  [cgraph]
  (map (fn [edge]
         (let [src (graph/src edge)
               dest (graph/dest edge)]
           {:src src
            :src-type (graph/attr cgraph src :type)
            :src-version (graph/attr cgraph src :inScheme)
            :dest dest
            :dest-type (graph/attr cgraph dest :type)
            :dest-version (graph/attr cgraph dest :inScheme)
            :type (graph/attr cgraph edge :type)}))
       (graph/edges cgraph)))

;; Edge property specs

;; Is the destination not nil?
(s/def ::valid-dest
  (fn [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

;; Is the source an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-src
  (fn [{:keys [src-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} src-type)))

;; Is the destination an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)))

;; Is the source an Activity Extension?
(s/def ::aext-src
  (fn [{:keys [src-type]}]
    (contains? #{"ActivityExtension"} src-type)))

;; Is the source a Context or Result Extension?
(s/def ::crext-src
  (fn [{:keys [src-type]}]
    (contains? #{"ContextExtension" "ResultExtension"} src-type)))

;; Is the destination an Activity Type?
(s/def ::at-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

;; Is the destination a Verb?
(s/def ::verb-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

;; Are both the source and destination the same type of Concept?
(s/def ::same-concept
  (fn [{:keys [src-type dest-type]}]
    (= src-type dest-type)))

;; Are both the source and destination of the same version?
(s/def ::same-version
  (fn [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Edge validation multimethod 

(defmulti valid-edge? util/type-dispatch)

;; broader MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :broader [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

;; narrower MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :narrower [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

;; related MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :related [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

; ;; recommendedActivityTypes MUST point to ActivityType Concepts
(defmethod valid-edge? :recommendedActivityTypes [_]
  (s/and ::aext-src
         ::valid-dest
         ::graph/not-self-loop
         ::at-dest))

; ;; recommendedVerbs MUST point to Verb Concepts
(defmethod valid-edge? :recommendedVerbs [_]
  (s/and ::crext-src
         ::valid-dest
         ::graph/not-self-loop
         ::verb-dest))

;; Is one edge valid?
(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))

;; Are all edges valid?
(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::concept-graph
  (fn [cgraph] (s/valid? ::valid-edges (get-edges cgraph))))

(defn explain-graph [cgraph]
  (s/explain-data ::valid-edges (get-edges cgraph)))
