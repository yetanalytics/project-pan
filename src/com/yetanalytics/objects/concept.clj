(ns com.yetanalytics.objects.concept
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.objects.concepts.verbs :as v]
            [com.yetanalytics.objects.concepts.activities :as a]
            [com.yetanalytics.objects.concepts.activity-types :as at]
            [com.yetanalytics.objects.concepts.extensions.result :as re]
            [com.yetanalytics.objects.concepts.extensions.context :as ce]
            [com.yetanalytics.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.objects.concepts.attachment-usage-types :as a-ut]
            [com.yetanalytics.objects.concepts.document-resources.state :as state]
            [com.yetanalytics.objects.concepts.document-resources.agent-profile :as agent-p]
            [com.yetanalytics.objects.concepts.document-resources.activity-profile :as activity-p]))

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

(s/def ::concepts (s/coll-of ::concept :kind vector?))

(defn get-edges
  [cgraph]
  (let [edges (uber/edges cgraph)]
    (mapv (fn [edge]
            (let [src (uber/src edge) dest (uber/dest edge)]
              {:src src
               :src-type (uber/attr cgraph src :type)
               :dest dest
               :dest-type (uber/attr cgraph dest :type)
               :type (uber/attr pgraph edge :type)}))
          edges)))

(defmulti valid-edge? #(:type %))

;; Verbs, ActivityTypes, and AttachmentUsageTypes
;; TODO broadMatch, narrowMatch, relatedMatch and exactMatch

(defmethod valid-edge? :broader
  [{:keys src-type dest-type src-version dest-version}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

(defmethod valid-edge? :narrower
  [{:keys src-type dest-type src-version dest-version}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

(defmethod valid-edge? :related
  [{:keys src-type dest-type src-version dest-version}]
  (and (#{"ActivityType" "AttachmentUsageType" "Verb"} src-type)
       (#{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)
       (= src-type dest-type)
       (= src-version dest-version)))

;; Extensions
(defmethod valid-edge? :recommended-activity-types
  [{:keys src-type dest-type}]
  (and (#{"ActvityExtension"} src-type)
       (#{"ActivityType"} dest-type)))

(defmethod valid-edge? :recommended-verbs
  [{:keys src-type dest-type}]
  (and (#{"ContextExtension" "ResultExtension"} src-type)
       (#{"Verb"} dest-type)))

(defmethod valid-edge? :default [_] false)

(s/def ::valid-edge valid-edge?)

(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::concept-graph
  (fn [cgraph]
    (let [edges (get-edges cgraph)]
      (s/valid? (s/coll-of ::valid-edge edges)))))

(s/def ::concept-graph
  (fn [cgraph] (s/valid? ::valid-edges (get-edges cgraph))))

(defn explain-concept-graph [cgraph]
  (s/explain-data ::valid-edges (get-edges cgraph)))
