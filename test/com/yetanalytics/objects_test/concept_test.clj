(ns com.yetanalytics.objects-test.concept-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.util :as util]
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

(def ex-concepts
  [{:id "https://foo.org/verb1"
    :type "Verb"
    :in-scheme "https://foo.org/v1"
    :broader ["https://foo.org/verb2"]}
   {:id "https://foo.org/verb2"
    :type "Verb"
    :in-scheme "https://foo.org/v1"
    :narrower ["https://foo.org/verb1"]}
   {:id "https://foo.org/at1"
    :type "ActivityType"
    :in-scheme "https://foo.org/v1"
    :broader ["https://foo.org/at2"]}
   {:id "https://foo.org/at2"
    :type "ActivityType"
    :in-scheme "https://foo.org/v1"
    :narrower ["https://foo.org/at1"]}
   {:id "https://foo.org/aut1"
    :type "AttachmentUsageType"
    :in-scheme "https://foo.org/v1"
    :broader ["https://foo.org/aut2"]}
   {:id "https://foo.org/aut2"
    :type "AttachmentUsageType"
    :in-scheme "https://foo.org/v1"
    :narrower ["https://foo.org/aut1"]}])

(def cgraph (let [graph (uber/digraph)
                  nodes (mapv (partial util/node-with-attrs) ex-concepts)
                  edges (reduce concat (mapv (partial util/edges-with-attrs) ex-concepts))]
              (-> graph
                  (uber/add-nodes-with-attrs* nodes)
                  (uber/add-directed-edges* edges))))

(deftest graph-test
  (testing "Graph properties"
    (is (= 6 (count (uber/nodes cgraph))))
    (is (= 6 (count (uber/edges cgraph))))
    (is (= (set (uber/nodes cgraph))
           #{"https://foo.org/verb1" "https://foo.org/verb2"
             "https://foo.org/at1" "https://foo.org/at2"
             "https://foo.org/aut1" "https://foo.org/aut2"}))
    (is (= (set (concept/get-edges cgraph))
           #{{:src "https://foo.org/verb1" :src-type "Verb" :src-version "https://foo.org/v1"
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
              :type :narrower}}))
    (should-satisfy ::concept/concept-graph cgraph)))
