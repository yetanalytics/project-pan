(ns com.yetanalytics.objects-test.template-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.template :as template]))

(deftest id-test
  (testing "id property"
    (should-satisfy ::template/id
                    ; StatementTemplate ID 
                    "https://w3id.org/xapi/catch/templates#score-rubric")
    (should-not-satisfy ::template/id "what the pineapple")))

(deftest type-test
  (testing "type property"
    (should-satisfy ::template/type "StatementTemplate")
    (should-not-satisfy ::template/type "Foo Bar")
    (should-not-satisfy ::template/type "Pattern")))

(deftest verb-test
  (testing "verb property"
    (should-satisfy ::template/verb "http://id.tincanapi.com/verb/viewed")
    (should-not-satisfy ::template/verb "")))

(deftest object-activity-type-test
  (testing "objectActivityType property"
    (should-satisfy ::template/object-activity-type
                    "https://w3id.org/xapi/catch/activitytypes/competency")
    (should-not-satisfy ::template/object-activity-type "")))

(deftest context-grouping-activity-type-test
  (testing "contextGroupingActiivtyType property"
    (should-satisfy ::template/context-grouping-activity-type
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/context-grouping-activity-type [])
    (should-not-satisfy ::template/context-grouping-activity-type
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-parent-activity-type-test
  (testing "contextParentActivityType property"
    (should-satisfy ::template/context-parent-activity-type
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/context-parent-activity-type [])
    (should-not-satisfy ::template/context-parent-activity-type
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-other-activity-type-test
  (testing "contextOtherActivityType property"
    (should-satisfy ::template/context-other-activity-type
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/context-other-activity-type [])
    (should-not-satisfy ::template/context-other-activity-type
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-category-activity-type-test
  (testing "contextCategoryActivityType property"
    (should-satisfy ::template/context-category-activity-type
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/context-category-activity-type [])
    (should-not-satisfy ::template/context-category-activity-type
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest attachment-usage-type-test
  (testing "attachmentUsageType property"
    (should-satisfy ::template/attachment-usage-type
                    ["https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"])
    (should-not-satisfy ::template/attachment-usage-type [])
    (should-not-satisfy ::template/attachment-usage-type
                        "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents")))

(deftest object-statement-ref-template-test
  (testing "objectStatementRefTemplate property"
    (should-satisfy ::template/object-statement-ref-template
                    ["https://w3id.org/xapi/catch/templates#communicated-with-families"])
    (should-not-satisfy ::template/object-statement-ref-template [])
    (should-not-satisfy ::template/object-statement-ref-template
                        "https://w3id.org/xapi/catch/templates#communicated-with-families")))

(deftest context-statement-ref-template-test
  (testing "contextStatementRefTemplate property"
    (should-satisfy ::template/context-statement-ref-template
                    ["https://w3id.org/xapi/catch/templates#communicated-with-families"])
    (should-not-satisfy ::template/context-statement-ref-template [])
    (should-not-satisfy ::template/context-statement-ref-template
                        "https://w3id.org/xapi/catch/templates#communicated-with-families")))

(deftest rule-test
  (testing "template rules"
    (should-satisfy+ :rule/location
                     "$.location"
                     "$['location']"
                     "$.result.score.raw"
                     "$.context.contextActivities.category['https://w3id.org/xapi/catch/v1']"
                     :bad
                     ""
                     "what the pineapple")
    (should-satisfy+ :rule/selector
                     "$.selector"
                     "$['selector']"
                     "$['selector'][*]"
                     :bad
                     ""
                     "what the pineapple")
    (should-satisfy+ :rule/presence
                     "included"
                     "excluded"
                     "recommended"
                     :bad
                     "Included"
                     "Profile"
                     "stan loona")
    (should-satisfy+ :rule/any
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])
    (should-satisfy+ :rule/all
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])
    (should-satisfy+ :rule/none
                     ["https://w3id.org/xapi/catch/activitytypes/evidence"
                      "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     [])
    (should-satisfy :rule/scope-note
                    {"en" "this states the the statement conforms to this
                          profile"})
    (is (s/valid? ::template/rule
                  {:location "$.result.score.raw"
                   :presence "included"
                   :scope-note {"en" "the total number of points awarded.
                                    This value will be determined by the point
                                    values associated w/ various criteria."}}))
    (is (s/valid? ::template/rule
                  {:location "$.object.definition.type"
                   :any
                   ["https://w3id.org/xapi/catch/activitytypes/evidence"
                    "https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :scope-note
                   {"en" "the different types of activities within the
                         CATCH application. Select the one appropriate to the
                         completed activity."}}))
    (is (not (s/valid? ::template/rule
                       {:presence "included"
                        :scope-note {"en" "This rule is invalid because
                                          it lacks a location."}})))
    (is (not (s/valid? ::template/rule
                       {:location "$.foo.bar"
                        :scope-note {"en" "This rule is invalid because it
                                          does not include presence, any, all
                                          nor none."}})))))

(deftest template-test
  (testing "template"
    (is (s/valid? ::template/template
                  {:id "https://w3id/xapi/minimal/template"
                   :type "StatementTemplate"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "minimal template"}
                   :definition {"en" "A test of only the required properties
                                     for a template"}}))
    (is (s/valid? ::template/template
                  {:id "https://w3id.org/xapi/catch/templates#score-rubric"
                   :type "StatementTemplate"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "score rubric"}
                   :definition {"en" "This template is for statements that are
                                     the result of a mentor reviewing and
                                     scoring somthing within the catch
                                     application."}
                   :context-grouping-activity-type
                   ["https://w3id.org/xapi/catch/activitytypes/domain"]
                   :verb "http://adlnet.gov/expapi/verbs/scored"
                   :context-parent-activity-type
                   ["https://w3id.org/xapi/catch/activitytypes/competency"]
                   :rules [{:location "$.result.score.raw"
                            :presence "included"
                            :scope-note {"en"
                                         "the total number of points awarded.
                                          This value will be determined by the
                                          point values associated with the
                                          various criteria"}}
                           {:location "$.result.score.min"
                            :presence "included"
                            :scope-note {"en"
                                         "the lowest possible number of
                                         points"}}
                           {:location "$.result.score.max"
                            :presence "included"
                            :scope-note {"en"
                                         "the greatest possible number of
                                          points"}}]}))
    (is (not (s/valid? ::template/template
                       {:id "https://w3id/xapi/bad/template"
                        :type "StatementTemplate"
                        :in-scheme "https://w3id.org/xapi/catch/v1"
                        :pref-label {"en" "bad template"}
                        :definition {"en" "A test of a template that contains
                                         both the objectActivityType and the
                                         objectStatementRefTemplate properties,
                                         which is bad the required properties
                                     for a template"}
                        :object-activity-type
                        "https://w3id.org/xapi/catch/activitytypes/competency"
                        :object-statement-ref-template
                        ["https://w3id.org/xapi/catch/templates#communicated-with-families"]})))))

(deftest templates-test
  (testing "array of templates"
    (is (s/valid? ::template/templates
                  [{:id "https://w3id/xapi/minimal/template"
                    :type "StatementTemplate"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "minimal template"}
                    :definition {"en" "A test of only the required properties
                                     for a template"}}]))
    (is (not (s/valid? ::template/templates [])))))

(def template-ex
  {:id "https://foo.org/template"
   :type "StatementTemplate"
   :in-scheme "https://foo.org/v1"
   :pref-label {"en" "Example template"}
   :definition {"en" "An example template for test purposes"}
   :verb "https://foo.org/verb"
   :object-activity-type "https://foo.org/oat"
   :context-grouping-activity-type ["https://foo.org/cgat1"
                                    "https://foo.org/cgat2"]
   :context-parent-activity-type ["https://foo.org/cpat1"
                                  "https://foo.org/cpat2"]
   :context-other-activity-type ["https://foo.org/coat1"
                                 "https://foo.org/coat2"]
   :context-category-activity-type ["https://foo.org/ccat1"
                                    "https://foo.org/ccat2"]
   :attachment-usage-type ["https://foo.org/aut1"
                           "https://foo.org/aut2"]
   :context-statement-ref-template ["https://foo.org/csrt1"
                                    "https://foo.org/csrt2"]})

(deftest edge-with-attrs-test
  (testing "Creating list of edges"
    (is (= (util/edges-with-attrs template-ex)
           [["https://foo.org/template" "https://foo.org/verb" {:type :verb}]
            ["https://foo.org/template" "https://foo.org/oat" {:type :object-activity-type}]
            ["https://foo.org/template" "https://foo.org/cgat1" {:type :context-grouping-activity-type}]
            ["https://foo.org/template" "https://foo.org/cgat2" {:type :context-grouping-activity-type}]
            ["https://foo.org/template" "https://foo.org/cpat1" {:type :context-parent-activity-type}]
            ["https://foo.org/template" "https://foo.org/cpat2" {:type :context-parent-activity-type}]
            ["https://foo.org/template" "https://foo.org/coat1" {:type :context-other-activity-type}]
            ["https://foo.org/template" "https://foo.org/coat2" {:type :context-other-activity-type}]
            ["https://foo.org/template" "https://foo.org/ccat1" {:type :context-category-activity-type}]
            ["https://foo.org/template" "https://foo.org/ccat2" {:type :context-category-activity-type}]
            ["https://foo.org/template" "https://foo.org/aut1" {:type :attachment-usage-type}]
            ["https://foo.org/template" "https://foo.org/aut2" {:type :attachment-usage-type}]
            ["https://foo.org/template" "https://foo.org/csrt1" {:type :context-statement-ref-template}]
            ["https://foo.org/template" "https://foo.org/csrt2" {:type :context-statement-ref-template}]]))))

(util/edges-with-attrs template-ex)
