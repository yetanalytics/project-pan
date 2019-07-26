(ns com.yetanalytics.profile-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.profiles.versions :as versions]))

(deftest id-test
  (testing "profile ID"
    (should-satisfy+ ::profile/id
                     "https://w3id.org/xapi/catch/"
                     :bad
                     "https:///w3id.org/xapi/catch/"
                     "what the pineapple")))

(deftest context-test
  (testing "context property"
    (is (s/valid? ::profile/context "https://w3id.org/xapi/profiles/context"))
    (is (s/valid? ::profile/context ["https://w3id.org/xapi/profiles/context"]))
    (is (s/valid? ::profile/context ["https://w3id.org/xapi/profiles/context"
                                     "https://w3id.org/xapi/some/other/context"]))
    (is (not (s/valid? ::profile/context ["https://w3id.org/incorrect/context"])))
    (is (not (s/valid? ::profile/context [])))))

(deftest type-test
  (testing "type property"
    (is (s/valid? ::profile/type "Profile"))
    (is (not (s/valid? ::profile/type "Foo Bar")))
    (is (not (s/valid? ::profile/type "Activity")))))

(deftest conforms-to-test
  (testing "conformsTo property"
    (is (s/valid? ::profile/conformsTo "https://w3id.org/xapi/profiles#1.0"))))

(deftest prefLabel-test
  (testing "prefLabel property"
    (is (s/valid? ::profile/prefLabel {"en" "Catch"}))
    (is (s/valid? ::profile/prefLabel {"en" ""}))
    (is (s/valid? ::profile/prefLabel {:en "Catch"}))))

(deftest definition-test
  (testing "definition property"
    (is (s/valid? ::profile/definition
                  {"en" "A learning path within the EPISD Dual Language
                        Competency Framework"}))
    (is (s/valid? ::profile/definition
                  {"zh-guoyu" "双语能力框架学习计划"}))))

(deftest see-also-test
  (testing "seeAlso property"
    (is (s/valid? ::profile/seeAlso "https://see.also.org/"))))

(deftest profile-test
  (testing "top-level profile properties"
    (is (s/valid? ::profile/profile
                  {:versions
                   [{:id "https://w3id.org/xapi/catch/v1"
                     :generatedAtTime "2017-12-22T22:30:00-07:00"}]
                   :context "https://w3id.org/xapi/profiles/context"
                   :author {:url "https://www.yetanalytics.io"
                            :type "Organization"
                            :name "Yet Analytics"}
                   :type "Profile"
                   :id "https://w3id.org/xapi/catch"
                   :definition {"en" "The profile for the trinity education application CATCH"}
                   :conformsTo "https://w3id.org/xapi/profiles#1.0"
                   :prefLabel {"en" "Catch"}}))))

;; Test IDs

(deftest validate-ids-test
  (testing "profile ID MUST be distinct from version IDs"
    (is (nil? (profile/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :versions [{:id "https://w3id.org/xapi/catch/v2"}
                           {:id "https://w3id.org/xapi/catch/v1"}]})))
    (is (some? (profile/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/xapi/catch"}
                            {:id "https://w3id.org/xapi/catch/v1"}]}))))
  (testing "Every Profile version ID MUST be distinct"
    (is (some? (profile/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/xapi/catch/v1"}
                            {:id "https://w3id.org/xapi/catch/v1"}]}))))
  (testing "Concept IDs MUST be distinct"
    (is (nil? (profile/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :concepts [{:id "https://w3id.org/xapi/catch/verb#1"
                            :type "Verb"}
                           {:id "https://w3id.org/xapi/catch/verb#2"
                            :type "Verb"}]})))
    (is (some? (profile/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :concepts [{:id "https://w3id.org/xapi/catch/verb#duplicate"
                             :type "Verb"}
                            {:id "https://w3id.org/xapi/catch/verb#duplicate"
                             :type "Verb"}]}))))
  (testing "Statement Template IDs MUST be distinct"
    (is (nil? (profile/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :templates [{:id "https://w3id.org/xapi/catch/template#1"
                             :type "StatementTemplate"}
                            {:id "https://w3id.org/xapi/catch/template#2"
                             :type "StatementTemplate"}]})))
    (is (some? (profile/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :templates [{:id "https://w3id.org/xapi/catch/template#dup"
                              :type "StatementTemplate"}
                             {:id "https://w3id.org/xapi/catch/template#dup"
                              :type "StatementTemplate"}]}))))
  (testing "Pattern IDs MUST be distinct"
    (is (nil? (profile/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :patterns [{:id "https://w3id.org/xapi/catch/pattern#1"
                            :type "Pattern"}
                           {:id "https://w3id.org/xapi/catch/pattern#2"
                            :type "Pattern"}]})))
    (is (some? (profile/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :patterns [{:id "https://w3id.org/xapi/catch/pattern#dup"
                             :type "Pattern"}
                            {:id "https://w3id.org/xapi/catch/pattern#dup"
                             :type "Pattern"}]})))))

(deftest in-scheme-test
  (testing "object inScheme MUST be a valid Profile version"
    (is (empty? (profile/validate-in-schemes
                 {:versions [{:id "https://w3id.org/catch/v1"}
                             {:id "https://w3id.org/catch/v2"}]
                  :concepts [{:id "https://w3id.org/catch/some-verb"
                              :inScheme "https://w3id.org/catch/v1"}]
                  :templates [{:id "https://w3id.org/catch/some-template"
                               :inScheme "https://w3id.org/catch/v1"}]
                  :patterns [{:id "https://w3id.org/catch/some-pattern"
                              :inScheme "https://w3id.org/catch/v2"}]})))
    (is (some? (profile/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :concepts [{:id "https://w3id.org/catch/some-verb"
                             :inScheme "https://w3id.org/catch/v3"}]
                 :templates [] :patterns []})))
    (is (some? (profile/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :templates [{:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v3"}]
                 :concepts [] :patterns []})))
    (is (some? (profile/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :patterns [{:id "https://w3id.org/catch/some-pattern"
                             :inScheme "https://w3id.org/catch/v3"}]
                 :concepts [] :templates []})))))

(def will-profile
  (util/convert-json (slurp "resources/sample_profiles/will-profile.json") ""))

#_(deftest profile-integration-test
    (testing "performing intergration testing using Will's CATCH profile"
      (is (nil? (profile/validate will-profile)))
      (is (empty? (profile/validate-in-schemes will-profile)))
      (is (empty? (profile/validate-context will-profile)))))
