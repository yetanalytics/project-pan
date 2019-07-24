# project-pan

A Clojure library for validating xAPI Profiles, according to the xAPI Profile
specification: https://github.com/adlnet/xapi-profiles

## Usage

To use the library to validate a whole Profile, call `validate-profile` in
the core namespace. Arguments may be supplied for different levels of
validation strictness; if there are no arguments provided, only basic syntax
validation is performed. More details can be found in the `validate-profile`
docstring.

You can also use the library to validate individual Profile objects (ie.
Concepts, Statement Templates and Patterns) by referencing the spec keywords
from the non-core namespaces.

## Done

- Axiom specs: 
    - JSONPath
    - Language Maps
    - Booleans, Strings, Timestamps
    - Arrays of axioms
- Basic syntax validation
- In-profile identifier links

## TODO

- Axiom specs:
    - IRIs/IRLs/URIs/URLs (This is an xapi-schema problem)
    - JSON Schema: Currently only draft-07 supported
- at-context validation
- Identifiers that link to external profiles (need triple store)
- Interface to outside world

## License

Copyright © 2019 Yet Analtyics

Distributed under the Eclipse Public License version 1.0.
