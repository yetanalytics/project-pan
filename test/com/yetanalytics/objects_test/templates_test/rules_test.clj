(ns com.yetanalytics.objects-test.templates-test.rules-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.templates.rules :as rules]))

(deftest location-test
  (testing "location property"
    (should-satisfy+ ::rules/location
                     "$.location"
                     "$['location']"
                     "$.result.score.raw"
                     "$.context.contextActivities.category['https://w3id.org/xapi/catch/v1']"
                     :bad
                     ""
                     "what the pineapple")))

(deftest selector-test
  (testing "selector property"
    (should-satisfy+ ::rules/selector
                     "$.selector"
                     "$['selector']"
                     "$['selector'][*]"
                     :bad
                     ""
                     "what the pineapple")))

(deftest presence-test
  (testing "'presence' property"
    (should-satisfy+ ::rules/presence
                     "included"
                     "excluded"
                     "recommended"
                     :bad
                     "Included"
                     "Profile"
                     "stan loona")))

(deftest any-test
  (testing "'any' property"
    (should-satisfy+ ::rules/presence
                     "included"
                     "excluded"
                     "recommended"
                     :bad
                     "Included"
                     "Profile"
                     "stan loona")))

(deftest all-test
  (testing "'all' property"
    (should-satisfy+ ::rules/all
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])))

(deftest none-test
  (testing "'none' property"
    (should-satisfy+ ::rules/none
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])))

(deftest scope-note-test
  (testing "scopeNote property"
    (should-satisfy ::rules/scope-note
                    {"en" "this states the the statement conforms to this
                          profile"})))

(deftest rule-test
  (testing "template rules"
    (is (s/valid? ::rules/rule
                  {:location "$.result.score.raw"
                   :presence "included"
                   :scope-note {"en" "the total number of points awarded.
                                    This value will be determined by the point
                                    values associated w/ various criteria."}}))
    (is (s/valid? ::rules/rule
                  {:location "$.object.definition.type"
                   :any
                   ["https://w3id.org/xapi/catch/activitytypes/evidence"
                    "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :scope-note
                   {"en" "the different types of activities within the
                         CATCH application. Select the one appropriate to the
                         completed activity."}}))
    (is (s/valid? ::rules/rule
                  {:location "$.object.definition.foobar"
                   :presence "included"
                   :any
                   ["https://w3id.org/xapi/catch/activitytypes/evidence"
                    "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :scope-note
                   {"en" "In this case, both 'presence' and 'any' are included in the rule."}}))
    (is (not (s/valid? ::rules/rule
                       {:presence "included"
                        :scope-note {"en" "This rule is invalid because
                                          it lacks a location."}})))
    (is (not (s/valid? ::rules/rule
                       {:location "$.foo.bar"
                        :scope-note {"en" "This rule is invalid because it
                                          does not include presence, any, all
                                          nor none."}})))))

(run-tests)
