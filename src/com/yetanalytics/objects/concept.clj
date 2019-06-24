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

