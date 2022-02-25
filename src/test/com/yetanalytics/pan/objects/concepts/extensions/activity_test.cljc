(ns com.yetanalytics.pan.objects.concepts.extensions.activity-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.test-utils :refer [should-satisfy
                                                 should-satisfy+
                                                 should-not-satisfy]]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::ae/type
                     "ActivityExtension"
                     :bad
                     "ContextExtension"
                     "ResultExtension"
                     "Stan Loona")))

(deftest recommended-activity-types-test
  (testing "recommendedActivityTypes property"
    (should-satisfy+ ::ae/recommendedActivityTypes
                     ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                     :bad
                     []
                     nil)))

;; TODO Review spec to see what it means that this "cannot be used"
; (deftest recommended-verbs-test
;   (testing "recommendedVerbs property"
;     (should-satisfy+ ::activity-extension/recommended-verbs
;                      []
;                      :bad
;                      ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
;                      "stan loona")))

(deftest context-test
  (testing "context property"
    (should-satisfy ::ae/context
                    "https://w3id.org/xapi/some-context")))

(deftest schema-test
  (testing "(JSON) schema property"
    ; Example taken from the SCORM profile
    (should-satisfy ::ae/schema
                    "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
    (should-not-satisfy ::ae/schema "what the pineapple")))

(deftest inline-schema-test
  (testing "inline (JSON) schema property"
    (should-satisfy ::ae/inlineSchema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::ae/inlineSchema
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
    (should-not-satisfy ::ae/inlineSchema "what the pineapple")))

(deftest activity-extension-test
  (testing "ActivityExtension extension"
    (is (s/valid? ::ae/extension
                  {:id "https://w3id.org/xapi/catch/activity-extensions/lesson-plan/design"
                   :type "ActivityExtension"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Lesson Plan Info"}
                   :definition {"en"
                                "The fields of a Lesson Plan template filled in by the learner"}
                   :recommended-activity-types
                   ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
                   :inlineSchema
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
                          \"linguistic-cognitive-scaffolds\":{\"type\":\"string\"}}}"}))
    ;; Cannot both have schema and inlineSchema
    (is (not (s/valid? ::ae/extension
                       {:id "https://foo.org/bar"
                        :type "ActivityExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inlineSchema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended verbs
    (is (not (s/valid? ::ae/extension
                       {:id "https://foo.org/bar"
                        :type "ActivityExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :recommendedVerbs ["https://this.org/is-bad"]})))))

;; Graph tests
(deftest edges-with-attrs-test
  (testing "Creating edges from node"
    (is (= [["https://foo.org/ae" "https://foo.org/at1" {:type :recommendedActivityTypes}]
            ["https://foo.org/ae" "https://foo.org/at2" {:type :recommendedActivityTypes}]]
           (graph/edges-with-attrs {:id                       "https://foo.org/ae"
                                    :type                     "ActivityExtension"
                                    :inScheme                 "https://foo.org/v1"
                                    :recommendedActivityTypes ["https://foo.org/at1"
                                                               "https://foo.org/at2"]})))
    (is (= []
           (graph/edges-with-attrs {:id       "https://foo.org/ae2"
                                    :type     "ActivityExtension"
                                    :inScheme "https://foo.org/v1"})))))
