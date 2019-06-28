(ns com.yetanalytics.objects-test.concepts-test.extensions-test.activity-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.extensions.activity
             :as activity-extension]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::activity-extension/type
                     "ActivityExtension"
                     :bad
                     "ContextExtension"
                     "ResultExtension"
                     "Stan Loona")))

(deftest recommended-activity-types-test
  (testing "recommendedActivityTypes property"
    (should-satisfy+ ::activity-extension/recommended-activity-types
                     ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     []
                     nil)))

;; TODO Review spec to see what it means that this "cannot be used"
(deftest recommended-verbs-test
  (testing "recommendedVerbs property"
    (should-satisfy+ ::activity-extension/recommended-verbs
                     []
                     :bad
                     ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     "stan loona")))

(deftest context-test
  (testing "context property"
    (should-satisfy ::activity-extension/context
                    "https://w3id.org/xapi/some-context")))

(deftest schema-test
  (testing "(JSON) schema property"
    ; Example taken from the SCORM profile
    (should-satisfy ::activity-extension/schema
                    "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
    (should-not-satisfy ::activity-extension/schema "what the pineapple")))

(deftest inline-schema-test
  (testing "inline (JSON) schema property"
    (should-satisfy ::activity-extension/inline-schema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::activity-extension/inline-schema
                    "{\"type\":\"object\",
                      \"properties\":{
                        \"application\":{\"type\":\"string\"},
                        \"background\":{\"type\":\"string\"},
                        \"adaptation\":{\"type\":\"string\"},
                        \"assessment\":{\"type\":\"string\"},
                        \"scaffolding\":{\"type\":\"string\"},
                        \"review\":{\"type\":\"string\"},
                        \"group-options\":{\"type\":\"string\"},
                        \"vocabulary\":{
                          \"type\":\"array\",
                          \"items\":{
                            \"type\":\"string\",
                            \"uniqueItems\":true}},
                          \"language-skills\":{
                            \"type\":\"array\",
                            \"items\":{
                             \"type\":\"string\",
                             \"uniqueItems\":true}},
                          \"linguistic-cognitive-scaffolds\":{\"type\":\"string\"}}}")
    (should-not-satisfy ::activity-extension/inline-schema "what the pineapple")))

(deftest activity-extension
  (testing "ActivityExtension extension"
    (is (s/valid? ::activity-extension/extension
                  {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                   :type "ActivityExtension"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Lesson Plan Info"}
                   :definition {"en"
                                "The fields of a Lesson Plan template filled in by the learner"}
                   :recommended-activity-types
                   ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :inline-schema
                   "{\"type\":\"object\",
                      \"properties\":{
                        \"application\":{\"type\":\"string\"},
                        \"background\":{\"type\":\"string\"},
                        \"adaptation\":{\"type\":\"string\"},
                        \"assessment\":{\"type\":\"string\"},
                        \"scaffolding\":{\"type\":\"string\"},
                        \"review\":{\"type\":\"string\"},
                        \"group-options\":{\"type\":\"string\"},
                        \"vocabulary\":{
                          \"type\":\"array\",
                          \"items\":{
                            \"type\":\"string\",
                            \"uniqueItems\":true}},
                          \"language-skills\":{
                            \"type\":\"array\",
                            \"items\":{
                             \"type\":\"string\",
                             \"uniqueItems\":true}},
                          \"linguistic-cognitive-scaffolds\":{\"type\":\"string\"}}}"}))))

(def concept-map
  {"https://w3id.org/xapi/catch/verbs/provided"
   {:id "https://w3id.org/xapi/catch/verbs/provided"
    :type "Verb"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "provided"}
    :definition {"en" "supplying a link to an online resource"}}
   "https://w3id.org/xapi/catch/verbs/submitted"
   {:id "https://w3id.org/xapi/catch/verbs/submitted"
    :type "Verb"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "submitted"}
    :definition {"en" "The actor has clicked the submit or save button within the CATCH application"}}
   "https://w3id.org/xapi/catch/activitytypes/reflection"
   {:id "https://w3id.org/xapi/catch/activitytypes/reflection"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Reflection"}
    :definition {"en" "An activity where a learner reads an article and optionally provides a reflection on that article."}}
   "https://w3id.org/xapi/catch/activitytypes/check-in"
   {:id "https://w3id.org/xapi/catch/activitytypes/check-in"
    :type "ActivityType"
    :in-scheme "https://w3id.org/xapi/catch/v1"
    :pref-label {"en" "Check in"}
    :definition {"en" "An activity in which the learner reports progression."}}})

(deftest recommended-activity-types-uri-test
  (testing "Recommended activity types URI array"
    (is (s/valid? ::activity-extension/recommended-activity-types-uris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                    :type "ActivityExtension"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Lesson Plan Info"}
                    :definition {"en" "The fields of a Lesson Plan template filled in by the learner"}
                    :recommended-activity-types
                    ["https://w3id.org/xapi/catch/activitytypes/check-in"
                     "https://w3id.org/xapi/catch/activitytypes/reflection"]
                    :inline-schema "{\"foo\":\"bar\"}"}
                   :concepts-table concept-map}))
    (is (s/valid? ::activity-extension/recommended-activity-types-uris
                  {:object
                   {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                    :type "ActivityExtension"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Lesson Plan Info"}
                    :definition {"en" "The fields of a Lesson Plan template filled in by the learner"}
                    :inline-schema "{\"foo\":\"bar\"}"}
                   :concepts-table concept-map}))
    (is (not (s/valid? ::activity-extension/recommended-activity-types-uris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                         :type "ActivityExtension"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Lesson Plan Info"}
                         :definition {"en" "The fields of a Lesson Plan template filled in by the learner"}
                         :recommended-activity-types
                         ["https://w3id.org/xapi/catch/activitytypes/check-in"
                          "https://w3id.org/xapi/catch/verbs/provided"]
                         :inline-schema "{\"foo\":\"bar\"}"}
                        :concepts-table concept-map})))
    (is (not (s/valid? ::activity-extension/recommended-activity-types-uris
                       {:object
                        {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                         :type "ActivityExtension"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Lesson Plan Info"}
                         :definition {"en" "The fields of a Lesson Plan template filled in by the learner"}
                         :recommended-activity-types
                         ["https://w3id.org/xapi/catch/activitytypes/check-in"
                          "https://w3id.org/xapi/catch/activitytypes/non-existent"]
                         :inline-schema "{\"foo\":\"bar\"}"}
                        :concepts-table concept-map})))))
