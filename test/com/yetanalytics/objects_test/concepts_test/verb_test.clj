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
    (should-satisfy ::verbs/broad-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/broad-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/broad-match [])))

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
    (should-satisfy ::verbs/narrow-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/narrow-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/narrow-match [])))

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
    (should-satisfy ::verbs/related-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/related-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::verbs/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::verbs/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::verbs/exact-match [])))

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
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "presented"}
                   :definition {"en" "leading a discussion at an advocacy event"}}))))

(def ex-concept {:id "https://foo.org/verb"
                 :type "Verb"
                 :in-scheme "https://foo.org/v1"
                 :broader ["https://foo.org/verb2"]
                 :broad-match ["https://foo.org/verb3"]
                 :narrower ["https://foo.org/verb4"]
                 :narrow-match ["https://foo.org/verb5"]
                 :related ["https://foo.org/verb6"]
                 :related-match ["https://foo.org/verb7"]
                 :exact-match ["https://foo.org/verb8"]})

(deftest edges-with-attrs-test
  (testing "create edges from node"
    (is (= (util/edges-with-attrs ex-concept)
           [["https://foo.org/verb" "https://foo.org/verb2" {:type :broader}]
            ["https://foo.org/verb" "https://foo.org/verb3" {:type :broad-match}]
            ["https://foo.org/verb" "https://foo.org/verb4" {:type :narrower}]
            ["https://foo.org/verb" "https://foo.org/verb5" {:type :narrow-match}]
            ["https://foo.org/verb" "https://foo.org/verb6" {:type :related}]
            ["https://foo.org/verb" "https://foo.org/verb7" {:type :related-match}]
            ["https://foo.org/verb" "https://foo.org/verb8" {:type :exact-match}]]))))
