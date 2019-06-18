(ns com.yetanalytics.patterns
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

;; TODO Ensure that one of the following must be present

(s/def ::alternates (s/coll-of ::ax/iri :type vector?))
(s/def ::optional ::ax/iri)
(s/def ::one-or-more ::ax/iri)
(s/def ::sequence (s/coll-of ::ax/iri :type vector?))
(s/def ::zero-or-more ::ax/iri)

(s/def ::pattern (s/keys :req [::id
                               ::type]
                         :opt [::primary
                               ::in-schem
                               ::pref-label
                               ::definition
                               ::deprecated
                               ::alternates
                               ::optional
                               ::one-or-more
                               ::sequence
                               ::zero-or-more]))
