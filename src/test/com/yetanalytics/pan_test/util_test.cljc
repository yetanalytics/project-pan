(ns com.yetanalytics.pan-test.util_test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            ;; Not to be confused with test-utils in test dir
            [com.yetanalytics.pan.utils.spec :as u]))

;; (deftest read-resource-test
;;   (testing "testing that read-resource returns strings on valid files"
;;     (is (string? (u/read-resource "media_types.edn")))
;;     (is (string? (u/read-resource "context/activity-context.json")))
;;     (is (string? (u/read-resource "json/schema-07.json")))
;;     (is (string? (u/read-resource "sample_profiles/will-profile.json")))))

;; TODO Normalize profile test

;; related MUST only be used on deprecated concepts
(deftest related-only-deprecated-test
  (testing "testing related-only-deprecated on Verbs"
    (is (s/valid? ::u/related-only-deprecated
                  {:id "https://foo.org/verb"
                   :type "Verb"
                   :deprecated true
                   :related ["https://foo.org/other-verb"]}))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :deprecated false
                        :related ["https://foo.org/other-verb"]})))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :related ["https://foo.org/other-verb"]}))))
  (testing "testing related-only-deprecated on ActivityTypes"
    (is (s/valid? ::u/related-only-deprecated
                  {:id "https://foo.org/activity-type"
                   :type "ActivityType"
                   :deprecated true
                   :related ["https://foo.org/other-activity-type"]}))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :deprecated false
                        :related ["https://foo.org/other-activity-type"]})))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :related ["https://foo.org/other-activity-type"]}))))
  (testing "testing related-only-deprecated on AttachmentUsageTypes"
    (is (s/valid? ::u/related-only-deprecated
                  {:id "https://foo.org/attachment-usage-type"
                   :type "AttachmentUsageType"
                   :deprecated true
                   :related ["https://foo.org/other-attachment-usage-type"]}))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :deprecated false
                        :related ["https://foo.org/other-attachment-usage-type"]})))
    (is (not (s/valid? ::u/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :related ["https://foo.org/other-attachment-usage-type"]})))))
