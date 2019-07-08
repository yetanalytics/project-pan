(ns com.yetanalytics.objects-test.concepts-test.verb-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.verbs :as verbs]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::verbs/type
                     "Verb"
                     :bad
                     "ActivityType"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::verbs/broader
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/broader
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::verbs/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::verbs/broad-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/broad-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/broad-match [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::verbs/narrower
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/narrower
                        "https://w3id.org/xapi/catch/verbs/submitted")
    (should-not-satisfy ::verbs/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::verbs/narrow-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/narrow-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/narrow-match [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::verbs/related
                    ["https://w3id.org/xapi/catch/verbs/submitted"
                     "https://w3id.org/xapi/catch/verbs/provided"])
    (should-not-satisfy ::verbs/related
                        "https://w3id.org/xapi/catch/verbs/provided")
    (should-not-satisfy ::verbs/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::verbs/related-match
                    ["http://adlnet.gov/expapi/verbs/shared"])
    (should-not-satisfy ::verbs/related-match
                        "http://adlnet.gov/expapi/verbs/shared")
    (should-not-satisfy ::verbs/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::verbs/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::verbs/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::verbs/exact-match [])))

(deftest related-only-deprecated-test
  (testing "related MUST only be used on deprecated concepts"
    (is (s/valid? ::verbs/related-only-deprecated
                  {:id "https://foo.org/verb"
                   :type "Verb"
                   :deprecated true
                   :related ["https://foo.org/other-verb"]}))
    (is (not (s/valid? ::verbs/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :deprecated false
                        :related ["https://foo.org/other-verb"]})))
    (is (not (s/valid? ::verbs/related-only-deprecated
                       {:id "https://foo.org/verb"
                        :type "Verb"
                        :related ["https://foo.org/other-verb"]})))))

(deftest verb-test
  (testing "Verb concept"
    (is (s/valid? ::verbs/verb
                  {:id "https://w3id.org/xapi/catch/verbs/presented"
                   :type "Verb"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "presented"}
                   :definition {"en" "leading a discussion at an advocacy event"}}))))

;; Testing in-profile conceptual relationships

; (def concept-map
;   {"https://w3id.org/xapi/catch/verbs/provided"
;    {:id "https://w3id.org/xapi/catch/verbs/provided"
;     :type "Verb"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "provided"}
;     :definition {"en" "supplying a link to an online resource"}}
;    "https://w3id.org/xapi/catch/verbs/submitted"
;    {:id "https://w3id.org/xapi/catch/verbs/submitted"
;     :type "Verb"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "submitted"}
;     :definition {"en" "The actor has clicked the submit or save button within the CATCH application"}}
;    "https://w3id.org/xapi/catch/verbs/uploaded"
;    {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;     :type "Verb"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "uploaded"}
;     :definition {"en" "Uploading a resource from a local file system"}
;     :broader ["https://w3id.org/xapi/catch/verbs/submitted"
;               "https://w3id.org/xapi/catch/verbs/provided"]}
;    "https://w3id.org/xapi/catch/verbs/uploaded-future"
;    {:id "https://w3id.org/xapi/catch/verbs/uploaded-future"
;     :type "Verb"
;     :in-scheme "https://w3id.org/xapi/catch/v2"
;     :pref-label {"en" "uploaded"}
;     :definition {"en" "Uploading a resource from a futuristic file system"}}
;    "https://w3id.org/xapi/catch/activitytypes/check-in"
;    {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
;     :type "ActivityType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Check in"}
;     :definition {"en" "An activity in which the learner reports progression."}}
;    "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;     :type "AttachmentUsageType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Parent Survey"}
;     :definition {"en" "A survey provided to the parent(s) of a DL student"}}})

; (deftest broader-concept-iris-test
;   (testing "Test that the verb correctly relates to broader concepts"
;     (is (s/valid? ::verbs/broader-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}
;                             :broader ["https://w3id.org/xapi/catch/verbs/submitted"
;                                       "https://w3id.org/xapi/catch/verbs/provided"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a broader property should not be penalized
;     (is (s/valid? ::verbs/broader-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a broader concept that's not in the profile
;     (is (not (s/valid? ::verbs/broader-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :broader ["https://w3id.org/xapi/catch/verbs/bubbled"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::verbs/broader-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :broader ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::verbs/broader-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :broader ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::verbs/broader-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :broader ["https://w3id.org/xapi/catch/verbs/uploaded-future"]}
;                         :concepts-table concept-map})))))

; (deftest narrower-concept-iris-test
;   (testing "Test that the verb correctly relates to narrower concepts"
;     (is (s/valid? ::verbs/narrower-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}
;                             :narrower ["https://w3id.org/xapi/catch/verbs/submitted"
;                                        "https://w3id.org/xapi/catch/verbs/provided"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a narrower property should not be penalized
;     (is (s/valid? ::verbs/narrower-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a narrower concept that's not in the profile
;     (is (not (s/valid? ::verbs/narrower-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :narrower ["https://w3id.org/xapi/catch/verbs/bubbled"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::verbs/narrower-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :narrower ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::verbs/narrower-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :narrower ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::verbs/narrower-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :narrower ["https://w3id.org/xapi/catch/verbs/uploaded-future"]}
;                         :concepts-table concept-map})))))

; (deftest related-concept-iris-test
;   (testing "Test that the verb correctly relates to related concepts"
;     (is (s/valid? ::verbs/related-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}
;                             :related ["https://w3id.org/xapi/catch/verbs/submitted"
;                                       "https://w3id.org/xapi/catch/verbs/provided"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a related property should not be penalized
;     (is (s/valid? ::verbs/related-concept-iris
;                   {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                             :type "Verb"
;                             :in-scheme "https://w3id.org/xapi/catch/v1"
;                             :pref-label {"en" "uploaded"}
;                             :definition {"en" "Uploading a resource from a local file system"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a related concept that's not in the profile
;     (is (not (s/valid? ::verbs/related-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :related ["https://w3id.org/xapi/catch/verbs/bubbled"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::verbs/related-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :related ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::verbs/related-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :related ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::verbs/related-concept-iris
;                        {:object {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                                  :type "Verb"
;                                  :in-scheme "https://w3id.org/xapi/catch/v1"
;                                  :pref-label {"en" "uploaded"}
;                                  :definition {"en" "Uploading a resource from a local file system"}
;                                  :related ["https://w3id.org/xapi/catch/verbs/uploaded-future"]}
;                         :concepts-table concept-map})))))
