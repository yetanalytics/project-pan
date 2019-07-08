(ns com.yetanalytics.objects-test.concepts-test.extensions-test.result-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
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

(deftest result-extension
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
                   "{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"uniqueItems\":true}"}))
    ;; Cannot both have schema and inlineSchema
    (is (not (s/valid? ::result-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ResultExtension"
                        :in-scheme "https://foo.org/"
                        :pref-label {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :inline-schema "{\"some\" : \"schema\"}"})))
    ;; Cannot have recommended activity types
    (is (not (s/valid? ::result-extension/extension
                       {:id "https://foo.org/bar"
                        :type "ResultExtension"
                        :in-scheme "https://foo.org/"
                        :pref-label {"en" "Bar"}
                        :definition {"en" "Supercalifragilisticexpialidocious"}
                        :schema "https://some.schema"
                        :recommended-activity-types ["https://this.org/is-bad"]})))))

;; Graph tests
(deftest edges-with-attrs-test
  (testing "Creating edges from node"
    (is (= (util/edges-with-attrs {:id "https://foo.org/re"
                                   :type "ResultExtension"
                                   :in-scheme "https://foo.org/v1"
                                   :recommended-verbs
                                   ["https://foo.org/verb1"
                                    "https://foo.org/verb2"]})
           [["https://foo.org/re" "https://foo.org/verb1" {:type :recommended-verbs}]
            ["https://foo.org/re" "https://foo.org/verb2" {:type :recommended-verbs}]]))
    (is (= (util/edges-with-attrs {:id "https://foo.org/re2"
                                   :type "ResultExtension"
                                   :in-scheme "https://foo.org/v1"})
           []))))
