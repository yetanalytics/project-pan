# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## 0.5.0 - TBD
- Add the `validate-object` function to the top-level API to validate individual Concepts, Templates, and Patterns.
- Add value generation capabilities for Profile syntax specs, including `::concept`, `::template`, `::pattern`, and `::profile`.
- Fix bugs when validating Pattern relations across multiple Profiles. (See [#39](https://github.com/yetanalytics/project-pan/pull/39) for details.)
- TODO: Add API function to individually validate Concepts, StatementTemplates, and Patterns.
- TODO: Change get `get-external-iris` to return _all_ IRIS, and in a more useful format.
- TODO: Relax ID validation constraints to allow different versions of the same Profile to reuse IDs (with the exception of changed StatementTemplates and Patterns).

## 0.4.3 - 2022-03-04
- Fix bug where outgoing edges of profile-external StatementTemplates were incorrectly included in the Pattern graph.

## 0.4.2 - 2022-03-03
- Add `:concept-rels?`, `:template-rels?`, and `:pattern-rels?`, keyword arguments.

## 0.4.1 - 2022-02-25
- Add `json-profile->edn` API function.

## 0.4.0 - 2022-02-25
- Add multi-profile support
  - Extra API function `validate-profile-coll` that accepts a collection of Profiles to validate.
  - `:extra-profiles` keyword arg to reference Profiles with external Concepts, Templates, and Patterns.
  - `:extra-contexts` keyword arg to reference external, non-default JSON-LD contexts.
- Replace `:print-err?` keyword arg to `:result`, with options `:spec-error-data`, `:type-path-string`, `:path-string`, `:string`, and `:print`.
- Overhaul internal logic for identifier, graph and context validation.
- Rename various internal namespaces.

See [Pull Requests #23 to #27](https://github.com/yetanalytics/project-pan/pulls?q=is%3Apr+is%3Aclosed) for more details.

## 0.3.0 - 2021-04-07
- Upgrade to latest Expound version and clean up internal error logic.
- Made error messages more consistent across different error types.
- Rename certain spec names and simplify graph validation functions.
- Remove JSON Schema-related deps - no more downstream downloading of jitpack!
- Updated xapi-schema dep to reflect latest fixes.

## 0.2.0 - 2021-02-01
- Add ClojureScript compatibility.
- Singleton sub-Pattern IDs now MUST be IRIs, not object maps.

## 0.1.0 - 2019-09-12
- Initial alpha release.
