# project-pan

[![CI](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml/badge.svg)](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml)

A Clojure library for validating xAPI Profiles, according to the [xAPI Profile specification](https://github.com/adlnet/xapi-profiles).

## Usage

To use the library to validate a whole Profile, call `validate-profile` in the `pan` namespace. This method takes in an entire Profile (either as a JSON-LD string or an EDN data structure) and prints either a success message on success (obviously) or an error message on failure.

Arguments may be supplied for different levels of validation strictness, which are listed as follows:
- `:syntax?` - Basic validation; check only the types of properties and simple syntax of all Profile objects. Set to `true` by default.
- `:ids?` - Validate the correctness of all object and versioning IDs (the id and inScheme properties). Validate that all IDs are distinct and that all inScheme values correspond to valid Profile version IDs.
- `:relations?` - Validate that all relations between Profile objects are valid. These relations are given by IRIs and include the following:
    - the broader, narrower, related, broadMatch, narrowMatch, relatedMatch and exactMatch properties for Verbs, Activity Types and Attachment Usage Types.
    - the `recommendedActivityTypes` property for Activity Extensions
    - the `recommendedVerbs` property for Context and Result Extensions
    - Determining Properties, `objectStatementRefTemplate` property and the `contextStatementRefTemplate` properties for Statement Templates.
    - `sequence`, `alternates`, `optional`, `oneOrMore` and `zeroOrMore` properties for Patterns.
- `:contexts?` - Validate that all instances of `@context` resolve to valid JSON-LD contexts and that they allow all properties to expand out to absolute IRIs during JSON-LD processing. The `@context` property is always found in the Profile metadata and in Activity Definitions, though they can also be found in Extensions for said Activity Definitions.
- `:print-errs?` - Print validation errors out if true; otherwise return spec error data (or nil, if the profile is valid) without printing. True by default.

In addition to these arguments, there is another argument that needs to be implemented in a future iteration but currently does not exist.
- `:external-iris?` - Allow the profile to access external links, either by executing SPARQL queries on a RDF triple store or by executing HTTP requests. This is useful when `:relations?` and `:contexts?` are set to true.

Besides validating whole Profiles, you can also use library methods and specs to validate parts of Profiles, such as individual  Concepts, Templates and Patterns).

## Done

- Axiom specs:
    - Basic types: booleans and strings
    - Identifiers (IRIs/IRLs/URIs/URLs)
    - Timestamps
    - JSONPath strings
    - JSON Schema (but only supports draft-07)
    - RFC 2046 Media Types
    - Language Maps
    - Arrays of axioms
- Basic syntax validation
    - Profile metadata
    - Concepts
    - Statement Templates
    - Patterns
- In-profile identifier links
- `@context` validation

## TODO

- Be able to access external links:
    - External Profile objects
        - `broadMatch`, `narrowMatch`, `relatedMatch` and `exactMatch` Concepts all need to come from external Profiles.
        - Extensions may also link to external `recommendedActivityTypes` and `recommendedVerbs`.
        - Templates and Patterns may link to external objects.
    - Non-inline `schema` values are given by IRIs that link out to external JSON Schemas.
    - Except for the two w3id contexts given by the Profile spec, any and all `@context` values need to be given by external links.
    - For many of these use cases, we need an RDF triple store to use as a Profile server (and be able to make SPARQL queries to it).
- xapi-schema problems:
    - IRI and IRL specs do not validate non-ASCII chars (thus defeating the whole point); see Issue #64 in xapi-schema.
    - Language map specs are incorrect; see Issue #67 in xapi-schema
- Log errors to somewhere instead of printing them out.
- Handle non-JSON-LD Profiles (eg. XML, Turtle).
- More graceful exception handling.
- Any bugs that need to be fixed (natch).

## License

Copyright © 2020 Yet Analtyics

Project Pan is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text
