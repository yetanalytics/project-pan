(ns com.yetanalytics.pan-test.errors-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.objects.profile :as p]
            [com.yetanalytics.pan.objects.profiles.author :as ah]
            [com.yetanalytics.pan.objects.template :as t]
            [com.yetanalytics.pan.objects.pattern :as pt]
            [com.yetanalytics.pan.identifiers :as id]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sample Profiles
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Metadata-only profile
(def good-profile-1
  {:id "https://w3id.org/xapi/catch"
   :type "Profile"
   :prefLabel {"en" "Catch"}
   :definition {"en" "The profile for the trinity education application CATCH"}
   :_context "https://w3id.org/xapi/profiles/context"
   :versions [{:id "https://w3id.org/xapi/catch/v1"
               :generatedAtTime "2017-12-22T22:30:00-07:00"}]
   :author {:url "https://www.yetanalytics.io"
            :type "Organization"
            :name "Yet Analytics"}
   :conformsTo "https://w3id.org/xapi/profiles#1.0"})

;; context properties is wrong
(def bad-profile-1a (assoc good-profile-1
                           :context "what the pineapple"))

;; id and type property are wrong
(def bad-profile-1b (assoc good-profile-1
                           :id "not an id"
                           :type "FooBar"))

;; id and author properties are wrong
(def bad-profile-1c (assoc good-profile-1
                           :id "not an id"
                           :author {:url "bad url" :type "Organization" :name "Yet Analytics"}))

;; Everything is just wrong
(def bad-profile-1d (-> good-profile-1
                        (assoc
                         :id "invalid"
                         :type "FooBar"
                         :prefLabel {"es" "uno"
                                     "en" false}
                         :definition {74 "definition"}
                         :versions [{:id "invalid"
                                     :generatedAtTime "not-a-timestamp"}]
                         :author {:url "bad"
                                  :type "BarFoo"
                                  :name 12345}
                         :conformsTo "stan loona")
                        (dissoc :_context)))

