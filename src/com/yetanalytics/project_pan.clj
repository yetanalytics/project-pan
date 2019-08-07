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

;; TODO Catch exceptions thrown on invalid JSON parsing 
(defn- convert-profile
  "Converts profile, if it is a JSON-LD string, into EDN format. Otherwise 
  keeps it in EDN format. Note that all instances of @ in keywords are
  replaced by underscores."
  [profile]
  (if (string? profile)
    (util/convert-json profile "_")
    profile))

(defn validate-profile
  "Validate a profile from the top down. Takes in a Profile and either prints
  an error or a success message. Takes in the following arguments: 
    (no args) - Basic syntax validation only
    :ids - Validate object and versioning IDs.
    :relations - Validate IRI-given relations between Concepts, Statement
    Templates and Patterns 
    :contexts - Validate @context values and that all keys expand to absolute
    IRIs using @context. 
    :external-iris - Allow the profile to access external links (HAS YET TO BE
    IMPLEMENTED).
  More information can be found in the README."
  ;; TODO: Implement :external-iris and :no-short
  [profile & {:keys [ids relations contexts]
              :or {ids false relations false contexts false}}]
  (let [profile (convert-profile profile)
        errors (cond-> {:syntax-errors (profile/validate profile)}
                 (true? ids) ;; ID duplicate and inScheme errors
                 (assoc :id-errors (id/validate-ids profile)
                        :in-scheme-errors (id/validate-in-schemes profile))
                 (true? relations) ;; URI errors
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
                 (true? contexts) ;; @context errors
                 (merge (context/validate-contexts profile)))]
    (if (every? nil? (vals errors))
      (do (println "Success!") nil) ;; Exactly like spec/explain
      (errors/expound-errors errors))))
