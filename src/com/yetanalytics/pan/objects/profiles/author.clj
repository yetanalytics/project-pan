(ns com.yetanalytics.pan.objects.profiles.author
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Author
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::type #{"Organization" "Person"})
(s/def ::name ::ax/string)
(s/def ::url ::ax/url)
(s/def ::author (s/keys :req-un [::type ::name] :opt-un [::url]))
