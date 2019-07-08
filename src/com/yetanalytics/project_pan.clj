(ns com.yetanalytics.project-pan
  (:require [clojure.string :as string]
            [camel-snake-kebab.core :as kebab]
            [cheshire.core :as cheshire]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.profile :as profile]))

(defn remove-chars [s]
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  (string/replace s #"@|\s" ""))

(defn convert-json [json]
  (cheshire/parse-string json (fn [k] (-> k remove-chars kebab/->kebab-case-keyword))))
;; ^ example usage of ->

(defn validate-profile
  "Validate a profile from the top down.
  *** Validation strictness settings ***
  1. Weak Validation: Check only properties and simple syntax
  2. Semi-Strict Validation: Validate and resolve IRIs that point to locations
  WITHIN the profile. This includes:
      - inSchema validation
      - in-profile Concept relation validation
      - in-profile Extensions URI array validation
      - in-profile StatementTemplate IRI validation
      - in-profile Pattern validation
  If an IRI cannot be resolved, then the validation fails (or should it just
  give a warning?)
  3. Strict Validation: Validate and resolve IRIs that can point to locations
  OUTSIDE the profile. This include:
      - Same validations as Semi-Strict validation, except that IRIs can now
        point outside the profile.
      - Validation relating to @contexts 
  EXTRA SETTING: No short-circuit errors
      - Allow the validator to collect all errors, instead of terminating on 
      the first error encountered."
  [profile & {:keys [validation-level no-short?]}]
  (let [edn-profile (convert-json profile)]
    (cond
      ;; TODO Add strict validation and no-short-circuit
      (= validation-level 1) (profile/validate-profile+ edn-profile)
      :else (profile/validate-profile edn-profile))))

;; ID VALIDATION
;; 1. Check that top-level properties pass basic validation
;; 2. Check that IDs are distinct
;; 3. Go in and check inSchemes
;;
;; CONCEPT VALIDATION
;; 1. Make a map of IDs to concepts
;; 2. When validating a Concept, get the correct other Concept and see that it
;; has the same type
;;
;; ACTIVITY VALIDATION
;; 1. Check to see that the activity has a correct activityDefinition
;; 2a. Check @context
;; 2b. ???
;; 2c. Profit!
;;
;; STATEMENT TEMPLATE VALIDATION
;; 1a. Check @context
;; 1b. ???
;; 1c. Profit!
;;
;; PATTERN VALIDATION
;; 1. Create a directed graph of all Patterns and reachable Templates.
;; - Primary Patterns act as entry points for the graph
;; - Templates (and Patterns outside the graph, if External is turned off) are
;; the leaves of the graph
;; 2. Check for cycles. If a cycle exists, that means a Pattern includes
;; itself, which MUST NOT occur.
;; 3. Check that an 'alternate' Pattern does not include an 'optional' or
;; 'zeroOrMore' Pattern in its adjacency list.
;; 4. If a one-member 'sequence' Pattern exists, ensure:
;; - It has no incoming edges (ie. not used elswhere in the Profile)
;; - Its outgoing edge points to a Template and not a Pattern
