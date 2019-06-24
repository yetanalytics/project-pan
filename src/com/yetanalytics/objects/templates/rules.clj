(ns com.yetanalytics.objects.templates.rules
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

(s/def ::location ::ax/json-path)
(s/def ::selector ::ax/json-path)
(s/def ::presence #{"included" "excluded" "recommended"})
(s/def ::any (s/coll-of any? :type vector? :min-count 1))
(s/def ::all (s/coll-of any? :type vector? :min-count 1))
(s/def ::none (s/coll-of any? :type vector? :min-count 1))
(s/def ::scope-note ::ax/language-map)

;; TODO: rule-must:context

(s/def ::rule-must
  (s/or :presence ::presence
        :any      ::any
        :all      ::all
        :none     ::none))

(s/def ::rule
  (s/and (s/keys :req-un [::location]
                 :opt-un [::selector ::presence ::any ::all ::none ::scope-note])
         ::rule-must))
