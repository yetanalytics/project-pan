# Example error messages

*Note:* The `Detected x errors` at the bottom may not always make sense in
terms of how it's displayed. This is due to limitations of the Expound library
and the hacky workarounds needed to make it work semi-decently.

## No arguments 
Failed basic syntax validation:
```
-- Spec failed --------------------

  {:id "not an id",
       ^^^^^^^^^^^
   :type ...,
   :prefLabel ...,
   :definition ...,
   :_context ...,
   :versions ...,
   :author ...,
   :conformsTo ...}

should be a valid IRI

-------------------------
Detected 1 error

-- Spec failed --------------------

  {:id ...,
   :type "FooBar",
         ^^^^^^^^
   :prefLabel ...,
   :definition ...,
   :_context ...,
   :versions ...,
   :author ...,
   :conformsTo ...}

should be: "Profile"

-------------------------
Detected 1 error
```

## 'id' argument 

Duplicate IDs present:
```
-- Spec failed --------------------

Duplicate id: https://w3id.org/xapi/catch/v1
 with count: 2

the id value is not unique

-- Spec failed --------------------

Duplicate id: https://foo.org/template
 with count: 2

the id value is not unique

-------------------------
Detected 2 errors
```

Invalid inSchemes:
```
-- Spec failed --------------------

Invalid inScheme: https://foo.org/invalid
 at object: https://foo.org/template
 profile version ids:
  https://w3id.org/xapi/catch/v2
  https://w3id.org/xapi/catch/v1

the inScheme value is not a valid version ID

-- Spec failed --------------------

Invalid inScheme: https://foo.org/also-invalid
 at object: https://foo.org/template2
 profile version ids:
  https://w3id.org/xapi/catch/v2
  https://w3id.org/xapi/catch/v1

the inScheme value is not a valid version ID

-------------------------
Detected 2 errors
```

## 'relations' argument

Links pointed to non-existent objects (here a Verb and an attachmentUsageType):
```
-- Spec failed --------------------

Invalid verb identifier:
 https://foo.org/dead-verb

 at object:
  {:id "https://foo.org/template",
   :type "StatementTemplate",
   :inScheme "https://w3id.org/xapi/catch/v1",
   ...}

 linked object:
  {:id "https://foo.org/dead-verb",
   :type nil,
   :inScheme nil,
   ...}

linked concept or template does not exist

-------------------------
Detected 1 error

-- Spec failed --------------------

Invalid attachmentUsageType identifier:
 https://foo.org/dead-aut1

 at object:
  {:id "https://foo.org/template",
   :type "StatementTemplate",
   :inScheme "https://w3id.org/xapi/catch/v1",
   ...}

 linked object:
  {:id "https://foo.org/dead-aut1",
   :type nil,
   :inScheme nil,
   ...}

linked concept or template does not exist

-------------------------
Detected 1 error
```

Cyclical patterns detected:
```
-- Spec failed --------------------

Cycle detected involving the following nodes:
  https://foo.org/pattern-one
  https://foo.org/pattern-two

cyclical reference detected

-------------------------
Detected 1 error
```

## 'context' argument:

Invalid `@context` value
```
-- Spec failed --------------------

  {:Profile ...,
   :Verb "xapi:Verb",
         ^^^^^^^^^^^
   :ActivityType ...,
   :AttachmentUsageType ...}

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-- Spec failed --------------------

  {:Profile ...,
   :Verb ...,
   :ActivityType "xapi:ActivityType",
                 ^^^^^^^^^^^^^^^^^^^
   :AttachmentUsageType ...}

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-- Spec failed --------------------

  {:Profile ...,
   :Verb ...,
   :ActivityType ...,
   :AttachmentUsageType "xapi:AttachmentUsageType"}
                        ^^^^^^^^^^^^^^^^^^^^^^^^^^

simple term definition does not have valid prefix

or

expanded term definition does not have valid prefix

-------------------------
Detected 3 errors
```

Key was not in `@context` value:
```
-- Spec failed --------------------

  {:id ...,
   :type ...,
   :_context ...,
   :baz ...,
   :foo ...}
   ^^^^

key cannot be expanded into absolute IRI

or

key is not @context

-- Spec failed --------------------

  {:id ...,
   :type ...,
   :_context ...,
   :foo ...,
   :baz ...}
   ^^^^

key cannot be expanded into absolute IRI

or

key is not @context

-------------------------
Detected 2 errors
```
