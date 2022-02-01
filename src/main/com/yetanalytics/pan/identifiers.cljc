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
   `selected-keys`), return a set of all such IRI values."
  [obj-coll selected-keys]
  (set (reduce
        (fn [acc obj]
          (-> obj (select-keys selected-keys) vals flatten (concat acc)))
        []
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
  (reduce (fn [accum id] (update accum id #(if (nil? %) 1 (inc %))))
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

;; Validate that a single ID count is 1
(s/def ::one-count (fn one? [n] (= 1 n)))

;; Validate that all ID counts are 1
;; Ideally IDs should be identifiers (IRIs, IRLs, etc.), but we do not check
;; for that here.
(s/def ::distinct-ids
  (s/map-of any? ::one-count))

(defn validate-ids
  "Takes a Profile and validates that all ID values in it are distinct
   (including across the extra Profiles). Returns nil on success, or
   spec error data on failure."
  ([profile]
   (let [profile-ids (profile->id-seq profile)
         counts      (count-ids profile-ids)]
     (s/explain-data ::distinct-ids counts)))
  ([profile extra-profiles]
   (let [profile-ids (profile->id-seq profile)
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
