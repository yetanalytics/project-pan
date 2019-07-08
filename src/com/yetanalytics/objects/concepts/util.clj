(ns com.yetanalytics.objects.concepts.util
  (:require [com.yetanalytics.util :as u]
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inline-schema)]
      (not (and schema? inline-schema?)))))