;; Profile with some Templates
(def good-profile-2
  (assoc good-profile-1
         :versions [{:id "https://w3id.org/xapi/catch/v1"
                     :generatedAtTime "2017-12-22T22:30:00-07:00"}
                    {:id "https://w3id.org/xapi/catch/v2"
                     :generatedAtTime "2018-12-22T22:30:00-07:00"}]
         :templates [{:id "https://foo.org/template"
                      :type "StatementTemplate"
                      :inScheme "https://w3id.org/xapi/catch/v1"
                      :prefLabel {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :rules [{:location "$.actor.mbox"
                               :all ["yet@yetanalytics.io"]}]}
                     {:id "https://foo.org/template2"
                      :type "StatementTemplate"
                      :inScheme "https://w3id.org/xapi/catch/v1"
                      :prefLabel {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :rules [{:location "$.actor.name"
                               :all ["Yet Analytics"]}]}]))

;; Nuke the first Template
(def bad-profile-2a
  (assoc-in good-profile-2 [:templates 0]
            {:id "this-template-is-invalid"}))

;; Make IDs duplicate
(def bad-profile-2b
  (-> good-profile-2
      (assoc-in [:versions 1 :id] "https://w3id.org/xapi/catch/v1")
      (assoc-in [:templates 1 :id] "https://foo.org/template")))

;; Invalidate inScheme values


(def bad-profile-2c
  (-> good-profile-2
      (assoc-in [:templates 0 :inScheme] "https://foo.org/invalid")
      (assoc-in [:templates 1 :inScheme] "https://foo.org/also-invalid")))

;; Add a bunch of dead links
(def bad-profile-2d
  (-> good-profile-2
      (assoc-in [:templates 0 :verb] "https://foo.org/dead-verb")
      (assoc-in [:templates 0 :attachmentUsageType] ["https://foo.org/dead-aut1"])))

;; Add a bunch of invalid links
(def bad-profile-2e
  (-> good-profile-2
      (assoc-in [:templates 0 :verb] "https://foo.org/template2")
      (assoc-in [:templates 0 :attachmentUsageType] ["https://foo.org/template"])))

;; Let's add patterns! But they have cycles
(def bad-profile-2f
  (assoc good-profile-2 :patterns
         [{:id "https://foo.org/pattern-one"
           :type "Pattern"
           :primary true
           :oneOrMore "https://foo.org/pattern-two"}
          {:id "https://foo.org/pattern-two"
           :type "Pattern"
           :primary true
           :oneOrMore "https://foo.org/pattern-one"}]))

;; Let's add patterns! But our pattern refers to itself
(def bad-profile-2g
  (assoc good-profile-2 :patterns
         [{:id "https://foo.org/pattern-three"
           :type "Pattern"
           :primary true
           :oneOrMore "https://foo.org/pattern-three"}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest group-by-in-test
  (testing "group-by-in function"
    (is (= (p/validate bad-profile-1a)
           (first (e/group-by-in (p/validate bad-profile-1a)))))
    (is (= (-> bad-profile-1a p/validate e/group-by-in first ::s/problems first :in)
           (-> bad-profile-1a p/validate e/group-by-in first ::s/problems second :in)))))

(deftest compare-properties-test
  (testing "compare-properties function"
    (is (neg? (e/compare-properties :id :aaaaaaah)))
    (is (neg? (e/compare-properties :id :type)))
    (is (neg? (e/compare-properties :aaah :aarg)))
    (is (zero? (e/compare-properties :same :same)))
    (is (zero? (e/compare-properties :id :id)))
    (is (= '(:id :type :inScheme :prefLabel :definition :deprecated)
           (sort e/compare-properties
                 '(:prefLabel :definition :inScheme :id :deprecated :type))))))

(deftest compare-arrs-test
  (testing "compare-arrs function"
    (is (= '([:a] [:z] [:a :b]) (sort '([:z] [:a :b] [:a]))))
    (is (= '([:a] [:a :b] [:z]) (sort e/compare-arrs '([:z] [:a :b] [:a]))))
    (is (neg? (e/compare-arrs [:author] [:template])))
    (is (neg? (e/compare-arrs [:author] [:author :url])))
    (is (neg? (e/compare-arrs [:author] [:template :id])))
    (is (neg? (e/compare-arrs [:author :url] [:template])))
    (is (neg? (e/compare-arrs [] [:id])))))

(deftest sort-by-path-test
  (testing "sort-by-path function"
    (is (= [:id]
           (-> bad-profile-1b p/validate e/group-by-in e/sort-by-path first ::s/problems first :path)))
    (is (= [:type]
           (-> bad-profile-1b p/validate e/group-by-in e/sort-by-path second ::s/problems first :path)))
    (is (= [:id]
           (-> bad-profile-1c p/validate e/group-by-in e/sort-by-path first ::s/problems first :path)))
    (is (= [:author :url]
           (-> bad-profile-1c p/validate e/group-by-in e/sort-by-path second ::s/problems first :path)))
    (is (= [[] [:id] [:type] [:conformsTo] [:prefLabel 1] [:definition 0]
            [:author :type] [:author :name] [:author :url]
            [:versions :id] [:versions :generatedAtTime]]
           (->> bad-profile-1d p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :path)))))))

(deftest spec-test
  (testing "ensure that correct specs are returned in problem maps"
    (is (= [::p/profile ::ax/iri ::p/type ::ax/uri ::ax/lang-map-string ::ax/language-tag
            ::ah/type ::ax/string ::ax/url ::ax/iri ::ax/timestamp]
           (->> bad-profile-1d p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))))
    (is (= [::t/template ::ax/uri]
           (->> bad-profile-2a p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))))
    (is (= [::id/one-count ::id/one-count]
           (->> bad-profile-2b id/validate-ids e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))))
    (is (= [::id/in-scheme ::id/in-scheme]
           (->> bad-profile-2c id/validate-in-schemes e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))))))

(deftest expound-test-silent
  (testing "error/expound-errors error messages with 'silent' set to true"
    (is (= ""
           (with-out-str
             (e/expound-error-map (p/validate bad-profile-1b)
                                  :silent true))))
    (is (= ""
           (with-out-str
             (e/expound-error (id/validate-ids bad-profile-2b)
                              :error-type "id"
                              :silent true))))
    (is (= ""
           (with-out-str
             (e/expound-error (id/validate-in-schemes bad-profile-2c)
                              :error-type "in-scheme"
                              :silent true))))
    (is (= ""
           (with-out-str
             (e/expound-error-list
              (:context-key-errors
               (ctx/validate-contexts {:id       "https://foo.org/profile"
                                       :type     "Profile"
                                       :_context "https://w3id.org/xapi/profiles/context"
                                       :foo      "Bar"
                                       :baz      "Qux"}))
              :silent true))))))

(deftest expound-test
  (testing "error/expound-errors error messages"
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "  {:id \"not an id\",\n"
                "       ^^^^^^^^^^^\n"
                "   :type ...,\n"
                "   :prefLabel ...,\n"
                "   :definition ...,\n"
                "   :_context ...,\n"
                "   :versions ...,\n"
                "   :author ...,\n"
                "   :conformsTo ...}\n"
                "\n"
                "should be a valid IRI\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n"
                "\n"
                "-- Spec failed --------------------\n"
                "\n"
                "  {:id ...,\n"
                "   :type \"FooBar\",\n"
                "         ^^^^^^^^\n"
                "   :prefLabel ...,\n"
                "   :definition ...,\n"
                "   :_context ...,\n"
                "   :versions ...,\n"
                "   :author ...,\n"
                "   :conformsTo ...}\n"
                "\n"
                "should be: \"Profile\"\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n"
                "\n"
                "")
           (with-out-str
             (e/expound-error-map (p/validate bad-profile-1b)))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Duplicate id: \"https://w3id.org/xapi/catch/v1\"\n"
                " with count: 2\n"
                "\n"
                "the id value is not unique\n"
                "\n"
                "-- Spec failed --------------------\n"
                "\n"
                "Duplicate id: \"https://foo.org/template\"\n"
                " with count: 2\n"
                "\n"
                "the id value is not unique\n"
                "\n"
                "-------------------------\n"
                "Detected 2 errors\n")
           (with-out-str
             (e/expound-error (id/validate-ids bad-profile-2b)
                              :error-type "id"))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Invalid inScheme: \"https://foo.org/invalid\"\n"
                " at object: \"https://foo.org/template\"\n"
                " profile version ids:\n"
                "  https://w3id.org/xapi/catch/v2\n"
                "  https://w3id.org/xapi/catch/v1\n"
                "\n"
                "the inScheme value is not a valid version ID\n"
                "\n"
                "-- Spec failed --------------------\n"
                "\n"
                "Invalid inScheme: \"https://foo.org/also-invalid\"\n"
                " at object: \"https://foo.org/template2\"\n"
                " profile version ids:\n"
                "  https://w3id.org/xapi/catch/v2\n"
                "  https://w3id.org/xapi/catch/v1\n"
                "\n"
                "the inScheme value is not a valid version ID\n"
                "\n"
                "-------------------------\n"
                "Detected 2 errors\n")
           (with-out-str
             (e/expound-error (id/validate-in-schemes bad-profile-2c)
                              :error-type "in-scheme"))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Invalid verb identifier:\n"
                " \"https://foo.org/dead-verb\"\n"
                "\n"
                " at object:\n"
                "  {:id \"https://foo.org/template\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                " linked object:\n"
                "  {:id \"https://foo.org/dead-verb\",\n"
                "   :type nil,\n"
                "   :inScheme nil,\n"
                "   ...}\n"
                "\n"
                "linked concept or template does not exist\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n"
                "-- Spec failed --------------------\n"
                "\n"
                "Invalid attachmentUsageType identifier:\n"
                " \"https://foo.org/dead-aut1\"\n"
                "\n"
                " at object:\n"
                "  {:id \"https://foo.org/template\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                " linked object:\n"
                "  {:id \"https://foo.org/dead-aut1\",\n"
                "   :type nil,\n"
                "   :inScheme nil,\n"
                "   ...}\n"
                "\n"
                "linked concept or template does not exist\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n")
          (with-out-str
            (e/expound-error-map (t/explain-graph
                                  (t/create-graph [] (:templates bad-profile-2d)))
                                 :error-type "edge"))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Invalid verb identifier:\n"
                " \"https://foo.org/template2\"\n"
                "\n"
                " at object:\n"
                "  {:id \"https://foo.org/template\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                " linked object:\n"
                "  {:id \"https://foo.org/template2\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                "should link to type: \"Verb\"\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n"
                "-- Spec failed --------------------\n"
                "\n"
                "Invalid attachmentUsageType identifier:\n"
                " \"https://foo.org/template\"\n"
                "\n"
                " at object:\n"
                "  {:id \"https://foo.org/template\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                " linked object:\n"
                "  {:id \"https://foo.org/template\",\n"
                "   :type \"StatementTemplate\",\n"
                "   :inScheme \"https://w3id.org/xapi/catch/v1\",\n"
                "   ...}\n"
                "\n"
                "object cannot refer to itself\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n")
           (with-out-str
             (e/expound-error-map (t/explain-graph
                                   (t/create-graph [] (:templates bad-profile-2e)))
                                  :error-type "edge"))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Cycle detected involving the following nodes:\n"
                "  https://foo.org/pattern-one\n"
                "  https://foo.org/pattern-two\n"
                "\n"
                "cyclical reference detected\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n")
           (with-out-str
             (e/expound-error (pt/explain-graph-cycles
                               (pt/create-graph (:templates bad-profile-2f)
                                                (:patterns bad-profile-2f)))
                              :error-type "cycle"))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "Invalid oneOrMore identifier:\n"
                " \"https://foo.org/pattern-three\"\n"
                "\n"
                " at object:\n"
                "  {:id \"https://foo.org/pattern-three\",\n"
                "   :type \"Pattern\",\n"
                "   :primary true,\n"
                "   ...}\n"
                "\n"
                " linked object:\n"
                "   {:id \"https://foo.org/pattern-three\",\n"
                "    :type \"Pattern\",\n"
                "    :oneOrMore ...,\n"
                "    ...}\n"
                "\n"
                " pattern is used 1 time in the profile\n"
                " and links out to 1 other object.\n"
                "\n"
                "object cannot refer to itself\n"
                "\n"
                "-------------------------\n"
                "Detected 1 error\n")
           (with-out-str
             (e/expound-error (pt/explain-graph
                               (pt/create-graph (:templates bad-profile-2g)
                                                (:patterns bad-profile-2g)))
                              :error-type "edge"))))))

