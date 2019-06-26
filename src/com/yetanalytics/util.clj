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

;; Create a map of ids to the objects they identify
(defn id-map [map-with-ids] (zipmap (only-ids map-with-ids) map-with-ids))

(defn filter-by-in-scheme
  [target-in-scheme coll]
  (filterv (fn [{:keys [in-scheme]}] (= target-in-scheme in-scheme)) coll))

(defn containsv? [coll v]
  (try (not= -1 (.indexOf coll v))
       (catch Exception e false)))

(defn in-scheme?
  [in-scheme profile]
  (-> profile :versions only-ids (containsv? in-scheme)))

(defn explain-spec-map
  "Return a map of error messages (given by spec/explain-data) when spec-ing an 
  array of items."
  [spec item-vec]
  (filterv some?
           (mapv (partial s/explain-data spec) item-vec)))

(s/def ::foo-spec (s/or :first #(contains? % :id) :second #(contains? % :another-valid-key)))

(count (explain-spec-map ::foo-spec [{:id "bar"} {:not-id "baz"} {:another-valid-key "blue"}]))
(count (s/explain-data ::foo-spec [{:id "bar"} {:not-id "baz"} {:another-valid "blue"}]))

(defn spec-map+
  "Return true or false from spec-ing an array of iterms with arguments."
  [spec obj-kword item-vec args-map]
  (let [args (dissoc args-map obj-kword)
        ivec (obj-kword args-map)]
    (every? true? (mapv #(s/valid? spec (conj args-map [obj-kword %])) ivec))))

(defn explain-spec-map+
  "Return a map of error messages (viven by spec/explain-data) when spec-ing
  an array of items with arguments."
  [spec obj-kword item-vec args-map]
  (let [args (dissoc args-map obj-kword)
        ivec (obj-kword args-map)]
    (filterv some?
             (mapv #(s/explain-data spec (conj args-map [obj-kword %])) ivec))))

(s/def ::foo-spec+
  (fn [{:keys [foo others]}]
    (s/valid? ::foo-spec foo)))

(count (explain-spec-map+ ::foo-spec+
                          :foo
                          [{:id "bar"} {:not-id "baz"} {:id "blue"}]
                          {:foo [{:id "bar"} {:not-id "baz"} {:id "blue"}]
                           :others "something else"}))

(s/def ::foo-spec-map+
  (fn [{foos :foo :as args}]
    (spec-map+ ::template)))

(s/explain ::foo-spec-map+
           ::foo-spec+
           :foo
           [{:id "bar"} {:not-id "baz"} {:id "blue"}]
           {:foo [{:id "bar"} {:not-id "baz"} {:id "blue"}]
            :others "something else"})

;; Determine whether a in-scheme IRI is valid within a profile.
(s/def ::in-scheme-strict-scalar
  (fn [{:keys [in-scheme profile]}]
    (in-scheme? in-scheme profile)))

(s/def ::in-scheme-valid?
  (fn [{:keys [object version-id-set]}]
    (if (nil? version-id-set)
      false
      (contains? version-id-set (object :in-scheme)))))
