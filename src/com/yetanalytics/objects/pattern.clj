(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [xapi-schema.spec :as xs]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.objects.object :as object]))

;; Basic properties

(s/def ::id ::ax/uri)
(s/def ::type #{"Pattern"})
(s/def ::primary ::ax/boolean)

;; Regex properties
(s/def :regex/id ::ax/iri)

(s/def ::alternates
  (s/coll-of ::ax/iri :type vector? :min-count 2))
(s/def ::sequence
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::optional
  (s/and (s/keys :req-un [:regex/id]) (xs/restrict-keys :id)))
(s/def ::one-or-more
  (s/and (s/keys :req-un [:regex/id]) (xs/restrict-keys :id)))
(s/def ::zero-or-more
  (s/and (s/keys :req-un [:regex/id]) (xs/restrict-keys :id)))

(s/def ::common
  (s/keys :req-un [(or ::alternates
                       ::optional
                       ::one-or-more
                       ::sequence
                       ::zero-or-more)]
          :opt-un [::primary
                   ::object/in-scheme
                   ::object/deprecated]))

;; Descriptions (prefLabel and definition) are mandatory for primary patterns
;; and are optional for non-primary patterns.

(defmulti pattern? ::primary)

(defmethod pattern? true [_]
  (s/merge ::common ::object/description))

(defmethod pattern? :default [_]
  (s/merge ::common
           (s/keys :opt-un [:object/pref-label
                            :object/definition])))

;; Ensure that only one of the five regex properties are included in pattern.
;; Including two or more properties should fail the spec.

(defn pattern-xor? [p]
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
      zom? (not (or alt? opt? oom? sqn?)))))

(s/def ::pattern (s/and (s/multi-spec pattern? ::primary)
                        pattern-xor?))

(s/def ::patterns (s/coll-of ::pattern :kind vector? :min-count 1))
