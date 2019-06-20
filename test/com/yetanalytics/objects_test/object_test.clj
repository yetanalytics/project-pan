(ns com.yetanalytics.objects-test.object-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.object :as object]))

(deftest id-test
  (testing "object IDs"
    (should-satisfy+
     ::object/id
     "https://w3id.org/xapi/catch"    ; Profile ID
     "https://www.yetanalyitcs.io"    ; Author ID
     ; Activity ID
     "https://w3id.org/xapi/catch/activities/domain/planning-and-preparation"
     ; ActivityType ID
     "https://w3id.org/xapi/catch/activitytypes/domain"
     ; ActivityExtension ID
     "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
     ; ContextExtension ID
     "https://w3id.org/xapi/catch/context-extensions/communication-with-families-criteria"
     ; AttachmentUsageType ID
     "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
     ; Verb ID
     "https://w3id.org/xapi/catch/verbs/presented"
     ; StatementTemplate ID 
     ; "https://w3id.org/xapi/catch/templates#score-rubric"
     ; Pattern ID
     ; "https://w3id.org/xapi/catch/patterns#view-rubric"
     :bad
     "https:///www.yetanalyitcs.io" ; Why Will needs to take me to dinner
     "what the pineapple")))

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

(deftest in-scheme-test
  (testing "inScheme property"
    (should-satisfy ::object/in-scheme
                    "https://w3id.org/xapi/catch/v1")
    (should-not-satisfy ::object/in-scheme "what the pineapple")))

(deftest pref-label-test
  (testing "prefLabel property"
    (is (s/valid? ::object/pref-label {"en" "Catch"}))
    (is (not (s/valid? ::object/pref-label {"en" ""})))
    (is (not (s/valid? ::object/pref-label {:en "Catch"})))))

(deftest definition-test
  (testing "definition property"
    (is (s/valid? ::object/definition
                  {"en" "A learning path within the EPISD Dual Language
                        Competency Framework"}))
    (is (s/valid? ::object/definition
                  {"zh-guoyu" "双语能力框架学习计划"}))
    (is (not (s/valid? ::object/definition {"en" ""})))))

(deftest deprecated-test
  (testing "deprecated property"
    (should-satisfy ::object/deprecated true)
    (should-satisfy ::object/deprecated false)
    (should-not-satisfy ::object/deprecated 74)))

(deftest description-test
  (testing "prefLabel and definition properties at once"
    (is (s/valid? ::object/description
                  {:pref-label {"en" "Catch"}
                   :definition {"en" "The profile for the trinity education
                                     application CATCH"}}))))

(deftest common-test
  (testing "descriptions plus inScheme and deprecated properties"
    (is (s/valid? ::object/common
                  {:in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Domain"}
                   :definition {"en" "A learning path within the EPISD Dual
                                     Language Competency Framework"}
                   :deprecated false}))))

(run-tests)
