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
  (assoc-in good-profile-2 [:templates 0] {:id   "this-template-is-invalid"
                                           :type "FooBar"}))

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

(def err-msg-1 
"
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"not an id\"

of property:
:id

in object:
{:id \"not an id\",
 :type \"FooBar\",
 :prefLabel {\"en\" \"Catch\"},
 :definition
 {\"en\" \"The profile for the trinity education application CATCH\"},
 :_context \"https://w3id.org/xapi/profiles/context\",
 :versions
 [{:id \"https://w3id.org/xapi/catch/v1\",
   :generatedAtTime \"2017-12-22T22:30:00-07:00\"}],
 :author
 {:url \"https://www.yetanalytics.io\",
  :type \"Organization\",
  :name \"Yet Analytics\"},
 :conformsTo \"https://w3id.org/xapi/profiles#1.0\"}

should be a valid IRI

-- Spec failed --------------------

Value:
\"FooBar\"

of property:
:type

in object:
{:id \"not an id\",
 :type \"FooBar\",
 :prefLabel {\"en\" \"Catch\"},
 :definition
 {\"en\" \"The profile for the trinity education application CATCH\"},
 :_context \"https://w3id.org/xapi/profiles/context\",
 :versions
 [{:id \"https://w3id.org/xapi/catch/v1\",
   :generatedAtTime \"2017-12-22T22:30:00-07:00\"}],
 :author
 {:url \"https://www.yetanalytics.io\",
  :type \"Organization\",
  :name \"Yet Analytics\"},
 :conformsTo \"https://w3id.org/xapi/profiles#1.0\"}

should be: \"Profile\"

-------------------------
Detected 2 errors
")

(def err-msg-2
"
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"this-template-is-invalid\", :type \"FooBar\"}

should contain keys: :definition, :inScheme, :prefLabel

| key         | spec                                          |
|=============+===============================================|
| :definition | (map-of                                       |
|             |  :com.yetanalytics.pan.axioms/language-tag    |
|             |  :com.yetanalytics.pan.axioms/lang-map-string |
|             |  :min-count                                   |
|             |  1)                                           |
|-------------+-----------------------------------------------|
| :inScheme   | (and                                          |
|             |  :com.yetanalytics.pan.axioms/string          |
|             |  (partial                                     |
|             |   re-matches                                  |
|             |   xapi-schema.spec.regex/AbsoluteIRIRegEx))   |
|-------------+-----------------------------------------------|
| :prefLabel  | (map-of                                       |
|             |  :com.yetanalytics.pan.axioms/language-tag    |
|             |  :com.yetanalytics.pan.axioms/lang-map-string |
|             |  :min-count                                   |
|             |  1)                                           |

-- Spec failed --------------------

Value:
\"this-template-is-invalid\"

of property:
:id

in object:
{:id \"this-template-is-invalid\", :type \"FooBar\"}

should be a valid URI

-- Spec failed --------------------

Value:
\"FooBar\"

of property:
:type

in object:
{:id \"this-template-is-invalid\", :type \"FooBar\"}

should be: \"StatementTemplate\"

-------------------------
Detected 3 errors
")

(def err-msg-3
"
**** ID Errors ****

-- Spec failed --------------------

Duplicate id: https://w3id.org/xapi/catch/v1
 with count:  2

the id value is not unique

-- Spec failed --------------------

Duplicate id: https://foo.org/template
 with count:  2

the id value is not unique

-------------------------
Detected 2 errors
")

(def err-msg-4
"
**** Version Errors ****

-- Spec failed --------------------

Invalid inScheme: https://foo.org/invalid
 at object: https://foo.org/template
 profile version ids https://w3id.org/xapi/catch/v2
  https://w3id.org/xapi/catch/v1

the inScheme value is not a valid version ID

-- Spec failed --------------------

Invalid inScheme: https://foo.org/also-invalid
 at object: https://foo.org/template2
 profile version ids https://w3id.org/xapi/catch/v2
  https://w3id.org/xapi/catch/v1

the inScheme value is not a valid version ID

-------------------------
Detected 2 errors
")

(def err-msg-5
"
**** Template Edge Errors ****

-- Spec failed --------------------

Invalid :verb identifier: https://foo.org/dead-verb

 at object:
  {:id \"https://foo.org/template\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

 linked object:
  {:id \"https://foo.org/dead-verb\",
   :type \"null\",
   :inScheme \"null\",
   ...}

linked concept or template does not exist

-- Spec failed --------------------

Invalid :attachmentUsageType identifier: https://foo.org/dead-aut1

 at object:
  {:id \"https://foo.org/template\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

 linked object:
  {:id \"https://foo.org/dead-aut1\",
   :type \"null\",
   :inScheme \"null\",
   ...}

linked concept or template does not exist

-------------------------
Detected 2 errors
")

(def err-msg-6
"
**** Template Edge Errors ****

-- Spec failed --------------------

Invalid :verb identifier: https://foo.org/template2

 at object:
  {:id \"https://foo.org/template\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

 linked object:
  {:id \"https://foo.org/template2\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

should link to type: \"Verb\"

-- Spec failed --------------------

Invalid :attachmentUsageType identifier: https://foo.org/template

 at object:
  {:id \"https://foo.org/template\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

 linked object:
  {:id \"https://foo.org/template\",
   :type \"StatementTemplate\",
   :inScheme \"https://w3id.org/xapi/catch/v1\",
   ...}

object cannot refer to itself

-------------------------
Detected 2 errors
")

(def err-msg-7
"
**** Pattern Cycle Errors ****

-- Spec failed --------------------

Cycle detected involving the following nodes:
  https://foo.org/pattern-one
  https://foo.org/pattern-two

cyclical reference detected

-------------------------
Detected 1 error
")

(def err-msg-8
"
**** Pattern Edge Errors ****

-- Spec failed --------------------

Invalid :oneOrMore identifier: https://foo.org/pattern-three

 at object:
  {:id \"https://foo.org/pattern-three\",
   :type \"Pattern\",
   :primary true,
   ...}

 linked object:
   {:id \"https://foo.org/pattern-three\",
    :type \"Pattern\",
    :oneOrMore ...,
    ...}

 pattern is used 1 time in the profile and links out to 1 other object.

object cannot refer to itself

-------------------------
Detected 1 error
")

(def err-msg-9
"
**** Context Errors ****

-- Spec failed --------------------

Value:
\"profile:Profile\"

in context:
{:type \"@type\",
 :id \"@id\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\",
 :Profile \"profile:Profile\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-- Spec failed --------------------

Value:
\"xapi:Verb\"

in context:
{:Profile \"profile:Profile\",
 :type \"@type\",
 :id \"@id\",
 :prov \"http://www.w3.org/ns/prov#\",
 :Verb \"xapi:Verb\",
 :skos \"http://www.w3.org/2004/02/skos/core#\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-- Spec failed --------------------

Value:
\"xapi:ActivityType\"

in context:
{:Profile \"profile:Profile\",
 :type \"@type\",
 :id \"@id\",
 :prov \"http://www.w3.org/ns/prov#\",
 :Verb \"xapi:Verb\",
 :skos \"http://www.w3.org/2004/02/skos/core#\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-- Spec failed --------------------

Value:
\"xapi:AttachmentUsageType\"

in context:
{:Profile \"profile:Profile\",
 :type \"@type\",
 :id \"@id\",
 :prov \"http://www.w3.org/ns/prov#\",
 :Verb \"xapi:Verb\",
 :skos \"http://www.w3.org/2004/02/skos/core#\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-------------------------
Detected 4 errors
")

(def err-msg-10
"
**** Context Key Errors ****

-- Spec failed --------------------

Value:
:hello

in object:
{:id \"https://foo.org/activity/1\",
 :type \"Activity\",
 :_context \"https://w3id.org/xapi/profiles/activity-context\",
 :hello \"World\"}

key cannot be expanded into absolute IRI

or

key is not JSON-LD keyword

-- Spec failed --------------------

Value:
:foo

in object:
{:id \"https://foo.org/profile\",
 :type \"Profile\",
 :_context \"https://w3id.org/xapi/profiles/context\",
 :foo \"Bar\",
 :baz \"Qux\",
 :concepts [...]}

key cannot be expanded into absolute IRI

or

key is not JSON-LD keyword

-- Spec failed --------------------

Value:
:baz

in object:
{:id \"https://foo.org/profile\",
 :type \"Profile\",
 :_context \"https://w3id.org/xapi/profiles/context\",
 :foo \"Bar\",
 :baz \"Qux\",
 :concepts [...]}

key cannot be expanded into absolute IRI

or

key is not JSON-LD keyword

-------------------------
Detected 3 errors
")

(deftest expound-test
  (testing "error/expound-errors error messages"
    (is (= err-msg-1
           (-> {:syntax-errors (p/validate bad-profile-1b)}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-2
           (-> {:syntax-errors (p/validate bad-profile-2a)}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-3
           (-> {:id-errors (id/validate-ids bad-profile-2b)}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-4
           (-> {:in-scheme-errors (id/validate-in-schemes bad-profile-2c)}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-5
           (-> {:concept-edge-errors nil
                :pattern-edge-errors nil
                :template-edge-errors
                (t/validate-template-edges
                 (t/create-graph [] (:templates bad-profile-2d)))}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-6
           (-> {:concept-edge-errors nil
                :pattern-edge-errors nil
                :template-edge-errors
                (t/validate-template-edges
                 (t/create-graph [] (:templates bad-profile-2e)))}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-7
           (-> {:pattern-cycle-errors
                (pt/validate-pattern-tree
                 (pt/create-graph (:templates bad-profile-2f)
                                  (:patterns bad-profile-2f)))}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-8
           (-> {:pattern-edge-errors
                (pt/validate-pattern-edges
                 (pt/create-graph (:templates bad-profile-2g)
                                  (:patterns bad-profile-2g)))}
               e/expound-errors
               with-out-str)))
    (is (= err-msg-9
           (-> {:id       "https://foo.org/profile"
                :type     "Profile"
                :_context {:type                "@type"
                           :id                  "@id"
                           :prov                "http://www.w3.org/ns/prov#"
                           :skos                "http://www.w3.org/2004/02/skos/core#"
                           :profile             "https://w3id.org/xapi/profiles/ontology#"
                           :Profile             "profile:Profile"
                           :Verb                "xapi:Verb"
                           :ActivityType        "xapi:ActivityType"
                           :AttachmentUsageType "xapi:AttachmentUsageType"}
                :concepts [{:id       "https://foo.org/activity/1"
                            :type     "Activity"
                            :_context {:type    "@type"
                                       :id      "@id"
                                       :prov    "http://www.w3.org/ns/prov#"
                                       :skos    "http://www.w3.org/2004/02/skos/core#"
                                       :Profile "profile:Profile"}}]}
               ctx/validate-contexts
               e/expound-errors
               with-out-str)))
    (is (= err-msg-10
           (-> {:id       "https://foo.org/profile"
                :type     "Profile"
                :_context "https://w3id.org/xapi/profiles/context"
                :foo      "Bar"
                :baz      "Qux"
                :concepts [{:id       "https://foo.org/activity/1"
                            :type     "Activity"
                            :_context "https://w3id.org/xapi/profiles/activity-context"
                            :hello    "World"}]}
               ctx/validate-contexts
               e/expound-errors
               with-out-str)))))
