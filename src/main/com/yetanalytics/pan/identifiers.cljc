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

(defn- coll->count-map
  [coll]
  (reduce (fn [m x] (if (contains? m x) (update m x inc) (assoc m x 1)))
          {}
          coll))

(defn- assoc-counts
  [map-coll]
  (let [count-m (coll->count-map map-coll)]
    (reduce (fn [acc x] (conj acc (assoc x :count (get count-m x))))
            []
            map-coll)))

(s/def ::singleton-inscheme-map
  (s/map-of string? any? :max-count 1))

;; IDs

(s/def ::distinct-object
  (s/and #(contains? % :count)
         #(= 1 (:count %))))

(s/def ::distinct-objects
  (s/coll-of ::distinct-object))

(s/def ::map-of-distinct-objects
  (s/map-of any? ::object-vec))

;; inSchemes

(s/def ::inScheme string?)
(s/def ::versionIds (s/coll-of string? :kind set?))

(s/def ::inscheme-props
  (s/map-of string? (s/and (s/keys :req-un [::inScheme ::versionIds])
                           (fn [{:keys [version-ids inscheme]}]
                             (contains? version-ids inscheme)))))

(s/def ::map-of-inscheme-props
  (s/map-of any? ::inscheme-props))

;; helpers

(defn- profile->object-seq
  "Return a lazy seq of all the Concepts, Templates, and Patterns in
   a Profile."
  [{:keys [concepts templates patterns]}]
  (concat concepts templates patterns))

(defn- profile->inscheme-objects-m
  [profile]
  (let [head-ids (into [(select-keys profile [:id :type])]
                       (:versions profile))]
    (->> (profile->object-seq profile)
         (into head-ids)
         (map #(select-keys % [:id :inScheme]))
         (group-by :inScheme)
         (reduce-kv (fn [m k v] 
                      (assoc m k (-> v (concat head-ids) assoc-counts vec)))
                    {}))))

(defn- profile->inscheme-props-m
  [profile]
  (let [ver-id-set (->> profile :versions (map :id) set)]
    (->> profile
         profile->object-seq
         (map #(select-keys % [:id :inScheme]))
         (map #(assoc % :versionIds ver-id-set))
         (group-by :inScheme))))

;; Global version

(defn validate-ids-globally
  "Validate that every ID in `profile`, regardless of the corresponding
   inScheme, is distinct. If `extra-profiles` is provided, ensure that
   IDs in `profile` are not duplicated in there either."
  ([profile]
   (->> profile
        profile->inscheme-objects-m
        (s/explain-data (s/and ::map-of-distinct-objects
                               ::singleton-inscheme-map))))
  ([profile extra-profiles]
   (let [extra-objs    (mapcat profile->object-seq extra-profiles)
         count-extra   (fn [id] (count (filter (fn [{xid :id}] (= id xid))
                                               extra-objs)))
         counts-red-fn (fn [acc obj]
                         (let [extra-count (count-extra (:id obj))
                               updated-obj (update obj :count + extra-count)]
                           (conj acc updated-obj)))]
     (->> profile
          profile->inscheme-objects-m
          (map (partial reduce counts-red-fn []))
          (s/explain-data (s/and ::map-of-distinct-objects
                                 ::singleton-inscheme-map))))))

(defn validate-same-inschemes
  [profile]
  (->> profile
       profile->inscheme-props-m
       (s/explain-data (s/and ::map-of-inscheme-props
                              ::singleton-inscheme-map))))

;; Per-inScheme version

(defn validate-ids-by-inscheme
  "Validate that every object in `profile` _within each version_ has
   distinct IDs. IDs may be reused between versions."
  [profile]
  (->> profile
       profile->inscheme-objects-m
       (s/explain-data ::map-of-distinct-objects)))

(defn validate-inschemes
  [profile]
  (->> profile
       profile->inscheme-props-m
       (s/explain-data ::map-of-inscheme-props)))

;; Versioning

;; TODO: Revisit what Concept/Template/Pattern changes should count as
;; "breaking"

(defn- dissoc-properties
  "Dissoc properties that may change between version increments."
  [object]
  (cond-> (dissoc object :inScheme :deprecated)
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

(defn validate-version-change
  "Validate that every object in `profile` that is shared between
   versions follows versioning requirements, i.e. they don't differ
   in certain properties."
  [profile]
  (->> (profile->object-seq profile)
       (group-by :id)
       vals
       (s/explain-data (s/every ::versioned-objects))))
