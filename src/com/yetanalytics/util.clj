(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns + specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; True if collection is not empty, false otherwise.
(defn not-empty? [coll] (coll? (not-empty coll)))

;; Filter s.t. only false values remain.
(defn any-falsy? [coll] (filterv false? coll))

;; Filter s.t. only true values remain.
(defn any-truthy? [coll] (filterv true? coll))

(defn valid-boolean-coll?
  "False if the collection contains any false values, true otherwise."
  [coll]
  (if-let [invalids? (not-empty? (any-falsy? coll))]
    false ;; we have false values in a boolean-coll
    (if-let [valids? (not-empty? (any-truthy? coll))]
      true ;; we have no falses and some true
      true))) ;; we may have nils - case of iri living in current profile OR external profile

;; Spec version of valid-boolean-coll
(s/def ::valid-boolean-coll
  (fn [coll] valid-boolean-coll? coll))

;; Returns a collection of ids
(defn only-ids [filtered-coll] (mapv (fn [{:keys [id]}] id) filtered-coll))

(defn filter-by-in-scheme
  [target-in-scheme coll]
  (filterv (fn [{:keys [in-scheme]}] (= target-in-scheme in-scheme)) coll))

(defn containsv? [coll v]
  (try (not= -1 (.indexOf coll v))
       (catch Exception e false)))

(defn in-scheme?
  [in-scheme profile]
  (-> profile :versions only-ids (containsv? in-scheme)))

;; Determine whether a in-scheme IRI is valid within a profile.
(s/def ::in-scheme-strict-scalar
  (fn [{:keys [in-scheme profile]}]
    (in-scheme? in-scheme profile)))
