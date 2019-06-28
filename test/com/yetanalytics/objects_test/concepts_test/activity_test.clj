(ns com.yetanalytics.objects-test.concepts-test.activity-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.activities :as activities]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activities/type
                     "Activity"
                     :bad
                     "Profile"
                     "Verb"
                     "StanLoona")))

(deftest context-test
  (testing "context property"
    (should-satisfy ::activities/context
                    "https://w3id.org/xapi/profiles/activity-context")
    (should-satisfy ::activities/context "https://some-other-context")
    (should-satisfy ::activities/context
                    ["https://w3id.org/xapi/profiles/activity-context"])
    (should-not-satisfy ::activities/context ["https://some-other-context"])
    (should-not-satisfy ::activities/context "foo bar")))

(deftest camel-case-keys-test
  (testing "turning kebab-case keys into camelCase keys"
    (is (= (activities/camel-case-keys
            {:key "Zero" :key-one "One" :key-two "Two"})
           {:key "Zero" :keyOne "One" :keyTwo "Two"}))))

(deftest activity-definition
  (testing "activityDefinition property"
    (is (s/valid? ::activities/activity-definition
                  {:context "https://w3id.org/xapi/profiles/activity-context"
                   :name {"en" "Cross Linguistic Connections"}
                   :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                   :type "https://w3id.org/xapi/catch/activitytypes/competency"}))))

(deftest activity-test
  (testing "Activity concept"
    (is (s/valid? ::activities/activity
                  {:id "https://w3id.org/xapi/catch/activities/competency/cross-linguistic-connections"
                   :type "Activity"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :activity-definition
                   {:context "https://w3id.org/xapi/profiles/activity-context"
                    :name {:en "Cross Linguistic Connections"}
                    :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                    :type "https://w3id.org/xapi/catch/activitytypes/competency"}}))))
