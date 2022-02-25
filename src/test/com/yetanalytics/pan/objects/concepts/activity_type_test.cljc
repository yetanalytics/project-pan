(ns com.yetanalytics.pan.objects.concepts.activity-type-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concepts.activity-type :as at]
            [com.yetanalytics.test-utils :refer [should-satisfy
                                                 should-satisfy+
                                                 should-not-satisfy]]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::at/type
                     "ActivityType"
                     :bad
                     "Verb"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::at/broader
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::at/broader
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::at/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::at/broadMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::at/broadMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::at/broadMatch [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::at/narrower
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::at/narrower
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::at/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::at/narrowMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::at/narrowMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::at/narrowMatch [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::at/related
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::at/related
                        "https://w3id.org/xapi/catch/activity-types/provided")
    (should-not-satisfy ::at/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::at/relatedMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::at/relatedMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::at/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::at/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::at/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::at/exactMatch [])))

(deftest activity-type-test
  (testing "activityType concept"
    (is (s/valid? ::at/activity-type
                  {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                   :type "ActivityType"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Check in"}
                   :definition {"en" "An activity in which the learner reports progression."}}))))

(def ex-concept {:id "https://foo.org/at"
                 :type "ActivityType"
                 :inScheme "https://foo.org/v1"
                 :broader ["https://foo.org/at2"]
                 :broadMatch ["https://foo.org/at3"]
                 :narrower ["https://foo.org/at4"]
                 :narrowMatch ["https://foo.org/at5"]
                 :related ["https://foo.org/at6"]
                 :relatedMatch ["https://foo.org/at7"]
                 :exactMatch ["https://foo.org/at8"]})

(deftest edges-with-attrs-test
  (testing "create edges from node"
    (is (= [["https://foo.org/at" "https://foo.org/at2" {:type :broader}]
            ["https://foo.org/at" "https://foo.org/at3" {:type :broadMatch}]
            ["https://foo.org/at" "https://foo.org/at4" {:type :narrower}]
            ["https://foo.org/at" "https://foo.org/at5" {:type :narrowMatch}]
            ["https://foo.org/at" "https://foo.org/at6" {:type :related}]
            ["https://foo.org/at" "https://foo.org/at7" {:type :relatedMatch}]
            ["https://foo.org/at" "https://foo.org/at8" {:type :exactMatch}]]
           (graph/edges-with-attrs ex-concept)))))
