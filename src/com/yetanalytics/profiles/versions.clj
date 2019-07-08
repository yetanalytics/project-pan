(ns com.yetanalytics.profiles.versions
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Versions 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::was-revision-of (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::generated-at-time ::ax/timestamp)

(defn version-set
  "Returns a set of all version IDs."
  [versions] (set (util/only-ids versions)))

;; Every version ID MUST be unique
(s/def ::versions-distinct
  (fn [vcoll]
    (let [vid-set (version-set vcoll)]
      (= (count vid-set) (count vcoll)))))

(s/def ::version (s/keys :req-un [::id ::generated-at-time]
                         :opt-un [::was-revision-of]))

(s/def ::versions (s/and (s/coll-of ::version :type vector? :min-count 1)
                         ::versions-distinct))
