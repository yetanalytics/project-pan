(ns com.yetanalytics.profiles.versions
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]))

(s/def ::id ::ax/iri)
(s/def ::was-revision-of (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::generated-at-time ::ax/timestamp)

;; Make sure that version IDs are distinct from each other
(s/def ::versions-distinct
  (fn [vcoll]
    (let [vids (u/only-ids vcoll)]
      (= (count vids)
         (count (set vids))))))

(s/def ::version (s/keys :req-un [::id ::generated-at-time]
                         :opt-un [::was-revision-of]))

(s/def ::versions (s/and (s/coll-of ::version :type vector? :min-count 1)
                         ::versions-distinct))
