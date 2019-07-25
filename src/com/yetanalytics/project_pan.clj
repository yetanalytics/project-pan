(ns com.yetanalytics.project-pan
  (:require [clojure.string :as string]
            [camel-snake-kebab.core :as kebab]
            [cheshire.core :as cheshire]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.profile :as profile]))

(defn validate-profile
  "Validate a profile from the top down. Takes in a Profile (either as a
  JSON-LD string or an EDN data structure) and returns true on validation, or
  a clojure spec error trace if the Profile is invalid. In addition, the
  function accepts a number of arguments for different levels of validation
  strictness. 
    No args - Weak validation; check only the types of properties and simple 
  syntax of all Profile objects.
    :in-scheme - Validate that all instances of the inScheme property are
  valid, ie. they all correspond to a valid Profile version ID.
    :profile-iris - Validate that all IRI-valued properties (e.g. broader and
  narrower in Concepts, determining properties in Templates, etc.) link to
  valid Profile objects.
    :at-context - Validate that all instances of @context resolve to valid
  JSON-LD contexts and that they allow all properties to expand out to absolute
  IRIs during JSON-LD processing.
    :external-iris - Allow the profile to access external links, either by
  executing SPARQL queries on a RDF triple store or by executing HTTP requests.
    :no-short - Allow the validator to collect all errors, instead of having to
  terminate on the first error encountered."
  ;; TODO: Implement :external-iris and :no-short
  [profile & {:keys [in-scheme profile-iris at-context] :as extra-params}]
  (let [profile (if (string? profile)
                  (util/convert-json profile "")
                  profile)]
    (cond-> (profile/validate-explain profile)
      (true? in-scheme) (profile/validate-in-schemes profile)
      (true? profile-iris) (profile-validate-context profile)
      (true? at-context) (profile/validate-context profile))))