(deftest context-error-msg-tests
  (testing "@context-related error messages"
    (is (= (str
            "-- Spec failed --------------------\n"
            "\n"
            "  {:Profile ...,\n"
            "   :Verb \"xapi:Verb\",\n"
            "         ^^^^^^^^^^^\n"
            "   :ActivityType ...,\n"
            "   :AttachmentUsageType ...}\n"
            "\n"
            "simple term definition does not have valid prefix\n"
            "\n"
            "or\n"
            "\n"
            "expanded term definition does not have valid prefix\n"
            "\n"
            "-- Spec failed --------------------\n"
            "\n"
            "  {:Profile ...,\n"
            "   :Verb ...,\n"
            "   :ActivityType \"xapi:ActivityType\",\n"
            "                 ^^^^^^^^^^^^^^^^^^^\n"
            "   :AttachmentUsageType ...}\n"
            "\n"
            "simple term definition does not have valid prefix\n"
            "\n"
            "or\n"
            "\n"
            "expanded term definition does not have valid prefix\n"
            "\n"
            "-- Spec failed --------------------\n"
            "\n"
            "  {:Profile ...,\n"
            "   :Verb ...,\n"
            "   :ActivityType ...,\n"
            "   :AttachmentUsageType \"xapi:AttachmentUsageType\"}\n"
            "                        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
            "\n"
            "simple term definition does not have valid prefix\n"
            "\n"
            "or\n"
            "\n"
            "expanded term definition does not have valid prefix\n"
            "\n"
            "-------------------------\n"
            "Detected 3 errors\n"
            "\n")
           (with-out-str
             (e/expound-error-list
              (:context-errors
               (ctx/validate-contexts
                {:id       "https://foo.org/profile"
                 :type     "Profile"
                 :_context {:type                "@type"
                            :id                  "@id"
                            :prov                "http://www.w3.org/ns/prov#"
                            :skos                "http://www.w3.org/2004/02/skos/core#"
                            :profile             "https://w3id.org/xapi/profiles/ontology#"
                            :Profile             "profile:Profile"
                            :Verb                "xapi:Verb"
                            :ActivityType        "xapi:ActivityType"
                            :AttachmentUsageType "xapi:AttachmentUsageType"}}))))))
    (is (= (str "-- Spec failed --------------------\n"
                "\n"
                "  {:id ...,\n"
                "   :type ...,\n"
                "   :_context ...,\n"
                "   :baz ...,\n"
                "   :foo ...}\n"
                "   ^^^^\n"
                "\n"
                "key cannot be expanded into absolute IRI\n"
                "\n"
                "or\n"
                "\n"
                "key is not JSON-LD keyword\n"
                "\n"
                "-- Spec failed --------------------\n"
                "\n"
                "  {:id ...,\n"
                "   :type ...,\n"
                "   :_context ...,\n"
                "   :foo ...,\n"
                "   :baz ...}\n"
                "   ^^^^\n"
                "\n"
                "key cannot be expanded into absolute IRI\n"
                "\n"
                "or\n"
                "\n"
                "key is not JSON-LD keyword\n"
                "\n"
                "-------------------------\n"
                "Detected 2 errors\n"
                "\n")
           (with-out-str
             (e/expound-error-list
              (:context-key-errors
               (ctx/validate-contexts
                {:id       "https://foo.org/profile"
                 :type     "Profile"
                 :_context "https://w3id.org/xapi/profiles/context"
                 :foo      "Bar"
                 :baz      "Qux"}))))))))