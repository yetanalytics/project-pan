(ns com.yetanalytics.profiles.versions
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

(s/def ::id ::ax/iri)
(s/def ::was-revision-of (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::gen-at-time ::ax/timestamp)
(s/def ::version (s/keys :req-un [::id ::gen-at-time]
                         :opt-un [::was-revision-of]))
(s/def ::versions (s/coll-of ::version :type vector? :min-count 1))
