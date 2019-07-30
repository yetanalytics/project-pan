(ns com.yetanalytics.objects.concept
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
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
             :as s-pr]
            [com.yetanalytics.objects.concepts.document-resources.agent-profile
             :as ag-pr]
            [com.yetanalytics.objects.concepts.document-resources.activity-profile
             :as act-pr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concepts 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (s/def ::concept
;   (s/or :verb     ::v/verb
;         :activity ::a/activity
;         :activity-type ::at/activity-type
;         :attachment-usage-type ::a-ut/attachment-usage-type
;         :activity-extension ::ae/extension
;         :context-extension ::ce/extension
;         :result-extension  ::re/extension
;         :activity-profile ::activity-p/document-resource
;         :agent-profile    ::agent-p/document-resource
;         :state            ::state/document-resource))

(defmulti concept? #(:type %))

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

;; Graph creation functions

;; Create a ubergraph digraph out of the vector of concepts
(defn create-graph [concepts]
  (let [cgraph (uber/digraph)
        cnodes (mapv (partial util/node-with-attrs) concepts)
        cedges (util/collect-edges
                (mapv (partial util/edges-with-attrs) concepts))]
    (-> cgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-directed-edges* cedges))))

;; Returns a vector of edge maps, with src, dest and attribute keys
(defn get-edges
  [cgraph]
  (let [edges (uber/edges cgraph)]
    (mapv (fn [edge]
            (let [src (uber/src edge) dest (uber/dest edge)]
              {:src src
               :src-type (uber/attr cgraph src :type)
               :src-version (uber/attr cgraph src :inScheme)
               :dest dest
               :dest-type (uber/attr cgraph dest :type)
               :dest-version (uber/attr cgraph dest :inScheme)
               :type (uber/attr cgraph edge :type)}))
          edges)))

;; Edge validation 

(defmulti valid-edge? #(:type %))

;; Verbs, ActivityTypes, and AttachmentUsageTypes
;; TODO broadMatch, narrowMatch, relatedMatch and exactMatch

;; broader MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :broader
  [{:keys [src-type dest-type src-version dest-version]}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

;; narrower MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :narrower
  [{:keys [src-type dest-type src-version dest-version]}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

;; related MUST point to same-type Concepts from the same profile version
(defmethod valid-edge? :related
  [{:keys [src-type dest-type src-version dest-version]}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

;; Extensions

;; recommendedActivityTypes MUST point to ActivityType Concepts
(defmethod valid-edge? :recommendedActivityTypes
  [{:keys [src-type dest-type]}]
  (and (#{"ActivityExtension"} src-type)
       (#{"ActivityType"} dest-type)))

;; recommendedVerbs MUST point to Verb Concepts
(defmethod valid-edge? :recommendedVerbs
  [{:keys [src-type dest-type]}]
  (and (#{"ContextExtension" "ResultExtension"} src-type)
       (#{"Verb"} dest-type)))

(defmethod valid-edge? :default [_] false)

(s/def ::valid-edge valid-edge?)

(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::concept-graph
  (fn [cgraph] (s/valid? ::valid-edges (get-edges cgraph))))

(defn explain-graph [cgraph]
  (s/explain-data ::valid-edges (get-edges cgraph)))
