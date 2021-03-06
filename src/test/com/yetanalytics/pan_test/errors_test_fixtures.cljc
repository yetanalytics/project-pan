(ns com.yetanalytics.pan-test.errors-test-fixtures)

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

;; Concept error

(def err-msg-2
  "
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"not an iri\"

of property:
:recommendedVerbs

in object:
{:id \"https://foo.org/bad-concept\",
 :type \"ContextExtension\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :prefLabel {\"en\" \"Bad Concept\"},
 :definition {\"en\" \"foo bar\"},
 :recommendedVerbs [\"not an iri\"],
 :inlineSchema \"{\\\"type\\\": \\\"notAType\\\"}\"}

should be a valid IRI

-- Spec failed --------------------

Value:
\"{\\\"type\\\": \\\"notAType\\\"}\"

of property:
:inlineSchema

in object:
{:id \"https://foo.org/bad-concept\",
 :type \"ContextExtension\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :prefLabel {\"en\" \"Bad Concept\"},
 :definition {\"en\" \"foo bar\"},
 :recommendedVerbs [\"not an iri\"],
 :inlineSchema \"{\\\"type\\\": \\\"notAType\\\"}\"}

should be a valid JSON schema

-------------------------
Detected 2 errors
")

;; Statement Template error
;; Note: cljs version differs from clj by having an extra column in the table

(def err-msg-3
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

(def err-msg-4
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

(def err-msg-5
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

(def err-msg-7
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

(def err-msg-8
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

(def err-msg-9
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

(def err-msg-10
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

(def err-msg-11
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