(ns com.yetanalytics.objects-test.concepts-test.attachment-usage-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.attachment-usage-types
             :as attachment-usage-types]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::attachment-usage-types/type
                     "AttachmentUsageType"
                     :bad
                     "ActivityType"
                     "Verb"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::attachment-usage-types/broader
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/broader
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::attachment-usage-types/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::attachment-usage-types/broad-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/broad-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/broad-match [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::attachment-usage-types/narrower
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/narrower
                        "https://w3id.org/xapi/catch/attachment-usage-types/submitted")
    (should-not-satisfy ::attachment-usage-types/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::attachment-usage-types/narrow-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/narrow-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/narrow-match [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::attachment-usage-types/related
                    ["https://w3id.org/xapi/catch/attachment-usage-types/submitted"
                     "https://w3id.org/xapi/catch/attachment-usage-types/provided"])
    (should-not-satisfy ::attachment-usage-types/related
                        "https://w3id.org/xapi/catch/attachment-usage-types/provided")
    (should-not-satisfy ::attachment-usage-types/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::attachment-usage-types/related-match
                    ["http://adlnet.gov/expapi/attachment-usage-types/shared"])
    (should-not-satisfy ::attachment-usage-types/related-match
                        "http://adlnet.gov/expapi/attachment-usage-types/shared")
    (should-not-satisfy ::attachment-usage-types/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::attachment-usage-types/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::attachment-usage-types/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::attachment-usage-types/exact-match [])))

(deftest related-only-deprecated-test
  (testing "related MUST only be used on deprecated concepts"
    (is (s/valid? ::attachment-usage-types/related-only-deprecated
                  {:id "https://foo.org/attachment-usage-type"
                   :type "AttachmentUsageType"
                   :deprecated true
                   :related ["https://foo.org/other-attachment-usage-type"]}))
    (is (not (s/valid? ::attachment-usage-types/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :deprecated false
                        :related ["https://foo.org/other-attachment-usage-type"]})))
    (is (not (s/valid? ::attachment-usage-types/related-only-deprecated
                       {:id "https://foo.org/attachment-usage-type"
                        :type "AttachmentUsageType"
                        :related ["https://foo.org/other-attachment-usage-type"]})))))

(deftest attachment-usage-types-test
  (testing "attachmentUsageType concept"
    (is (s/valid? ::attachment-usage-types/attachment-usage-type
                  {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
                   :type "AttachmentUsageType"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Supporting documents"}
                   :definition {"en" "Documents which provide aditional 
                                     information about the lesson plan. Can be
                                     instructions for lesson plan execution
                                     demonstrating implementation or any other 
                                     documents related to the lesson plan"}}))))

; (def concept-map
;   {"https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;     :type "AttachmentUsageType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Parent Survey"}
;     :definition {"en" "A survey provided to the parent(s) of a DL student"}}
;    "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;     :type "AttachmentUsageType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Parent Survey Result"}
;     :definition {"en" "The results of the survey provided to the parent(s) of a DL student"}}
;    "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
;    {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
;     :type "AttachmentUsageType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Supporting documents"}
;     :definition {"en" "Documents which provide aditional information about the lesson plan. Can be instructions for lesson plan execution demonstrating implementation or any other documents related to the lesson plan"}}
;    "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents-futuristic"
;    {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents-futuristic"
;     :type "AttachmentUsageType"
;     :in-scheme "https://w3id.org/xapi/catch/v2"
;     :pref-label {"en" "Supporting documents"}
;     :definition {"en" "Futuristic documents which provide aditional information about the lesson plan."}}
;    "https://w3id.org/xapi/catch/activitytypes/check-in"
;    {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
;     :type "ActivityType"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "Check in"}
;     :definition {"en" "An activity in which the learner reports progression."}}
;    "https://w3id.org/xapi/catch/verbs/provided"
;    {:id "https://w3id.org/xapi/catch/verbs/provided"
;     :type "Verb"
;     :in-scheme "https://w3id.org/xapi/catch/v1"
;     :pref-label {"en" "provided"}
;     :definition {"en" "supplying a link to an online resource"}}})

; (deftest broader-concept-iris-test
;   (testing "Test that the verb correctly relates to broader concepts"
;     (is (s/valid? ::attachment-usage-types/broader-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                     :broader ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                               "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a broader property should not be penalized
;     (is (s/valid? ::attachment-usage-types/broader-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a broader concept that's not in the profile
;     (is (not (s/valid? ::attachment-usage-types/broader-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :broader ["https://w3id.org/xapi/catch/attachment-usage-types/gallup-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::attachment-usage-types/broader-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :broader ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::attachment-usage-types/broader-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :broader ["https://w3id.org/xapi/catch/verbs/provided"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::attachment-usage-types/broader-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :broader ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                                    "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents-futuristic"]}
;                         :concepts-table concept-map})))))

; (deftest narrower-concept-iris-test
;   (testing "Test that the verb correctly relates to narrower concepts"
;     (is (s/valid? ::attachment-usage-types/narrower-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                     :narrower ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                                "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a narrower property should not be penalized
;     (is (s/valid? ::attachment-usage-types/narrower-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a narrower concept that's not in the profile
;     (is (not (s/valid? ::attachment-usage-types/narrower-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :narrower ["https://w3id.org/xapi/catch/attachment-usage-types/gallup-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::attachment-usage-types/narrower-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :narrower ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::attachment-usage-types/narrower-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :narrower ["https://w3id.org/xapi/catch/verbs/provided"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::attachment-usage-types/narrower-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :narrower ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                                     "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents-futuristic"]}
;                         :concepts-table concept-map})))))

; (deftest related-concept-iris-test
;   (testing "Test that the verb correctly relates to related concepts"
;     (is (s/valid? ::attachment-usage-types/related-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                     :related ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                               "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"]}
;                    :concepts-table concept-map}))
;     ;; Concepts that don't have a related property should not be penalized
;     (is (s/valid? ::attachment-usage-types/related-concept-iris
;                   {:object
;                    {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                     :type "AttachmentUsageType"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "Parent Survey"}
;                     :definition {"en" "A survey provided to the parent(s) of a DL student"}}
;                    :concepts-table concept-map}))
;     ;; Concepts that include a related concept that's not in the profile
;     (is (not (s/valid? ::attachment-usage-types/related-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :related ["https://w3id.org/xapi/catch/attachment-usage-types/gallup-survey"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong type
;     (is (not (s/valid? ::attachment-usage-types/related-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :related ["https://w3id.org/xapi/catch/activitytypes/check-in"]}
;                         :concepts-table concept-map})))
;     (is (not (s/valid? ::attachment-usage-types/related-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :related ["https://w3id.org/xapi/catch/verbs/provided"]}
;                         :concepts-table concept-map})))
;     ;; If a concept is of the wrong version
;     (is (not (s/valid? ::attachment-usage-types/related-concept-iris
;                        {:object
;                         {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
;                          :type "AttachmentUsageType"
;                          :in-scheme "https://w3id.org/xapi/catch/v1"
;                          :pref-label {"en" "Parent Survey"}
;                          :definition {"en" "A survey provided to the parent(s) of a DL student"}
;                          :related ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey-result"
;                                    "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents-futuristic"]}
;                         :concepts-table concept-map})))))
