(ns com.yetanalytics.pan-test.objects-test.concepts-test.resources-test.activity-profile-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+]]
            [com.yetanalytics.pan.objects.concepts.resources.activity-profile
             :as activity-profile]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activity-profile/type
                     "ActivityProfileResource"
                     :bad
                     "AgentProfileResource"
                     "StateResource"
                     "StanLoona")))

;; TODO: RFC 2046 valid content types
(deftest content-type-test
  (testing "contentType property"
    (should-satisfy+ ::activity-profile/contentType
                     "application/json"
                     :bad
                     74)))

;; Examples taken from the SCORM profile

(deftest schema-test
  (testing "schema property"
    (should-satisfy ::activity-profile/schema
                    "https://w3id.org/xapi/scorm/activity-profile/scorm.profile.activity.profile.schema")))

(deftest activity-profile-resource-test
  (testing "ActivityProfileResource document resource"
    (is (s/valid? ::activity-profile/document-resource
                  {:id "https://w3id.org/xapi/scorm/activity-profile"
                   :type "ActivityProfileResource"
                   :inScheme "https://w3id.org/xapi/scorm/v1.0"
                   :prefLabel {"en" "SCORM Activity Profile"}
                   :definition
                   {"en" "Used to store document data associated with the activity and not intended to capture learning experience data in the form of a statement. The SCORM Activity Profile Object contains the profile data for the specified Activity."}
                   :contentType "application/json"
                   :schema
                   "https://w3id.org/xapi/scorm/activity-profile/scorm.profile.activity.profile.schema"}))))
