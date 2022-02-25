# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

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
