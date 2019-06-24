(ns com.yetanalytics.profiles.author
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

(s/def ::type #{"Organization" "Person"})
(s/def ::name ::ax/string)
(s/def ::url ::ax/url)
(s/def ::author (s/keys :req-un [::type
                                 ::name]
                        :opt-un [::url]))
