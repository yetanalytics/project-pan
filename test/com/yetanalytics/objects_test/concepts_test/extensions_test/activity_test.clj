(ns com.yetanalytics.objects-test.concepts-test.extensions-test.activity-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
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
    (should-satisfy+ ::activity-extension/recommendedActivityTypes
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
    (should-satisfy ::activity-extension/inlineSchema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::activity-extension/inlineSchema
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
    (should-not-satisfy ::activity-extension/inlineSchema "what the pineapple")))

(deftest activity-extension-test
  (testing "ActivityExtension extension"
    (is (s/valid? ::activity-extension/extension
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
    (is (not (s/valid? ::activity-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ActivityExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inlineSchema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended verbs
    (is (not (s/valid? ::activity-extension/extension
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
    (is (= (util/edges-with-attrs {:id "https://foo.org/ae"
                                   :type "ActivityExtension"
                                   :inScheme "https://foo.org/v1"
                                   :recommendedActivityTypes
                                   ["https://foo.org/at1"
                                    "https://foo.org/at2"]})
           [["https://foo.org/ae" "https://foo.org/at1" {:type :recommendedActivityTypes}]
            ["https://foo.org/ae" "https://foo.org/at2" {:type :recommendedActivityTypes}]]))
    (is (= (util/edges-with-attrs {:id "https://foo.org/ae2"
                                   :type "ActivityExtension"
                                   :inScheme "https://foo.org/v1"})
           []))))
