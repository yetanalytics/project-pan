(ns com.yetanalytics.objects-test.concept-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concept :as concept]))

(deftest valid-relation-test
  (testing "Concepts MUST be of the same type from this Profile version."
    (should-satisfy+
     ::concept/valid-edge
     {:src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     {:src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     {:src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     :bad
     {:src-type "Activity" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src-type "AttachmentUsageType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v2"
      :type :broader}
     ;; TODO Let broadMatch be a valid relation
     {:src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v2"
      :type :broad-match})))

(deftest valid-extension-test
  (testing "Extensions MUST point to appropriate recommended concepts."
    (should-satisfy+
     ::concept/valid-edge
     {:src-type "ActivityExtension" :dest-type "ActivityType"
      :type :recommended-activity-types}
     {:src-type "ContextExtension" :dest-type "Verb"
      :type :recommended-verbs}
     {:src-type "ResultExtension" :dest-type "Verb"
      :type :recommended-verbs}
     :bad
     {:src-type "ActivityExtension" :dest-type "Activity"
      :type :recommended-activity-types}
     {:src-type "ActivityExtension" :dest-type "ActivityType"
      :type :recommended-verbs}
     {:src-type "ActivityExtension" :dest-type "Verb"
      :type :recommended-verbs})))

;; TODO Add graph integration tests
