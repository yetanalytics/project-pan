(ns com.yetanalytics.pan-test.objects-test.concepts-test.extensions-test.result-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.concepts.extensions.result
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
    (should-satisfy+ ::result-extension/recommendedVerbs
                     ["https://w3id.org/xapi/catch/resulttypes/lesson-plan"]
                     :bad
                     []
                     nil)))

;; TODO Review spec to see what it means that this "cannot be used"
; (deftest recommended-activity-types-test
;   (testing "recommendedActivityTypes property"
;     (should-satisfy+ ::result-extension/recommended-activity-types
;                      []
;                      :bad
;                      ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
;                      "stan loona")))

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
    (should-satisfy ::result-extension/inlineSchema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::result-extension/inlineSchema
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
    (should-not-satisfy ::result-extension/inlineSchema "what the pineapple")))

(deftest result-extension
  (testing "ResultExtension extension"
    (is (s/valid? ::result-extension/extension
                  {:id "https://w3id.org/xapi/catch/result-extensions/some-extension"
                   :type "ResultExtension"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Label"}
                   :definition {"en" "Some description"}
                   :recommended-verbs
                   ["https://w3id.org/xapi/catch/verbs/sent"
                    "https://w3id.org/xapi/catch/verbs/provided"
                    "https://w3id.org/xapi/catch/verbs/uploaded"
                    "https://w3id.org/xapi/catch/verbs/submitted"]
                   :inlineSchema
                   "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}))
    ;; Cannot both have schema and inlineSchema
    (is (not (s/valid? ::result-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ResultExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inlineSchema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended activity types
    (is (not (s/valid? ::result-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ResultExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :recommendedActivityTypes ["https://this.org/is-bad"]})))))

;; Graph tests
(deftest edges-with-attrs-test
  (testing "Creating edges from node"
    (is (= [["https://foo.org/re" "https://foo.org/verb1" {:type :recommendedVerbs}]
            ["https://foo.org/re" "https://foo.org/verb2" {:type :recommendedVerbs}]]
           (graph/edges-with-attrs {:id "https://foo.org/re"
                                    :type "ResultExtension"
                                    :inScheme "https://foo.org/v1"
                                    :recommendedVerbs
                                    ["https://foo.org/verb1"
                                     "https://foo.org/verb2"]})))
    (is (= []
           (graph/edges-with-attrs {:id "https://foo.org/re2"
                                    :type "ResultExtension"
                                    :inScheme "https://foo.org/v1"})))))
