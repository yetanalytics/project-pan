(ns com.yetanalytics.pan
  (:require [com.yetanalytics.pan.objects.profile  :as profile]
            [com.yetanalytics.pan.objects.concept  :as concept]
            [com.yetanalytics.pan.objects.template :as template]
            [com.yetanalytics.pan.objects.pattern  :as pattern]
            [com.yetanalytics.pan.identifiers      :as id]
            [com.yetanalytics.pan.context          :as context]
            [com.yetanalytics.pan.errors           :as errors]
            [com.yetanalytics.pan.utils.json       :as json]))

(defn get-external-iris
  "Return a map of keys to sets of IRIs, where the IRIs reference
   objects that do not exist in `profile`. Values include IRIs
   from Concepts, Statement Templates, and Patterns as well as
   \"@context\" IRI values."
  [profile]
  (let [iris-m (merge (concept/get-external-iris profile)
                      (template/get-external-iris profile)
                      (pattern/get-external-iris profile))]
    (if-some [ctx-iris (not-empty (context/get-context-iris profile))]
      (assoc iris-m :_context ctx-iris)
      iris-m)))

(defn json-profile->edn
  "Convert an JSON string xAPI Profile into an EDN data structure,
   where all keys are keywordized (and `@`s, e.g. in `@context`, are
   converted into `_`)."
  [json-profile]
  (json/convert-json json-profile))

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
  [?cgraph ?tgraph ?pgraph]
  (cond-> {}
    ?cgraph
    (assoc :concept-edge-errors (concept/validate-concept-edges ?cgraph))
    ?tgraph
    (assoc :template-edge-errors (template/validate-template-edges ?tgraph))
    ?pgraph
    (assoc :pattern-edge-errors (pattern/validate-pattern-edges ?pgraph)
           :pattern-cycle-errors (pattern/validate-pattern-tree ?pgraph))))

(defn- find-graph-errors
  ([profile {:keys [concepts? templates? patterns?]}]
   (let [?cgraph (when concepts? (concept/create-graph profile))
         ?tgraph (when templates? (template/create-graph profile))
         ?pgraph (when patterns? (pattern/create-graph profile))]
     (find-graph-errors* ?cgraph ?tgraph ?pgraph)))
  ([profile extra-profiles {:keys [concepts? templates? patterns?]}]
   (let [?cgraph (when concepts? (concept/create-graph profile extra-profiles))
         ?tgraph (when templates? (template/create-graph profile extra-profiles))
         ?pgraph (when patterns? (pattern/create-graph profile extra-profiles))]
     (find-graph-errors* ?cgraph ?tgraph ?pgraph))))

(defn- find-context-errors
  [profile ?extra-contexts-map]
  (if ?extra-contexts-map
    {:context-errors (context/validate-contexts profile ?extra-contexts-map)}
    {:context-errors (context/validate-contexts profile)}))

