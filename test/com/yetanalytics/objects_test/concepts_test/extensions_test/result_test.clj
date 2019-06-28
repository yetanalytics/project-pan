(ns com.yetanalytics.objects-test.concepts-test.extensions-test.result-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.concepts.extensions.result
             :as result-extension]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::result-extension/type
                     "ResultExtension"
                     :bad
                     "ActivityExtension"
                     "ContextExtension"
                     "Stan Loona")))

(deftest recommended-verbs-test
  (testing "recommendedVerbs property"
    (should-satisfy+ ::result-extension/recommended-verbs
                     ["https://w3id.org/xapi/catch/resulttypes/lesson-plan"]
                     :bad
                     []
                     nil)))

;; TODO Review spec to see what it means that this "cannot be used"
(deftest recommended-activity-types-test
  (testing "recommendedActivityTypes property"
    (should-satisfy+ ::result-extension/recommended-activity-types
                     []
                     :bad
                     ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     "stan loona")))

(deftest context-test
  (testing "context property"
    (should-satisfy ::result-extension/context
                    "https://w3id.org/xapi/some-context")))

(deftest schema-test
  (testing "(JSON) schema property"
    ; Example taken from the SCORM profile
    (should-satisfy ::result-extension/schema
                    "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
    (should-not-satisfy ::result-extension/schema "what the pineapple")))

(deftest inline-schema-test
  (testing "inline (JSON) schema property"
    (should-satisfy ::result-extension/inline-schema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::result-extension/inline-schema
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
    (should-not-satisfy ::result-extension/inline-schema "what the pineapple")))

(deftest context-extension
  (testing "ResultExtension extension"
    (is (s/valid? ::result-extension/extension
                  {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                   :type "ResultExtension"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "Label"}
                   :definition {"en" "Some description"}
                   :recommended-verbs
                   ["https://w3id.org/xapi/catch/verbs/sent"
                    "https://w3id.org/xapi/catch/verbs/provided"
                    "https://w3id.org/xapi/catch/verbs/uploaded"
                    "https://w3id.org/xapi/catch/verbs/submitted"]
                   :inline-schema
                   "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}))))

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

(deftest recommended-verbs-uri-test
  (testing "Recommended verbs URI array"
    (is (s/valid? ::result-extension/recommended-verbs-uris
                  {:object
                   {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                    :type "ResultExtension"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Label"}
                    :definition {"en" "Some description"}
                    :recommended-verbs
                    ["https://w3id.org/xapi/catch/verbs/provided"
                     "https://w3id.org/xapi/catch/verbs/submitted"]
                    :inline-schema
                    "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}
                   :concepts-table concept-map}))
    (is (s/valid? ::result-extension/recommended-verbs-uris
                  {:object
                   {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                    :type "ResultExtension"
                    :in-scheme "https://w3id.org/xapi/catch/v1"
                    :pref-label {"en" "Label"}
                    :definition {"en" "Some description"}
                    :inline-schema
                    "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}
                   :concepts-table concept-map}))
    (is (not (s/valid? ::result-extension/recommended-verbs-uris
                       {:object
                        {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                         :type "ResultExtension"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Label"}
                         :definition {"en" "Some description"}
                         :recommended-verbs
                         ["https://w3id.org/xapi/catch/verbs/provided"
                          "https://w3id.org/xapi/catch/verbs/submitted"
                          "https://w3id.org/xapi/catch/activitytypes/check-in"]
                         :inline-schema
                         "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}
                        :concepts-table concept-map})))
    (is (not (s/valid? ::result-extension/recommended-verbs-uris
                       {:object
                        {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                         :type "ResultExtension"
                         :in-scheme "https://w3id.org/xapi/catch/v1"
                         :pref-label {"en" "Label"}
                         :definition {"en" "Some description"}
                         :recommended-verbs
                         ["https://w3id.org/xapi/catch/verbs/sent"
                          "https://w3id.org/xapi/catch/verbs/provided"
                          "https://w3id.org/xapi/catch/verbs/uploaded"
                          "https://w3id.org/xapi/catch/verbs/submitted"]
                         :inline-schema
                         "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}
                        :concepts-table concept-map})))))

(run-tests)
