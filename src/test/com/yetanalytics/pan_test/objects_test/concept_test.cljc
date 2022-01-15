(ns com.yetanalytics.pan-test.objects-test.concept-test
  (:require [clojure.test :refer [deftest is testing]]
            [loom.attr]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils :refer [should-satisfy+]]
            [com.yetanalytics.pan.objects.concept :as concept]))

;; TODO Add test for testing a complete vector of concepts

(def at-1->at-2-fix
  {:src          "https://foo.org/at1"
   :dest         "https://foo.org/at2"
   :src-type     "ActivityType"
   :dest-type    "ActivityType"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"})

(def aut-1->aut-2-fix
  {:src          "https://foo.org/aut1"
   :dest         "https://foo.org/aut2"
   :src-type     "AttachmentUsageType"
   :dest-type    "AttachmentUsageType"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"})

(def verb-1->verb-2-fix
  {:src          "https://foo.org/verb1"
   :dest         "https://foo.org/verb2"
   :src-type     "Verb"
   :dest-type    "Verb"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"})

(def at-1->at-2-fix-2
  (assoc at-1->at-2-fix :dest-version "https://foo.org/v2"))

(def aut-1->aut-2-fix-2
  (assoc aut-1->aut-2-fix :dest-version "https://foo.org/v2"))

(def verb-1->verb-2-fix-2
  (assoc verb-1->verb-2-fix :dest-version "https://foo.org/v2"))

(deftest valid-relation-test
  (testing "Concepts MUST be of the same type from this Profile version."
    (should-satisfy+
     ::concept/concept-edge
     (assoc at-1->at-2-fix :type :broader)
     (assoc at-1->at-2-fix :type :narrower)
     (assoc at-1->at-2-fix :type :related)
     (assoc aut-1->aut-2-fix :type :broader)
     (assoc aut-1->aut-2-fix :type :narrower)
     (assoc aut-1->aut-2-fix :type :related)
     (assoc verb-1->verb-2-fix :type :broader)
     (assoc verb-1->verb-2-fix :type :narrower)
     (assoc verb-1->verb-2-fix :type :related)
     :bad
     (assoc at-1->at-2-fix
            :type :broader
            :src "https://foo.org/act"
            :src-type "Activity")
     (assoc aut-1->aut-2-fix
            :type :broader
            :dest "https://foo.org/at"
            :dest-type "ActivityType")
     (assoc at-1->at-2-fix-2
            :type :broader)))
  (testing "Concepts MUST be of the same type from a different Profile version"
    (should-satisfy+
     ::concept/concept-edge
     (assoc at-1->at-2-fix-2 :type :broadMatch)
     (assoc at-1->at-2-fix-2 :type :narrowMatch)
     (assoc at-1->at-2-fix-2 :type :relatedMatch)
     (assoc at-1->at-2-fix-2 :type :exactMatch)
     (assoc aut-1->aut-2-fix-2 :type :broadMatch)
     (assoc aut-1->aut-2-fix-2 :type :narrowMatch)
     (assoc aut-1->aut-2-fix-2 :type :relatedMatch)
     (assoc aut-1->aut-2-fix-2 :type :exactMatch)
     (assoc verb-1->verb-2-fix-2 :type :broadMatch)
     (assoc verb-1->verb-2-fix-2 :type :narrowMatch)
     (assoc verb-1->verb-2-fix-2 :type :relatedMatch)
     (assoc verb-1->verb-2-fix-2 :type :exactMatch)
     :bad
     (assoc at-1->at-2-fix-2
            :type :broadMatch
            :src "http://foo.org/act"
            :src-type "Activity")
     (assoc at-1->at-2-fix
            :type :broadMatch))))

(def act-ext-fix
  {:src          "https://foo.org/ae"
   :dest         "https://foo.org/at"
   :src-type     "ActivityExtension"
   :dest-type    "ActivityType"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"
   :type         :recommendedActivityTypes})

(def ctx-ext-fix
  {:src          "https://foo.org/ce"
   :dest         "https://foo.org/verb"
   :src-type     "ContextExtension"
   :dest-type    "Verb"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"
   :type         :recommendedVerbs})

(def res-ext-fix
  {:src          "https://foo.org/re"
   :dest         "https://foo.org/verb"
   :src-type     "ResultExtension"
   :dest-type    "Verb"
   :src-version  "https://foo.org/v1"
   :dest-version "https://foo.org/v1"
   :type         :recommendedVerbs})

(deftest valid-extension-test
  (testing "Extensions MUST point to appropriate recommended concepts."
    (should-satisfy+
     ::concept/concept-edge
     act-ext-fix
     ctx-ext-fix
     res-ext-fix
     :bad
     (assoc act-ext-fix :dest-type "Activity")
     (assoc act-ext-fix :type :recommendedVerbs)
     (assoc act-ext-fix :dest-type "Verb" :type :recommendedVerbs))))

(def cprof
  {:concepts [{:id       "https://foo.org/verb1"
               :type     "Verb"
               :inScheme "https://foo.org/v1"
               :broader  ["https://foo.org/verb2"]}
              {:id       "https://foo.org/verb2"
               :type     "Verb"
               :inScheme "https://foo.org/v1"
               :narrower ["https://foo.org/verb1"]}
              {:id       "https://foo.org/at1"
               :type     "ActivityType"
               :inScheme "https://foo.org/v1"
               :broader  ["https://foo.org/at2"]}
              {:id       "https://foo.org/at2"
               :type     "ActivityType"
               :inScheme "https://foo.org/v1"
               :narrower ["https://foo.org/at1"]}
              {:id       "https://foo.org/aut1"
               :type     "AttachmentUsageType"
               :inScheme "https://foo.org/v1"
               :broader  ["https://foo.org/aut2"]}
              {:id       "https://foo.org/aut2"
               :type     "AttachmentUsageType"
               :inScheme "https://foo.org/v1"
               :narrower ["https://foo.org/aut1"]}
              {:id          "https://foo.org/aut3"
               :type        "AttachmentUsageType"
               :inScheme    "https://foo.org/v1"
               :narrowMatch ["https://bar.org/ext-aut"]}]})

(def cgraph
  (concept/create-graph cprof
                        [{:concepts [{:id       "https://bar.org/ext-aut"
                                      :type     "AttachmentUsageType"
                                      :inScheme "https://bar.org/v1"}]}]))

(deftest graph-test
  (testing "Graph properties"
    (is (= 8 (count (graph/nodes cgraph))))
    (is (= 7 (count (graph/edges cgraph))))
    (is (= #{"https://foo.org/verb1"
             "https://foo.org/verb2"
             "https://foo.org/at1"
             "https://foo.org/at2"
             "https://foo.org/aut1"
             "https://foo.org/aut2"
             "https://foo.org/aut3"
             "https://bar.org/ext-aut"}
           (set (graph/nodes cgraph))))
    (is (= #{{:src          "https://foo.org/verb1"
              :src-type     "Verb"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/verb2"
              :dest-type    "Verb"
              :dest-version "https://foo.org/v1"
              :type         :broader}
             {:src          "https://foo.org/verb2"
              :src-type     "Verb"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/verb1"
              :dest-type    "Verb"
              :dest-version "https://foo.org/v1"
              :type         :narrower}
             {:src          "https://foo.org/at1"
              :src-type     "ActivityType"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/at2"
              :dest-type    "ActivityType"
              :dest-version "https://foo.org/v1"
              :type         :broader}
             {:src          "https://foo.org/at2"
              :src-type     "ActivityType"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/at1"
              :dest-type    "ActivityType"
              :dest-version "https://foo.org/v1"
              :type         :narrower}
             {:src          "https://foo.org/aut1"
              :src-type     "AttachmentUsageType"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/aut2"
              :dest-type    "AttachmentUsageType"
              :dest-version "https://foo.org/v1"
              :type         :broader}
             {:src          "https://foo.org/aut2"
              :src-type     "AttachmentUsageType"
              :src-version  "https://foo.org/v1"
              :dest         "https://foo.org/aut1"
              :dest-type    "AttachmentUsageType"
              :dest-version "https://foo.org/v1"
              :type         :narrower}
             {:src          "https://foo.org/aut3"
              :src-type     "AttachmentUsageType"
              :src-version  "https://foo.org/v1"
              :dest         "https://bar.org/ext-aut"
              :dest-type    "AttachmentUsageType"
              :dest-version "https://bar.org/v1"
              :type         :narrowMatch}}
           (set (concept/get-edges cgraph))))
    (is (nil? (concept/validate-concept-edges cgraph)))))
