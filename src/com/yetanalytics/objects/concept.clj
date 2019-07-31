(ns com.yetanalytics.objects.concept
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.graph :as graph]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.objects.concepts.verbs :as v]
            [com.yetanalytics.objects.concepts.activities :as a]
            [com.yetanalytics.objects.concepts.activity-types :as at]
            [com.yetanalytics.objects.concepts.extensions.result :as re]
            [com.yetanalytics.objects.concepts.extensions.context :as ce]
            [com.yetanalytics.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.objects.concepts.attachment-usage-types
             :as a-ut]
            [com.yetanalytics.objects.concepts.document-resources.state
             :as state]
            [com.yetanalytics.objects.concepts.document-resources.agent-profile
             :as agent-p]
            [com.yetanalytics.objects.concepts.document-resources.activity-profile
             :as activity-p]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concepts 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::concept
  (s/or :verb     ::v/verb
        :activity ::a/activity
        :activity-type ::at/activity-type
        :attachment-usage-type ::a-ut/attachment-usage-type
        :activity-extension ::ae/extension
        :context-extension ::ce/extension
        :result-extension  ::re/extension
        :activity-profile ::activity-p/document-resource
        :agent-profile    ::agent-p/document-resource
        :state            ::state/document-resource))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO make broadMatch, narrowMatch, relatedMatch and exactMatch work

;; Graph creation functions

(defn create-graph
  "Create a ubergraph digraph out of the vector of concepts"
  [concepts]
  (let [cgraph (uber/digraph)
        cnodes (mapv (partial graph/node-with-attrs) concepts)
        cedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) concepts))]
    (-> cgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-directed-edges* cedges))))

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
  (let [edges (uber/edges cgraph)]
    (map (fn [edge]
           (let [src (uber/src edge) dest (uber/dest edge)]
             {:src src
              :src-type (uber/attr cgraph src :type)
              :src-version (uber/attr cgraph src :inScheme)
              :dest dest
              :dest-type (uber/attr cgraph dest :type)
              :dest-version (uber/attr cgraph dest :inScheme)
              :type (uber/attr cgraph edge :type)}))
         edges)))

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
  (s/and ::graph/not-self-loop ::valid-dest
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

; ;; narrower MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :narrower [_]
  (s/and ::graph/not-self-loop ::valid-dest
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

; ;; related MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :related [_]
  (s/and ::graph/not-self-loop ::valid-dest
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

; ;; recommendedActivityTypes MUST point to ActivityType Concepts
(defmethod valid-edge? :recommendedActivityTypes [_]
  (s/and ::graph/not-self-loop ::valid-dest
         ::aext-src ::at-dest))

; ;; recommendedVerbs MUST point to Verb Concepts
(defmethod valid-edge? :recommendedVerbs [_]
  (s/and ::graph/not-self-loop ::valid-dest
         ::crext-src ::verb-dest))

;; Is one edge valid?
(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))

;; Are all edges valid?
(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::concept-graph
  (fn [cgraph] (s/valid? ::valid-edges (get-edges cgraph))))

(defn explain-graph [cgraph]
  (s/explain-data ::valid-edges (get-edges cgraph)))
