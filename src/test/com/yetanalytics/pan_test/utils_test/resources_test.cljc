(ns com.yetanalytics.pan-test.utils-test.resources-test
  (:require [clojure.test :refer [deftest is testing]])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-resource
                             read-edn-resource
                             read-json-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-resource
                                     read-edn-resource
                                     read-json-resource]])))

;; Need to assign macros to defs to avoid null pointer errors

(def res-1 (read-resource "media_types.edn"))
(def res-2 (read-resource "context/activity-context.json"))
(def res-3 (read-resource "json/schema-07.json"))
(def res-4 (read-resource "sample_profiles/catch.json"))

(deftest read-resource-testy
  (testing "testing that read-resource returns strings"
    (is (string? res-1))
    (is (string? res-2))
    (is (string? res-3))
    (is (string? res-4))))

(def res-5 (read-edn-resource "media_types.edn"))

(deftest read-edn-resource-test
  (testing "read-edn-resource function"
    (is (associative? res-5))))

(def res-6 (read-json-resource "json/schema-07.json"))
(def res-7 (read-json-resource "context/activity-context.json" "at/"))
(def res-8 (read-json-resource "sample_profiles/catch.json" "_"))

(deftest read-json-resource-test
  (testing "read-json-resource function"
    (is (associative? res-6))
    (is (associative? res-7))
    (is (associative? res-8))))
