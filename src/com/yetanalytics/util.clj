(ns com.yetanalytics.util)

(defn containsv? [coll v]
  (try (not= -1 (.indexOf coll v))
       (catch Exception e false)))

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

(defn only-ids [filtered-coll] (mapv (fn [{:keys [id]}] id) filtered-coll))

(defn in-scheme?
  [in-scheme profile]
  (-> profile :versions only-ids (containsv? in-scheme)))

(defn filter-by-in-scheme
  [target-in-scheme coll]
  (filterv (fn [{:keys [in-scheme]}] (= target-in-scheme in-scheme)) coll))