(defn validate-profile
  "Validate `profile` from the top down, printing or returning errors
   on completion. Supports multiple levels of validation based on the
   following keyword arguments:
   - `:syntax?`        Basic syntax validation only. Default `true`.
   - `:ids?`           Validate object and versioning IDs. Default `false`.
   - `:relations?`     Validate IRI-given relations between Concepts,
                       Statement Templates and Patterns. Default
                       `false`.
   - `:concept-rels?`  Validate Concept-specific relations. Default `false`,
                       and is overridden if `:relations?` is `true`.
   - `:template-rels?` Validate Statement Template-specific relations (incl.
                       Concept-Template relations). Default `false`, and is
                       overridden if `:relations?` is `true`.
   - `:pattern-rels`?  Validate Pattern-specific relations (incl. Pattern and
                       Statement Template relations). Default `false`, and is
                       overridden if `:relations?` is `true`.
   - `:contexts?`      Validate \"@context\" values and that Profile keys
                       expand to absolute IRIs using RDF contexts. Default
                       `false.`

   Also supports multiple resources with the following:
   - `:extra-profiles` Extra profiles from which Concepts, Templates, and
                         Patterns can be referenced from. Default `[]`.
   - `:extra-contexts` Extra \"@context\" values (other than the xAPI Profile
                         and Activity contexts) that \"@context\" IRIs in
                         `profile` can reference during context validation.
                         Default `{}`.
   
   The `:result` keyword argument affects the return data, and can take one
   of the following values:
   - `:spec-error-data`  Return a `{:error-type spec-error-data}` map.
   - `:type-path-string` Return a `{:err-type {:err-path err-string}}` map.
   - `:type-string`      Return a `{:err-type err-string}` map.
   - `:string`           Return the Expound-generated error string.
   - `:print`            Print the error string to standard output."
  [profile & {:keys [syntax?
                     ids?
                     relations?
                     concept-rels?
                     template-rels?
                     pattern-rels?
                     contexts?
                     extra-profiles
                     extra-contexts
                     result]
              :or {syntax?        true
                   ids?           false
                   relations?     false
                   concept-rels?  false
                   template-rels? false
                   pattern-rels?  false
                   contexts?      false
                   extra-profiles []
                   extra-contexts {}
                   result         :spec-error-data}}]
  (let [?rel-opts (not-empty
                   (cond-> {}
                     (or relations? concept-rels?)  (assoc :concepts? true)
                     (or relations? template-rels?) (assoc :templates? true)
                     (or relations? pattern-rels?)  (assoc :patterns? true)))
        errors (if (not-empty extra-profiles)
                 (cond-> {}
                   syntax?
                   (merge (find-syntax-errors profile))
                   ids?
                   (merge (find-id-errors profile extra-profiles))
                   ?rel-opts
                   (merge (find-graph-errors profile extra-profiles ?rel-opts))
                   contexts?
                   (merge (find-context-errors profile extra-contexts)))
                 (cond-> {}
                   syntax?
                   (merge (find-syntax-errors profile))
                   ids?
                   (merge (find-id-errors profile))
                   ?rel-opts
                   (merge (find-graph-errors profile ?rel-opts))
                   contexts?
                   (merge (find-context-errors profile extra-contexts))))
        errors? (not (every? nil? (vals errors)))]
    (case result
      :spec-error-data
      (when errors? errors)
      :type-path-string
      (cond-> errors
        errors? (errors/errors->type-path-str-m {:print-objects? true}))
      :type-string
      (cond-> errors
        errors? (errors/errors->type-str-m {:print-objects? true}))
      :string
      (cond-> errors
        errors? (errors/errors->string {:print-objects? true}))
      :print
      (if-not errors?
        (println "Success!")
        (println (errors/errors->string errors {:print-objects? true}))))))

(defn validate-profile-coll
  "Like `validate-profile`, but takes a `profile-coll` instead of a
   single Profile. Each Profile can reference objects in other Profiles
   (as well as those in `:extra-profiles`) and must not share object
   IDs with those in other Profiles. During context validation, all
   Profiles reference the global `:extra-contexts` map. Keyword
   arguments are the same as in `validate-profile`, though the error
   result (except for `:print`) are now all vectors."
  [profile-coll & {:keys [syntax?
                          ids?
                          relations?
                          concept-rels?
                          template-rels?
                          pattern-rels?
                          contexts?
                          extra-profiles
                          extra-contexts
                          result]
                   :or {syntax?        true
                        ids?           false
                        relations?     false
                        concept-rels?  false
                        template-rels? false
                        pattern-rels?  false
                        contexts?      false
                        extra-profiles []
                        extra-contexts {}
                        result         :spec-error-data}}]
  (let [profiles-set (set profile-coll)
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
                               :concept-rels?  concept-rels?
                               :template-rels? template-rels?
                               :pattern-rels?  pattern-rels?
                               :contexts?      contexts?
                               :extra-profiles extra-profiles*
                               :extra-contexts extra-contexts
                               :result         :spec-error-data)))
                          profile-coll)
        errors?      (not (every? (fn [perr] (every? nil? (vals perr)))
                                  profile-errs))]
    (case result
      :spec-error-data
      (when errors? (vec profile-errs))
      :type-path-string
      (cond->> profile-errs
        errors? (mapv (comp not-empty #(errors/errors->type-path-str-m % {:print-objects? true}))))
      :type-string
      (cond->> profile-errs
        errors? (mapv (comp not-empty #(errors/errors->type-str-m % {:print-objects? true}))))
      :string
      (cond->> profile-errs
        errors? (mapv (comp not-empty #(errors/errors->string % {:print-objects? true}))))
      :print
      (if-not errors?
        (println "Success!")
        (dorun (map (comp println errors/errors->string) profile-errs))))))
