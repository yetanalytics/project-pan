(ns com.yetanalytics.pan-test.objects-test.concepts-test.document-resources-test.agent-profile-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+]]
            [com.yetanalytics.pan.objects.concepts.document-resources.agent-profile
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
    (should-satisfy+ ::agent-profile/contentType
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
                   :inScheme "https://w3id.org/xapi/scorm/v1.0"
                   :prefLabel {"en" "SCORM Agent Profile"}
                   :definition
                   {"en" "The SCORM Activity State Object contains the profile data for the specified Agent. The agent profile has three properties: learner_id, learner_name, and preferences."}
                   :contentType "application/json"
                   :schema
                   "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.agent.profile.schema.json"}))))
