(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns + specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn not-empty? [coll] (coll? (not-empty coll)))

(defn any-falsy? [coll] (filterv false? coll))

(defn any-truthy? [coll] (filterv true? coll))

(defn valid-boolean-cool?
  [coll]
  (if-let [invalids? (not-empty? (any-falsy? coll))]
    false ;; we have false values in a boolean-coll
    (if-let [valids? (not-empty? (any-truthy? coll))]
      true ;; we have no falses and some true
      false))) ;; we may have nils

(s/def ::valid-boolean-coll
  (fn [coll] valid-boolean-cool? coll))

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

(s/def ::in-scheme-strict-scalar
  (fn [{:keys [in-scheme profile]}]
    (in-scheme? in-scheme profile)))


