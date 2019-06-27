(ns com.yetanalytics.objects-test.concepts-test.activity-type-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.activity-types
             :as activity-types]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activity-types/type
                     "ActivityType"
                     :bad
                     "Verb"
                     "AttachmentUsageType"
                     "Activity"
                     "Stan Loona")))

(deftest broader-test
  (testing "broader property"
    (should-satisfy ::activity-types/broader
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/broader
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::activity-types/broader [])))

(deftest broad-match-test
  (testing "broadMatch property"
    (should-satisfy ::activity-types/broad-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/broad-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/broad-match [])))

(deftest narrower-test
  (testing "narrower property"
    (should-satisfy ::activity-types/narrower
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/narrower
                        "https://w3id.org/xapi/catch/activity-types/submitted")
    (should-not-satisfy ::activity-types/narrower [])))

(deftest narrow-match-test
  (testing "narrowMatch property"
    (should-satisfy ::activity-types/narrow-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/narrow-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/narrow-match [])))

(deftest related-test
  (testing "related property"
    (should-satisfy ::activity-types/related
                    ["https://w3id.org/xapi/catch/activity-types/submitted"
                     "https://w3id.org/xapi/catch/activity-types/provided"])
    (should-not-satisfy ::activity-types/related
                        "https://w3id.org/xapi/catch/activity-types/provided")
    (should-not-satisfy ::activity-types/related [])))

(deftest related-match-test
  (testing "relatedMatch property"
    (should-satisfy ::activity-types/related-match
                    ["http://adlnet.gov/expapi/activity-types/shared"])
    (should-not-satisfy ::activity-types/related-match
                        "http://adlnet.gov/expapi/activity-types/shared")
    (should-not-satisfy ::activity-types/related-match [])))

(deftest exact-match-test
  (testing "exactMatch property"
    (should-satisfy ::activity-types/exact-match
                    ["http://activitystrea.ms/schema/1.0/article"])
    (should-not-satisfy ::activity-types/exact-match
                        "http://activitystrea.ms/schema/1.0/article")
    (should-not-satisfy ::activity-types/exact-match [])))

(deftest activity-type-test
  (testing "activityType concept"
    (is (s/valid? ::activity-types/activity-type
                  {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                   :type "ActivityType"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Check in"}
                   :definition {"en"
                                "An activity in which the learner reports
                                     progression."}}))))

(def concept-map
  {"https://w3id.org/xapi/catch/activitytypes/check-in"
   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Check in"}
    :definition {"en" "An activity in which the learner reports progression."}}
   "https://w3id.org/xapi/catch/activitytypes/reflection"
   {:id "https://w3id.org/xapi/catch/activitytypes/reflection"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Reflection"}
    :definition {"en" "An activity where a learner reads an article and optionally provides a reflection on that article."}}
   "https://w3id.org/xapi/catch/activitytypes/reflection-future"
   {:id "https://w3id.org/xapi/catch/activitytypes/reflection-future"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v2"
    :pref-label {"en" "Reflection"}
    :definition {"en" "An activity where a learner watches a hologram and optionally provides a reflection on that hologram."}}
   "https://w3id.org/xapi/catch/activitytypes/instance-of-interaction"
   {:id "https://w3id.org/xapi/catch/activitytypes/instance-of-interaction"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Family communication"}
    :definition {"en" "This activity type describes a teacher engaging with a family/parent/community in some way on a given date."}}
   "https://w3id.org/xapi/catch/verbs/provided"
   {:id "https://w3id.org/xapi/catch/verbs/provided"
    :type "Verb"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "provided"}
    :definition {"en" "supplying a link to an online resource"}}
   "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
   {:id "https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"
    :type "AttachmentUsageType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Parent Survey"}
    :definition {"en" "A survey provided to the parent(s) of a DL student"}}})

(deftest broader-concept-iris-test
  (testing "Test that the verb correctly relates to broader concepts"
    (is (s/valid? ::activity-types/broader-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}
                    :broader ["https://w3id.org/xapi/catch/activitytypes/check-in"
                              "https://w3id.org/xapi/catch/activitytypes/reflection"]}
                   :concepts-table concept-map}))
    ;; Concepts that don't have a broader property should not be penalized
    (is (s/valid? ::activity-types/broader-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}}
                   :concepts-table concept-map}))
    ;; Concepts that include a broader concept that's not in the profile
    (is (not (s/valid? ::activity-types/broader-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :broader ["https://w3id.org/xapi/catch/activitytypes/check-out"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong type
    (is (not (s/valid? ::activity-types/broader-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :broader ["https://w3id.org/xapi/catch/verbs/provided"]}
                        :concepts-table concept-map})))
    (is (not (s/valid? ::activity-types/broader-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :broader ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong version
    (is (not (s/valid? ::activity-types/broader-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :broader ["https://w3id.org/xapi/catch/activitytypes/reflection-future"]}
                        :concepts-table concept-map})))))

(deftest narrower-concept-iris-test
  (testing "Test that the verb correctly relates to narrower concepts"
    (is (s/valid? ::activity-types/narrower-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}
                    :narrower ["https://w3id.org/xapi/catch/activitytypes/check-in"
                               "https://w3id.org/xapi/catch/activitytypes/reflection"]}
                   :concepts-table concept-map}))
    ;; Concepts that don't have a narrower property should not be penalized
    (is (s/valid? ::activity-types/narrower-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}}
                   :concepts-table concept-map}))
    ;; Concepts that include a narrower concept that's not in the profile
    (is (not (s/valid? ::activity-types/narrower-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :narrower ["https://w3id.org/xapi/catch/activitytypes/check-out"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong type
    (is (not (s/valid? ::activity-types/narrower-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :narrower ["https://w3id.org/xapi/catch/verbs/provided"]}
                        :concepts-table concept-map})))
    (is (not (s/valid? ::activity-types/narrower-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :narrower ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong version
    (is (not (s/valid? ::activity-types/narrower-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :narrower ["https://w3id.org/xapi/catch/activitytypes/reflection-future"]}
                        :concepts-table concept-map})))))

(deftest related-concept-iris-test
  (testing "Test that the verb correctly relates to related concepts"
    (is (s/valid? ::activity-types/related-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}
                    :related ["https://w3id.org/xapi/catch/activitytypes/check-in"
                              "https://w3id.org/xapi/catch/activitytypes/reflection"]}
                   :concepts-table concept-map}))
    ;; Concepts that don't have a related property should not be penalized
    (is (s/valid? ::activity-types/related-concept-iris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                    :type "ActivityType"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Check in"}
                    :definition {"en" "An activity in which the learner reports progression."}}
                   :concepts-table concept-map}))
    ;; Concepts that include a related concept that's not in the profile
    (is (not (s/valid? ::activity-types/related-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :related ["https://w3id.org/xapi/catch/activitytypes/check-out"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong type
    (is (not (s/valid? ::activity-types/related-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :related ["https://w3id.org/xapi/catch/verbs/provided"]}
                        :concepts-table concept-map})))
    (is (not (s/valid? ::activity-types/related-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :related ["https://w3id.org/xapi/catch/attachment-usage-types/evidence/parent-survey"]}
                        :concepts-table concept-map})))
    ;; If a concept is of the wrong version
    (is (not (s/valid? ::activity-types/related-concept-iris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
                         :type "ActivityType"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Check in"}
                         :definition {"en" "An activity in which the learner reports progression."}
                         :related ["https://w3id.org/xapi/catch/activitytypes/reflection-future"]}
                        :concepts-table concept-map})))))
