(ns com.yetanalytics.pan.identifiers
  (:require [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn objs->ids
  "Return a lazy seq of IDs from a collection of objects."
  [obj-coll]
  (map :id obj-coll))

(defn objs->out-ids
  "Given `obj-coll` of maps with IRI/IRI-array valued keys (defined by
   `selected-keys`), return a set of all such IRI values. Does not check
   whether or not the IDs already exist in `obj-coll`."
  [obj-coll selected-keys]
  (set (reduce
        (fn [acc obj]
          (-> obj (select-keys selected-keys) vals flatten (concat acc)))
        []
        obj-coll)))

(defn- conj-set
  [coll v]
  (if coll (conj coll v) #{v}))

(defn objs->out-ids-map
  "Like `obj->out-ids`, but returns a map from keys to sets of IRI values.
   Passing in the optional `filter-id-set` arg removes IRI values form
   the result that are in that set."
  ([obj-coll selected-keys]
   (objs->out-ids-map obj-coll selected-keys #{}))
  ([obj-coll selected-keys filter-id-set]
   (reduce
    (fn [m obj]
      (->> (select-keys obj selected-keys)
           (mapcat (fn [[k v]] (if (coll? v) (map (fn [x] [k x]) v) [[k v]])))
           (filter (fn [[_ v]] (not (contains? filter-id-set v))))
           (reduce (fn [m* [k v]] (update m* k conj-set v)) m)))
    {}
    obj-coll)))

(defn filter-by-ids
  "Given `obj-coll` of maps with the `:id` key, filter such that only those
   with IDs present in `id-set` remain."
  [id-set obj-coll]
  (filter (fn [{id :id}] (contains? id-set id)) obj-coll))

(defn filter-by-ids-kv
  "Similar to `filter-by-ids`, except that `id-val-m` is a map from IDs
   to values."
  [id-set id-val-m]
  (reduce-kv (fn [m k v] (cond-> m (contains? id-set k) (assoc k v)))
             {}
             id-val-m))

(defn count-ids
  "Count the number of ID instances by creating a map between IDs and their
  respective counts. (Ideally theys should all be one, as IDs MUST be unique
  by definition.)"
  [ids-coll]
  (reduce (fn [m x]
            (if (contains? m x)
              (update m x inc)
              (assoc m x 1)))
          {}
          ids-coll))

(defn- profile->object-seq
  "Return a lazy seq of all the Concepts, Templates, and Patterns in
   a Profile."
  [{:keys [concepts templates patterns]}]
  (concat concepts templates patterns))

(defn- profile->id-count-m
  "Return a count map for all IDs in the Profile."
  [{:keys [id versions concepts templates patterns]}]
  (->> (concat versions concepts templates patterns)
       (map :id)
       (into [id])
       count-ids))

(defn- profile->inscheme-id-count-m
  "Return a inscheme-id-count map for `profile`."
  [profile]
  (let [head-ids (into [(select-keys profile [:id])]
                       (:versions profile))]
    (->> (profile->object-seq profile)
         (map #(select-keys % [:id :inScheme]))
         (group-by :inScheme)
         (reduce-kv (fn [m k v]
                      (assoc m k (->> v
                                      (into head-ids)
                                      (map :id)
                                      count-ids)))
                    {}))))

(defn- apply-extra-counts
  "Given an inscheme-id-count map, add the appropriate counts from
   `extra-id-count-ms` to each ID."
  [inscheme-id-count-m extra-id-count-ms]
  (let [count-extra (fn [id] (get extra-id-count-ms id 0))]
    (reduce-kv (fn [m is id-cnt]
                 (assoc m is (reduce-kv
                              (fn [m id cnt]
                                (assoc m id (+ cnt (count-extra id))))
                              {}
                              id-cnt)))
               {}
               inscheme-id-count-m)))

(defn- profile->inscheme-props-m
  "Return an inscheme->property map for the objects in `profile`."
  [profile]
  (let [ver-id-set (->> profile :versions (map :id) set)]
    (->> profile
         profile->object-seq
         (map #(select-keys % [:id :inScheme]))
         (map #(assoc % :versionIds ver-id-set))
         (group-by :inScheme))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Common specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id any?)
(s/def ::inScheme any?)
(s/def ::versionIds (s/coll-of any? :kind set?))

(s/def ::singleton-inscheme-map
  (s/map-of ::inScheme any? :max-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ID distinctness validation
;;
;; All ID values MUST be distinct from each other
;; Covers requirements that version IDs MUST be distinct from each other and
;; from the overall profile ID.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- one? [n] (= 1 n))

(s/def ::one-count one?)

(s/def ::distinct-ids
  (s/map-of any? ::one-count))

(s/def ::map-of-distinct-ids
  (s/map-of any? ::distinct-ids))

(defn validate-ids
  "Validate that every ID in `profile` within each inScheme/version
   is distinct. If `extra-profiles` is provided, ensure that
   IDs in `profile` are not duplicated in there either."
  ([profile]
   (let [inscheme-id-count-m (profile->inscheme-id-count-m profile)]
     (s/explain-data ::map-of-distinct-ids inscheme-id-count-m)))
  ([profile extra-profiles]
   (let [extra-counts  (->> extra-profiles
                            (map profile->id-count-m)
                            (apply merge-with +))
         is-id-count-m (-> (profile->inscheme-id-count-m profile)
                           (apply-extra-counts extra-counts))]
     (s/explain-data ::map-of-distinct-ids is-id-count-m))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; inScheme property validation
;;
;; All inScheme values MUST be a valid Profile version ID
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- has-inscheme?
  [{:keys [inScheme versionIds]}]
  (contains? versionIds inScheme))

(s/def ::inscheme-prop has-inscheme?)

(s/def ::inscheme-props
  (s/coll-of ::inscheme-prop))

(s/def ::map-of-inscheme-props
  (s/map-of any? ::inscheme-props))

(defn validate-same-inschemes
  [profile]
  (->> profile
       profile->inscheme-props-m
       (s/explain-data (s/and ::map-of-inscheme-props
                              ::singleton-inscheme-map))))

(defn validate-inschemes
  "Takes a Profile and validates all object inSchemes, which MUST be valid
   version IDs. Returns nil on success, or spec error data on failure."
  [profile]
  (->> profile
       profile->inscheme-props-m
       (s/explain-data ::map-of-inscheme-props)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Changes across versions
;;
;; Covers the following spec requirements:
;; - A Profile Author MUST change a Statement Template's id between versions if
;;   any of the Determining Properties, StatementRef properties, or rules
;;   change. Changes of scopeNote are not considered changes in rules.
;; - A Profile Author MUST change a Pattern's id between versions if any of
;;   alternates, optional, oneOrMore, sequence, or zeroOrMore change.
;;
;; TODO: Somehow validate the following Concept requirement:
;; - A Profile MUST NOT define a Concept that is defined in another Profile
;;   UNLESS it supersedes all versions of the other Profile containing the
;;   Concept and indicates that in with wasRevisionOf.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn dissoc-properties
  "Dissoc properties that may change between version increments."
  [object]
  (cond-> (select-keys object [:id
                               :type
                               ;; StatementTemplate
                               :verb
                               :objectActivityType
                               :contextCategoryActivityType
                               :contextGroupingActivityType
                               :contextParentActivityType
                               :contextOtherActivityType
                               :attachmentUsageType
                               :objectStatementRefTemplate
                               :contextStatementRefTemplate
                               :rules
                               ;; Patterns
                               :alternates
                               :sequence
                               :optional
                               :oneOrMore
                               :zeroOrMore])
    (contains? object :rules)
    (update :rules (partial map #(dissoc % :scopeNote)))))

(defn- versioned-objects?
  [objects]
  (->> objects
       (map dissoc-properties)
       distinct
       count
       (= 1)))

(s/def ::versioned-objects versioned-objects?)

(s/def ::coll-of-versioned-objects (s/coll-of ::versioned-objects))

(defn validate-version-change
  "Validate that every object in `profile` that is shared between
   versions follows versioning requirements, i.e. they don't differ
   in certain properties."
  [profile]
  (->> profile
       profile->object-seq
       (group-by :id)
       vals
       (s/explain-data ::coll-of-versioned-objects)))
