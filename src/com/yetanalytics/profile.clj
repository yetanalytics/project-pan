(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.profiles.versions :as versions]
            [com.yetanalytics.profiles.author :as author]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]))

(s/def ::id ::ax/iri)
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))
(s/def ::type #{"Profile"})
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)

;; prefLabel + definition
(s/def ::see-also ::ax/url)


(s/def ::profile
  (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                   ::definition ::author/author ::versions/versions]
          :opt-un [::see-also
                   ::concept/concepts
                   ::template/templates
                   ::pattern/patterns]))

;; TODO: stricter validation levels
