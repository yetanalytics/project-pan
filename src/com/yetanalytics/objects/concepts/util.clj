(ns com.yetanalytics.objects.concepts.util
  (:require [com.yetanalytics.util :as u]
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn filter-concepts
  [{:keys [target-type-str concepts ?deprecated]}]
  (if ?deprecated
    (filterv (fn [concept]
               (let [{c-type :type
                      c-dep? :deprecated} concept]
                 (and (= target-type-str c-type) c-dep?))) concepts)
    (filterv (fn [concept] (-> concept :type (= target-type-str))) concepts)))

(defn ids-of-target-concept-type
  [{:keys [target-type-str profile ?deprecated]}]
  (->> {:target-type-str target-type-str
        :concepts (:concepts profile)
        :?deprecated ?deprecated}
       filter-concepts
       u/only-ids))

(defn iri-in-profile-concepts?
  ;; assumes profile is a keywordized map
  [{:keys [iri target-type-str profile ?deprecated]}]
  (let [concepts (ids-of-target-concept-type {:target-type-str target-type-str
                                              :profile profile
                                              :?deprecated ?deprecated})]
    (u/containsv? concepts iri)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::inline-or-iri
  (fn json-schema-xor? [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inline-schema)]
      (or (and schema? (not inline-schema?))
          (and inline-schema? (not inline-schema?))
          (and (not schema?) (not inline-schema?))))))
