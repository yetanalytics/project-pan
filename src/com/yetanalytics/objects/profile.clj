(ns com.yetanalytics.objects.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.objects.object :as object]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]))

(s/def ::type #{"Profile"})

(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))

(s/def ::conforms-to ::ax/uri)
(s/def ::see-also ::ax/url)

; Version object
; (s/def :version/id ::object/id)
(s/def :version/was-revision-of (s/coll-of ::ax/iri
                                           :type vector? :min-count 1))
(s/def :version/gen-at-time ::ax/timestamp)
(s/def ::version (s/keys :req-un [::object/id
                                  :version/gen-at-time]
                         :opt-un [:version/was-revision-of]))
(s/def ::versions (s/coll-of ::version :type vector? :min-count 1))

; Author object
(s/def :author/type #{"Organization" "Person"})
(s/def :author/name ::ax/string)
(s/def :author/url ::ax/url)
(s/def ::author (s/keys :req-un [::object/type
                                 :author/name]
                        :opt-un [:author/url]))

(s/def ::profile
  (s/merge ::object/description
           (s/keys :req-un [::object/id
                            ::type
                            ::context
                            ::conforms-to]
                   :opt-un [::see-also
                            ::versions
                            ::author
                            ::concept/concepts
                            ::template/templates
                            ::pattern/patterns])))
