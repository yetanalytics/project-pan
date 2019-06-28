(ns com.yetanalytics.objects.concepts.util
  (:require [com.yetanalytics.util :as u]
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn relate-concept
  [c-type c-version c-table iri]
  (let [other-concept (c-table iri)]
    (and (= c-type (:type other-concept))
         (= c-version (:in-scheme other-concept)))))

(defn recommend-concept
  [c-type c-table uri]
  (let [other-concept (c-table uri)]
    (= c-type (:type other-concept))))

(defn relate-concepts
  [c-type c-version c-table iri-vec]
  (if (some? iri-vec)
    (every? true?
            (map (partial relate-concept c-type c-version c-table) iri-vec))
    true))

(defn recommend-concepts
  [c-type c-table uri-vec]
  (if (some? uri-vec)
    (every? true?
            (map (partial recommend-concept c-type c-table) uri-vec))
    true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inline-schema)]
      (not (and schema? inline-schema?)))))
