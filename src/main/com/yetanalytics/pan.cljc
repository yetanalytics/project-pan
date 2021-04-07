(ns com.yetanalytics.pan
  (:require [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.objects.concept :as concept]
            [com.yetanalytics.pan.objects.template :as template]
            [com.yetanalytics.pan.objects.pattern :as pattern]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan.context :as context]
            [com.yetanalytics.pan.errors :as errors]
            [com.yetanalytics.pan.utils.json :as json]))

;; TODO Add conversion from Turtle and XML formats
;; Currently only supports JSON-LD

(defn- convert-profile
  "Converts profile, if it is a JSON-LD string, into EDN format. Otherwise keeps it in EDN format.
   - Note that all instances of @ in keywords are replaced by '_'"
  [profile]
  (if (string? profile)
    (try (json/convert-json profile "_")
         (catch #?(:clj Exception :cljs js/Error) e
           (ex-info "JSON parsing error!" (ex-data e))))
    profile))

(defn validate-profile
  "Validate a profile from the top down. Takes in a Profile and
   validates it, printing or returning errors on completion.
   Supports multiple levels of validation based on the following
   boolean arguments:

   - Default On:
     `:print-errs?`  Print errors if true; return spec error data
                     only if false.
     `:syntax?`      Basic syntax validation only.

   - Default Off:
     `:ids?`            Validate object and versioning IDs.
     `:relations?`      Validate IRI-given relations between Concepts,
                        Statement Templates and Patterns.
     `:contexts?`       Validate @context values and that all keys
                        expand to absolute IRIs using @context.
     `:external-iris?`  Allow the profile to access external links
                        (HAS YET TO BE IMPLEMENTED).

  More information can be found in the README."
  ;; TODO: Implement :external-iris
  [profile & {:keys [syntax? ids? relations? contexts? external-iris? print-errs?]
              :or {syntax? true
                   ids? false
                   relations? false
                   contexts? false
                   external-iris? false
                   print-errs? true}}]
  (let [profile (convert-profile profile)
        errors  (cond-> {}
                  syntax?
                  (assoc :syntax-errors (profile/validate profile))
                  ids? ; ID duplicate and inScheme errors
                  (assoc :id-errors (id/validate-ids profile)
                         :in-scheme-errors (id/validate-in-schemes profile))
                  relations? ; URI errors
                  (merge
                   (let [{:keys [concepts templates patterns]} profile
                         ;; Graphs
                         cgraph (concept/create-graph concepts)
                         tgraph (template/create-graph concepts templates)
                         pgraph (pattern/create-graph templates patterns)
                         ;; Errors
                         cerrors   (concept/validate-graph-edges cgraph)
                         terrors   (template/validate-template-edges tgraph)
                         perrors   (pattern/validate-pattern-edges pgraph)
                         pc-errors (pattern/validate-pattern-tree pgraph)]
                     {:concept-edge-errors  cerrors
                      :template-edge-errors terrors
                      :pattern-edge-errors  perrors
                      :pattern-cycle-errors pc-errors}))
                  contexts? ; @context errors
                  (merge (context/validate-contexts profile)))
        no-errors? (every? nil? (vals errors))]
    (if print-errs?
      (if no-errors?
        (println "Success!") ; Exactly like spec/explain
        (errors/expound-errors errors))
      (when-not no-errors?
        errors))))
