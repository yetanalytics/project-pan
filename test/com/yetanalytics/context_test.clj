(ns com.yetanalytics.context-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.zip :as zip]
            [com.yetanalytics.context :as c]
            [com.yetanalytics.utils :refer :all]))

(def profile-context
  (c/get-context "https://w3id.org/xapi/profiles/context"))

#_(:profile profile-context)

(def activity-context
  (c/get-context "https://w3id.org/xapi/profiles/activity-context"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Parsing and validating context tests 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest get-context-test
  (testing "get-context function: Get @context and convert from JSON to EDN."
    (is (map? profile-context))
    (is (= activity-context
           {:xapi "https://w3id.org/xapi/ontology#"
            :type {:id "xapi:type" :type "@id"}
            :name {:id "xapi:name" :container "@language"}
            :description {:id "xapi:description" :container "@language"}
            :more-info {:id "xapi:moreInfo" :type "@id"}
            :extensions {:id "xapi:extensions" :container "@set"}
            :interaction-type {:id "xapi:interactionType"}
            :correct-responses-pattern {:id "xapi:correctResponsesPattern"
                                        :container "@set"}
            :choices {:id "xapi:choices" :container "@list"}
            :scale {:id "xapi:scale" :container "@list"}
            :source {:id "xapi:source" :container "@list"}
            :target {:id "xapi:target" :container "@list"}
            :steps {:id "xapi:steps" :container "@list"}
            :id {:id "xapi:interactionId"}}))))

(deftest prefix-spec-test
  (testing "prefix spec"
    (should-satisfy+ ::c/prefix
                     "https://w3id.org/xapi/ontology#"
                     "https://foo.org/"
                     "https://foo.org?"
                     :bad
                     "Profile"
                     "what the pineapple"
                     "https//bad-uri/"
                     "https://w3id.org/xapi/ontology")
    (should-satisfy+ ::c/prefix
                     {:id "http://example.com/compact-iris-" :prefix true}
                     :bad
                     {:id "http://example.com/compact-iris-" :prefix false}
                     {:id "http://example.com/compact-iris-"})))

(deftest collect-prefixes-test
  (testing "collect-prefixes function: get all prefixes from a context"
    ;; FIXME: Spec does not pass
    ;; Need to get rid of camelCase to kebab-case conversion
    ;; Also should add at namespace instead of getting rid of @ symbol
    (is (= (c/collect-prefixes profile-context)
           {:prov "http://www.w3.org/ns/prov#"
            :skos "http://www.w3.org/2004/02/skos/core#"
            :xapi "https://w3id.org/xapi/ontology#"
            :profile "https://w3id.org/xapi/profiles/ontology#"
            :dcterms "http://purl.org/dc/terms/"
            :schemaorg "http://schema.org/"
            :rdfs "http://www.w3.org/2000/01/rdf-schema#"}))
    (is (= (c/collect-prefixes activity-context)
           {:xapi "https://w3id.org/xapi/ontology#"}))))

(deftest compact-iri?-test
  (testing "compact-iri? predicate"
    (is (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                        "xapi:Verb"))
    (is (not (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                             "skos:prefLabel")))
    (is (not (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                             {:id "xapi:type" :type "@id"})))))

(deftest context-map?-test
  (testing "context-map? predicate"
    (is (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                        {:id "xapi:type" :type "@id"}))
    (is (not (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                             {:type "@id" :id "dcterms:conformsTo"})))
    (is (not (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                             "xapi:Verb")))))

(deftest value-spec
  (testing "value-spec spec creation function"
    (is (s/valid? (c/value-spec {:xapi "https://w3id.org/xapi/ontology#"})
                  "xapi:Verb"))
    (is (s/valid? (c/value-spec {:xapi "https://w3id.org/xapi/ontology#"})
                  {:id "xapi:type" :type "@id"}))
    (is (not (s/valid? (c/value-spec {:xapi "https://w3id.org/xapi/ontology#"})
                       "skos:prefLabel")))
    (is (not (s/valid? (c/value-spec {:xapi "https://w3id.org/xapi/ontology#"})
                       {type "@id" :id "dcterms:conformsTo"})))))

(deftest context-spec-test
  (testing "context-spec spec creation function"
    (is (s/valid? (c/context-spec activity-context) activity-context))
    (is (not (s/valid? (c/context-spec activity-context) profile-context)))))

(deftest create-context-test
  (testing "create-context function"
    (is (some? (c/create-context "https://w3id.org/xapi/profiles/activity-context")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validating profile against context tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def dummy-profile {:foo {:bar "b1" :baz "b2"}
                    :nums [{:one 1} {:two 2} {:three 3}]
                    :context "https://w3id.org/xapi/profiles/activity-context"})

(def ex-activity-def {:context "https://w3id.org/xapi/profiles/activity-context"
                      :name {:en "Example Activity Def"}
                      :description {:en "This is an example"}
                      :type "https://foo.org/bar"})

(deftest profile-to-zipper-test
  (testing "profile-to-zipper function"
    (is (= (zip/node (c/profile-to-zipper dummy-profile)) dummy-profile))
    (is (= (zip/children (c/profile-to-zipper dummy-profile))
           '({:bar "b1" :baz "b2"} {:one 1} {:two 2} {:three 3})))
    (is (= '() (zip/children (c/profile-to-zipper ex-activity-def))))))

(deftest create-context-test
  (testing "create-context-function"))

(deftest subvec?-test
  (testing "subvec? predicate: v1 should be a subvector of v2"
    (is (c/subvec? [1 2 3] [1 2 3 4]))
    (is (c/subvec? [1 2 3 4] [1 2 3 4]))
    (is (c/subvec? [] [{:foo 1 :bar 2}]))
    (is (not (c/subvec? [1 2 3 4] [1 2 3])))
    (is (not (c/subvec? [1 3 5] [1 2 3 4])))))

(deftest pop-context-test
  (testing "pop-context function"
    ;; Do not pop if the context is located at a parent
    (is (= [{:path [] :context activity-context}]
           (c/pop-context [{:path [] :context activity-context}]
                          (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Do not pop if the context is located at the current node
    (is (= [{:path [dummy-profile] :context activity-context}]
           (c/pop-context [{:path [dummy-profile] :context activity-context}]
                          (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Pop if the current location is at a different branch
    (is (= []
           (c/pop-context [{:path [{:super "cali"}] :context activity-context}]
                          (-> dummy-profile c/profile-to-zipper))))
    ;; Pop if the current location is a parent of the context location
    (is (= []
           (c/pop-context [{:path [dummy-profile {:super "cali"}]
                            :context activity-context}]
                          (-> dummy-profile c/profile-to-zipper))))))

(deftest pop-contexts-test
  (testing "pop-contexts function"
    (is (= [{:path [] :context activity-context}]
           (c/pop-contexts [{:path [] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper zip/down))))
    (is (= [{:path [dummy-profile] :context activity-context}]
           (c/pop-contexts [{:path [dummy-profile] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper zip/down))))
    (is (= []
           (c/pop-contexts [{:path [{:super "cali"}] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper))))
    (is (= [{:path [] :context activity-context}]
           (c/pop-contexts [{:path [] :context activity-context}
                            {:path [{:super "cali"}] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper))))
    (is (= [{:path [] :context activity-context}]
           (c/pop-contexts [{:path [] :context activity-context}
                            {:path [dummy-profile] :context activity-context}
                            {:path [dummy-profile {:bar "b1" :baz "b2"}] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper))))))

(deftest push-context-test
  (testing "push-context function"
    (is (= [{:path [] :context activity-context}]
           (c/push-context [] (-> dummy-profile c/profile-to-zipper))))
    ;; Do not push if there is no @context key
    (is (= [{:path [] :context activity-context}]
           (c/push-context [{:path [] :context activity-context}]
                           (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Push if we have a vector as an @context value
    (is (= [{:path [] :context activity-context}]
           (c/push-context
            [] (c/profile-to-zipper
                {:foo {:bar "b1" :baz "b2"}
                 :nums [{:one 1} {:two 2} {:three 3}]
                 :context ["https://w3id.org/xapi/profiles/activity-context"]}))))))

(deftest update-contexts-test
  (testing "update-contexts function"
    (is (= [{:path [] :context activity-context}]
           (c/update-contexts [] (c/profile-to-zipper dummy-profile))))
    (is (= [{:path [] :context activity-context}]
           (-> []
               (c/push-context (-> dummy-profile c/profile-to-zipper))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next
                                   zip/next))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next
                                   zip/next zip/next))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next
                                   zip/next zip/next zip/next))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next
                                   zip/next zip/next zip/next zip/next))
               (c/push-context (-> dummy-profile c/profile-to-zipper zip/next
                                   zip/next zip/next zip/next zip/next zip/next)))))))

(deftest search-contexts-test
  (testing "search-contexts function"
    (is (c/search-contexts [{:path [] :context activity-context}] :type))
    (is (not (c/search-contexts [{:path [] :context activity-context}] :context)))
    (is (not (c/search-contexts [{:path [] :context activity-context}] :foo)))))

(deftest validate-all-contexts-test
  (testing "validate-all-contexts function"
    (is (c/validate-all-contexts ex-activity-def))
    (is (not (c/validate-all-contexts dummy-profile)))))
