(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.concepts.verbs :as cv]
            [com.yetanalytics.concepts.extensions :as ce]
            [com.yetanalytics.concepts.doc-resources :as cd]
            [com.yetanalytics.concepts.activities :as ca]
            [com.yetanalytics.templates :as t]
            [com.yetanalytics.patterns :as p]))

(s/def ::id ::ax/iri)
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/coll-of ::ax/uri :type vector?)))
(s/def ::type ::ax/typekey-profile)
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::see-also ::ax/url)

(s/def :version/id ::ax/iri)
(s/def :version/was-revision-of (s/coll-of ::ax/iri :type vector?))
(s/def :version/gen-at-time ::ax/timestamp)
(s/def ::versions (s/coll-of (s/keys :req [:version/id :version/gen-at-time]
                                     :opt [:version/was-revision-of])
                             :type vector?))

(s/def :author/type #{"Organization" "Person"})
(s/def :author/name ::ax/string)
(s/def :author/url ::ax/url)
(s/def ::author (s/keys :req [:author/type :author/name]
                        :opt [:author/url]))

(s/def ::concepts (s/coll-of (s/or :verbs ::cv/verb
                                   :extensions ::ce/extensions
                                   :doc-resources ::cd/resources
                                   :activities ::ca/activities)
                             :type vector?))

(s/def ::templates (s/coll-of ::t/template :type vector?))
(s/def ::patterns (s/coll-of ::p/pattern :type vector?))

(s/def ::profile (s/keys :req [::id
                               ::context
                               ::type
                               ::conforms-to
                               ::pref-label
                               ::definition
                               ::versions
                               ::author]
                         :opt [::see-also
                               ::concepts
                               ::templates
                               ::patterns]))
