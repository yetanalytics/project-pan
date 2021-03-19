(ns com.yetanalytics.pan-test.errors-test
  (:require [clojure.test :refer [deftest testing is are]]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.objects.profile :as p]
            [com.yetanalytics.pan.objects.template :as t]
            [com.yetanalytics.pan.objects.pattern :as pt]
            [com.yetanalytics.pan.identifiers :as id]))

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
         [{:id        "https://foo.org/pattern-one"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-two"}
          {:id        "https://foo.org/pattern-two"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-one"}]))

;; Let's add patterns! But our pattern refers to itself
(def bad-profile-2g
  (assoc good-profile-2 :patterns
         [{:id        "https://foo.org/pattern-three"
           :type      "Pattern"
           :primary   true
           :oneOrMore "https://foo.org/pattern-three"}]))

;; This profile has two invalid contexts
(def bad-profile-3a
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
              :AttachmentUsageType "xapi:AttachmentUsageType"}
   :concepts [{:id       "https://foo.org/activity/1"
               :type     "Activity"
               :_context {:type    "@type"
                          :id      "@id"
                          :prov    "http://www.w3.org/ns/prov#"
                          :skos    "http://www.w3.org/2004/02/skos/core#"
                          :Profile "profile:Profile"}}]})

;; This profile has two instances where keys cannot be expanded via @context
(def bad-profile-3b
  {:id       "https://foo.org/profile"
   :type     "Profile"
   :_context "https://w3id.org/xapi/profiles/context"
   :foo      "Bar"
   :baz      "Qux"
   :concepts [{:id       "https://foo.org/activity/1"
               :type     "Activity"
               :_context "https://w3id.org/xapi/profiles/activity-context"
               :hello    "World"}]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Profile metadata error

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
 :conformsTo \"https://w3id.org/xapi/profiles#1.0\",
 :author
 {:url \"https://www.yetanalytics.io\",
  :type \"Organization\",
  :name \"Yet Analytics\"},
 :versions
 [{:id \"https://w3id.org/xapi/catch/v1\",
   :generatedAtTime \"2017-12-22T22:30:00-07:00\"}]}

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
 :conformsTo \"https://w3id.org/xapi/profiles#1.0\",
 :author
 {:url \"https://www.yetanalytics.io\",
  :type \"Organization\",
  :name \"Yet Analytics\"},
 :versions
 [{:id \"https://w3id.org/xapi/catch/v1\",
   :generatedAtTime \"2017-12-22T22:30:00-07:00\"}]}

should be: \"Profile\"

-------------------------
Detected 2 errors
")

;; Statement Template error
;; Note: cljs version differs from clj by having an extra column in the table

(def err-msg-2
  #?(:clj
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
"
     :cljs
     "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"this-template-is-invalid\", :type \"FooBar\"}

should contain keys: :definition, :inScheme, :prefLabel

| key         | spec                                           |
|=============+================================================|
| :definition | (map-of                                        |
|             |  :com.yetanalytics.pan.axioms/language-tag     |
|             |  :com.yetanalytics.pan.axioms/lang-map-string  |
|             |  :min-count                                    |
|             |  1)                                            |
|-------------+------------------------------------------------|
| :inScheme   | (and                                           |
|             |  :com.yetanalytics.pan.axioms/string           |
|             |  (partial                                      |
|             |   re-matches                                   |
|             |   xapi-schema.spec.regex/AbsoluteIRIRegEx))    |
|-------------+------------------------------------------------|
| :prefLabel  | (map-of                                        |
|             |  :com.yetanalytics.pan.axioms/language-tag     |
|             |  :com.yetanalytics.pan.axioms/lang-map-string  |
|             |  :min-count                                    |
|             |  1)                                            |

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
"))

;; ID Error

(def err-msg-3
"
**** ID Errors ****

-- Spec failed --------------------

Identifier:
\"https://w3id.org/xapi/catch/v1\"

which occurs 2 times in the Profile

should be a unique identifier value

-- Spec failed --------------------

Identifier:
\"https://foo.org/template\"

which occurs 2 times in the Profile

should be a unique identifier value

-------------------------
Detected 2 errors
")

;; InScheme Error

(def err-msg-4
"
**** Version Errors ****

-- Spec failed --------------------

InScheme IRI:
\"https://foo.org/invalid\"

associated with the identifier:
\"https://foo.org/template\"

in a Profile with the following version IDs:
\"https://w3id.org/xapi/catch/v1\"
\"https://w3id.org/xapi/catch/v2\"

should be a valid version ID

-- Spec failed --------------------

InScheme IRI:
\"https://foo.org/also-invalid\"

associated with the identifier:
\"https://foo.org/template2\"

in a Profile with the following version IDs:
\"https://w3id.org/xapi/catch/v1\"
\"https://w3id.org/xapi/catch/v2\"

should be a valid version ID

-------------------------
Detected 2 errors
")

;; Template edge errors - nonexistent destination node

(def err-msg-5
"
**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
{:id \"https://foo.org/template\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://foo.org/dead-verb\",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:verb

should not link to non-existent Concept or Template

-- Spec failed --------------------

Statement Template:
{:id \"https://foo.org/template\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://foo.org/dead-aut1\",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:attachmentUsageType

should not link to non-existent Concept or Template

-------------------------
Detected 2 errors
")

;; Template edge errors - invalid destinations

(def err-msg-6
"
**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
{:id \"https://foo.org/template\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://foo.org/template2\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

via the property:
:verb

should link to type: \"Verb\"

-- Spec failed --------------------

Statement Template:
{:id \"https://foo.org/template\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://foo.org/template\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

via the property:
:attachmentUsageType

should not refer to itself

-------------------------
Detected 2 errors
")

;; Cyclic pattern error

(def err-msg-7
"
**** Pattern Cycle Errors ****

-- Spec failed --------------------

The following Patterns:
\"https://foo.org/pattern-one\"
\"https://foo.org/pattern-two\"

should not contain cyclical references

-------------------------
Detected 1 error
")

;; Pattern edge errors

(def err-msg-8
"
**** Pattern Edge Errors ****

-- Spec failed --------------------

Pattern:
{:id \"https://foo.org/pattern-three\",
 :type \"Pattern\",
 :primary true,
 ...}

that links to object:
{:id \"https://foo.org/pattern-three\",
 :type \"Pattern\",
 :oneOrMore ...,
 ...}

via the property:
:oneOrMore

and is used 1 time to link out to 1 object

should not refer to itself

-------------------------
Detected 1 error
")

;; Context errors

(def err-msg-9
"
**** Context Errors ****

-- Spec failed --------------------

Value:
\"profile:Profile\"

in context:
{:id \"@id\",
 :type \"@type\",
 :Profile \"profile:Profile\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

should be a simple term definition with a valid prefix

or

should be an expanded term definition with a valid prefix

-- Spec failed --------------------

Value:
\"xapi:Verb\"

in context:
{:id \"@id\",
 :type \"@type\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :Profile \"profile:Profile\",
 :Verb \"xapi:Verb\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

should be a simple term definition with a valid prefix

or

should be an expanded term definition with a valid prefix

-- Spec failed --------------------

Value:
\"xapi:ActivityType\"

in context:
{:id \"@id\",
 :type \"@type\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :Profile \"profile:Profile\",
 :Verb \"xapi:Verb\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

should be a simple term definition with a valid prefix

or

should be an expanded term definition with a valid prefix

-- Spec failed --------------------

Value:
\"xapi:AttachmentUsageType\"

in context:
{:id \"@id\",
 :type \"@type\",
 :ActivityType \"xapi:ActivityType\",
 :AttachmentUsageType \"xapi:AttachmentUsageType\",
 :Profile \"profile:Profile\",
 :Verb \"xapi:Verb\",
 :profile \"https://w3id.org/xapi/profiles/ontology#\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\"}

should be a JSON-LD context keyword

or

should be a JSON-LD prefix

or

should be a simple term definition with a valid prefix

or

should be an expanded term definition with a valid prefix

-------------------------
Detected 4 errors
")

;; Context key errors

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

should be expandable into an absolute IRI

or

should be a JSON-LD keyword

-- Spec failed --------------------

Value:
:foo

in object:
{:id \"https://foo.org/profile\",
 :type \"Profile\",
 :_context \"https://w3id.org/xapi/profiles/context\",
 :concepts [...],
 :baz \"Qux\",
 :foo \"Bar\"}

should be expandable into an absolute IRI

or

should be a JSON-LD keyword

-- Spec failed --------------------

Value:
:baz

in object:
{:id \"https://foo.org/profile\",
 :type \"Profile\",
 :_context \"https://w3id.org/xapi/profiles/context\",
 :concepts [...],
 :baz \"Qux\",
 :foo \"Bar\"}

should be expandable into an absolute IRI

or

should be a JSON-LD keyword

-------------------------
Detected 3 errors
")

(deftest expound-test
  (testing "error messages"
    (are [expected-str err-map] (= expected-str
                                   (with-out-str (e/expound-errors err-map)))
      err-msg-1 {:syntax-errors (p/validate bad-profile-1b)}
      err-msg-2 {:syntax-errors (p/validate bad-profile-2a)}
      err-msg-3 {:id-errors (id/validate-ids bad-profile-2b)}
      err-msg-4 {:in-scheme-errors (id/validate-in-schemes bad-profile-2c)}
      err-msg-5 {:concept-edge-errors nil
                 :pattern-edge-errors nil
                 :template-edge-errors
                 (t/validate-template-edges
                  (t/create-graph [] (:templates bad-profile-2d)))}
      err-msg-6 {:concept-edge-errors nil
                 :pattern-edge-errors nil
                 :template-edge-errors
                 (t/validate-template-edges
                  (t/create-graph [] (:templates bad-profile-2e)))}
      err-msg-7 {:pattern-cycle-errors
                 (pt/validate-pattern-tree
                  (pt/create-graph (:templates bad-profile-2f)
                                   (:patterns bad-profile-2f)))}
      err-msg-8 {:pattern-edge-errors
                 (pt/validate-pattern-edges
                  (pt/create-graph (:templates bad-profile-2g)
                                   (:patterns bad-profile-2g)))}
      err-msg-9 (ctx/validate-contexts bad-profile-3a)
      err-msg-10 (ctx/validate-contexts bad-profile-3b)))
  (testing "combining error messages"
    (is (= (str err-msg-3 err-msg-4)
           (-> {:id-errors (id/validate-ids bad-profile-2b)
                :in-scheme-errors (id/validate-in-schemes bad-profile-2c)}
               e/expound-errors
               with-out-str)))
    (is (= (str err-msg-6 err-msg-8 err-msg-7)
           (-> {:concept-edge-errors nil
                :template-edge-errors
                (t/validate-template-edges
                 (t/create-graph [] (:templates bad-profile-2e)))
                :pattern-edge-errors
                (pt/validate-pattern-edges
                 (pt/create-graph (:templates bad-profile-2g)
                                  (:patterns bad-profile-2g)))
                :pattern-cycle-errors
                (pt/validate-pattern-tree
                 (pt/create-graph (:templates bad-profile-2f)
                                  (:patterns bad-profile-2f)))}
               e/expound-errors
               with-out-str)))))
