# Example error messages

```clojure
(def minimal-profile
  {:id         "https://example.org/example"
   :type       "Profile"
   :prefLabel  {"en" "Ex-Profile"}
   :definition {"en" "Example Profile"}
   :_context   "https://w3id.org/xapi/profiles/context"
   :versions   [{:id "https://example.org/example/v1"
                 :generatedAtTime "2017-12-22T22:30:00-07:00"}]
   :author     {:url  "https://www.yetanalytics.io"
                :type "Organization"
                :name "Yet Analytics"}
   :conformsTo "https://w3id.org/xapi/profiles#1.0"})
```

## No arguments (i.e. `:syntax?` argument)

We invalidate the `:id` and `:type` properties of the Profile (these sorts of errors can also apply to Concepts, Templates, and Patterns).

```clojure
(def bad-profile-1
  (-> minimal-profile
      (assoc :id "not an id")
      (assoc :type "FooBar")))

(pan/validate-profile bad-profile-1)
```

```
**** Syntax Errors ****

-- Spec failed --------------------

Value:
"not an id"

of property:
:id

in object:
{:id "not an id",
 :type "FooBar",
 :prefLabel {"en" "Ex-Profile},
 :definition {"en" "Example Profile"},
 :_context "https://w3id.org/xapi/profiles/context",
 :conformsTo "https://w3id.org/xapi/profiles#1.0",
 :author
 {:url "https://www.yetanalytics.io",
  :type "Organization",
  :name "Yet Analytics"},
 :versions
 [{:id "https://example.org/example/v1",
   :generatedAtTime "2017-12-22T22:30:00-07:00"}]}

should be a valid IRI

-- Spec failed --------------------

Value:
\"FooBar\"

of property:
:type

in object:
{:id "not an id",
 :type "FooBar",
 :prefLabel {"en" "Ex-Profile},
 :definition {"en" "Example Profile"},
 :_context "https://w3id.org/xapi/profiles/context",
 :conformsTo "https://w3id.org/xapi/profiles#1.0",
 :author
 {:url "https://www.yetanalytics.io",
  :type "Organization",
  :name "Yet Analytics"},
 :versions
 [{:id "https://example.org/example/v1",
   :generatedAtTime "2017-12-22T22:30:00-07:00"}]}

should be: \"Profile\"

-------------------------
Detected 2 errors

```

## `:id?` argument 

We have duplicate IDs and `:inScheme` values that are not listed Profile versions:

```clojure
(def bad-templates
  [{:id         "https://foo.org/template"
    :type       "StatementTemplate"
    :inScheme   "https://example.org/example/v1"
    :prefLabel  {"en" "Example 1"}
    :definition {"en" "Example Template 1"}
    :rules      [{:location "$.actor.mbox"
                  :all      ["yet@yetanalytics.io"]}]}
    {:id        "https://foo.org/template"
    :type       "StatementTemplate"
    :inScheme   "https://example.org/example/v2"
    :prefLabel  {"en" "Example 2"}
    :definition {"en" "Example Template 2"}
    :rules      [{:location "$.actor.name"
                  :all      ["Yet Analytics"]}]}])

(def bad-profile-2
  (assoc minimal-profile :templates bad-templates))

(pan/validate-profile bad-profile-2 :syntax? false :ids? true)
```

```
**** ID Errors ****

-- Spec failed --------------------

Identifier:
"https://foo.org/template"

which occurs 2 times in the Profile

should be a unique identifier value

-------------------------
Detected 1 error

**** Version Errors ****

-- Spec failed --------------------

InScheme IRI:
"https://example.org/example/v2"

associated with the identifier:
"https://foo.org/template"

in a Profile with the following version IDs:
"https://example.org/example/v1"

should be a valid version ID

-------------------------
Detected 1 error
```

## `:relations?` argument

The Statement Template links to a non-existent Verb, while the Patterns form a cyclical reference (so they both eventually contain themselves).

```clojure
(def bad-templates-2
  [{:id         "https://foo.org/template"
    :type       "StatementTemplate"
    :inScheme   "https://example.org/example/v1"
    :prefLabel  {"en" "Example 1"}
    :definition {"en" "Example Template 1"}
    :verb       "https://foo.org/dead-verb"}])

(def bad-patterns
  [{:id        "https://foo.org/pattern-one"
    :type      "Pattern"
    :primary   true
    :oneOrMore "https://foo.org/pattern-two"}
  {:id        "https://foo.org/pattern-two"
    :type      "Pattern"
    :primary   true
    :oneOrMore "https://foo.org/pattern-one"}])

(def bad-profile-3
  (-> minimal-profile
      (assoc :templates bad-templates-2
      (assoc :patterns bad-patterns))))

(pan/validate-profile bad-profile-3 :syntax? false :relations? true)
```

```
**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
{:id "https://foo.org/template",
 :type "StatementTemplate",
 :inScheme "https://example.org/example/v1",
 ...}

that links to object:
{:id "https://foo.org/dead-verb",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:verb

should not link to non-existent Concept or Template

-------------------------
Detected 1 error

**** Pattern Cycle Errors ****

-- Spec failed --------------------

The following Patterns:
\"https://foo.org/pattern-one\"
\"https://foo.org/pattern-two\"

should not contain cyclical references

-------------------------
Detected 1 error
```

## `context?` argument:

Invalid `@context` value:

```
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

-------------------------
Detected 1 error
```

Key that cannot be expanded using a `@context` value:

```
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

-------------------------
Detected 1 error
```
