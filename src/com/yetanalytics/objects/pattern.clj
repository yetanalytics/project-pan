(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

;; Basic properties

(s/def ::id ::ax/uri)
(s/def ::type #{"Pattern"})
(s/def ::primary ::ax/boolean)
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

(s/def ::alternates (s/coll-of ::ax/iri :type vector? :min-count 2))
(s/def ::optional (s/keys :req-un [::id]))
(s/def ::one-or-more (s/keys :req-un [::id]))
(s/def ::sequence (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::zero-or-more (s/keys :req-un [::id]))

;; Ensure that only one of the five regex properties are included in pattern.
;; Including two or more properties should fail the spec.

(s/def ::pattern-clause
  (fn [p]
    (let [alt? (contains? p :alternates)
          opt? (contains? p :optional)
          oom? (contains? p :one-or-more)
          sqn? (contains? p :sequence)
          zom? (contains? p :zero-or-more)]
      (cond
        alt? (not (or opt? oom? sqn? zom?))
        opt? (not (or alt? oom? sqn? zom?))
        oom? (not (or alt? opt? sqn? zom?))
        sqn? (not (or alt? opt? oom? zom?))
        zom? (not (or alt? opt? oom? sqn?))))))

(s/def ::primary-pattern
  (s/and (s/keys :req-un [::id ::type ::pref-label ::definition ::primary]
                 :opt-un [::in-scheme ::deprecated ::alternates ::optional
                          ::one-or-more ::sequence ::zero-or-more])
         ::pattern-clause))

(s/def ::pattern
  (s/and (s/keys :req-un [::id ::type]
                 :opt-un [::primary ::in-scheme ::pref-label ::definition
                          ::deprecated ::alternates ::optional ::one-or-more
                          ::sequence ::zero-or-more])
         ::pattern-clause))

(s/def ::patterns
  (s/coll-of (s/or :pattern ::pattern
                   :primary ::primary-pattern) :kind vector? :min-count 1))

;; TODO: MUST + MUST NOTS from Profile Authors: section
;; https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#90-patterns
