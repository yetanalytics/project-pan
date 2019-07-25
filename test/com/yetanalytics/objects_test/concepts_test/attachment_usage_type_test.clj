(ns com.yetanalytics.objects-test.concepts-test.attachment-usage-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
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
    (should-satisfy ::attachment-usage-types/broadMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/broadMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/broadMatch [])))

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
    (should-satisfy ::attachment-usage-types/narrowMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/narrowMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/narrowMatch [])))

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
    (should-satisfy ::attachment-usage-types/relatedMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/relatedMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::attachment-usage-types/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::attachment-usage-types/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::attachment-usage-types/exactMatch [])))

(deftest related-only-deprecated-test
  (testing "related MUST only be used on deprecated concepts"
    (is (s/valid? ::attachment-usage-types/related-only-deprecated
                  {:id "https://foo.org/attachment-usage-type"
                   :type "AttachmentUsageType"
                   :deprecated true
                   :related ["https://foo.org/other-attachment-usage-type"]}))
    (is (not (s/valid? ::attachment-usage-types/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :deprecated false
                        :related ["https://foo.org/other-attachment-usage-type"]})))
    (is (not (s/valid? ::attachment-usage-types/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :related ["https://foo.org/other-attachment-usage-type"]})))))

(deftest attachment-usage-types-test
  (testing "attachmentUsageType concept"
    (is (s/valid? ::attachment-usage-types/attachment-usage-type
                  {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
                   :type "AttachmentUsageType"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Supporting documents"}
                   :definition {"en"
                                "Documents which provide aditional information about the lesson plan. Can be instructions for lesson plan execution demonstrating implementation or any other documents related to the lesson plan"}}))))

(def ex-concept {:id "https://foo.org/aut"
                 :type "AttachmentUsageType"
                 :inScheme "https://foo.org/v1"
                 :broader ["https://foo.org/aut2"]
                 :broadMatch ["https://foo.org/aut3"]
                 :narrower ["https://foo.org/aut4"]
                 :narrowMatch ["https://foo.org/aut5"]
                 :related ["https://foo.org/aut6"]
                 :relatedMatch ["https://foo.org/aut7"]
                 :exactMatch ["https://foo.org/aut8"]})

(deftest edges-with-attrs-test
  (testing "create edges from node"
    (is (= (util/edges-with-attrs ex-concept)
           [["https://foo.org/aut" "https://foo.org/aut2" {:type :broader}]
            ["https://foo.org/aut" "https://foo.org/aut3" {:type :broadMatch}]
            ["https://foo.org/aut" "https://foo.org/aut4" {:type :narrower}]
            ["https://foo.org/aut" "https://foo.org/aut5" {:type :narrowMatch}]
            ["https://foo.org/aut" "https://foo.org/aut6" {:type :related}]
            ["https://foo.org/aut" "https://foo.org/aut7" {:type :relatedMatch}]
            ["https://foo.org/aut" "https://foo.org/aut8" {:type :exactMatch}]]))))
