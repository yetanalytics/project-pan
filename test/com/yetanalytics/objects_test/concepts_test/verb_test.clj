(ns com.yetanalytics.objects-test.concepts-test.verb-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.verbs :as verbs]))

; (deftest schema-test
;   (testing "(JSON) schema property"
;     ; Example taken from the SCORM profile
;     (should-satisfy ::concept/schema
;                     "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
;     (should-not-satisfy ::concept/schema "what the pineapple")))

; (deftest inline-schema-test
;   (testing "inline (JSON) schema property"
;     (should-satisfy ::concept/inline-schema
;                     "{\"type\":\"array\",
;                       \"items\":{\"type\":\"string\"},
;                       \"uniqueItems\":true}")
;     (should-satisfy ::concept/inline-schema
;                     "{\"type\":\"object\",
;                       \"properties\":{
;                         \"application\":{\"type\":\"string\"},
;                         \"background\":{\"type\":\"string\"},
;                         \"adaptation\":{\"type\":\"string\"},
;                         \"assessment\":{\"type\":\"string\"},
;                         \"scaffolding\":{\"type\":\"string\"},
;                         \"review\":{\"type\":\"string\"},
;                         \"group-options\":{\"type\":\"string\"},
;                         \"vocabulary\":{
;                           \"type\":\"array\",
;                           \"items\":{
;                             \"type\":\"string\",
;                             \"uniqueItems\":true}},
;                           \"language-skills\":{
;                             \"type\":\"array\",
;                             \"items\":{
;                              \"type\":\"string\",
;                              \"uniqueItems\":true}},
;                           \"linguistic-cognitive-scaffolds\":{\"type\":\"string\"}}}")
;     (should-not-satisfy ::concept/inline-schema "what the pineapple")))

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

(deftest verb-test
  (testing "Verb concept"
    (is (s/valid? ::verbs/verb
                  {:id "https://w3id.org/xapi/catch/verbs/presented"
                   :type "Verb"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "presented"}
                   :definition {"en"

                                "leading a discussion at an advocacy
                                     event"}}))))

(run-tests)

; (deftest verb-and-type-test
;   (testing "Verb, ActivityType and AttachmentUsageType Concepts"
;     (is (s/explain ::concept/concept
;                    {:id "https://w3id.org/xapi/catch/verbs/presented"
;                     :type "Verb"
;                     :in-scheme "https://w3id.org/xapi/catch/v1"
;                     :pref-label {"en" "presented"}
;                     :definition {"en" "leading a discussion at an advocacy
;                                      event"}}))
;     (is (s/valid? ::concept/concept
;                   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
;                    :type "ActivityType"
;                    :in-scheme "https://w3id.org/xapi/catch/v1"
;                    :pref-label {"en" "Check in"}
;                    :definition {"en" "An activity in which the learner reports
;                                      progression."}}))
;     (is (s/valid? ::concept/concept
;                   {:id "https://w3id.org/xapi/catch/attachment-usage-types/supporting-documents"
;                    :type "AttachmentUsageType"
;                    :in-scheme "https://w3id.org/xapi/catch/v1"
;                    :pref-label {"en" "Supporting documents"}
;                    :definition {"en" "Documents which provide aditional 
;                                      information about the lesson plan. Can be
;                                      instructions for lesson plan execution
;                                      demonstrating implementation or any other 
;                                      documents related to the lesson plan"}}))
;     (is (not (s/valid? ::concept/concept
;                        {:id "https://w3id.org/xapi/catch/verbs/uploaded"
;                         :type "Verb"
;                         :in-scheme "https://w3id.org/xapi/catch/v1"
;                         :pref-label {"en" "provided"}
;                         :definition {"en" "Uploading a resource from a local file system"}
;                         :related ["https://w3id.org/xapi/catch/verbs/submitted"
;                                   "https://w3id.org/xapi/catch/verbs/provided"]})))))
