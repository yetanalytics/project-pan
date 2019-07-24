(ns com.yetanalytics.context-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.context :as c]
            [com.yetanalytics.utils :refer :all]))

(def profile-context
  (c/get-context "https://w3id.org/xapi/profiles/context"))

#_(:profile profile-context)

(def activity-context
  (c/get-context "https://w3id.org/xapi/profiles/activity-context"))

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
