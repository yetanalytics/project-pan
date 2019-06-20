(ns com.yetanalytics.profile-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.object :as object]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.utils :refer :all]))

; (deftest id-test
;   (testing "id property"
;     (is (s/valid? ::o/id "https://w3id.org/xapi/catch")) ; "2017-12-22T22:30:00-07:00"
;     (is (not (s/valid? ::o/id "Not a id")))))

(deftest context-test
  (testing "context property"
    (is (s/valid? ::profile/context "https://w3id.org/xapi/profiles/context"))
    (is (s/valid? ::profile/context ["https://w3id.org/xapi/profiles/context"]))
    (is (s/valid? ::profile/context ["https://w3id.org/xapi/profiles/context"
                                     "https://w3id.org/xapi/some/other/context"]))
    (is (not (s/valid? ::profile/context ["https://w3id.org/incorrect/context"])))
    (is (not (s/valid? ::profile/context [])))))

; (deftest type-test
;   (testing "type property"
;     (is (s/valid? ::p/type "Profile"))
;     (is (not (s/valid? ::p/type "Foo Bar")))))


(deftest conforms-to-test
  (testing "conformsTo property"
    (is (s/valid? ::profile/conforms-to "https://w3id.org/xapi/profiles#1.0"))))

; (deftest pref-label-test
;   (testing "prefLabel property"
;     (is (s/valid? ::p/pref-label {"en" "Catch"}))))

; (deftest definition-test
;   (testing "definition property"
;     (is (s/valid? ::p/definition {"en"
;                                   "The profile for the trinity education application CATCH"}))))

(deftest see-also-test
  (testing "seeAlso property"
    (is (s/valid? ::profile/see-also "https://see.also.org/"))))

(deftest versions-test
  (testing "versions property"
    (is (s/valid? :version/gen-at-time "2017-12-22T22:30:00-07:00"))
    (is (s/valid? ::profile/versions
                  [{:id "https://w3id.org/xapi/catch/v1"
                    :gen-at-time "2017-12-22T22:30:00-07:00"}]))
    (is (s/valid? ::profile/versions
                  [{:id "https://w3id.org/xapi/catch/v2"
                    :was-revision-of ["https://w3id.org/xapi/catch/v1"]
                    :gen-at-time "2017-12-22T22:30:00-07:00"}]))
    (is (not (s/valid? ::profile/versions [])))
    (is (not (s/valid? ::profile/versions
                       [{:id "https://w3id.org/xapi/catch/v2"
                         :was-revision-of []
                         :gen-at-time "2017-12-22T22:30:00-07:00"}])))))

(s/explain ::object/object {:url "https://www.yetanalytics.io"
                            :type "Organization"
                            :name "Yet Analytics"})

(::object/type {:type "Profile"})
(object/object? {::object/type "Profile"})
(object/foo "hello")

(s/explain ::object/object {:type "Profile"})

(deftest author-test
  (testing "author property"
    ; (is (s/valid? :object/type "Organization"))
    ; (is (s/valid? :object/type "Person"))
    ; (is (not (s/valid? :author/type "Foo Bar")))
    ; (is (not (s/valid? :author/type "Profile")))
    (is (s/valid? :author/name "Yet Analytics"))
    (is (s/valid? :author/url "https://www.yetanalytics.io"))
    (is (not (s/valid? :author/url "https:///www.yetanalytics.io")))
    (is (s/explain ::object/object {:url "https://www.yetanalytics.io"
                                    :type "Organization"
                                    :name "Yet Analytics"}))
    ; (is (s/valid? ::p/author {:author/url "https://www.yetanalytics.io"
    ;                           :author/type "Organization"
    ;                           :author/name "Yet Analytics"}))
))

(deftest profile-test
  (testing "top-level profile properties"
    (is (s/valid? ::p/profile {::p/versions [{:version/id "https://w3id.org/xapi/catch/v1"
                                              :version/gen-at-time "2017-12-22T22:30:00-07:00"}]
                               ::p/context "https://w3id.org/xapi/profiles/context"
                               ::p/author {:author/url "https://www.yetanalytics.io"
                                           :author/type "Organization"
                                           :author/name "Yet Analytics"}
                               ::p/type "Profile"
                               ::p/id "https://w3id.org/xapi/catch"
                               ::p/definition {"en" "The profile for the trinity education application CATCH"}
                               ::p/conforms-to "https://w3id.org/xapi/profiles#1.0"
                               ::p/pref-label {"en" "Catch"}}))))

(run-tests)
