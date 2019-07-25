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

(deftest id-distinct-test
  (testing "profile ID MUST be distinct from version IDs"
    (is (s/valid? ::profile/id-distinct
                  {:id "https://w3id.org/xapi/catch"
                   :versions
                   [{:id "https://w3id.org/xapi/catch/v2"
                     :generatedAtTime "2017-12-22T22:30:00-07:00"}
                    {:id "https://w3id.org/xapi/catch/v1"
                     :generatedAtTime "2017-12-22T22:30:00-07:00"}]}))
    (is (not (s/valid? ::profile/id-distinct
                       {:id "https://w3id.org/xapi/catch"
                        :versions
                        [{:id "https://w3id.org/xapi/catch"
                          :generatedAtTime "2017-12-22T22:30:00-07:00"}
                         {:id "https://w3id.org/xapi/catch/v1"
                          :generatedAtTime "2017-12-22T22:30:00-07:00"}]})))))

(deftest in-scheme-test
  (testing "object inScheme MUST be a valid Profile version"
    (is (s/valid? ::profile/valid-in-schemes
                  [{:object {:inScheme "https://foo.org/v1"}
                    :vid-set #{"https://foo.org/v1" "https://foo.org/v2"}}
                   {:object {:inScheme "https://foo.org/v2"}
                    :vid-set #{"https://foo.org/v1" "https://foo.org/v2"}}]))
    (is (not (s/valid? ::profile/valid-in-schemes
                       [{:object {:inScheme "https://foo.org/v0"}
                         :vid-set #{"https://foo.org/v1" "https://foo.org/v2"}}])))))

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

(def will-profile
  (util/convert-json (slurp "resources/sample_profiles/will-profile.json") ""))

(deftest profile-integration-test
  (testing "performing intergration testing using Will's CATCH profile"
    (is (nil? (profile/validate will-profile)))
    (is (empty? (profile/validate-in-schemes will-profile)))
    (is (empty? (profile/validate-context will-profile)))))
