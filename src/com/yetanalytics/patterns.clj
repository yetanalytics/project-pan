(ns com.yetanalytics.patterns
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.object :as object]))

;; Basic properties

; (s/def ::id ::ax/uri)
; (s/def ::type #{"Pattern"})
; (s/def ::primary ::ax/boolean)
; (s/def ::in-scheme ::ax/iri)
; (s/def ::pref-label ::ax/language-map)
; (s/def ::definition ::ax/language-map)
; (s/def ::deprecated ::ax/boolean)

(s/def ::primary ::ax/boolean)

;; TODO Ensure that one of the following must be present

(s/def ::alternates (s/coll-of ::ax/iri :type vector?))
(s/def ::optional ::ax/iri)
(s/def ::one-or-more ::ax/iri)
(s/def ::sequence (s/coll-of ::ax/iri :type vector?))
(s/def ::zero-or-more ::ax/iri)

; (s/def ::pattern (s/keys :req [::id
;                                ::type
;                                (or ::alternates
;                                    ::optional
;                                    ::one-or-more
;                                    ::sequence
;                                    ::zero-or-more)]
;                          :opt [::primary
;                                ::in-schem
;                                ::pref-label
;                                ::definition
;                                ::deprecated]))

(s/def ::common
  (s/merge ::object/common
           (s/keys :req-un [(or ::alternates
                                ::optional
                                ::one-or-more
                                ::sequence
                                ::zero-or-more)]
                   :opt-un [::primary
                            ::object/in-scheme
                            ::object/deprecated])))

(defmulti pattern? ::primary)

(defmethod pattern? true [_]
  (s/merge ::common
           (s/keys :req-un [:object/pref-label
                            :object/definition])))

(defmethod pattern? :default [_]
  (s/merge ::common
           (s/keys :opt-un [:object/pref-label
                            :object/definition])))

(defn pattern-xor? [p]
  (let [alt? (contains? p :alternates)
        opt? (contains? p :optional)
        oom? (contains? p :one-or-more)
        sqn? (contains? p :sequence)
        zom? (contains? p :zero-or-more)]
    (cond
      (alt?) (not (or opt? oom? sqn? zom?))
      (opt?) (not (or alt? oom? sqn? zom?))
      (oom?) (not (or alt? opt? sqn? zom?))
      (sqn?) (not (or alt? opt? oom? zom?))
      (zom?) (not (or alt? opt? oom? sqn?)))))

(s/def ::pattern (s/and (s/multi-spec pattern? ::primary)
                        pattern-xor?))

(s/def ::patterns (s/coll-of ::pattern :kind vector?))

(defmethod object/object? "Pattern" [_] ::pattern)
