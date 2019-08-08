# project-pan

A Clojure library for validating xAPI Profiles, according to the xAPI Profile
specification: https://github.com/adlnet/xapi-profiles

## Usage

To use the library to validate a whole Profile, call `validate-profile` in
the core namespace. This method takes in an entire Profile (either as a
JSON-LD string or an EDN data structure) and prints either a success message
on success (obviously) or an error message on failure.

Arguments may be supplied for different levels of validation strictness, which
are listed as follows:
- no args - Basic validation; check only the types of properties and simple 
syntax of all Profile objects.
- `:ids` - Validate the correctness of all object and versioning IDs (the id
and inScheme properties). Validate that all IDs are distinct and that all
inScheme values correspond to valid Profile version IDs.
- `:relations` - Validate that all relations between Profile objects are valid.
These relations are given by IRIs and include the following:
    - the broader, narrower, related, broadMatch, narrowMatch, relatedMatch and
exactMatch properties for Verbs, Activity Types and Attachment Usage 
Types. 
    - the `recommendedActivityTypes` property for Activity Extensions 
    - the `recommendedVerbs` property for Context and Result Extensions 
    - Determining Properties, `objectStatementRefTemplate` property and the
    `contextStatementRefTemplate` properties for Statement Templates.
    - `sequence`, `alternates`, `optional`, `oneOrMore` and `zeroOrMore` 
    properties for Patterns.
- `:contexts` - Validate that all instances of `@context` resolve to valid
JSON-LD contexts and that they allow all properties to expand out to absolute
IRIs during JSON-LD processing. The `@context` property is always found in the
Profile metadata and in Activity Definitions, though they can also be found
in Extensions for said Activity Definitions.

In addition to these arguments, there is another argument that needs to be
implemented in a future iteration but currently does not exist.
- `:external-iris` - Allow the profile to access external links, either by
executing SPARQL queries on a RDF triple store or by executing HTTP requests.
This is useful when `:relations` and `:contexts` are set to true.

Besides validating whole Profiles, you can also use library methods and specs
to validate parts of Profiles, such as individual  Concepts, Templates and 
Patterns).

## Done

- Axiom specs:
    - Basic types: booleans and strings
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
- at-context validation

## TODO

- Be able to access external links:
    - External Profile objects
        - `broadMatch`, `narrowMatch`, `relatedMatch` and `exactMatch` Concepts
        all need to come from external Profiles.
        - Extensions may also link to external `recommendedActivityTypes` and
        `recommendedVerbs`.
        - Templates and Patterns may link to external objects.
    - Non-inline `schema` values are given by IRIs that link out to external
    JSON Schemas.
    - Except for the two w3id contexts given by the Profile spec, any and all
    `@context` values need to be given by external links.
    - For many of these use cases, we need an RDF triple store to use as a
    Profile server (and be able to make SPARQL queries to it).
- xapi-schema problems:
    - IRI and IRL specs do not validate non-ASCII chars (thus defeating the
    whole point); see Issue #64 in xapi-schema.
    - Language map specs are incorrect; see Issue #67 in xapi-schema
- Improve error messages
    - Currently the default error message printer doesn't give good information
    on which particular object the error occured.
    - There is a branch of Expound (yetanalytics/expound) that is working on
    fixing its grouping error: https://github.com/yetanalytics/expound
    - We may move away from Expound and create our own in-house error messaging
    library (especially since Expound has a ton of dependencies).
- Log errors to somewhere instead of printing them out. 
- Handle non-JSON-LD Profiles (eg. XML, Turtle).
- More graceful exception handling.
- Any bugs that need to be fixed (natch).
- Demo of the completed product

## License

Copyright © 2019 Yet Analtyics

Distributed under the Eclipse Public License version 1.0.
