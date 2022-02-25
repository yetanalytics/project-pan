(ns com.yetanalytics.pan.objects.concepts.activity-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.concepts.activity :as a]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::a/type
                     "Activity"
                     :bad
                     "Profile"
                     "Verb"
                     "StanLoona")))

(deftest context-test
  (testing "@context property"
    (should-satisfy ::a/_context
                    "https://w3id.org/xapi/profiles/activity-context")
    (should-satisfy ::a/_context "https://some-other-context")
    (should-satisfy ::a/_context
                    ["https://w3id.org/xapi/profiles/activity-context"])
    (should-not-satisfy ::a/_context ["https://some-other-context"])
    (should-not-satisfy ::a/_context "foo bar")))

(deftest stringify-lang-keys-test
  (testing "turning keys in the name and description language maps into strings"
    (is (= {:_context "https://w3id.org/xapi/profiles/activity-context"
            :name {"en" "Blah"}
            :description {"en" "Blah Blah Blah"}
            :type "https://w3id.org/xapi/catch/activitytypes/blah"}
           (a/stringify-submaps
            {:_context "https://w3id.org/xapi/profiles/activity-context"
             :name {:en "Blah"}
             :description {:en "Blah Blah Blah"}
             :type "https://w3id.org/xapi/catch/activitytypes/blah"})))))

(deftest activity-definition
  (testing "activityDefinition property"
    (is (s/valid? ::a/activityDefinition
                  {:_context "https://w3id.org/xapi/profiles/activity-context"
                   :name {:en "Cross Linguistic Connections"}
                   :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                   :type "https://w3id.org/xapi/catch/activitytypes/competency"}))
    (testing "with extension object"
      (is (s/valid? ::a/activityDefinition
                    {:_context "https://w3id.org/xapi/profiles/activity-context"
                     :name {:en "Cross Linguistic Connections"}
                     :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                     :type "https://w3id.org/xapi/catch/activitytypes/competency"
                     :extensions {"http://foo.org/extension-1"
                                  {:_context {:foo "http://foo.org/"
                                              :display {:_id        "foo:display"
                                                        :_container "@language"}}
                                   :display {:en-US "My Extension"}}
                                  "http://foo.org/extension-2" 2}}))
      (is (not (s/valid? ::a/activityDefinition
                         {:_context "https://w3id.org/xapi/profiles/activity-context"
                          :name {:en "Cross Linguistic Connections"}
                          :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                          :type "https://w3id.org/xapi/catch/activitytypes/competency"
                          :extensions {"http://foo.org/extension-1"
                                       {:display {:en-US "My Extension"}}
                                       "http://foo.org/extension-2" 2}}))))))

(deftest activity-test
  (testing "Activity concept"
    (is (s/valid? ::a/activity
                  {:id "https://w3id.org/xapi/catch/activities/competency/cross-linguistic-connections"
                   :type "Activity"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :activityDefinition
                   {:_context "https://w3id.org/xapi/profiles/activity-context"
                    :name {:en "Cross Linguistic Connections"}
                    :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                    :type "https://w3id.org/xapi/catch/activitytypes/competency"}}))))
