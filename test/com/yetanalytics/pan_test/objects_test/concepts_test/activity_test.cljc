(ns com.yetanalytics.pan-test.objects-test.concepts-test.activity-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.concepts.activities :as activities]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activities/type
                     "Activity"
                     :bad
                     "Profile"
                     "Verb"
                     "StanLoona")))

(deftest context-test
  (testing "@context property"
    (should-satisfy ::activities/_context
                    "https://w3id.org/xapi/profiles/activity-context")
    (should-satisfy ::activities/_context "https://some-other-context")
    (should-satisfy ::activities/_context
                    ["https://w3id.org/xapi/profiles/activity-context"])
    (should-not-satisfy ::activities/_context ["https://some-other-context"])
    (should-not-satisfy ::activities/_context "foo bar")))

(deftest stringify-lang-keys-test
  (testing "turning keys in the name and description language maps into strings"
    (is (= (activities/stringify-lang-keys
            {:_context "https://w3id.org/xapi/profiles/activity-context"
             :name {:en "Blah"}
             :description {:en "Blah Blah Blah"}
             :type "https://w3id.org/xapi/catch/activitytypes/blah"})
           {:_context "https://w3id.org/xapi/profiles/activity-context"
            :name {"en" "Blah"}
            :description {"en" "Blah Blah Blah"}
            :type "https://w3id.org/xapi/catch/activitytypes/blah"}))))

(deftest activity-definition
  (testing "activityDefinition property"
    (is (s/valid? ::activities/activityDefinition
                  {:_context "https://w3id.org/xapi/profiles/activity-context"
                   :name {:en "Cross Linguistic Connections"}
                   :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                   :type "https://w3id.org/xapi/catch/activitytypes/competency"}))))

(deftest activity-test
  (testing "Activity concept"
    (is (s/valid? ::activities/activity
                  {:id "https://w3id.org/xapi/catch/activities/competency/cross-linguistic-connections"
                   :type "Activity"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :activityDefinition
                   {:_context "https://w3id.org/xapi/profiles/activity-context"
                    :name {:en "Cross Linguistic Connections"}
                    :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                    :type "https://w3id.org/xapi/catch/activitytypes/competency"}}))))
