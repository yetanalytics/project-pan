(ns com.yetanalytics.pan.objects.templates.rule-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+]]
            [com.yetanalytics.pan.objects.templates.rule :as r]))

(deftest location-test
  (testing "location property"
    (should-satisfy+ ::r/location
                     "$.location"
                     "$['location']"
                     "$.result.score.raw"
                     "$.context.contextActivities.category['https://w3id.org/xapi/catch/v1']"
                     :bad
                     ""
                     "what the pineapple")))

(deftest selector-test
  (testing "selector property"
    (should-satisfy+ ::r/selector
                     "$.selector"
                     "$['selector']"
                     "$['selector'][*]"
                     :bad
                     ""
                     "what the pineapple")))

(deftest presence-test
  (testing "'presence' property"
    (should-satisfy+ ::r/presence
                     "included"
                     "excluded"
                     "recommended"
                     :bad
                     "Included"
                     "Profile"
                     "stan loona")))

(deftest any-test
  (testing "'any' property"
    (should-satisfy+ ::r/presence
                     "included"
                     "excluded"
                     "recommended"
                     :bad
                     "Included"
                     "Profile"
                     "stan loona")))

(deftest all-test
  (testing "'all' property"
    (should-satisfy+ ::r/all
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])))

(deftest none-test
  (testing "'none' property"
    (should-satisfy+ ::r/none
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])))

(deftest scope-note-test
  (testing "scopeNote property"
    (should-satisfy ::r/scopeNote
                    {"en" "this states the the statement conforms to this
                          profile"})))

(deftest rule-test
  (testing "template rules"
    (is (s/valid? ::r/rule
                  {:location "$.result.score.raw"
                   :presence "included"
                   :scopeNote {"en" "the total number of points awarded.
                                    This value will be determined by the point
                                    values associated w/ various criteria."}}))
    (is (s/valid? ::r/rule
                  {:location "$.object.definition.type"
                   :any
                   ["https://w3id.org/xapi/catch/activitytypes/evidence"
                    "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :scopeNote
                   {"en" "the different types of activities within the CATCH application. Select the one appropriate to the completed activity."}}))
    (is (s/valid? ::r/rule
                  {:location "$.object.definition.foobar"
                   :presence "included"
                   :any
                   ["https://w3id.org/xapi/catch/activitytypes/evidence"
                    "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :scopeNote
                   {"en" "In this case, both 'presence' and 'any' are included in the rule."}}))
    (is (not (s/valid? ::r/rule
                       {:presence "included"
                        :scopeNote {"en" "This rule is invalid because it lacks a location."}})))
    (is (not (s/valid? ::r/rule
                       {:location "$.foo.bar"
                        :scopeNote {"en" "This rule is invalid because it does not include presence, any, all nor none."}})))))
