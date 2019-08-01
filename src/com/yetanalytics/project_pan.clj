(ns com.yetanalytics.project-pan
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]
            [com.yetanalytics.identifiers :as id]
            [com.yetanalytics.context :as context]
            [com.yetanalytics.errors :as errors]
            [com.yetanalytics.util :as util]))

;; TODO Add conversion from Turtle and XML formats
;; Currently only supports JSON-LD

(defn- convert-profile
  "Converts profile, if it is a JSON-LD string, into EDN format.
  Otherwise keeps it in EDN format."
  [profile]
  (if (string? profile)
    (util/convert-json profile "")
    profile))

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
  (let [profile (convert-profile profile)
        errors (cond-> {:syntax-errors (profile/validate profile)}
                 (true? ids)
                 (assoc :id-errors (id/validate-ids profile)
                        :in-scheme-errors (id/validate-in-schemes profile))
                 (true? relations)
                 (merge
                  (let [cgraph (concept/create-graph (:concepts profile))
                        tgraph (template/create-graph (:concepts profile)
                                                      (:templates profile))
                        pgraph (pattern/create-graph (:templates profile)
                                                     (:patterns profile))]
                    {:concept-errors (concept/explain-graph cgraph)
                     :template-errors (template/explain-graph tgraph)
                     :pattern-errors (pattern/explain-graph pgraph)
                     :pattern-cycle-errors
                     (pattern/explain-graph-cycles pgraph)}))
                 (true? contexts)
                 (assoc :context-errors
                        (context/validate-all-contexts profile)))]
    (if (every? nil? (vals errors))
      (do (println "Success!") nil) ;; Exactly like spec/explain
      (errors/expound-errors errors))))
