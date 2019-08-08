(ns com.yetanalytics.util_test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            ;; Not to be confused with util.clj in src
            [com.yetanalytics.test-utils.clj :refer :all]
            [com.yetanalytics.util :as u]))

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
