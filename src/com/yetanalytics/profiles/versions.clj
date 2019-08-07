(ns com.yetanalytics.profiles.versions
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Versions 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::wasRevisionOf (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::generatedAtTime ::ax/timestamp)

(s/def ::version (s/keys :req-un [::id ::generatedAtTime]
                         :opt-un [::wasRevisionOf]))

(s/def ::versions (s/coll-of ::version :type vector? :min-count 1))
