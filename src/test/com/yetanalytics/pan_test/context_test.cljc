(ns com.yetanalytics.pan-test.context-test
  (:require [clojure.test :refer [deftest is testing are]]
            [clojure.spec.alpha :as s]
            [clojure.zip :as zip]
            [com.yetanalytics.pan.context :as c]
            #_[com.yetanalytics.test-utils :refer [should-satisfy+]])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-json-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-json-resource]])))

(def profile-context
  (c/get-context "https://w3id.org/xapi/profiles/context"))

(def activity-context
  (c/get-context "https://w3id.org/xapi/profiles/activity-context"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Parsing and validating context tests 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO Test getting external @context
(deftest get-context-test
  (testing "get-context function: Get @context and convert from JSON to EDN."
    (is (map? profile-context)) ; Too big to equate directly
    (is (= {:xapi                    "https://w3id.org/xapi/ontology#"
            :type                    {:at/id "xapi:type" :at/type "@id"}
            :name                    {:at/id "xapi:name" :at/container "@language"}
            :description             {:at/id "xapi:description" :at/container "@language"}
            :moreInfo                {:at/id "xapi:moreInfo" :at/type "@id"}
            :extensions              {:at/id "xapi:extensions" :at/container "@set"}
            :interactionType         {:at/id "xapi:interactionType"}
            :correctResponsesPattern {:at/id "xapi:correctResponsesPattern" :at/container "@set"}
            :choices                 {:at/id "xapi:choices" :at/container "@list"}
            :scale                   {:at/id "xapi:scale" :at/container "@list"}
            :source                  {:at/id "xapi:source" :at/container "@list"}
            :target                  {:at/id "xapi:target" :at/container "@list"}
            :steps                   {:at/id "xapi:steps" :at/container "@list"}
            :id                      {:at/id "xapi:interactionId"}}
           activity-context))))

(deftest prefix-spec-test
  (testing "jsonld-prefix? predicate"
    (are [x] (c/jsonld-prefix? x)
      "https://w3id.org/xapi/ontology#"
      "https://foo.org/"
      "https://foo.org?"
      {:id "http://example.com/compact-iris-" :prefix true})
    (are [x] (not (c/jsonld-prefix? x))
      "Profile"
      "what the pineapple"
      "https//bad-uri/"
      "https://w3id.org/xapi/ontology"
      {:id "http://example.com/compact-iris-" :prefix false}
      {:id "http://example.com/compact-iris-"})
    #_(should-satisfy+ ::c/prefix
                     "https://w3id.org/xapi/ontology#"
                     "https://foo.org/"
                     "https://foo.org?"
                     :bad
                     "Profile"
                     "what the pineapple"
                     "https//bad-uri/"
                     "https://w3id.org/xapi/ontology")
    #_(should-satisfy+ ::c/prefix
                     {:id "http://example.com/compact-iris-" :prefix true}
                     :bad
                     {:id "http://example.com/compact-iris-" :prefix false}
                     {:id "http://example.com/compact-iris-"})))

(deftest collect-prefixes-test
  (testing "collect-prefixes function: get all prefixes from a context"
    (is (= {:prov      "http://www.w3.org/ns/prov#"
            :skos      "http://www.w3.org/2004/02/skos/core#"
            :xapi      "https://w3id.org/xapi/ontology#"
            :profile   "https://w3id.org/xapi/profiles/ontology#"
            :dcterms   "http://purl.org/dc/terms/"
            :schemaorg "http://schema.org/"
            :rdfs      "http://www.w3.org/2000/01/rdf-schema#"}
           (c/collect-prefixes profile-context)))
    (is (= {:xapi "https://w3id.org/xapi/ontology#"}
           (c/collect-prefixes activity-context)))))

(deftest dissoc-prefixes-test
  (testing "dissoc-prefixes function: get all that are NOT prefixes or kwords"
    (is (= {:type                    {:at/id "xapi:type" :at/type "@id"}
            :name                    {:at/id "xapi:name" :at/container "@language"}
            :description             {:at/id "xapi:description" :at/container "@language"}
            :moreInfo                {:at/id "xapi:moreInfo" :at/type "@id"}
            :extensions              {:at/id "xapi:extensions" :at/container "@set"}
            :interactionType         {:at/id "xapi:interactionType"}
            :correctResponsesPattern {:at/id "xapi:correctResponsesPattern" :at/container "@set"}
            :choices                 {:at/id "xapi:choices" :at/container "@list"}
            :scale                   {:at/id "xapi:scale" :at/container "@list"}
            :source                  {:at/id "xapi:source" :at/container "@list"}
            :target                  {:at/id "xapi:target" :at/container "@list"}
            :steps                   {:at/id "xapi:steps" :at/container "@list"}
            :id                      {:at/id "xapi:interactionId"}}
           (c/dissoc-prefixes activity-context)))
    (is (= (dissoc activity-context :xapi)
           (c/dissoc-prefixes activity-context)))
    (is (not (contains? (c/dissoc-prefixes profile-context)
                        :type)))
    (is (not (contains? (c/dissoc-prefixes profile-context)
                        :id)))
    (is (not (contains? (c/dissoc-prefixes profile-context)
                        :prov)))
    (is (not (contains? (c/dissoc-prefixes profile-context)
                        :skos)))
    (is (contains? (c/dissoc-prefixes profile-context)
                   :Profile))))

(deftest compact-iri?-test
  (testing "compact-iri? predicate"
    (is (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                        "xapi:Verb"))
    (is (not (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                             "skos:prefLabel")))
    (is (not (c/compact-iri? {:xapi "https://w3id.org/xapi/ontology#"}
                             {:at/id "xapi:type" :at/type "@id"})))))

(deftest context-map?-test
  (testing "context-map? predicate"
    (is (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                        {:at/id "xapi:type" :at/type "@id"}))
    (is (not (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                             {:at/type "@id" :at/id "dcterms:conformsTo"})))
    (is (not (c/context-map? {:xapi "https://w3id.org/xapi/ontology#"}
                             "xapi:Verb")))))

(deftest simple-term-spec-test
  (testing "simple-term-def spec"
    (is (s/valid? ::c/simple-term-def
                  [{:xapi "https://w3id.org/xapi/ontology#"} "xapi:Verb"]))
    (is (not (s/valid?
              ::c/simple-term-def
              [{:xapi "https://w3id.org/xapi/ontology#"} "skos:prefLabel"])))
    (is (= [::c/simple-term-def]
           (-> (s/explain-data
                ::c/simple-term-def
                [{:xapi "https://w3id.org/xapi/ontology#"} "skos:prefLabel"])
               ::s/problems
               first
               :via)))))

(deftest expanded-term-spec-test
  (testing "expanded-term-def spec"
    (is (s/valid? ::c/expanded-term-def
                  [{:xapi "https://w3id.org/xapi/ontology#"}
                   {:at/id "xapi:type" :at/type "@id"}]))
    (is (not (s/valid? ::c/expanded-term-def
                       [{:xapi "https://w3id.org/xapi/ontology#"}
                        {:at/type "@id" :at/id "dcterms:conformsTo"}])))
    (is (= [::c/expanded-term-def]
           (-> (s/explain-data ::c/expanded-term-def
                               [{:xapi "https://w3id.org/xapi/ontology#"}
                                {:at/type "@id" :at/id "dcterms:conformsTo"}])
               ::s/problems
               first
               :via)))))

(deftest term-def-spec-test
  (testing "term-def spec"
    (is (s/valid? ::c/term-def
                  [{:xapi "https://w3id.org/xapi/ontology#"} "xapi:Verb"]))
    (is (s/valid? ::c/term-def 
                  [{:xapi "https://w3id.org/xapi/ontology#"}
                   {:at/id "xapi:type" :at/type "@id"}]))
    (is (s/valid? ::c/term-def
                  [(c/collect-prefixes profile-context)
                   {:at/id "profile:schema" :at/type "@id"}]))
    (is (not (s/valid? ::c/term-def
                       [{:xapi "https://w3id.org/xapi/ontology#"}
                        "skos:prefLabel"])))
    (is (not (s/valid? ::c/term-def
                       [{:xapi "https://w3id.org/xapi/ontology#"}
                        {:at/type "@id" :at/id "dcterms:conformsTo"}])))
    (is (= ::c/simple-term-def
           (-> (s/explain-data
                ::c/term-def
                [{:xapi "https://w3id.org/xapi/ontology#"} "skos:prefLabel"])
               ::s/problems
               first
               :via
               last)))
    (is (= ::c/expanded-term-def
           (-> (s/explain-data
                ::c/term-def
                [{:xapi "https://w3id.org/xapi/ontology#"} "skos:prefLabel"])
               ::s/problems
               second
               :via
               last)))))

(deftest jsonld-context-spec-test
  (testing "jsonld-context spec"
    (is (nil? (s/explain-data ::c/jsonld-context profile-context)))
    (is (nil? (s/explain-data ::c/jsonld-context activity-context)))
    (is (nil? (s/explain-data
               ::c/jsonld-context
               {:xapi "https://w3id.org/xapi/ontology#"
                :type {:at/id "xapi:type" :at/type "@id"}
                :name {:at/id "xapi:name" :at/container "@language"}})))
    (is (nil? (s/explain-data
               ::c/jsonld-context
               {:xapi       "https://w3id.org/xapi/ontology#"
                :skos       "http://www.w3.org/2004/02/skos/core#"
                :prefLabel  {:at/id        "skos:prefLabel"
                             :at/container "@language"}
                :definition {:at/id        "skos:definition"
                             :at/container "@language"}})))
    (is (some? (s/explain-data
                ::c/jsonld-context
                {:xapi       "https://w3id.org/xapi/ontology#"
                 :prefLabel  {:at/id        "skos:prefLabel"
                              :at/container "@language"}
                 :definition {:at/id        "skos:definition"
                              :at/container "@language"}})))
    (is (= 4 (count (::s/problems ; 2 for each property, 2 properties total
                     (s/explain-data
                      ::c/jsonld-context
                      {:xapi       "https://w3id.org/xapi/ontology#"
                       :prefLabel  {:at/id        "skos:prefLabel"
                                    :at/container "@language"}
                       :definition {:at/id        "skos:definition"
                                    :at/container "@language"}})))))
    (is (= {:xapi "https://w3id.org/xapi/ontology#" ; value should be context
            :prefLabel  {:at/id "skos:prefLabel"  :at/container "@language"}
            :definition {:at/id "skos:definition" :at/container "@language"}}
           (::s/value
            (s/explain-data
             ::c/jsonld-context
             {:xapi       "https://w3id.org/xapi/ontology#"
              :prefLabel  {:at/id        "skos:prefLabel"
                           :at/container "@language"}
              :definition {:at/id        "skos:definition"
                           :at/container "@language"}}))))
    (is (some? (s/explain-data
                ::c/jsonld-context
                {:prefix "https://foo.org/prefix"
                 :gamma  {:at/id "prefix2:gamma"}})))
    (is (some? (s/explain-data
                ::c/jsonld-context
                {:xapi    "https://w3id.org/xapi/ontology#"
                 :Profile "profile:Profile"})))
    (is (some? (s/explain-data
                ::c/jsonld-context
                {:xapi       "https://w3id.org/xapi/ontology#"
                 :prefLabel  {:at/id        "skos:prefLabel"
                              :at/container "@language"}
                 :definition {:at/id        "skos:definition"
                              :at/container "@language"}})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validating profile against context tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dummy-profile {:foo {:bar "b1" :baz "b2"}
                    :nums [{:one 1} {:two 2} {:three 3}]
                    :prefLabel {:en "example"}
                    :_context "https://w3id.org/xapi/profiles/activity-context"})

(def ex-activity-def {:_context "https://w3id.org/xapi/profiles/activity-context"
                      :name {:en "Example Activity Def"}
                      :description {:en "This is an example"}
                      :type "https://foo.org/bar"})

(deftest profile-to-zipper-test
  (testing "profile-to-zipper function"
    (is (= dummy-profile
           (zip/node (c/profile-to-zipper dummy-profile))))
    (is (= '({:bar "b1" :baz "b2"} {:one 1} {:two 2} {:three 3})
           (zip/children (c/profile-to-zipper dummy-profile))))
    ;; Returns nil if there are no children
    (is (= nil
           (zip/children (c/profile-to-zipper ex-activity-def)))))
  (testing "dfs through a single profile"
    (is (-> {:_context {:prefix "https://foo.org/prefix/"
                        :alpha  {:at/id "prefix:alpha"}
                        :beta   {:at/id "prefix:beta"}}
             :alpha    116
             :beta     {:_context {:prefix2 "https://foo.org/prefix2"
                                   :gamma   {:at/id "prefix2:gamma2"}}
                        :alpha    9001
                        :gamma    "foo bar"}}
            c/profile-to-zipper
            zip/next
            zip/next
            zip/end?))))

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
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/pop-context
            [{:path [] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Do not pop if the context is located at the current node
    (is (= [{:path [dummy-profile] :context activity-context :errors nil}]
           (c/pop-context
            [{:path [dummy-profile] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Pop if the current location is at a different branch
    (is (= []
           (c/pop-context
            [{:path [{:super "cali"}] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper))))
    ;; Pop if the current location is a parent of the context location
    (is (= []
           (c/pop-context
            [{:path    [dummy-profile {:super "cali"}]
              :context activity-context
              :errors  nil}]
            (-> dummy-profile c/profile-to-zipper))))))

(deftest pop-contexts-test
  (testing "pop-contexts function"
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/pop-contexts
            [{:path [] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper zip/down))))
    (is (= [{:path [dummy-profile] :context activity-context :errors nil}]
           (c/pop-contexts
            [{:path [dummy-profile] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper zip/down))))
    (is (= []
           (c/pop-contexts
            [{:path [{:super "cali"}] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper))))
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/pop-contexts
            [{:path [] :context activity-context :errors nil}
             {:path [{:super "cali"}] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper))))
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/pop-contexts
            [{:path [] :context activity-context :errors nil}
             {:path [dummy-profile] :context activity-context :errors nil}
             {:path [dummy-profile {:bar "b1" :baz "b2"}] :context activity-context :errors nil}]
            (-> dummy-profile c/profile-to-zipper))))))

(deftest push-context-test
  (testing "push-context function"
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/push-context [] (-> dummy-profile c/profile-to-zipper))))
    ;; Do not push if there is no @context key
    (is (= [{:path [] :context activity-context :errors nil}]
           (c/push-context [{:path [] :context activity-context :errors nil}]
                           (-> dummy-profile c/profile-to-zipper zip/down))))
    ;; Push if we have a vector as an @context value
    (is (= [{:path [] :context activity-context :errors nil}
            {:path [] :context profile-context :errors nil}]
           (c/push-context
            []
            (c/profile-to-zipper
             {:foo {:bar "b1" :baz "b2"}
              :nums [{:one 1} {:two 2} {:three 3}]
              :_context ["https://w3id.org/xapi/profiles/activity-context"
                         "https://w3id.org/xapi/profiles/context"]}))))))

(deftest update-context-errors-test
  (testing "update-context-errors function"
    (is (= '({:a 1 :b 2})
           (c/update-context-errors [] [] '({:a 1 :b 2}))))
    (is (= '({:a 1 :b 2})
           (c/update-context-errors [] [{:path [] :context nil :errors {:a 1 :b 2}}] '())))))

(deftest search-contexts-test
  (testing "search-contexts function"
    (is (c/search-contexts [activity-context] :type))
    (is (c/search-contexts [profile-context activity-context] :versions))
    (is (not (c/search-contexts [activity-context] :context)))
    (is (not (c/search-contexts [activity-context] :foo)))))

(deftest jsonld-keys-spec-test
  (testing "jsonld-keys spec"
    (is (nil? (s/explain-data ::c/jsonld-keys
                              [[profile-context]
                               {:id "https://foo.org/" :type "Bar"}])))
    (is (nil? (s/explain-data ::c/jsonld-keys
                              [[activity-context]
                               {:id "https://foo.org/" :type "Bar"}])))
    (is (some? (s/explain-data ::c/jsonld-keys
                               [[activity-context]
                                {:prefLabel {:en "Blah"} :definition {:es "soy un dorito"}}])))))

(deftest validate-contexts-test
  (testing "validate-contexts function on valid profiles"
    (is (= {:context-errors nil :context-key-errors nil}
           (c/validate-contexts ex-activity-def)))
    (is (not= {:context-errors nil :context-key-errors nil}
              (c/validate-contexts dummy-profile)))
    (is (= {:context-errors nil :context-key-errors nil}
           (c/validate-contexts
            {:_context {:prefix "https://foo.org/prefix/"
                        :alpha  {:at/id "prefix:alpha"}
                        :beta   {:at/id "prefix:beta"}}
             :alpha    116
             :beta     {:_context {:prefix2 "https://foo.org/prefix2/"
                                   :gamma   {:at/id "prefix2:gamma2"}}
                        :alpha    9001
                        :gamma    "foo bar"}})))
    (is (= {:context-errors nil :context-key-errors nil}
           (c/validate-contexts
            {:_context "https://w3id.org/xapi/profiles/context"
             :id       "https://foo.org/profile"
             :type     "Profile"
             :concepts
             [{:id                 "https://foo.org/activity"
               :type               "Activity"
               :activityDefinition
               {:_context    "https://w3id.org/xapi/profiles/activity-context"
                :name        {:en "Name"}
                :description {:en "Description"}
                :extensions  {:_context {:prefix "https://foo.org/prefix/"
                                         :alpha  "prefix:alpha"}
                              :alpha    9001}}}]}))))
  (testing "validate-context function on invalid profiles"
    (is (nil? (-> {:_context {:prefix "https://foo.org/prefix/"
                              :gamma  {:at/id "prefix2:gamma"}}
                   :gamma    "foo bar"}
                  c/validate-contexts
                  :context-key-errors)))
    (is (= 2 (-> {:_context {:prefix "https://foo.org/prefix/"
                             :gamma  {:at/id "prefix2:gamma"}}
                  :gamma    "foo bar"}
                 c/validate-contexts
                 :context-errors
                 ::s/problems
                 count)))
    (is (nil? (-> {:_context {:prefix2 "https://foo.org/prefix2/"
                              :gamma   {:at/id "prefix2:gamma"}}
                   :alpha    9001}
                  c/validate-contexts
                  :context-errors)))
    (is (= 2 (-> {:_context {:prefix2 "https://foo.org/prefix2/"
                             :gamma   {:at/id "prefix2:gamma"}}
                  :alpha    9001}
                 c/validate-contexts
                 :context-key-errors
                 ::s/problems
                 count)))))

(def catch-profile
  (read-json-resource "sample_profiles/will-profile.json" ""))

(deftest validate-contexts-integration-test
  (testing "integration testing on Will's CATCH profile"
    (is (= {:context-errors nil :context-key-errors nil}
           (c/validate-contexts catch-profile)))))
