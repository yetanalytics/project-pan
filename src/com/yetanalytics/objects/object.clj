(ns com.yetanalytics.object.object
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

;; Basic properties

;; ID and type properties are required by ALL objects, no exceptions
(s/def ::id ::ax/iri) ; Technically Templates and Patterns require URIs

;; Properties common across all objects; however some objects may not have
;; them (or only have them under certain circumstances).
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

(s/def ::description (s/keys :req-un [::pref-label ::definition]))
(s/def ::common (s/merge ::description
                         (s/keys :req-un [::in-scheme]
                                 :opt-un [::deprecated])))
