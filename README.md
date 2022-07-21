# project-pan

[![CI](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml/badge.svg)](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml)

A Clojure library for validating xAPI Profiles, according to the [xAPI Profile specification](https://github.com/adlnet/xapi-profiles).

## Installation

Add the following to the `:deps` map in your `deps.edn` file:

```clojure
com.yetanalytics/project-pan {:mvn/version "0.5.1"
                              :exclusions [org.clojure/clojure
                                           org.clojure/clojurescript]}
```

## Usage

To use the library to validate a whole Profile, call `validate-profile` in the `pan` namespace. This method takes in an entire Profile as an EDN data structure and prints either a success message on success (obviously) or an error message on failure.

Similarly, to validate a collection of Profiles, call `validate-profile-coll` on them. Each Profile in the collection will be able to reference Concepts, Templates, and Patterns from the other Profiles.

Arguments may be supplied for different levels of validation strictness, which are listed as follows:
- `:syntax?` - Basic validation; check only the types of properties and simple syntax of all Profile objects. Default `true`.
- `:ids?` - Validate the correctness of all object and versioning IDs (the `id` and `inScheme` properties). Validate that all IDs are distinct and that all `inScheme` values correspond to valid Profile version IDs. If multiple Profiles are involved, all IDs are checked that they are distinct among _all_ Profiles (not just the Profile it is a part of).\*
- `:multi-version?` - A flag for the `:ids?` keyword arg; if `false` then ID validation will check that all objects have the same `inScheme` value. If `true`, then in addition to the usual ID checking, validation that Template and Pattern IDs change when their properties (other than `inScheme`, `prefLabel`, and `scopeNote`) change is active. Default `false`.
- `:relations?` - Validate that all relations between Profile objects are valid. These relations are given by IRIs and include the following:
    - the broader, narrower, related, broadMatch, narrowMatch, relatedMatch and exactMatch properties for Verbs, Activity Types and Attachment Usage Types.
    - the `recommendedActivityTypes` property for Activity Extensions
    - the `recommendedVerbs` property for Context and Result Extensions
    - Determining Properties, `objectStatementRefTemplate` property and the `contextStatementRefTemplate` properties for Statement Templates.
    - `sequence`, `alternates`, `optional`, `oneOrMore` and `zeroOrMore` properties for Patterns.
- `:concept-rels?`, `:template-rels?`, `:pattern-rels?` - Similar to `:relations?`, except that each only validates relations specific to Concept, Statement Template, and Pattern properties, respectively. Each are default `false`, and are overridden when `:relations?` are `true`.
- `:contexts?` - Validate that all instances of `@context` resolve to valid JSON-LD contexts and that they allow all properties to expand out to absolute IRIs during JSON-LD processing. The `@context` property is always found in the Profile metadata and in Activity Definitions, though they can also be found in Extensions for said Activity Definitions.

Other keyword arguments include:
- `:external-profiles` - Extra Profiles from which to reference Concepts, Statement Templates, and Patterns from. Unlike the Profiles passed into `validate-profile-coll`, these Profiles are not validated (though will be treated as part of the same scope in terms of ID distinctiveness validation).
- `:external-contexts` - Extra `@context` values (other than the xAPI Profile and Activity contexts) that `@context` IRIs in `profile` can reference during context validation. During multi-profile validation, all Profiles refer to the same global `:external-contexts` map. Default `{}`.
- `:result` - This value affects the format of the result type.
    - `:print` prints an [Expound](https://github.com/bhb/expound)-generated error string to standard output.
    - `:string` returns the aforementioned error string (or a vector of them, in the case of `validate-profile-coll`).
    - `:type-string` returns a `{:type string}` map, where `:type` is a keyword and `string` is an error message specific to that type of error. `:type` can be one of the following:
      - `:syntax-errors` for basic validation errors
      - `:id-errors` for ID errors
      - `:in-scheme-errors` for inScheme errors
      - `:concept-edge-errors` for errors between Concept relations
      - `:template-edge-errors` for errors between Template relations
      - `:pattern-edge-errors` for errors between Pattern relations
      - `:pattern-cycle-errors` for cycles in the Pattern relation graph
      - `:context-errors` for errors relating to JSON-LD context
    - `:type-path-string` returns a `{:type {path string}}` map, where `path` is the `assoc-in` vector needed to retrieve the erroneous value, and `string` is an error message specific to that error location.
   - `:spec-error-data` - Return a `{:type spec-error-data}` map, with each value being the map generated by Clojure spec. Default.
- `:error-msg-opts` - Takes an opt map that affects how error messages are formed and printed. Currently only takes `:print-objects?`, which when `true` (default) displays the entire object in error messages, and if `false` only displays IDs.

### Additional API Functions

The `validate-object` function is provided in order to provide a way to validate individual Concepts, Templates, and Patterns without having to go deeper than the top level API. The `:type` and `:result` keyword args are used to fix the expected spec and to affect the function result, respectively; more details can be found in the docstring.

The `get-external-iris` function is provided in the API in order to retrieve IRI values that refer to external objects, JSON-LD contexts, etc. (i.e. objects that do _not_ exist in the Profile). This allows the user to more easily retrieve external Profiles and contexts in their application from the Internet or their data store.

The `json-profile->edn` function is provided in the API to provide for convenient coercion of JSON profile strings into an EDN format that Pan recognizes.

Besides validating whole Profiles, you can also use library methods and specs to validate parts of Profiles, such as individual  Concepts, Templates and Patterns).

## TODO

- Be able to access external links:
    - Non-inline `schema` values are given by IRIs that link out to external JSON Schemas.
    - Except for the two w3id contexts given by the Profile spec, any and all `@context` values need to be given by external links.
- Log errors to somewhere instead of printing them out.
- More graceful exception handling.
- Any bugs that need to be fixed (natch).

## License

Copyright © 2019-2022 Yet Analytics, Inc.

Project Pan is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text
