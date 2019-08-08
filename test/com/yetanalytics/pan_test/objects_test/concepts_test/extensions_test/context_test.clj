(ns com.yetanalytics.pan-test.objects-test.concepts-test.extensions-test.context-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils.clj :refer :all]
            [com.yetanalytics.pan.objects.concepts.extensions.context
             :as context-extension]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::context-extension/type
                     "ContextExtension"
                     :bad
                     "ActivityExtension"
                     "ResultExtension"
                     "Stan Loona")))

(deftest recommended-verbs-test
  (testing "recommendedVerbs property"
    (should-satisfy+ ::context-extension/recommendedVerbs
                     ["https://w3id.org/xapi/catch/contexttypes/lesson-plan"]
                     :bad
                     []
                     nil)))

;; TODO Review spec to see what it means that this "cannot be used"
; (deftest recommended-activity-types-test
;   (testing "recommendedActivityTypes property"
;     (should-satisfy+ ::context-extension/recommended-activity-types
;                      []
;                      :bad
;                      ["https://w3id.org/xapi/catch/activitytypes/lesson-plan"]
;                      "stan loona")))

(deftest context-test
  (testing "context property"
    (should-satisfy ::context-extension/context
                    "https://w3id.org/xapi/some-context")))

(deftest schema-test
  (testing "(JSON) schema property"
    ; Example taken from the SCORM profile
    (should-satisfy ::context-extension/schema
                    "https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema")
    (should-not-satisfy ::context-extension/schema "what the pineapple")))

(deftest inline-schema-test
  (testing "inline (JSON) schema property"
    (should-satisfy ::context-extension/inlineSchema
                    "{\"type\":\"array\",
                      \"items\":{\"type\":\"string\"},
                      \"uniqueItems\":true}")
    (should-satisfy ::context-extension/inlineSchema
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
    (should-not-satisfy ::context-extension/inlineSchema "what the pineapple")))

(deftest context-extension-test
  (testing "ContextExtension extension"
    (is (s/valid? ::context-extension/extension
                  {:id "https://w3id.org/xapi/catch/context-extensions/communication-with-families-criteria"
                   :type "ContextExtension"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "Communication Criteria"}
                   :definition {"en" "Criteria for demonstrating communication with families."}
                   :recommendedVerbs
                   ["https://w3id.org/xapi/catch/verbs/sent"
                    "https://w3id.org/xapi/catch/verbs/provided"
                    "https://w3id.org/xapi/catch/verbs/uploaded"
                    "https://w3id.org/xapi/catch/verbs/submitted"]
                   :inlineSchema
                   "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}))
    ;; Cannot both have schema and inlineSchema
    (is (not (s/valid? ::context-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ContextExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inlineSchema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended activity types
    (is (not (s/valid? ::context-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ContextExtension"
                        :inScheme "https://foo.org/"
                        :prefLabel {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :recommendedActivityTypes ["https://this.org/is-bad"]})))))

;; Graph tests
(deftest edges-with-attrs-test
  (testing "Creating edges from node"
    (is (= (graph/edges-with-attrs {:id "https://foo.org/ce"
                                    :type "ContextExtension"
                                    :inScheme "https://foo.org/v1"
                                    :recommendedVerbs
                                    ["https://foo.org/verb1"
                                     "https://foo.org/verb2"]})
           [["https://foo.org/ce" "https://foo.org/verb1" {:type :recommendedVerbs}]
            ["https://foo.org/ce" "https://foo.org/verb2" {:type :recommendedVerbs}]]))
    (is (= (graph/edges-with-attrs {:id "https://foo.org/ce2"
                                    :type "ContextExtension"
                                    :inScheme "https://foo.org/v1"})
           []))))
