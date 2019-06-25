(ns com.yetanalytics.objects-test.concepts-test.document-resources-test.agent-profile-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.document-resources.agent-profile
             :as agent-profile]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::agent-profile/type
                     "AgentProfileResource"
                     :bad
                     "ActivityProfileResource"
                     "StateResource"
                     "StanLoona")))

;; TODO: RFC 2046 valid content types

(deftest content-type-test
  (testing "contentType property"
    (should-satisfy+ ::agent-profile/content-type
                     "application/json"
                     :bad
                     74)))

;; Examples taken from the SCORM profile

(deftest schema-test
  (testing "schema property"
    (should-satisfy ::agent-profile/schema
                    "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.agent.profile.schema.json")))

(deftest agent-profile-resource-test
  (testing "ActivityProfileResource document resource"
    (is (s/valid? ::agent-profile/document-resource
                  {:id "https://w3id.org/xapi/scorm/agent-profile"
                   :type "AgentProfileResource"
                   :in-scheme "https://w3id.org/xapi/scorm/v1.0"
                   :pref-label {"en" "SCORM Agent Profile"}
                   :definition
                   {"en" "The SCORM Activity State Object contains the profile data for the specified Agent. The agent profile has three properties: learner_id, learner_name, and preferences."}
                   :content-type "application/json"
                   :schema
                   "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.agent.profile.schema.json"}))))
