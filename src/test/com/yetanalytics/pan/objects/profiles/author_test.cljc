(ns com.yetanalytics.pan.objects.profiles.author-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.objects.profiles.author :as author]))

(deftest type-test
  (testing "type property"
    (is (s/valid? ::author/type "Organization"))
    (is (s/valid? ::author/type "Person"))
    (is (not (s/valid? ::author/type "Foo Bar")))
    (is (not (s/valid? ::author/type "Profile")))))

(deftest name-test
  (testing "name property"
    (is (s/valid? ::author/name "Yet Analytics"))
    (is (s/valid? ::author/name "supercalifragilisticexialidocious"))
    (is (not (s/valid? ::author/name 74)))))

(deftest url-test
  (testing "url property"
    (is (s/valid? ::author/url "https://www.yetanalytics.io"))
    (is (s/valid? ::author/url "https:///www.yetanalytics.io"))
    (is (not (s/valid? ::author/url "https://^www.yetanalytics.io")))))

(deftest author-test
  (testing "author object"
    (is (s/valid? ::author/author {:url "https://www.yetanalytics.io"
                                   :type "Organization"
                                   :name "Yet Analytics"}))))
