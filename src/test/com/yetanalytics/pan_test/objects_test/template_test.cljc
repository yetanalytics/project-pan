(ns com.yetanalytics.pan-test.objects-test.template-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.template :as template]))

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

(deftest prefLabel-test
  (testing "prefLabel property"
    (is (s/valid? ::template/prefLabel {"en" "score rubric"}))
    (is (s/valid? ::template/prefLabel {"en" ""}))
    (is (s/valid? ::template/prefLabel {:en "score rubric"}))))

(deftest definition-test
  (testing "definition property"
    (is (s/valid? ::template/definition
                  {"en" "This template is for statements that are the result of
                        a mentor reviewing and scoring somthing within the
                        catch application."}))))

(deftest verb-test
  (testing "verb property"
    (should-satisfy ::template/verb "http://id.tincanapi.com/verb/viewed")
    (should-not-satisfy ::template/verb "")))

(deftest object-activity-type-test
  (testing "objectActivityType property"
    (should-satisfy ::template/objectActivityType
                    "https://w3id.org/xapi/catch/activitytypes/competency")
    (should-not-satisfy ::template/objectActivityType "")))

(deftest context-grouping-activity-type-test
  (testing "contextGroupingActiivtyType property"
    (should-satisfy ::template/contextGroupingActivityType
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/contextGroupingActivityType [])
    (should-not-satisfy ::template/contextGroupingActivityType
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-parent-activity-type-test
  (testing "contextParentActivityType property"
    (should-satisfy ::template/contextParentActivityType
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/contextParentActivityType [])
    (should-not-satisfy ::template/contextParentActivityType
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-other-activity-type-test
  (testing "contextOtherActivityType property"
    (should-satisfy ::template/contextOtherActivityType
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/contextOtherActivityType [])
    (should-not-satisfy ::template/contextOtherActivityType
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest context-category-activity-type-test
  (testing "contextCategoryActivityType property"
    (should-satisfy ::template/contextCategoryActivityType
                    ["https://w3id.org/xapi/catch/activitytypes/domain"])
    (should-not-satisfy ::template/contextCategoryActivityType [])
    (should-not-satisfy ::template/contextCategoryActivityType
                        "https://w3id.org/xapi/catch/activitytypes/domain")))

(deftest attachment-usage-type-test
  (testing "attachmentUsageType property"
    (should-satisfy ::template/attachmentUsageType
                    ["https://w3id.org/xapi/catch/attachmentUsageTypes/supporting-documents"])
    (should-not-satisfy ::template/attachmentUsageType [])
    (should-not-satisfy ::template/attachmentUsageType
                        "https://w3id.org/xapi/catch/attachmentUsageTypes/supporting-documents")))

(deftest object-statement-ref-template-test
  (testing "objectStatementRefTemplate property"
    (should-satisfy ::template/objectStatementRefTemplate
                    ["https://w3id.org/xapi/catch/templates#communicated-with-families"])
    (should-not-satisfy ::template/objectStatementRefTemplate [])
    (should-not-satisfy ::template/objectStatementRefTemplate
                        "https://w3id.org/xapi/catch/templates#communicated-with-families")))

(deftest context-statement-ref-template-test
  (testing "contextStatementRefTemplate property"
    (should-satisfy ::template/contextStatementRefTemplate
                    ["https://w3id.org/xapi/catch/templates#communicated-with-families"])
    (should-not-satisfy ::template/contextStatementRefTemplate [])
    (should-not-satisfy ::template/contextStatementRefTemplate
                        "https://w3id.org/xapi/catch/templates#communicated-with-families")))

(deftest template-test
  (testing "template"
    (is (s/valid? ::template/template
                  {:id "https://w3id/xapi/minimal/template"
                   :type "StatementTemplate"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "minimal template"}
                   :definition {"en" "A test of only the required properties
                                     for a template"}}))
    (is (s/valid? ::template/template
                  {:id "https://w3id.org/xapi/catch/templates#score-rubric"
                   :type "StatementTemplate"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "score rubric"}
                   :definition {"en" "This template is for statements that are
                                     the result of a mentor reviewing and
                                     scoring somthing within the catch
                                     application."}
                   :contextGroupingActivityType
                   ["https://w3id.org/xapi/catch/activitytypes/domain"]
                   :verb "http://adlnet.gov/expapi/verbs/scored"
                   :contextParentActivityType
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
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :prefLabel {"en" "bad template"}
                        :definition {"en" "A test of a template that contains
                                         both the objectActivityType and the
                                         objectStatementRefTemplate properties,
                                         which is bad the required properties
                                     for a template"}
                        :objectActivityType
                        "https://w3id.org/xapi/catch/activitytypes/competency"
                        :objectStatementRefTemplate
                        ["https://w3id.org/xapi/catch/templates#communicated-with-families"]})))))

(deftest templates-test
  (testing "array of templates"
    (is (s/valid? ::template/templates
                  [{:id "https://w3id/xapi/minimal/template"
                    :type "StatementTemplate"
                    :inScheme "https://w3id.org/xapi/catch/v1"
                    :prefLabel {"en" "minimal template"}
                    :definition {"en" "A test of only the required properties
                                     for a template"}}]))
    (is (not (s/valid? ::template/templates [])))))

;; Strict validation tests

(def template-ex
  {:id "https://foo.org/template"
   :type "StatementTemplate"
   :inScheme "https://foo.org/v1"
   :prefLabel {"en" "Example template"}
   :definition {"en" "An example template for test purposes"}
   :verb "https://foo.org/verb"
   :objectActivityType "https://foo.org/oat"
   :contextGroupingActivityType ["https://foo.org/cgat1"
                                 "https://foo.org/cgat2"]
   :contextParentActivityType ["https://foo.org/cpat1"
                               "https://foo.org/cpat2"]
   :contextOtherActivityType ["https://foo.org/coat1"
                              "https://foo.org/coat2"]
   :contextCategoryActivityType ["https://foo.org/ccat1"
                                 "https://foo.org/ccat2"]
   :attachmentUsageType ["https://foo.org/aut1"
                         "https://foo.org/aut2"]
   :contextStatementRefTemplate ["https://foo.org/csrt1"
                                 "https://foo.org/csrt2"]})

(deftest edge-with-attrs-test
  (testing "Creating list of edges"
    (is (= [["https://foo.org/template" "https://foo.org/verb" {:type :verb}]
            ["https://foo.org/template" "https://foo.org/oat" {:type :objectActivityType}]
            ["https://foo.org/template" "https://foo.org/cgat1" {:type :contextGroupingActivityType}]
            ["https://foo.org/template" "https://foo.org/cgat2" {:type :contextGroupingActivityType}]
            ["https://foo.org/template" "https://foo.org/cpat1" {:type :contextParentActivityType}]
            ["https://foo.org/template" "https://foo.org/cpat2" {:type :contextParentActivityType}]
            ["https://foo.org/template" "https://foo.org/coat1" {:type :contextOtherActivityType}]
            ["https://foo.org/template" "https://foo.org/coat2" {:type :contextOtherActivityType}]
            ["https://foo.org/template" "https://foo.org/ccat1" {:type :contextCategoryActivityType}]
            ["https://foo.org/template" "https://foo.org/ccat2" {:type :contextCategoryActivityType}]
            ["https://foo.org/template" "https://foo.org/aut1" {:type :attachmentUsageType}]
            ["https://foo.org/template" "https://foo.org/aut2" {:type :attachmentUsageType}]
            ["https://foo.org/template" "https://foo.org/csrt1" {:type :contextStatementRefTemplate}]
            ["https://foo.org/template" "https://foo.org/csrt2" {:type :contextStatementRefTemplate}]]
           (graph/edges-with-attrs template-ex)))))

(deftest valid-edge-test
  (testing "Valid edge properties"
    (should-satisfy+
     ::template/template-edge
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :verb :src-type "StatementTemplate" :dest-type "Verb"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :objectActivityType
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextGroupingActivityType
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextParentActivityType
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextOtherActivityType
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextCategoryActivityType
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :attachmentUsageType
      :src-type "StatementTemplate" :dest-type "AttachmentUsageType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :objectStatementRefTemplate
      :src-type "StatementTemplate" :dest-type "StatementTemplate"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextStatementRefTemplate
      :src-type "StatementTemplate" :dest-type "StatementTemplate"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     :bad
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :verb :src-type "StatementTemplate" :dest-type "ActivityType"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :objectActivityType
      :src-type "StatementTemplate" :dest-type "Verb"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :attachmentUsageType
      :src-type "ActivityType" :dest-type "AttachmentUsageType"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :objectStatementRefTemplate
      :src-type "StatementTemplate" :dest-type "StatementTemplate"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v2"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :contextStatementRefTemplate
      :src-type "StatementTemplate" :dest-type "ActivityType"
      :src-version "https://foo.org/v1" :dest-version "https://foo.org/v1"}
     {:src "http://foo.org/st" :dest "http://foo.org/bar"
      :type :blah :src-type "StatementTemplate" :dest-type "Verb"})))

(def ex-concepts
  [{:id "https://foo.org/verb"
    :type "Verb"
    :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/activity-type"
    :type "ActivityType"
    :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/attachmentUsageType"
    :type "AttachmentUsageType"
    :inScheme "https://foo.org/v1"}])

(def ex-templates
  [{:id "https://foo.org/template1"
    :type "StatementTemplate"
    :inScheme "https://foo.org/v1"
    :verb "https://foo.org/verb"
    :objectActivityType "https://foo.org/activity-type"
    :attachmentUsageType ["https://foo.org/attachmentUsageType"]
    :contextStatementRefTemplate ["https://foo.org/template2"]}
   {:id "https://foo.org/template2"
    :type "StatementTemplate"
    :inScheme "https://foo.org/v1"
    :objectStatementRefTemplate ["https://foo.org/template1"]}])

(def ex-profile
  {:concepts  ex-concepts
   :templates ex-templates})

(def tgraph (template/create-graph ex-profile))

(deftest graph-test
  (testing "graph properties"
    (is (= 5 (count (graph/nodes tgraph))))
    (is (= 5 (count (graph/edges tgraph))))
    (is (= 5 (count (template/get-edges tgraph))))
    (is (= #{"https://foo.org/template1" "https://foo.org/template2"
             "https://foo.org/verb" "https://foo.org/activity-type"
             "https://foo.org/attachmentUsageType"}
           (set (graph/nodes tgraph))))
    (is (= #{{:src "https://foo.org/template1" :src-type "StatementTemplate" :src-version "https://foo.org/v1"
              :dest "https://foo.org/verb" :dest-type "Verb" :dest-version "https://foo.org/v1"
              :type :verb}
             {:src "https://foo.org/template1" :src-type "StatementTemplate" :src-version "https://foo.org/v1"
              :dest "https://foo.org/activity-type" :dest-type "ActivityType" :dest-version "https://foo.org/v1"
              :type :objectActivityType}
             {:src "https://foo.org/template1" :src-type "StatementTemplate" :src-version "https://foo.org/v1"
              :dest "https://foo.org/attachmentUsageType" :dest-type "AttachmentUsageType" :dest-version "https://foo.org/v1"
              :type :attachmentUsageType}
             {:src "https://foo.org/template1" :src-type "StatementTemplate" :src-version "https://foo.org/v1"
              :dest "https://foo.org/template2" :dest-type "StatementTemplate" :dest-version "https://foo.org/v1"
              :type :contextStatementRefTemplate}
             {:src "https://foo.org/template2" :src-type "StatementTemplate" :src-version "https://foo.org/v1"
              :dest "https://foo.org/template1" :dest-type "StatementTemplate" :dest-version "https://foo.org/v1"
              :type :objectStatementRefTemplate}}
           (set (template/get-edges tgraph))))
    (should-satisfy ::template/template-edges (template/get-edges tgraph))
    (is (nil? (template/validate-template-edges tgraph)))))
