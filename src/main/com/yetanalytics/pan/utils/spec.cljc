(ns com.yetanalytics.pan.utils.spec
  (:require [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-nil
  "Turn a nil array into an empty array"
  [value]
  (if (nil? value) [] value))

(defn normalize-profile
  "Turn any nil top-level arrays in a profile (versions, concepts, templates
  and patterns) into empty arrays."
  [profile]
  (-> profile
      (update :versions normalize-nil)
      (update :concepts normalize-nil)
      (update :templates normalize-nil)
      (update :patterns normalize-nil)))

(defn type-dispatch
  "Dispatch on the type key of an object, eg. in a multimethod.
  Works for both Profile objects and graph edges with the type attribute"
  [object]
  (:type object))

(defn subvec?
  "True if v1 is a subvector of v2, false otherwise."
  [v1 v2]
  (let [len1 (count v1) len2 (count v2)]
    (and (<= len1 len2)
         (= v1 (subvec v2 0 len1)))))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inlineSchema)]
      (not (and schema? inline-schema?)))))

;; In concepts that may have the related property, it MUST only be used on a
;; deprecated property
(s/def ::related-only-deprecated
  (fn [{:keys [deprecated related]}]
    (if (some? related)
      (true? deprecated)
      ;; Ignore if related property is not present
      true)))
