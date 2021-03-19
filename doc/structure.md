# Project structure

The structure of project-pan is given at a high level here, including what each namespace does and the different kinds of validation happens.

## axioms

The `axioms` namespace contains all base-level properties that are used to validate the values of Profile properties. These axioms include:
- Booleans
- Strings (non-empty ones only)
- Language Maps
- Media Types (RFC 2046)
- JSONPath strings
- JSON Schema
- IRIs/IRLs/URIs/URLs
- Arrays of IRIs and URIs

## objects

The `objects` folder contains all specs for syntax validation of Profiles, as well as relational IRI validation between objects. The structure of this folder is designed as such to reflect the hierarchical structure of a Profile based off of the values of the `type` property (though there are exceptions, such as the `rules` folder under `templates`.

```
objects
+-- profiles
    +-- author
    +-- versions
+-- concepts
    +-- verbs
    +-- activity_types
    +-- attachment_usage_types
    +-- extensions
        +-- activity
        +-- context
        +-- result
    +-- document_resources
        +-- activity_profile
        +-- agent_profile
        +-- state
    +-- activity
+-- templates
    +-- rules
+-- patterns
```

## identifiers

This namespace is used for ID-related validation tasks, include checking for duplicate IDs and validating `inScheme` values.

## context

This namespace is used for `@context`-related validation tasks, including validating `@context` maps and validating that keys can use them to expand to absolute IRIs.

## graph

This is a util namespace used for common graph-related functions, such as building nodes and edges as well as checking for self-loops.

## errors

This namespace is used for printing error messages using Expound. Check this namespace to get a full list of error messages.

## util

This folder contains shared functions and specs, including those for JSON and EDN parsing.
