# project-pan

[![CI](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml/badge.svg)](https://github.com/yetanalytics/project-pan/actions/workflows/main.yml)

A Clojure library for validating xAPI Profiles, according to the [xAPI Profile specification](https://github.com/adlnet/xapi-profiles).

## Usage

To use the library to validate a whole Profile, call `validate-profile` in the `pan` namespace. This method takes in an entire Profile as an EDN data structure and prints either a success message on success (obviously) or an error message on failure.

Similarly, to validate a collection of Profiles, call `validate-profile-coll` on them. Each Profile in the collection will be able to reference Concepts, Templates, and Patterns from the other Profiles.

Arguments may be supplied for different levels of validation strictness, which are listed as follows:
- `:syntax?` - Basic validation; check only the types of properties and simple syntax of all Profile objects. Default `true`.
- `:ids?` - Validate the correctness of all object and versioning IDs (the `id` and `inScheme` properties). Validate that all IDs are distinct and that all `inScheme` values correspond to valid Profile version IDs. If multiple Profiles are involved, all IDs are checked that they are distinct among _all_ Profiles (not just the Profile it is a part of).
- `:relations?` - Validate that all relations between Profile objects are valid. These relations are given by IRIs and include the following:
    - the broader, narrower, related, broadMatch, narrowMatch, relatedMatch and exactMatch properties for Verbs, Activity Types and Attachment Usage Types.
    - the `recommendedActivityTypes` property for Activity Extensions
    - the `recommendedVerbs` property for Context and Result Extensions
    - Determining Properties, `objectStatementRefTemplate` property and the `contextStatementRefTemplate` properties for Statement Templates.
    - `sequence`, `alternates`, `optional`, `oneOrMore` and `zeroOrMore` properties for Patterns.
- `:contexts?` - Validate that all instances of `@context` resolve to valid JSON-LD contexts and that they allow all properties to expand out to absolute IRIs during JSON-LD processing. The `@context` property is always found in the Profile metadata and in Activity Definitions, though they can also be found in Extensions for said Activity Definitions.

Other keyword arguments include:
- `:external-profiles` - Extra Profiles from which to reference Concepts, Statement Templates, and Patterns from. Unlike the Profiles passed into `validate-profile-coll`, these Profiles are not validated (though will be treated as part of the same scope in terms of ID distinctiveness validation).
- `:external-contexts` - Extra `@context` values (other than the xAPI Profile and Activity contexts) that `@context` IRIs in `profile` can reference during context validation. During multi-profile validation, all Profiles refer to the same global `:external-contexts` map. Default `{}`.
- `:print-errs?` - Print validation errors out if true; otherwise return spec error data (or nil, if the profile is valid) without printing. Default `true`.

The `get-external-iris` function is provided in the API in order to retrieve IRI values that refer to external objects, JSON-LD contexts, etc. (i.e. objects that do _not_ exist in the Profile). This allows the user to more easily retrieve external Profiles and contexts in their application from the Internet or their data store.

Besides validating whole Profiles, you can also use library methods and specs to validate parts of Profiles, such as individual  Concepts, Templates and Patterns).

## TODO

- Be able to access external links:
    - Non-inline `schema` values are given by IRIs that link out to external JSON Schemas.
    - Except for the two w3id contexts given by the Profile spec, any and all `@context` values need to be given by external links.
- Log errors to somewhere instead of printing them out.
- More graceful exception handling.
- Any bugs that need to be fixed (natch).

## License

Copyright © 2019-2021 Yet Analytics, Inc.

Project Pan is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text
