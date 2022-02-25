(ns com.yetanalytics.pan.objects.profiles.version-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.objects.profiles.version :as version]))

(deftest id-test
  (testing "version ID"
    (is (s/valid? ::version/id "https://w3id.org/xapi/catch/v1"))
    (is (not (s/valid? ::version/id "what the pineapple")))))

(deftest was-revision-of-test
  (testing "wasRevisionOf property"
    (is (s/valid? ::version/wasRevisionOf
                  ["https://w3id.org/xapi/catch/v1"]))
    (is (not (s/valid? ::version/wasRevisionOf
                       ["https://w3id.org/xapi/catch/v1"
                        "foo bar"])))
    (is (not (s/valid? ::version/wasRevisionOf [])))))

(deftest generated-at-time-test
  (testing "genAtTime property"
    (is (s/valid? ::version/generatedAtTime "2017-12-22T22:30:00-07:00"))
    (is (not (s/valid? ::version/generatedAtTime 2017)))))

(deftest versions-test
  (testing "versions object"
    (is (s/valid? ::version/versions
                  [{:id "https://w3id.org/xapi/catch/v1"
                    :generatedAtTime "2017-12-22T22:30:00-07:00"}]))
    (is (s/valid? ::version/versions
                  [{:id "https://w3id.org/xapi/catch/v2"
                    :wasRevisionOf ["https://w3id.org/xapi/catch/v1"]
                    :generatedAtTime "2017-12-22T22:30:00-07:00"}]))
    (is (s/valid? ::version/versions
                  [{:id "http://example.com/profiles/superheroes/v3"
                    :wasRevisionOf ["http://example.com/profiles/superheroes/v2"]
                    :generatedAtTime "2020-02-20T20:20:20Z"}
                   {:id "http://example.com/profiles/superheroes/v2"
                    :wasRevisionOf ["http://example.com/profiles/superheroes/v1"]
                    :generatedAtTime "2010-01-15T03:14:15Z"}
                   {:id "http://example.com/profiles/superheroes/v1"
                    :generatedAtTime "2010-01-14T12:13:14Z"}]))
    (is (not (s/valid? ::version/versions [])))
    (is (not (s/valid? ::version/versions
                       [{:id "https://w3id.org/xapi/catch/v2"
                         :wasRevisionOf []
                         :generatedAtTime "2017-12-22T22:30:00-07:00"}])))))
