(ns com.yetanalytics.pan.objects.concepts.resources.state-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+]]
            [com.yetanalytics.pan.objects.concepts.resources.state
             :as state]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::state/type
                     "StateResource"
                     :bad
                     "AgentProfileResource"
                     "ActivityProfileResource"
                     "StanLoona")))

;; TODO: RFC 2046 valid content types
(deftest content-type-test
  (testing "contentType property"
    (should-satisfy+ ::state/contentType
                     "application/json"
                     :bad
                     74)))

;; Examples taken from the SCORM profile

(deftest schema-test
  (testing "schema property"
    (should-satisfy ::state/schema
                    "https://w3id.org/xapi/scorm/state/scorm.profile.activity.profile.schema")))

(deftest state-resource-test
  (testing "ActivityProfileResource document resource"
    (is (s/valid? ::state/document-resource
                  {:id "https://w3id.org/xapi/scorm/attempt-state"
                   :type "StateResource"
                   :inScheme "https://w3id.org/xapi/scorm/v1.0"
                   :prefLabel {"en" "SCORM Activity Attempt State"}
                   :definition
                   {"en" "The SCORM Activity Attempt State Object contains the state data for the specified attempt on an Activity. It has the following properties: credit, mode, location, preferences, total_time, and adl_data."}
                   :contentType "application/json"
                   :context
                   "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/context/attempt-state-context.jsonld"
                   :schema
                   "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.attempt.state.schema.json"}))))
