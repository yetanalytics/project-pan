(ns com.yetanalytics.pan
  (:require [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.objects.concept :as concept]
            [com.yetanalytics.pan.objects.template :as template]
            [com.yetanalytics.pan.objects.pattern :as pattern]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan.context :as context]
            [com.yetanalytics.pan.errors :as errors]))

(defn- find-syntax-errors
  [profile]
  {:syntax-errors (profile/validate profile)})

(defn- find-id-errors
  ([profile]
   {:id-errors        (id/validate-ids profile)
    :in-scheme-errors (id/validate-in-schemes profile)})
  ([profile extra-profiles]
   {:id-errors        (id/validate-ids profile extra-profiles)
    :in-scheme-errors (id/validate-in-schemes profile)}))

(defn- find-graph-errors*
  [cgraph tgraph pgraph]
  {:concept-edge-errors  (concept/validate-concept-edges cgraph)
   :template-edge-errors (template/validate-template-edges tgraph)
   :pattern-edge-errors  (pattern/validate-pattern-edges pgraph)
   :pattern-cycle-errors (pattern/validate-pattern-tree pgraph)})

(defn- find-graph-errors
  ([profile]
   (let [cgraph (concept/create-graph profile)
         tgraph (template/create-graph profile)
         pgraph (pattern/create-graph profile)]
     (find-graph-errors* cgraph tgraph pgraph)))
  ([profile extra-profiles]
   (let [cgraph (concept/create-graph profile extra-profiles)
         tgraph (template/create-graph profile extra-profiles)
         pgraph (pattern/create-graph profile extra-profiles)]
     (find-graph-errors* cgraph tgraph pgraph))))

(defn- find-context-errors
  [profile]
  (context/validate-contexts profile))

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
  [profile & {:keys [syntax?
                     ids?
                     relations?
                     contexts?
                     print-errs?
                     extra-profiles]
              :or {syntax?        true
                   ids?           false
                   relations?     false
                   contexts?      false
                   print-errs?    true
                   extra-profiles []}}]
  (let [errors   (if (not-empty extra-profiles)
                   (cond-> {}
                     syntax?    (merge (find-syntax-errors profile))
                     ids?       (merge (find-id-errors profile extra-profiles))
                     relations? (merge (find-graph-errors profile extra-profiles))
                     contexts?  (merge (find-context-errors profile)))
                   (cond-> {}
                     syntax?    (merge (find-syntax-errors profile))
                     ids?       (merge (find-id-errors profile))
                     relations? (merge (find-graph-errors profile))
                     contexts?  (merge (find-context-errors profile))))
        no-errs? (every? nil? (vals errors))]
    (if print-errs?
      ;; Print monolithic error message, exactly like spec/explain
      (if no-errs?
        (println "Success!")
        (errors/expound-errors errors))
      ;; Return the map of errors
      (when-not no-errs?
        errors))))

(defn validate-profiles
  [profiles & {:keys [syntax?
                      ids?
                      relations?
                      contexts?
                      print-errs?
                      extra-profiles]
               :or {syntax?        true
                    ids?           false
                    relations?     false
                    contexts?      false
                    print-errs?    true
                    extra-profiles []}}]
  (let [profiles-set (set profiles)
        profile-errs (map (fn [profile]
                            (let [extra-profiles*
                                  (-> profiles-set
                                      (disj profile)
                                      (concat extra-profiles))]
                              (validate-profile
                               profile
                               :syntax? syntax?
                               :ids? ids?
                               :relations? relations?
                               :contexts? contexts?
                               :print-errs? print-errs?
                               :extra-profiles extra-profiles*)))
                          profiles)
        no-errs?     (every? (fn [perr] (every? nil? (vals perr)))
                             profile-errs)]
    (if print-errs?
      (if no-errs?
        (println "Success!")
        (map errors/expound-errors profile-errs))
      (when-not no-errs?
        profile-errs))))
