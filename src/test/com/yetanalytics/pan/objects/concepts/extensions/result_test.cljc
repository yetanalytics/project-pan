(ns com.yetanalytics.pan.objects.concepts.extensions.result-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concepts.extensions.result :as re]
            [com.yetanalytics.test-utils :refer [should-satisfy
                                                 should-satisfy+
                                                 should-not-satisfy]]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::re/type
                     "ResultExtension"
                     :bad
                     "ActivityExtension"
                     "ContextExtension"
                     "Stan Loona")))

(deftest recommended-verbs-test
  (testing "recommendedVerbs property"
    (should-satisfy+ ::re/recommendedVerbs
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
    (should-satisfy ::re/context
                    "https://w3id.org/xapi/some-context")))

(deftest schema-test
  (testing "(JSON) schema property"
    ; Example taken from the SCORM profile
    (should-satisfy ::re/schema
                    "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
    (should-not-satisfy ::re/schema "what the pineapple")))

(deftest inline-schema-test
  (testing "inline (JSON) schema property"
    (should-satisfy ::re/inlineSchema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::re/inlineSchema
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
    (should-not-satisfy ::re/inlineSchema "what the pineapple")))

(deftest result-extension
  (testing "ResultExtension extension"
    (is (s/valid? ::re/extension
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
    (is (not (s/valid? ::re/extension
                       {:id "https://foo.org/bar"
                        :type "ResultExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inlineSchema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended activity types
    (is (not (s/valid? ::re/extension
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
