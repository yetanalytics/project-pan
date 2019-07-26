(ns com.yetanalytics.project-pan
  (:require [com.yetanalytics.util :as util]
            [com.yetanalytics.profile :as profile]))

(defn validate-profile
  "Validate a profile from the top down. Takes in a Profile (either as a
  JSON-LD string or an EDN data structure) and returns true on validation, or
  a clojure spec error trace if the Profile is invalid. In addition, the
  function accepts a number of arguments for different levels of validation
  strictness.
    (no args) - Weak validation; check only the types of properties and simple 
  syntax of all Profile objects.
    :ids - Validate the correctness of all object and versioning IDs (the id
  and inScheme properties). Validate that all IDs are distinct and that all
  inScheme values correspond to valid Profile version IDs.
    :relations - Validate that all relations between Profile objects are valid.
  These relations are given by IRIs and include the following:
    - the broader, narrower, related, broadMatch, narrowMatch, relatedMatch and
      exactMatch properties for Verbs, Activity Types and Attachment Usage 
      Types. 
    - the recommendedActivityTypes property for Activity Extensions 
    - the recommendedVerbs property for Context and Result Extensions 
    - Determining Properties, objectStatementRefTemplate property and the
    contextStatementRefTemplate properties for Statement Templates.
    - sequence, alternates, optional, oneOrMore and zeroOrMore properties for
      Patterns.
    :contexts - Validate that all instances of @context resolve to valid
  JSON-LD contexts and that they allow all properties to expand out to absolute
  IRIs during JSON-LD processing. The @context property is always found in the
  Profile metadata and in Activity Definitions, though they can also be found
  in Extensions for said Activity Definitions.
    :external-iris - Allow the profile to access external links, either by
  executing SPARQL queries on a RDF triple store or by executing HTTP requests.
  This is useful when :relations and :contexts are set to true.
    :no-short - Allow the validator to collect all errors, instead of having to
  terminate on the first error encountered."
  ;; TODO: Implement :external-iris and :no-short
  [profile & {:keys [ids relations contexts]
              :or {ids false relations false contexts false}}]
  (let [profile (if (string? profile)
                  (util/convert-json profile "") profile)]
    (let [errors
          (cond-> (seq (profile/validate profile))
            (true? ids) (concat (profile/validate-in-schemes profile))
            (true? profiles) (concat (profile/validate-iris profile))
            (true? contexts) (concat (profile/validate-context profile)))]
      (if (empty? errors)
        true
        (vec errors))))) ;; TODO Log errors to a logger
