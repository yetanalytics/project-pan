(ns com.yetanalytics.identifiers
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn only-ids
  "Return a collection of IDs from a collection of objects."
  [obj-coll] (mapv (fn [{:keys [id]}] id) obj-coll))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ID distinctness validation
;; All ID values MUST be distinct from each other
;; Covers requirements that version IDs MUST be distinct from each other and
;; from the overall profile ID.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate that a single ID count is 1 
(s/def ::one-count #(= 1 %))

;; Validate that all ID counts are 1 
(s/def ::distinct-ids
  (s/map-of (s/or :iri ::ax/iri :irl ::ax/irl :uri ::ax/uri :url ::ax/url)
            ::one-count))

(defn validate-ids
  "Validate that all ID values in a Profile are distinct.
  Returns nil on success, or spec error data on failure."
  [{:keys [id versions concepts templates patterns] :as profile}]
  (let [ids (concat
             [id] (only-ids-multiple [versions concepts templates patterns]))
        counts (count-ids ids)]
    (s/explain-data ::distinct-ids counts)))

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
  "Validate all object inSchemes, which MUST be valid version IDs.
  Returns nil on success, or spec error data on failure."
  [{:keys [versions concepts templates patterns] :as profile}]
  (let [version-ids (set (only-ids versions))
        all-objects (concat concepts templates patterns)
        vid-objects (mapv #(assoc % :version-ids version-ids) all-objects)]
    (s/explain-data ::in-schemes vid-objects)))
