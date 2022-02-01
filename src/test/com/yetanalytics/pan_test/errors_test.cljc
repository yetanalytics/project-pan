(ns com.yetanalytics.pan-test.errors-test
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.string :as cstr]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.objects.profile :as p]
            [com.yetanalytics.pan.objects.template :as t]
            [com.yetanalytics.pan.objects.pattern :as pt]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan-test.errors-test-fixtures :as fix]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sample Profiles
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Metadata-only profile
(def good-profile-1
  {:id         "https://w3id.org/xapi/catch"
   :type       "Profile"
   :prefLabel  {"en" "Catch"}
   :definition {"en" "The profile for the trinity education application CATCH"}
   :_context   "https://w3id.org/xapi/profiles/context"
   :versions   [{:id "https://w3id.org/xapi/catch/v1"
                 :generatedAtTime "2017-12-22T22:30:00-07:00"}]
   :author     {:url  "https://www.yetanalytics.io"
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
                           :author {:url  "bad url"
                                    :type "Organization"
                                    :name "Yet Analytics"}))

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
                         :author {:url  "bad"
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
         :templates [{:id         "https://foo.org/template"
                      :type       "StatementTemplate"
                      :inScheme   "https://w3id.org/xapi/catch/v1"
                      :prefLabel  {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :rules      [{:location "$.actor.mbox"
                                    :all      ["yet@yetanalytics.io"]}]}
                     {:id         "https://foo.org/template2"
                      :type       "StatementTemplate"
                      :inScheme   "https://w3id.org/xapi/catch/v1"
                      :prefLabel  {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :rules      [{:location "$.actor.name"
                                    :all      ["Yet Analytics"]}]}]))

;; Invalid Concept
(def bad-profile-2a
  (assoc good-profile-2
         :concepts
         [{:id                       "https://foo.org/bad-concept"
           :type                     "ContextExtension"
           :inScheme                 "https://w3id.org/xapi/catch/v1"
           :prefLabel                {"en" "Bad Concept"}
           :definition               {"en" "foo bar"}
           :recommendedVerbs         ["not an iri"]
           :inlineSchema             "{\"type\": \"notAType\"}"}]))

;; Nuke the first Template
(def bad-profile-2b
  (assoc-in good-profile-2 [:templates 0] {:id   "this-template-is-invalid"
                                           :type "FooBar"}))

;; Make IDs duplicate
(def bad-profile-2c
  (-> good-profile-2
      (assoc-in [:versions 1 :id] "https://w3id.org/xapi/catch/v1")
      (assoc-in [:templates 1 :id] "https://foo.org/template")))

;; Invalidate inScheme values
(def bad-profile-2d
  (-> good-profile-2
      (assoc-in [:templates 0 :inScheme] "https://foo.org/invalid")
      (assoc-in [:templates 1 :inScheme] "https://foo.org/also-invalid")))

;; Add a bunch of dead links
(def bad-profile-2e
  (-> good-profile-2
      (assoc-in [:templates 0 :verb] "https://foo.org/dead-verb")
      (assoc-in [:templates 0 :attachmentUsageType] ["https://foo.org/dead-aut1"])))

;; Add a bunch of invalid links
(def bad-profile-2f
  (-> good-profile-2
      (assoc-in [:templates 0 :verb] "https://foo.org/template2")
      (assoc-in [:templates 0 :attachmentUsageType] ["https://foo.org/template"])))

;; Let's add patterns! But they have cycles
(def bad-profile-2g
  (assoc good-profile-2
         :patterns
         [{:id        "https://foo.org/pattern-one"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-two"}
          {:id        "https://foo.org/pattern-two"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-one"}]))

;; Let's add patterns! But our pattern refers to itself
(def bad-profile-2h
  (assoc good-profile-2
         :patterns
         [{:id        "https://foo.org/pattern-three"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-three"}]))

;; This profile has two invalid contexts
(def bad-profile-3a
  (assoc good-profile-1
         :concepts
         [{:id       "https://foo.org/activity/1"
           :type     "Activity"
           :inScheme "https://w3id.org/xapi/catch/v1"
           :activityDefinition
           {:_context "https://w3id.org/xapi/profiles/activity-context"
            :extensions
            {"http://foo.org"
             {:_context {:type    "@type"
                         :id      "@id"
                         :prov    "http://www.w3.org/ns/prov#"
                         :skos    "http://www.w3.org/2004/02/skos/core#"
                         :Profile {:id "bee"}}}}}}]))

;; This profile has three instances where keys cannot be expanded via @context
(def bad-profile-3b
  {:id       "https://foo.org/profile"
   :type     "Profile"
   :_context "https://w3id.org/xapi/profiles/context"
   :foo      "Bar"
   :baz      "Qux"
   :concepts [{:id       "https://foo.org/activity/1"
               :type     "Activity"
               :activityDefinition
               {:_context "https://w3id.org/xapi/profiles/activity-context"
                :id       "https://foo.org/activity-name-1"
                :type     "https://foo.org/activity-type-1"
                :hello    "World"}}]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest expound-test
  (testing "error messages"
    (are [expected-str err-map] (= expected-str
                                   (with-out-str (e/print-errors err-map)))
      fix/err-msg-1 {:syntax-errors (p/validate bad-profile-1b)}
      fix/err-msg-2 {:syntax-errors (p/validate bad-profile-2a)}
      fix/err-msg-3 {:syntax-errors (p/validate bad-profile-2b)}
      fix/err-msg-4 {:id-errors (id/validate-ids bad-profile-2c)}
      fix/err-msg-5 {:in-scheme-errors (id/validate-in-schemes bad-profile-2d)}
      fix/err-msg-6 {:concept-edge-errors nil
                     :pattern-edge-errors nil
                     :template-edge-errors
                     (t/validate-template-edges
                      (t/create-graph bad-profile-2e))}
      fix/err-msg-7 {:concept-edge-errors nil
                     :pattern-edge-errors nil
                     :template-edge-errors
                     (t/validate-template-edges
                      (t/create-graph bad-profile-2f))}
      fix/err-msg-8 {:pattern-cycle-errors
                     (pt/validate-pattern-tree
                      (pt/create-graph bad-profile-2g))}
      fix/err-msg-9 {:pattern-edge-errors
                     (pt/validate-pattern-edges
                      (pt/create-graph bad-profile-2h))}
      fix/err-msg-10 {:syntax-errors
                      (p/validate bad-profile-3a)}
      fix/err-msg-11 {:context-errors
                      (ctx/validate-contexts bad-profile-3b)}))
  (testing "combining error messages"
    (is (= (str fix/err-msg-4 fix/err-msg-5)
           (-> {:id-errors (id/validate-ids bad-profile-2c)
                :in-scheme-errors (id/validate-in-schemes bad-profile-2d)}
               e/errors->string)))
    (is (= (str fix/err-msg-7 fix/err-msg-9 fix/err-msg-8)
           (-> {:concept-edge-errors nil
                :template-edge-errors
                (t/validate-template-edges
                 (t/create-graph bad-profile-2f))
                :pattern-edge-errors
                (pt/validate-pattern-edges
                 (pt/create-graph bad-profile-2h))
                :pattern-cycle-errors
                (pt/validate-pattern-tree
                 (pt/create-graph bad-profile-2g))}
               e/errors->string)))))

(deftest error-data-structures
  (testing "error string list map"
    (is (= {:syntax-errors fix/err-msg-1-list}
           (e/errors->string-vec-map {:syntax-errors (p/validate bad-profile-1b)})))
    (is (= {}
           (e/errors->string-vec-map nil))))
  (testing "error string map"
    (is (= {:syntax-errors
            (cstr/replace fix/err-msg-1 #"\s\*+ Syntax Errors \*+\s*" "")}
           (e/errors->string-map {:syntax-errors (p/validate bad-profile-1b)})))
    (is (= {}
           (e/errors->string-map nil))))
  (testing "error string"
    (is (= fix/err-msg-1
           (e/errors->string {:syntax-errors (p/validate bad-profile-1b)})))
    (is (= ""
           (e/errors->string nil)))))
