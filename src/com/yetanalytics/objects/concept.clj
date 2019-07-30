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

;; Create a ubergraph digraph out of the vector of concepts
(defn create-graph [concepts]
  (let [cgraph (uber/digraph)
        cnodes (mapv (partial graph/node-with-attrs) concepts)
        cedges (graph/collect-edges
                (mapv (partial graph/edges-with-attrs) concepts))]
    (-> cgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-directed-edges* cedges))))

;; Returns a sequence of edge maps, with src, dest and attribute keys
(defn get-edges
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

;; Spec to check that dest is not nil (i.e. the link itself is valid)
(s/def ::dest-exists
  (fn [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

(s/def ::relatable-src
  (fn [{:keys [src-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} src-type)))

(s/def ::relatable-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)))

(s/def ::aext-src
  (fn [{:keys [src-type]}]
    (contains? #{"ActivityExtension"} src-type)))

(s/def ::crext-src
  (fn [{:keys [src-type]}]
    (contains? #{"ContextExtension" "ResultExtension"} src-type)))

(s/def ::at-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

(s/def ::verb-dest
  (fn [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

(s/def ::same-concept
  (fn [{:keys [src-type dest-type]}]
    (= src-type dest-type)))

(s/def ::same-version
  (fn [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Edge validation 


(defmulti valid-edge? util/type-dispatch)

(defmethod valid-edge? :broader [_]
  (s/and ::graph/not-self-loop ::dest-exists
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

(defmethod valid-edge? :narrower [_]
  (s/and ::graph/not-self-loop ::dest-exists
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

(defmethod valid-edge? :related [_]
  (s/and ::graph/not-self-loop ::dest-exists
         ::relatable-src ::relatable-dest
         ::same-concept ::same-version))

(defmethod valid-edge? :recommendedActivityTypes [_]
  (s/and ::graph/not-self-loop ::dest-exists
         ::aext-src ::at-dest))

(defmethod valid-edge? :recommendedVerbs [_]
  (s/and ::graph/not-self-loop ::dest-exists
         ::crext-src ::verb-dest))

;; Verbs, ActivityTypes, and AttachmentUsageTypes
;; TODO broadMatch, narrowMatch, relatedMatch and exactMatch

;; broader MUST point to same-type Concepts from the same profile version


; (defmethod valid-edge? :broader
;   [{:keys [src-type dest-type src-version dest-version]}]
;   (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
;        (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
;        (= src-type dest-type)
;        (= src-version dest-version)))

; ;; narrower MUST point to same-type Concepts from the same profile version
; (defmethod valid-edge? :narrower
;   [{:keys [src-type dest-type src-version dest-version]}]
;   (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
;        (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
;        (= src-type dest-type)
;        (= src-version dest-version)))

; ;; related MUST point to same-type Concepts from the same profile version
; (defmethod valid-edge? :related
;   [{:keys [src-type dest-type src-version dest-version]}]
;   (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
;        (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
;        (= src-type dest-type)
;        (= src-version dest-version)))

;; Extensions

; ;; recommendedActivityTypes MUST point to ActivityType Concepts
; (defmethod valid-edge? :recommendedActivityTypes
;   [{:keys [src-type dest-type]}]
;   (and (#{"ActivityExtension"} src-type)
;        (#{"ActivityType"} dest-type)))

; ;; recommendedVerbs MUST point to Verb Concepts
; (defmethod valid-edge? :recommendedVerbs
;   [{:keys [src-type dest-type]}]
;   (and (#{"ContextExtension" "ResultExtension"} src-type)
;        (#{"Verb"} dest-type)))

(defmethod valid-edge? :default [_] false)

;(s/def ::valid-edge valid-edge?)
(s/def ::valid-edge (s/multi-spec valid-edge? util/type-dispatch))
(s/def ::valid-edges (s/coll-of ::valid-edge))

#_(s/def ::concept-graph
    (fn [cgraph] (s/valid? ::valid-edges (get-edges cgraph))))

(defn explain-graph [cgraph]
  (s/explain-data ::valid-edges (get-edges cgraph)))
