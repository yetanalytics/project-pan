(ns com.yetanalytics.pan.objects.concepts.attachment-usage-type-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.concepts.attachment-usage-type :as aut]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::aut/type
                     "AttachmentUsageType"
                     :bad
                     "ActivityType"
                     "Verb"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::aut/broader
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::aut/broader
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::aut/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::aut/broadMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::aut/broadMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::aut/broadMatch [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::aut/narrower
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::aut/narrower
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::aut/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::aut/narrowMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::aut/narrowMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::aut/narrowMatch [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::aut/related
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::aut/related
                        "https://w3id.org/xapi/catch/attachment-usage-types/provided")
    (should-not-satisfy ::aut/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::aut/relatedMatch
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::aut/relatedMatch
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::aut/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::aut/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::aut/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::aut/exactMatch [])))

(deftest attachment-usage-types-test
  (testing "attachmentUsageType concept"
    (is (s/valid? ::aut/attachment-usage-type
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
    (is (= [["https://foo.org/aut" "https://foo.org/aut2" {:type :broader}]
            ["https://foo.org/aut" "https://foo.org/aut3" {:type :broadMatch}]
            ["https://foo.org/aut" "https://foo.org/aut4" {:type :narrower}]
            ["https://foo.org/aut" "https://foo.org/aut5" {:type :narrowMatch}]
            ["https://foo.org/aut" "https://foo.org/aut6" {:type :related}]
            ["https://foo.org/aut" "https://foo.org/aut7" {:type :relatedMatch}]
            ["https://foo.org/aut" "https://foo.org/aut8" {:type :exactMatch}]]
           (graph/edges-with-attrs ex-concept)))))
