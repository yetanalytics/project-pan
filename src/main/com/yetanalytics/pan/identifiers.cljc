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

(defn profile->id-seq*
  "Given `profile`, return a lazy seq of ID from all of its
   sub-objects."
  [{:keys [id versions concepts templates patterns] :as _profile}]
  (concat [id] (mapcat objs->ids [versions concepts templates patterns])))

(def profile->id-seq
  "Memoized version of `profile->id-seq*`."
  (memoize profile->id-seq*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ID distinctness validation
;;
;; All ID values MUST be distinct from each other
;; Covers requirements that version IDs MUST be distinct from each other and
;; from the overall profile ID.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- dissoc-concept-props
  "Remove Concept properties that may change across versions without
   changing its fundamental nature."
  [concept]
  (dissoc concept :inScheme :deprecated))

(defn- dissoc-template-props
  "Remove StatementTemplate properties that may change across versions without
   changing its fundamental nature, such as rule scopeNotes."
  [template]
  (cond-> (dissoc template :inScheme :deprecated)
    (contains? template :rules)
    (update :rules (fn [rules] (mapv #(dissoc % :scopeNote) rules)))))

(defn- dissoc-pattern-props
  "Remove Pattern properties that may change across versions without
   changing its fundamental nature."
  [pattern]
  (dissoc pattern :inScheme :deprecated))

(defn dedupe-profile-objects
  "Deduplicate Concepts, Templates, and Patterns that are identical
   between Profile versions (other than their inScheme, deprecated,
   and scopeNote properties)."
  [{:keys [concepts templates patterns] :as profile}]
  (let [concepts*  (->> concepts (map dissoc-concept-props) distinct vec)
        templates* (->> templates (map dissoc-template-props) distinct vec)
        patterns*  (->> patterns (map dissoc-pattern-props) distinct vec)]
    (assoc profile
           :concepts concepts*
           :templates templates*
           :patterns patterns*)))

;; Validate that a single ID count is 1
(s/def ::one-count (fn one? [n] (= 1 n)))

;; Validate that all ID counts are 1
;; Ideally IDs should be identifiers (IRIs, IRLs, etc.), but we do not check
;; for that here.
(s/def ::distinct-ids (s/map-of any? ::one-count))

(defn validate-ids
  "Takes a Profile and validates that all ID values in it are distinct
   (including across the extra Profiles), excepting IDs of objects that
   are identical save for inScheme, deprecated, or scopeNote properties.
   Returns `nil` on success, or spec error data on failure."
  ([profile]
   (let [profile*    (dedupe-profile-objects profile)
         profile-ids (profile->id-seq profile*)
         counts      (count-ids profile-ids)]
     (s/explain-data ::distinct-ids counts)))
  ([profile extra-profiles]
   (let [profile*    (dedupe-profile-objects profile)
         profile-ids (profile->id-seq profile*)
         prof-id-set (set profile-ids)
         extra-ids   (mapcat profile->id-seq extra-profiles)
         ;; We count IDs in all the Profiles, but only validate the
         ;; counts in the main Profile.
         counts      (->> (concat profile-ids extra-ids)
                          count-ids
                          (filter-by-ids-kv prof-id-set))]
     (s/explain-data ::distinct-ids counts))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; inScheme property validation
;;
;; All inScheme values MUST be a valid Profile version ID
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate that the inScheme is an element of the set of version IDs
(s/def ::in-scheme
  (fn has-in-scheme? [{:keys [version-ids inScheme]}]
    (contains? version-ids inScheme)))

;; Validate an array of objects with inSchemes
(s/def ::in-schemes (s/coll-of ::in-scheme))

(defn validate-in-schemes
  "Takes a Profile and validates all object inSchemes, which MUST be valid
   version IDs. Returns nil on success, or spec error data on failure."
  [{:keys [versions concepts templates patterns]}]
  (let [version-ids (set (objs->ids versions))
        all-objects (concat concepts templates patterns)
        vid-objects (mapv #(assoc % :version-ids version-ids) all-objects)]
    (s/explain-data ::in-schemes vid-objects)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- not-profile-id? [{:keys [id profileId]}]
  (not= id profileId))

(defn- not-version-id? [{:keys [id versionIds]}]
  (not (contains? versionIds id)))

(defn- in-versions? [{:keys [inScheme versionIds]}]
  (contains? versionIds inScheme))

(s/def ::id-not-profile-id not-profile-id?)

(s/def ::id-not-version-id not-version-id?)

(s/def ::inscheme-in-versions in-versions?)

(s/def ::valid-version-ids
  (s/every (s/and ::id-not-profile-id
                  ::id-not-version-id)))

(s/def ::valid-object-ids
  (s/and ::id-not-profile-id
         ::id-not-version-id
         ::inscheme-in-versions))

(s/def ::valid-objects-ids
  (s/every ::valid-object-ids))

(s/def ::singleton-coll
  (s/every any? :min-count 1 :max-count 1))

(s/def ::coll-of-singleton-coll
  (s/every ::singleton-coll))

(defn- coll->count-map
  [coll]
  (reduce (fn [m x] (if (contains? m x) (update m x inc) (assoc m x 1)))
          {}
          coll))

(s/def ::singleton-count-map
  (s/map-of any? #(= 1 %)))

(defn- profile->object-seq
  "Return a lazy seq of all the Concepts, Templates, and Patterns in
   a Profile."
  [{:keys [concepts templates patterns]}]
  (concat concepts templates patterns))

;; Both versions

(defn validate-unique-versions
  "Validate that the profile and version IDs are all unique and are
   not duplicated between each other."
  [{:keys [id versions] :as _profile}]
  (let [version-ids (mapv :id versions)]
    (->> versions
         (map #(select-keys % [:id]))
         (map-indexed (fn [i v]
                        (let [vid-set (->> (assoc version-ids i nil)
                                           (filter some?)
                                           set)]
                          (assoc v :profileId id :versionIds vid-set))))
         (s/explain-data ::valid-version-ids))))

(defn validate-object-ids
  "Validate that every Concept, StatementTemplate, and Pattern in
   `profile` has valid IDs and inSchemes (i.e. IDs don't duplicate
   a profile or version ID, and inSchemes exist in the version ID set)."
  [{:keys [id versions] :as profile}]
  (let [version-ids (set (map :id versions))]
    (->> (profile->object-seq profile)
         (map #(select-keys % [:id :inScheme]))
         (map #(assoc % :profileId id :versionIds version-ids))
         (s/explain-data ::valid-objects-ids))))

;; Global version

(defn validate-ids-globally
  "Validate that every ID in `profile`, regardless of the corresponding
   inScheme, is distinct. If `extra-profiles` is provided, ensure that
   IDs in `profile` are not duplicated in there either."
  ([profile]
   (->> (profile->object-seq profile)
        (map #(select-keys % [:id]))
        coll->count-map
        (s/explain-data ::singleton-count-map)))
  ([profile extra-profiles]
   (let [extra-objs  (mapcat profile->object-seq extra-profiles)
         count-extra (fn [id] (count (filter (fn [{xid :id}] (= id xid))
                                             extra-objs)))]
     (->> (profile->object-seq profile)
          (map #(select-keys % [:id]))
          coll->count-map
          (reduce-kv (fn [m {:keys [id] :as obj} cnt]
                       (let [extra-count (count-extra id)]
                         (assoc m obj (+ cnt extra-count))))
                     {})
          (s/explain-data ::singleton-count-map)))))

(defn validate-same-inscheme
  "Validate that every object in `profile` has the same inScheme."
  [profile]
  (->> (profile->object-seq profile)
       (map #(select-keys % [:inScheme]))
       distinct
       (s/explain-data ::singleton-coll)))

;; Per-inScheme version

(defn validate-ids-by-inscheme
  "Validate that every object in `profile` _within each version_ has
   distinct IDs. IDs may be reused between versions."
  [profile]
  (->> (profile->object-seq profile)
       (map #(select-keys % [:id :inScheme]))
       coll->count-map
       (s/explain-data ::singleton-count-map)))

;; TODO: Revisit what Concept/Template/Pattern changes should count as
;; "breaking"

(defn- dissoc-properties
  "Dissoc properties that may change between version increments."
  [object]
  (cond-> (dissoc object :inScheme :deprecated)
    (contains? object :rules)
    (update :rules (partial map #(dissoc % :scopeNote)))))

(defn validate-version-change
  "Validate that every object in `profile` that is shared between
   versions follows versioning requirements, i.e. they don't differ
   in certain properties."
  [profile]
  (->> (profile->object-seq profile)
       (map dissoc-properties)
       (group-by :id)
       vals
       (map distinct)
       (s/explain-data ::coll-of-singleton-coll)))
