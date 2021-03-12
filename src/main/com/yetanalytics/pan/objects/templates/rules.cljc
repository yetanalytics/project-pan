(ns com.yetanalytics.pan.objects.templates.rules
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rules 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::location ::ax/json-path)
(s/def ::selector ::ax/json-path)
(s/def ::presence #{"included" "excluded" "recommended"})
(s/def ::any (s/coll-of any? :type vector? :min-count 1))
(s/def ::all (s/coll-of any? :type vector? :min-count 1))
(s/def ::none (s/coll-of any? :type vector? :min-count 1))
(s/def ::scopeNote ::ax/language-map)

(s/def ::rule
  (s/keys :req-un [::location
                   (or ::presence ::any ::all ::none)]
          :opt-un [::selector ::scopeNote]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: rule-must:context 
