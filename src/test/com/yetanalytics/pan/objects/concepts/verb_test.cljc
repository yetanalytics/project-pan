(ns com.yetanalytics.pan.objects.concepts.verb-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concepts.verb :as v]
            [com.yetanalytics.test-utils :refer [should-satisfy
                                                 should-satisfy+
                                                 should-not-satisfy]]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::v/type
                     "Verb"
                     :bad
                     "ActivityType"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::v/broader
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::v/broader
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::v/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::v/broadMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::v/broadMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::v/broadMatch [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::v/narrower
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::v/narrower
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::v/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::v/narrowMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::v/narrowMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::v/narrowMatch [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::v/related
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::v/related
                        "https://w3id.org/xapi/catch/verbs/provided")
    (should-not-satisfy ::v/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::v/relatedMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::v/relatedMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::v/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::v/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::v/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::v/exactMatch [])))

(deftest verb-test
  (testing "Verb concept"
    (is (s/valid? ::v/verb
                  {:id "https://w3id.org/xapi/catch/verbs/presented"
                   :type "Verb"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "presented"}
                   :definition {"en" "leading a discussion at an advocacy event"}}))
    ;; Related ONLY on deprecated
    (is (not (s/valid? ::v/verb
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :prefLabel {:en "foo"}
                        :definition {:en "some def"}
                        :deprecated false
                        :related ["https://foo.org/other-verb"]})))))

(def ex-concept {:id "https://foo.org/verb"
                 :type "Verb"
                 :inScheme "https://foo.org/v1"
                 :broader ["https://foo.org/verb2"]
                 :broadMatch ["https://foo.org/verb3"]
                 :narrower ["https://foo.org/verb4"]
                 :narrowMatch ["https://foo.org/verb5"]
                 :related ["https://foo.org/verb6"]
                 :relatedMatch ["https://foo.org/verb7"]
                 :exactMatch ["https://foo.org/verb8"]})

(deftest edges-with-attrs-test
  (testing "create edges from node"
    (is (= [["https://foo.org/verb" "https://foo.org/verb2" {:type :broader}]
            ["https://foo.org/verb" "https://foo.org/verb3" {:type :broadMatch}]
            ["https://foo.org/verb" "https://foo.org/verb4" {:type :narrower}]
            ["https://foo.org/verb" "https://foo.org/verb5" {:type :narrowMatch}]
            ["https://foo.org/verb" "https://foo.org/verb6" {:type :related}]
            ["https://foo.org/verb" "https://foo.org/verb7" {:type :relatedMatch}]
            ["https://foo.org/verb" "https://foo.org/verb8" {:type :exactMatch}]]
           (graph/edges-with-attrs ex-concept)))))
