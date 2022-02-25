(ns com.yetanalytics.pan.utils.spec-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            ;; Not to be confused with test-utils in test dir
            [com.yetanalytics.pan.utils.spec :as u]
            [com.yetanalytics.pan.objects.concepts.util :as cu]))

(deftest subvec?-test
  (testing "subvec? predicate: v1 should be a subvector of v2"
    (is (u/subvec? [1 2 3] [1 2 3 4]))
    (is (u/subvec? [1 2 3 4] [1 2 3 4]))
    (is (u/subvec? [] [{:foo 1 :bar 2}]))
    (is (not (u/subvec? [1 2 3 4] [1 2 3])))
    (is (not (u/subvec? [1 3 5] [1 2 3 4])))))

;; TODO Normalize profile test

;; related MUST only be used on deprecated concepts
(deftest related-only-deprecated-test
  (testing "testing related-only-deprecated on Verbs"
    (is (s/valid? ::cu/related-only-deprecated
                  {:id "https://foo.org/verb"
                   :type "Verb"
                   :deprecated true
                   :related ["https://foo.org/other-verb"]}))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :deprecated false
                        :related ["https://foo.org/other-verb"]})))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :related ["https://foo.org/other-verb"]}))))
  (testing "testing related-only-deprecated on ActivityTypes"
    (is (s/valid? ::cu/related-only-deprecated
                  {:id "https://foo.org/activity-type"
                   :type "ActivityType"
                   :deprecated true
                   :related ["https://foo.org/other-activity-type"]}))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :deprecated false
                        :related ["https://foo.org/other-activity-type"]})))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/activity-type"
                        :type "ActivityType"
                        :related ["https://foo.org/other-activity-type"]}))))
  (testing "testing related-only-deprecated on AttachmentUsageTypes"
    (is (s/valid? ::cu/related-only-deprecated
                  {:id "https://foo.org/attachment-usage-type"
                   :type "AttachmentUsageType"
                   :deprecated true
                   :related ["https://foo.org/other-attachment-usage-type"]}))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :deprecated false
                        :related ["https://foo.org/other-attachment-usage-type"]})))
    (is (not (s/valid? ::cu/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :related ["https://foo.org/other-attachment-usage-type"]})))))
