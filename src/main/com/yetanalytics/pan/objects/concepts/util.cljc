(ns com.yetanalytics.pan.objects.concepts.util
  (:require [clojure.spec.alpha :as s]))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inlineSchema)]
      (not (and schema? inline-schema?)))))

;; In Concepts that may have the related property, it MUST only be used on a
;; deprecated property
(s/def ::related-only-deprecated
  (fn [{:keys [deprecated related]}]
    (if (some? related)
      (true? deprecated)
      ;; Ignore if related property is not present
      true)))

(defmulti get-iris
  "Given an object, return a map of keys `:type/property` to IRI coll."
  :type)