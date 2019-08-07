(ns com.yetanalytics.objects-test.concepts-test.activity-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.graph :as graph]
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
    (should-satisfy ::activity-types/broadMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/broadMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/broadMatch [])))

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
    (should-satisfy ::activity-types/narrowMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/narrowMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/narrowMatch [])))

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
    (should-satisfy ::activity-types/relatedMatch
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/relatedMatch
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::activity-types/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::activity-types/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::activity-types/exactMatch [])))

(deftest activity-type-test
  (testing "activityType concept"
    (is (s/valid? ::activity-types/activity-type
                  {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                   :type "ActivityType"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Check in"}
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
    (is (= (graph/edges-with-attrs ex-concept)
           [["https://foo.org/at" "https://foo.org/at2" {:type :broader}]
            ["https://foo.org/at" "https://foo.org/at3" {:type :broadMatch}]
            ["https://foo.org/at" "https://foo.org/at4" {:type :narrower}]
            ["https://foo.org/at" "https://foo.org/at5" {:type :narrowMatch}]
            ["https://foo.org/at" "https://foo.org/at6" {:type :related}]
            ["https://foo.org/at" "https://foo.org/at7" {:type :relatedMatch}]
            ["https://foo.org/at" "https://foo.org/at8" {:type :exactMatch}]]))))
