(ns com.yetanalytics.pan.objects.concept
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
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

(defmulti concept? :type)

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
(defmethod concept? :default [_] (constantly false))

(s/def ::concept (s/multi-spec concept? :type))

(s/def ::concepts (s/coll-of ::concept :type vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO make broadMatch, narrowMatch, relatedMatch and exactMatch work

;; Graph creation functions

(def concept-ext-keys
  [:broader :broadMatch
   :narrower :narrowMatch
   :related :relatedMatch
   :exactMatch
   :recommendedActivityTypes
   :recommendedVerbs])

(defn- collect-concept
  [acc concept]
  (-> concept
      (select-keys concept-ext-keys)
      vals
      flatten
      (concat acc)))

(defn- get-graph-concepts
  [profile extra-profiles]
  (let [concepts     (:concepts profile)
        ext-ids      (set (reduce collect-concept [] concepts))
        ext-cons     (->> (mapcat :concepts extra-profiles)
                          (filter (fn [{id :id}] (contains? ext-ids id))))]
    {:concepts     concepts
     :ext-concepts ext-cons}))

(defn create-graph
  ([profile]
   (let [{:keys [concepts]} profile
         cnodes (->> concepts
                     (mapv graph/node-with-attrs))
         cedges (->> concepts
                     (mapv graph/edges-with-attrs)
                     graph/collect-edges)]
     (graph/create-graph cnodes cedges)))
  ([profile extra-profiles]
   (let [{:keys [concepts
                 ext-concepts]} (get-graph-concepts profile extra-profiles)
         cnodes (->> (concat concepts ext-concepts)
                     (mapv graph/node-with-attrs))
         cedges (->> concepts
                     (mapv graph/edges-with-attrs)
                     graph/collect-edges)]
     (graph/create-graph cnodes cedges))))

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
         (let [src  (graph/src edge)
               dest (graph/dest edge)]
           {:src          src
            :src-type     (graph/attr cgraph src :type)
            :src-version  (graph/attr cgraph src :inScheme)
            :dest         dest
            :dest-type    (graph/attr cgraph dest :type)
            :dest-version (graph/attr cgraph dest :inScheme)
            :type         (graph/attr cgraph edge :type)}))
       (graph/edges cgraph)))

;; Edge property specs

;; Is the destination not nil?
(s/def ::valid-dest
  (fn valid-dest? [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

;; Is the source an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-src
  (fn relatable-src? [{:keys [src-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} src-type)))

;; Is the destination an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-dest
  (fn relatable-dest? [{:keys [dest-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)))

;; Is the source an Activity Extension?
(s/def ::activity-ext-src
  (fn aext-src? [{:keys [src-type]}]
    (contains? #{"ActivityExtension"} src-type)))

;; Is the source a Context or Result Extension?
(s/def ::ctxt-result-ext-src
  (fn crext-src? [{:keys [src-type]}]
    (contains? #{"ContextExtension" "ResultExtension"} src-type)))

;; Is the destination an Activity Type?
(s/def ::activity-type-dest
  (fn at-dest? [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

;; Is the destination a Verb?
(s/def ::verb-dest
  (fn verb-dest? [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

;; Are both the source and destination the same type of Concept?
(s/def ::same-concept
  (fn same-concept? [{:keys [src-type dest-type]}]
    (= src-type dest-type)))

;; Are both the source and destination of the same version?
(s/def ::same-version
  (fn same-version? [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Are both the source and destination from different version/Profile?
(s/def ::diff-version
  (fn diff-version? [{:keys [src-version dest-version]}]
    (not= src-version dest-version)))

;; Edge validation multimethod 

(defmulti valid-edge? :type)

;; broader, narrower, and related  MUST point to same-type Concepts from the
;; same Profile version

(defmethod valid-edge? :broader [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

(defmethod valid-edge? :narrower [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

(defmethod valid-edge? :related [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

;; broadMatch, narrowMatch, relatedMatch, and exactMatch MUST point to same-type
;; Concepts from a different Profile

;; TODO: Currently never used due to lack of external Profiles

(defmethod valid-edge? :broadMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :narrowMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :relatedMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :exactMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

;; recommendedActivityTypes MUST point to ActivityType Concepts
(defmethod valid-edge? :recommendedActivityTypes [_]
  (s/and ::activity-ext-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; recommendedVerbs MUST point to Verb Concepts
(defmethod valid-edge? :recommendedVerbs [_]
  (s/and ::ctxt-result-ext-src
         ::valid-dest
         ::graph/not-self-loop
         ::verb-dest))

;; Is one edge valid?
(s/def ::concept-edge (s/multi-spec valid-edge? :type))

;; Are all edges valid?
(s/def ::concept-edges (s/coll-of ::concept-edge))

(defn validate-graph-edges [cgraph]
  (s/explain-data ::concept-edges (get-edges cgraph)))
