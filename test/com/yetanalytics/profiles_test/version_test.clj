(ns com.yetanalytics.profiles-test.version-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.profiles.versions :as versions]))

(deftest id-test
  (testing "version ID"
    (is (s/valid? ::versions/id "https://w3id.org/xapi/catch/v1"))
    (is (not (s/valid? ::versions/id "what the pineapple")))))

(deftest was-revision-of-test
  (testing "wasRevisionOf property"
    (is (s/valid? ::versions/was-revision-of
                  ["https://w3id.org/xapi/catch/v1"]))
    (is (not (s/valid? ::versions/was-revision-of
                       ["https://w3id.org/xapi/catch/v1"
                        "foo bar"])))
    (is (not (s/valid? ::versions/was-revision-of [])))))

(deftest gen-at-time-test
  (testing "genAtTime property"
    (is (s/valid? ::versions/gen-at-time "2017-12-22T22:30:00-07:00"))
    (is (not (s/valid? ::versions/gen-at-time 2017)))))

(deftest versions-test
  (testing "versions object"
    (is (s/valid? ::versions/versions
                  [{:id "https://w3id.org/xapi/catch/v1"
                    :gen-at-time "2017-12-22T22:30:00-07:00"}]))
    (is (s/valid? ::versions/versions
                  [{:id "https://w3id.org/xapi/catch/v2"
                    :was-revision-of ["https://w3id.org/xapi/catch/v1"]
                    :gen-at-time "2017-12-22T22:30:00-07:00"}]))
    (is (not (s/valid? ::versions/versions [])))
    (is (not (s/valid? ::versions/versions
                       [{:id "https://w3id.org/xapi/catch/v2"
                         :was-revision-of []
                         :gen-at-time "2017-12-22T22:30:00-07:00"}])))))

(run-tests)
