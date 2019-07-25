(ns com.yetanalytics.objects-test.concepts-test.verb-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.verbs :as verbs]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::verbs/type
                     "Verb"
                     :bad
                     "ActivityType"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::verbs/broader
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/broader
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::verbs/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::verbs/broadMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/broadMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/broadMatch [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::verbs/narrower
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/narrower
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::verbs/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::verbs/narrowMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/narrowMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/narrowMatch [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::verbs/related
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/related
                        "https://w3id.org/xapi/catch/verbs/provided")
    (should-not-satisfy ::verbs/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::verbs/relatedMatch
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/relatedMatch
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/relatedMatch [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::verbs/exactMatch
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::verbs/exactMatch
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::verbs/exactMatch [])))

(deftest related-only-deprecated-test
  (testing "related MUST only be used on deprecated concepts"
    (is (s/valid? ::verbs/related-only-deprecated
                  {:id "https://foo.org/verb"
                   :type "Verb"
                   :deprecated true
                   :related ["https://foo.org/other-verb"]}))
    (is (not (s/valid? ::verbs/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :deprecated false
                        :related ["https://foo.org/other-verb"]})))
    (is (not (s/valid? ::verbs/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :related ["https://foo.org/other-verb"]})))))

(deftest verb-test
  (testing "Verb concept"
    (is (s/valid? ::verbs/verb
                  {:id "https://w3id.org/xapi/catch/verbs/presented"
                   :type "Verb"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "presented"}
                   :definition {"en" "leading a discussion at an advocacy event"}}))))

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
    (is (= (util/edges-with-attrs ex-concept)
           [["https://foo.org/verb" "https://foo.org/verb2" {:type :broader}]
            ["https://foo.org/verb" "https://foo.org/verb3" {:type :broadMatch}]
            ["https://foo.org/verb" "https://foo.org/verb4" {:type :narrower}]
            ["https://foo.org/verb" "https://foo.org/verb5" {:type :narrowMatch}]
            ["https://foo.org/verb" "https://foo.org/verb6" {:type :related}]
            ["https://foo.org/verb" "https://foo.org/verb7" {:type :relatedMatch}]
            ["https://foo.org/verb" "https://foo.org/verb8" {:type :exactMatch}]]))))
