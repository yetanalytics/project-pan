(ns com.yetanalytics.pan-test.objects-test.concept-test
  (:require [clojure.test :refer [deftest is testing]]
            [loom.attr]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils :refer [should-satisfy+]]
            [com.yetanalytics.pan.objects.concept :as concept]))

;; TODO Add test for testing a complete vector of concepts

(deftest valid-relation-test
  (testing "Concepts MUST be of the same type from this Profile version."
    (should-satisfy+
     ::concept/valid-edge
     {:src "https://foo.org/at1" :dest "https://foo.org/at2"
      :src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src "https://foo.org/at1" :dest "https://foo.org/at2"
      :src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src "https://foo.org/at1" :dest "https://foo.org/at2"
      :src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     {:src "https://foo.org/aut1" :dest "https://foo.org/aut2"
      :src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src "https://foo.org/aut1" :dest "https://foo.org/aut2"
      :src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src "https://foo.org/aut1" :dest "https://foo.org/aut2"
      :src-type "AttachmentUsageType" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     {:src "https://foo.org/verb1" :dest "https://foo.org/verb2"
      :src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src "https://foo.org/verb1" :dest "https://foo.org/verb2"
      :src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :narrower}
     {:src "https://foo.org/verb1" :dest "https://foo.org/verb2"
      :src-type "Verb" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :related}
     :bad
     {:src "https://foo.org/act" :dest "https://foo.org/at"
      :src-type "Activity" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src "https://foo.org/aut" :dest "https://foo.org/at"
      :src-type "AttachmentUsageType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :broader}
     {:src "https://foo.org/at1" :dest "https://foo.org/at2"
      :src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v2"
      :type :broader}
     ;; TODO Let broadMatch be a valid relation
     {:src "https://foo.org/at1" :dest "https://foo.org/at2"
      :src-type "ActivityType" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v2"
      :type :broadMatch})))

(deftest valid-extension-test
  (testing "Extensions MUST point to appropriate recommended concepts."
    (should-satisfy+
     ::concept/valid-edge
     {:src "https://foo.org/ae" :dest "https://foo.org/at"
      :src-type "ActivityExtension" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedActivityTypes}
     {:src "https://foo.org/ce" :dest "https://foo.org/verb"
      :src-type "ContextExtension" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedVerbs}
     {:src "https://foo.org/re" :dest "https://foo.org/verb"
      :src-type "ResultExtension" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedVerbs}
     :bad
     {:src "https://foo.org/ae" :dest "https://foo.org/act"
      :src-type "ActivityExtension" :dest-type "Activity"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedActivityTypes}
     {:src "https://foo.org/ae" :dest "https://foo.org/at"
      :src-type "ActivityExtension" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedVerbs}
     {:src "https://foo.org/ae" :dest "https://foo.org/verb"
      :src-type "ActivityExtension" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"
      :type :recommendedVerbs})))

;; TODO Add graph integration tests

(def ex-concepts
  [{:id "https://foo.org/verb1"
    :type "Verb"
    :inScheme "https://foo.org/v1"
    :broader ["https://foo.org/verb2"]}
   {:id "https://foo.org/verb2"
    :type "Verb"
    :inScheme "https://foo.org/v1"
    :narrower ["https://foo.org/verb1"]}
   {:id "https://foo.org/at1"
    :type "ActivityType"
    :inScheme "https://foo.org/v1"
    :broader ["https://foo.org/at2"]}
   {:id "https://foo.org/at2"
    :type "ActivityType"
    :inScheme "https://foo.org/v1"
    :narrower ["https://foo.org/at1"]}
   {:id "https://foo.org/aut1"
    :type "AttachmentUsageType"
    :inScheme "https://foo.org/v1"
    :broader ["https://foo.org/aut2"]}
   {:id "https://foo.org/aut2"
    :type "AttachmentUsageType"
    :inScheme "https://foo.org/v1"
    :narrower ["https://foo.org/aut1"]}])

(def cgraph (concept/create-graph ex-concepts))

(deftest graph-test
  (testing "Graph properties"
    (is (= 6 (count (graph/nodes cgraph))))
    (is (= 6 (count (graph/edges cgraph))))
    (is (= #{"https://foo.org/verb1" "https://foo.org/verb2"
             "https://foo.org/at1" "https://foo.org/at2"
             "https://foo.org/aut1" "https://foo.org/aut2"}
           (set (graph/nodes cgraph))))
    (is (= #{{:src "https://foo.org/verb1" :src-type "Verb" :src-version "https://foo.org/v1"
              :dest "https://foo.org/verb2" :dest-type "Verb" :dest-version "https://foo.org/v1"
              :type :broader}
             {:src "https://foo.org/verb2" :src-type "Verb" :src-version "https://foo.org/v1"
              :dest "https://foo.org/verb1" :dest-type "Verb" :dest-version "https://foo.org/v1"
              :type :narrower}
             {:src "https://foo.org/at1" :src-type "ActivityType" :src-version "https://foo.org/v1"
              :dest "https://foo.org/at2" :dest-type "ActivityType" :dest-version "https://foo.org/v1"
              :type :broader}
             {:src "https://foo.org/at2" :src-type "ActivityType" :src-version "https://foo.org/v1"
              :dest "https://foo.org/at1" :dest-type "ActivityType" :dest-version "https://foo.org/v1"
              :type :narrower}
             {:src "https://foo.org/aut1" :src-type "AttachmentUsageType" :src-version "https://foo.org/v1"
              :dest "https://foo.org/aut2" :dest-type "AttachmentUsageType" :dest-version "https://foo.org/v1"
              :type :broader}
             {:src "https://foo.org/aut2" :src-type "AttachmentUsageType" :src-version "https://foo.org/v1"
              :dest "https://foo.org/aut1" :dest-type "AttachmentUsageType" :dest-version "https://foo.org/v1"
              :type :narrower}}
           (set (concept/get-edges cgraph))))
    (is (nil? (concept/explain-graph cgraph)))))
