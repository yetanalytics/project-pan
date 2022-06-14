(ns com.yetanalytics.pan.errors-test-fixtures)

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

(def err-msg-1-no-obj
  "
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"not an id\"

of property:
:id

in object:
\"not an id\"

should be a valid IRI

-- Spec failed --------------------

Value:
\"FooBar\"

of property:
:type

in object:
\"not an id\"

should be: \"Profile\"

-------------------------
Detected 2 errors
")

(def err-msg-1a
  "-- Spec failed --------------------

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

-------------------------
Detected 1 error
")

(def err-msg-1b
  "-- Spec failed --------------------

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
Detected 1 error
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

(def err-msg-2-no-obj
  "
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"not an iri\"

of property:
:recommendedVerbs

in object:
\"https://foo.org/bad-concept\"

should be a valid IRI

-- Spec failed --------------------

Value:
\"{\\\"type\\\": \\\"notAType\\\"}\"

of property:
:inlineSchema

in object:
\"https://foo.org/bad-concept\"

should be a valid JSON schema

-------------------------
Detected 2 errors
")

;; Statement Template error

(def err-msg-3
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"this-template-is-invalid\", :type \"FooBar\"}

should contain keys: :definition, :inScheme, :prefLabel

| key         | spec                                       |
|=============+============================================|
| :definition | (map-of                                    |
|             |  :com.yetanalytics.pan.axioms/language-tag |
|             |  string?                                   |
|             |  :min-count                                |
|             |  1)                                        |
|-------------+--------------------------------------------|
| :inScheme   | com.yetanalytics.pan.axioms/iri-str?       |
|-------------+--------------------------------------------|
| :prefLabel  | (map-of                                    |
|             |  :com.yetanalytics.pan.axioms/language-tag |
|             |  string?                                   |
|             |  :min-count                                |
|             |  1)                                        |

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

(def err-msg-3-no-obj
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
\"this-template-is-invalid\"

should contain keys: :definition, :inScheme, :prefLabel

| key         | spec                                       |
|=============+============================================|
| :definition | (map-of                                    |
|             |  :com.yetanalytics.pan.axioms/language-tag |
|             |  string?                                   |
|             |  :min-count                                |
|             |  1)                                        |
|-------------+--------------------------------------------|
| :inScheme   | com.yetanalytics.pan.axioms/iri-str?       |
|-------------+--------------------------------------------|
| :prefLabel  | (map-of                                    |
|             |  :com.yetanalytics.pan.axioms/language-tag |
|             |  string?                                   |
|             |  :min-count                                |
|             |  1)                                        |

-- Spec failed --------------------

Value:
\"this-template-is-invalid\"

of property:
:id

in object:
\"this-template-is-invalid\"

should be a valid URI

-- Spec failed --------------------

Value:
\"FooBar\"

of property:
:type

in object:
\"this-template-is-invalid\"

should be: \"StatementTemplate\"

-------------------------
Detected 3 errors
")

;; ID Error

(def err-msg-4
  "
**** ID Errors ****

-- Spec failed --------------------

Identifier:
\"https://w3id.org/xapi/catch/v1\"

which occurs 2 times in the version:
\"https://w3id.org/xapi/catch/v1\"

should be a unique identifier value

-- Spec failed --------------------

Identifier:
\"https://foo.org/template\"

which occurs 2 times in the version:
\"https://w3id.org/xapi/catch/v1\"

should be a unique identifier value

-------------------------
Detected 2 errors
")

;; InScheme Error

(def err-msg-5
  "
**** Version ID Errors ****

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

(def err-msg-6-no-obj
  "
**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
\"https://foo.org/template\"

that links to object:
\"https://foo.org/dead-verb\"

via the property:
:verb

should not link to non-existent Concept or Template

-- Spec failed --------------------

Statement Template:
\"https://foo.org/template\"

that links to object:
\"https://foo.org/dead-aut1\"

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

(def err-msg-7-no-obj
  "
**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
\"https://foo.org/template\"

that links to object:
\"https://foo.org/template2\"

via the property:
:verb

should link to type: \"Verb\"

-- Spec failed --------------------

Statement Template:
\"https://foo.org/template\"

that links to object:
\"https://foo.org/template\"

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

(def err-msg-9-no-obj
  "
**** Pattern Edge Errors ****

-- Spec failed --------------------

Pattern:
\"https://foo.org/pattern-three\"

that links to object:
\"https://foo.org/pattern-three\"

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
**** Syntax Errors ****

-- Spec failed --------------------

Value:
{:type \"@type\",
 :id \"@id\",
 :prov \"http://www.w3.org/ns/prov#\",
 :skos \"http://www.w3.org/2004/02/skos/core#\",
 :Profile {:id \"bee\"}}

of property:
:_context

in object:
{:id \"https://foo.org/activity/1\",
 :type \"Activity\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :activityDefinition
 {:_context \"https://w3id.org/xapi/profiles/activity-context\",
  :extensions
  {\"http://foo.org\"
   {:_context
    {:type \"@type\",
     :id \"@id\",
     :prov \"http://www.w3.org/ns/prov#\",
     :skos \"http://www.w3.org/2004/02/skos/core#\",
     :Profile {:id \"bee\"}}}}}}

should be a valid IRI

or

should be a valid inline context

-- Spec failed --------------------

Value:
{:id \"bee\"}

of property:
:Profile

in object:
{:id \"https://foo.org/activity/1\",
 :type \"Activity\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :activityDefinition
 {:_context \"https://w3id.org/xapi/profiles/activity-context\",
  :extensions
  {\"http://foo.org\"
   {:_context
    {:type \"@type\",
     :id \"@id\",
     :prov \"http://www.w3.org/ns/prov#\",
     :skos \"http://www.w3.org/2004/02/skos/core#\",
     :Profile {:id \"bee\"}}}}}}

should be one of: \"@id\", \"@type\"

or

should satisfy

  string?

or

should be a valid IRI

or

should contain key: :_id

| key  | spec                                              |
|======+===================================================|
| :_id | <can't find spec for unqualified spec identifier> |

-- Spec failed --------------------

Value:
{:_context
 {:type \"@type\",
  :id \"@id\",
  :prov \"http://www.w3.org/ns/prov#\",
  :skos \"http://www.w3.org/2004/02/skos/core#\",
  :Profile {:id \"bee\"}}}

of property:
:extensions

in object:
{:id \"https://foo.org/activity/1\",
 :type \"Activity\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :activityDefinition
 {:_context \"https://w3id.org/xapi/profiles/activity-context\",
  :extensions
  {\"http://foo.org\"
   {:_context
    {:type \"@type\",
     :id \"@id\",
     :prov \"http://www.w3.org/ns/prov#\",
     :skos \"http://www.w3.org/2004/02/skos/core#\",
     :Profile {:id \"bee\"}}}}}}

should be a valid Activity extension

-------------------------
Detected 3 errors
")

(def err-msg-11
  "
**** Context Errors ****

-- Spec failed --------------------

Key:
:foo

in object:
{\"@id\" \"https://foo.org/profile\",
 \"@type\" \"Profile\",
 :baz \"Qux\",
 :foo \"Bar\",
 \"https://w3id.org/xapi/profiles/ontology#concepts\" [...]}

should be a valid IRI

or

should be a language tag

or

should be one of: \"@id\", \"@type\"

-- Spec failed --------------------

Key:
:baz

in object:
{\"@id\" \"https://foo.org/profile\",
 \"@type\" \"Profile\",
 :baz \"Qux\",
 :foo \"Bar\",
 \"https://w3id.org/xapi/profiles/ontology#concepts\" [...]}

should be a valid IRI

or

should be a language tag

or

should be one of: \"@id\", \"@type\"

-- Spec failed --------------------

Key:
:hello

in object:
{:hello \"World\",
 \"https://w3id.org/xapi/ontology#interactionId\"
 \"https://foo.org/activity-name-1\",
 \"https://w3id.org/xapi/ontology#type\"
 \"https://foo.org/activity-type-1\"}

should be a valid IRI

or

should be a language tag

or

should be one of: \"@id\", \"@type\"

-------------------------
Detected 3 errors
")