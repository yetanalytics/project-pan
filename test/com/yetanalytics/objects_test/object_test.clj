(ns com.yetanalytics.objects-test.object-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.utils :refer :all]))

; (deftest id-test
;   (testing "object IDs"
;     (should-satisfy+
;      ::ax/iri
;      "https://w3id.org/xapi/catch"    ; Profile ID
;      "https://www.yetanalyitcs.io"    ; Author ID
;      ; Activity ID
;      "https://w3id.org/xapi/catch/activities/domain/planning-and-preparation"
;      ; ActivityType ID
;      "https://w3id.org/xapi/catch/activitytypes/domain"
;      ; ActivityExtension ID
;      "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
;      ; ContextExtension ID
;      "https://w3id.org/xapi/catch/context-extensions/communication-with-families-criteria"
;      ; AttachmentUsageType ID
;      "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
;      ; Verb ID
;      "https://w3id.org/xapi/catch/verbs/presented"
;      ; StatementTemplate ID 
;      ; "https://w3id.org/xapi/catch/templates#score-rubric"
;      ; Pattern ID
;      ; "https://w3id.org/xapi/catch/patterns#view-rubric"
;      :bad
;      "https:///www.yetanalyitcs.io" ; Why Will needs to take me to dinner
;      "what the pineapple")))

; (deftest type-test
;   (testing "object types"
;     (should-satisfy+ ::object/type
;                      "Profile"
;                      ; Author types
;                      "Organization"
;                      "Person"
;                      ; Extensions
;                      "ContextExtension"
;                      "ResultExtension"
;                      "ActivityExtension"
;                      ; Document Resources
;                      "StateResource"
;                      "AgentProfileResource"
;                      "ActivityProfileResource"
;                      ; Other concepts
;                      "Verb"
;                      "ActivityType"
;                      "AttachmentUsageType"
;                      "Activity"
;                      ; Other object types
;                      "StatementTemplate"
;                      "Pattern"
;                      :bad
;                      "Statement Template"
;                      "stan loona")))

; (deftest in-scheme-test
;   (testing "inScheme property"
;     (should-satisfy ::u/in-scheme-strict-scalar
;                     {:in-scheme "https://w3id.org/xapi/catch/v1"
;                      :profile {:versions [{:id "https://foo.bar"}
;                                           {:id "https://w3id.org/xapi/catch/v1"}]}})
;     (should-not-satisfy ::u/in-scheme-strict-scalar
;                         {:in-scheme "https://w3id.org/xapi/catch/v1"
;                          :profile {:versions [{:id "https://foo.bar"}
;                                               {:id "https://foo.baz"}]}})))

; (deftest pref-label-test
;   (testing "prefLabel property"
;     (is (s/valid? ::ax/language-map {"en" "Catch"}))
;     (is (s/valid? ::ax/language-map {"en" ""}))
;     (is (s/valid? ::ax/language-map {:en "Catch"}))))

; (deftest definition-test
;   (testing "definition property"
;     (is (s/valid? ::ax/language-map
;                   {"en" "A learning path within the EPISD Dual Language
;                         Competency Framework"}))
;     (is (s/valid? ::ax/language-map
;                   {"zh-guoyu" "双语能力框架学习计划"}))))

; (deftest deprecated-test
;   (testing "deprecated property"
;     (should-satisfy ::ax/boolean true)
;     (should-satisfy ::ax/boolean false)
;     (should-not-satisfy ::ax/boolean 74)))
