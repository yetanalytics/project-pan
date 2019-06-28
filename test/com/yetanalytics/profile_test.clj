(ns com.yetanalytics.profile-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
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
    (is (s/valid? ::profile/conforms-to "https://w3id.org/xapi/profiles#1.0"))))

(deftest pref-label-test
  (testing "prefLabel property"
    (is (s/valid? ::profile/pref-label {"en" "Catch"}))
    (is (s/valid? ::profile/pref-label {"en" ""}))
    (is (s/valid? ::profile/pref-label {:en "Catch"}))))

(deftest definition-test
  (testing "definition property"
    (is (s/valid? ::profile/definition
                  {"en" "A learning path within the EPISD Dual Language
                        Competency Framework"}))
    (is (s/valid? ::profile/definition
                  {"zh-guoyu" "双语能力框架学习计划"}))))

(deftest see-also-test
  (testing "seeAlso property"
    (is (s/valid? ::profile/see-also "https://see.also.org/"))))

; (deftest versions-test
;   (testing "versions property"
;     (is (s/valid? ::profile/versions/versions
;                   [{:id "https://w3id.org/xapi/catch/v1"
;                     :gen-at-time "2017-12-22T22:30:00-07:00"}]))
;     (is (s/valid? ::profile/versions
;                   [{:id "https://w3id.org/xapi/catch/v2"
;                     :was-revision-of ["https://w3id.org/xapi/catch/v1"]
;                     :gen-at-time "2017-12-22T22:30:00-07:00"}]))
;     (is (not (s/valid? ::profile/versions [])))
;     (is (not (s/valid? ::profile/versions
;                        [{:id "https://w3id.org/xapi/catch/v2"
;                          :was-revision-of []
;                          :gen-at-time "2017-12-22T22:30:00-07:00"}])))))

; (deftest author-test
;   (testing "author property"
;     (is (s/valid? :author/type "Organization"))
;     (is (s/valid? :author/type "Person"))
;     (is (not (s/valid? :author/type "Foo Bar")))
;     (is (not (s/valid? :author/type "Profile")))
;     (is (s/valid? :author/name "Yet Analytics"))
;     (is (s/valid? :author/url "https://www.yetanalytics.io"))
;     (is (not (s/valid? :author/url "https:///www.yetanalytics.io")))
;     (is (s/valid? ::profile/author {:url "https://www.yetanalytics.io"
;                                     :type "Organization"
;                                     :name "Yet Analytics"}))))

(deftest profile-test
  (testing "top-level profile properties"
    (is (s/valid? ::profile/profile
                  {:versions
                   [{:id "https://w3id.org/xapi/catch/v1"
                     :gen-at-time "2017-12-22T22:30:00-07:00"}]
                   :context "https://w3id.org/xapi/profiles/context"
                   :author {:url "https://www.yetanalytics.io"
                            :type "Organization"
                            :name "Yet Analytics"}
                   :type "Profile"
                   :id "https://w3id.org/xapi/catch"
                   :definition {"en" "The profile for the trinity
                                              education application CATCH"}
                   :conforms-to "https://w3id.org/xapi/profiles#1.0"
                   :pref-label {"en" "Catch"}}))))