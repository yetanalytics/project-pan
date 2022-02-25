(ns com.yetanalytics.pan.objects.templates.rule
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rules 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::value-array (s/coll-of any? :type vector? :min-count 1))

(s/def ::location ::ax/json-path)
(s/def ::selector ::ax/json-path)
(s/def ::presence #{"included" "excluded" "recommended"})
(s/def ::any ::value-array)
(s/def ::all ::value-array)
(s/def ::none ::value-array)
(s/def ::scopeNote ::ax/language-map)

(s/def ::rule-keys
  (s/keys :req-un [::location]
          :opt-un [::selector ::scopeNote ::presence ::any ::all ::none]))

;; Statement Template Rule MUST include one or more of
;; `presence`, `any`, `all`, or `none`.
;; NOTE: Need to separate out logic due to weird bug in Expound that causes
;; it to crash when `or` is used in s/keys.
(s/def ::rule-keywords
  (fn has-rule-keyword? [rule]
    (or (contains? rule :presence)
        (contains? rule :any)
        (contains? rule :all)
        (contains? rule :none))))

(s/def ::rule
  (s/and ::rule-keys ::rule-keywords))

;; The following MUST is validated during context validation:
;; A Profile Author MUST include the keys of any non-primitive objects in `any`,
;; `all`, and `none` in additional `@context` beyond the ones provided by
;; this specification.
