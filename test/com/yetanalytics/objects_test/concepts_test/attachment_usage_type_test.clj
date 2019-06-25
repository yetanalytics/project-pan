(ns com.yetanalytics.objects-test.concepts-test.attachment-usage-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.attachment-usage-types
             :as attachment-usage-types]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::attachment-usage-types/type
                     "AttachmentUsageType"
                     :bad
                     "ActivityType"
                     "Verb"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::attachment-usage-types/broader
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/broader
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::attachment-usage-types/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::attachment-usage-types/broad-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/broad-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/broad-match [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::attachment-usage-types/narrower
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/narrower
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::attachment-usage-types/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::attachment-usage-types/narrow-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/narrow-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/narrow-match [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::attachment-usage-types/related
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/related
                        "https://w3id.org/xapi/catch/attachment-usage-types/provided")
    (should-not-satisfy ::attachment-usage-types/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::attachment-usage-types/related-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/related-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::attachment-usage-types/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::attachment-usage-types/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::attachment-usage-types/exact-match [])))

(deftest attachment-usage-types-test
  (testing "attachmentUsageType concept"
    (is (s/valid? ::attachment-usage-types/attachment-usage-type
                  {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
                   :type "AttachmentUsageType"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Supporting documents"}
                   :definition {"en" "Documents which provide aditional 
                                     information about the lesson plan. Can be
                                     instructions for lesson plan execution
                                     demonstrating implementation or any other 
                                     documents related to the lesson plan"}}))))

(run-tests)
