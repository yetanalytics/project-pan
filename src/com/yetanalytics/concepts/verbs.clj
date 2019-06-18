(ns com.yetanalytics.concepts.verbs
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

;; Basic properties

(s/def ::id ::ax/iri)
(s/def ::type #{"Verb" "ActivityType" "AttachmentUsageType"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

;; Properties that relate to other Concepts
;; TODO: Validate that external Concepts have same type (and whether Concepts
;; should be from the same or different Profiles)
(s/def ::broader (s/coll-of ::ax/iri :type vector?))
(s/def ::broader-match (s/coll-of ::ax/iri :type vector?))
(s/def ::narrower (s/coll-of ::ax/iri :type vector?))
(s/def ::narrower-match (s/coll-of ::ax/iri :type vector?))
(s/def ::related (s/coll-of ::ax/iri :type vector?))
(s/def ::related-match (s/coll-of ::ax/iri :type vector?))
(s/def ::exact-match (s/coll-of ::ax/iri :type vector?))

(s/def ::verb (s/keys :req [::id
                            ::type
                            ::in-scheme
                            ::pref-label
                            ::definition]
                      :opt [::deprecated
                            ::broader
                            ::broader-match
                            ::narrower
                            ::narrower-match
                            ::related
                            ::related-match
                            ::exact-match]))
