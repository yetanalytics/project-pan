(ns com.yetanalytics.pan.identifiers
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as cset]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn only-ids
  "Return a collection of IDs from a collection of objects."
  [obj-coll]
  (mapv (fn [{:keys [id]}] id) obj-coll))

(defn only-ids-multiple
  "Return a collection of all IDs from multiple collections of objects"
  [obj-colls]
  (flatten (mapv only-ids obj-colls)))

(defn count-ids
  "Count the number of ID instances by creating a map between IDs and their
  respective counts. (Ideally theys should all be one, as IDs MUST be unique
  by definition.)"
  [ids-coll]
  (reduce (fn [accum id]
            (update accum id #(if (nil? %) 1 (inc %)))) {} ids-coll))

(defn profile->ids-map
  [{:keys [id versions concepts templates patterns] :as _profile}]
  {:id        id
   :versions  (mapv :id versions)
   :concepts  (mapv :id concepts)
   :templates (mapv :id templates)
   :patterns  (mapv :id patterns)})

(defn profile->id-seq*
  [{:keys [id versions concepts templates patterns] :as _profile}]
  (concat [id]
          (map :id versions)
          (map :id concepts)
          (map :id templates)
          (map :id patterns)))

(defn profile->id-set*
  [profile]
  (set (profile->id-seq* profile)))

(def profile->id-seq
  "Memoized version of `profile->id-seq*`."
  (memoize profile->id-seq*))

(def profile->id-set
  "Memoized version of `proifle->id-set*`."
  (memoize profile->id-set*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ID distinctness validation
;; All ID values MUST be distinct from each other
;; Covers requirements that version IDs MUST be distinct from each other and
;; from the overall profile ID.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate that a single ID count is 1 
(s/def ::one-count #(= 1 %))

;; Validate that all ID counts are 1
;; Ideally IDs should be identifiers (IRIs, IRLs, etc.), but we do not check
;; for that here.
(s/def ::distinct-ids
  (s/map-of any? ::one-count))

(defn validate-ids
  "Takes a Profile and validates that all ID values in it are distinct.
   Returns nil on success, or spec error data on failure."
  [{:keys [id versions concepts templates patterns]}]
  (let [ids (concat
             [id] (only-ids-multiple [versions concepts templates patterns]))
        counts (count-ids ids)]
    (s/explain-data ::distinct-ids counts)))

(s/def ::non-duped-id
       (fn [[id id-set]] (not (contains? id-set id))))

(defn validate-non-duped-ids
  "Takes `profile` and checks that none of its IDs are duplicated in any of
   the `extra-profiles`."
  [profile extra-profiles]
  (let [id-coll (profile->id-seq profile)
        id-set  (apply cset/union (map profile->id-set extra-profiles))]
    (s/explain-data (s/coll-of ::non-duped-id)
                    (map (fn [id] [id id-set]) id-coll))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; inScheme property validation
;; All inScheme values MUST be a valid Profile version ID
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate that the inScheme is an element of the set of version IDs
(s/def ::in-scheme
  (fn [{:keys [version-ids inScheme]}]
    (contains? version-ids inScheme)))

;; Validate an array of objects with inSchemes
(s/def ::in-schemes (s/coll-of ::in-scheme))

(defn validate-in-schemes
  "Takes a Profile and validates all object inSchemes, which MUST be valid
   version IDs. Returns nil on success, or spec error data on failure."
  [{:keys [versions concepts templates patterns]}]
  (let [version-ids (set (only-ids versions))
        all-objects (concat concepts templates patterns)
        vid-objects (mapv #(assoc % :version-ids version-ids) all-objects)]
    (s/explain-data ::in-schemes vid-objects)))
