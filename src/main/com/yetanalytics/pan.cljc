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
  "Validate `profile` from the top down, printing or returning errors
   on completion. Supports multiple levels of validation based on the
   following keyword arguments:
   - `:print-errs?`    Print errors if `true`; return spec error data
                         only if `false`. Default `true`.
   - `:syntax?`        Basic syntax validation only. Default `true`.
   - `:ids?`           Validate object and versioning IDs. Default
                         `false`.
   - `:relations?`     Validate IRI-given relations between Concepts,
                         Statement Templates and Patterns. Default
                         `false`.
   - `:contexts?`      Validate \"@context\" values and that Profile keys
                         expand to absolute IRIs using RDF contexts. Default
                         `false.`
   - `:extra-profiles` Extra profiles from which Concepts, Templates, and
                         Patterns can be referenced from. Default `[]`."
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
      (if no-errs?
        (println "Success!") ; Exactly like `spec/explain`
        (errors/expound-errors errors))
      (when-not no-errs?
        errors))))

(defn validate-profiles
  "Like `validate-profile`, but takes a coll of `profiles` instead of a
   single Profile. Each Profile can reference objects in other Profiles
   (as well as those in `:extra-profiles`) and must not share object
   IDs with those in other Profiles. Keyword arguments are the same as
   in `validate-profile`."
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
                               :syntax?        syntax?
                               :ids?           ids?
                               :relations?     relations?
                               :contexts?      contexts?
                               :extra-profiles extra-profiles*
                               :print-errs?    false)))
                          profiles)
        no-errs?     (every? (fn [perr] (every? nil? (vals perr)))
                             profile-errs)]
    (if print-errs?
      (if no-errs?
        (println "Success!")
        (map errors/expound-errors profile-errs))
      (when-not no-errs?
        profile-errs))))
