(ns com.yetanalytics.objects-test.concepts-test.activity-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.activity-types
             :as activity-types]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activity-types/type
                     "ActivityType"
                     :bad
                     "Verb"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::activity-types/broader
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/broader
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::activity-types/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::activity-types/broad-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/broad-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/broad-match [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::activity-types/narrower
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/narrower
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::activity-types/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::activity-types/narrow-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/narrow-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/narrow-match [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::activity-types/related
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/related
                        "https://w3id.org/xapi/catch/activity-types/provided")
    (should-not-satisfy ::activity-types/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::activity-types/related-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/related-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::activity-types/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::activity-types/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::activity-types/exact-match [])))

(deftest activity-type-test
  (testing "activityType concept"
    (is (s/valid? ::activity-types/activity-type
                  {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                   :type "ActivityType"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Check in"}
                   :definition {"en" "An activity in which the learner reports progression."}}))))

(deftest related-only-deprecated-test
  (testing "related MUST only be used on deprecated concepts"
    (is (s/valid? ::activity-types/related-only-deprecated
                  {:id "https://foo.org/activity-type"
                   :type "ActivityType"
                   :deprecated true
                   :related ["https://foo.org/other-activity-type"]}))
    (is (not (s/valid? ::activity-types/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :deprecated false
                        :related ["https://foo.org/other-activity-type"]})))
    (is (not (s/valid? ::activity-types/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :related ["https://foo.org/other-activity-type"]})))))

(def ex-concept {:id "https://foo.org/at"
                 :type "ActivityType"
                 :in-scheme "https://foo.org/v1"
                 :broader ["https://foo.org/at2"]
                 :broad-match ["https://foo.org/at3"]
                 :narrower ["https://foo.org/at4"]
                 :narrow-match ["https://foo.org/at5"]
                 :related ["https://foo.org/at6"]
                 :related-match ["https://foo.org/at7"]
                 :exact-match ["https://foo.org/at8"]})

(deftest edges-with-attrs-test
  (testing "create edges from node"
    (is (= (util/edges-with-attrs ex-concept)
           [["https://foo.org/at" "https://foo.org/at2" {:type :broader}]
            ["https://foo.org/at" "https://foo.org/at3" {:type :broad-match}]
            ["https://foo.org/at" "https://foo.org/at4" {:type :narrower}]
            ["https://foo.org/at" "https://foo.org/at5" {:type :narrow-match}]
            ["https://foo.org/at" "https://foo.org/at6" {:type :related}]
            ["https://foo.org/at" "https://foo.org/at7" {:type :related-match}]
            ["https://foo.org/at" "https://foo.org/at8" {:type :exact-match}]]))))
