(ns com.yetanalytics.pan-test.context-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.context :as c])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-json-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-json-resource]])))

(def profile-context
  (->> "https://w3id.org/xapi/profiles/context"
       (get c/default-context-map)))

(def activity-context
  (->> "https://w3id.org/xapi/profiles/activity-context"
       (get c/default-context-map)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context spec tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest context-spec-test
  (testing "::_context spec"
    (s/valid? ::c/_context profile-context)
    (s/valid? ::c/_context activity-context)
    (s/valid? ::c/_context "https://w3id.org/xapi/profiles/context")
    (s/valid? ::c/_context ["https://w3id.org/xapi/profiles/context"
                            activity-context])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile context tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dummy-jsonld
  {:foo       {:bar "b1"
               :baz "b2"}
   :nums      [{:one 1} {:two 2} {:three 3}]
   :prefLabel {:en "example"}
   :_context  "https://w3id.org/xapi/profiles/context"})

(def ex-activity-def
  {:_context    "https://w3id.org/xapi/profiles/activity-context"
   :name        {:en "Example Activity Def"}
   :description {:en "This is an example"}
   :type        "https://foo.org/bar"})

(def ex-context
  {:ex "http://example.org/ontology/"
   :id "@id"
   :display {:_id "ex:display"
             :_container "@language"}})

(def ex-profile
  {:id       "http://example.org"
   :type     "Profile"
   :_context "https://w3id.org/xapi/profiles/context"
   :concepts [{:id                 "http://example.org/activity"
               :type               "Activity"
               :activityDefinition ex-activity-def}]
   :templates [{:id    "http://example.org/template"
                :type  "StatementTemplate"
                :rules [{:location "$.verb"
                         :any      [{:_context "http://example.org/context"
                                     :id       "http://example.org/verb"
                                     :display  {:en-US "Foo"}}]}]}]})

(def catch-profile
  (read-json-resource "sample_profiles/catch.json" "_"))

(deftest expand-profile-keys-test
  (testing "expand-profile-keys function"
    (is (= {:foo       {:bar "b1"
                        :baz "b2"}
            :nums      [{:one 1} {:two 2} {:three 3}]
            "http://www.w3.org/2004/02/skos/core#prefLabel"
            {:_LANGTAG_en "example"}}
           (c/expand-profile-keys dummy-jsonld)))
    (is (= {"https://w3id.org/xapi/ontology#name"
            {:_LANGTAG_en "Example Activity Def"}
            "https://w3id.org/xapi/ontology#description"
            {:_LANGTAG_en "This is an example"}
            "https://w3id.org/xapi/ontology#type"
            "https://foo.org/bar"}
           (c/expand-profile-keys ex-activity-def)))
    (is (= {"@id"   "http://example.org"
            "@type" "Profile"
            "https://w3id.org/xapi/profiles/ontology#concepts"
            [{"@id" "http://example.org/activity"
              "@type" "Activity"
              "https://w3id.org/xapi/profiles/ontology#activityDefinition"
              {"https://w3id.org/xapi/ontology#name"
               {:_LANGTAG_en "Example Activity Def"}
               "https://w3id.org/xapi/ontology#description"
               {:_LANGTAG_en "This is an example"}
               "https://w3id.org/xapi/ontology#type"
               "https://foo.org/bar"}}]
            "https://w3id.org/xapi/profiles/ontology#templates"
            [{"@id"   "http://example.org/template"
              "@type" "StatementTemplate"
              "https://w3id.org/xapi/profiles/ontology#rules"
              [{"https://w3id.org/xapi/profiles/ontology#location"
                "$.verb"
                "https://w3id.org/xapi/profiles/ontology#any"
                [{"@id" "http://example.org/verb"
                  "http://example.org/ontology/display"
                  {:_LANGTAG_en-US "Foo"}}]}]}]}
           (c/expand-profile-keys ex-profile
                                  {"http://example.org/context" ex-context})))
    (is (map? (c/expand-profile-keys catch-profile)))))

(deftest validate-contexts-test
  (testing "validate-contexts function"
    (is (nil? (c/validate-contexts ex-profile
                                   {"http://example.org/context" ex-context})))
    (is (nil? (c/validate-contexts catch-profile)))
    (testing "on spec error"
      (let [err (c/validate-contexts {:_context "http://example.org/context"
                                      :id       "http://example.org/verb"
                                      :display  {:en-US "Foo"}})]
        (is (some? err))
        (is (= #{:id :display :en-US}
               (->> err ::s/problems (map :val) set)))
        (is (= #{`ax/non-empty-string? `c/jsonld-keyword? `c/lang-tag?}
               (->> err ::s/problems (map :pred) set))))
      (is (some? (c/validate-contexts ex-profile))))))
