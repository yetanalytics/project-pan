(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.object :as object]
            ; [com.yetanalytics.concepts.verbs :as cv]
            ; [com.yetanalytics.concepts.extensions :as ce]
            ; [com.yetanalytics.concepts.doc-resources :as cd]
            ; [com.yetanalytics.concepts.activities :as ca]
            ; [com.yetanalytics.templates :as t]
            ; [com.yetanalytics.patterns :as p]
))

; (s/def ::id ::ax/iri)
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))
; (s/def ::type ::ax/typekey-profile)
(s/def ::conforms-to ::ax/uri)
; (s/def ::pref-label ::ax/language-map)
; (s/def ::definition ::ax/language-map)
(s/def ::see-also ::ax/url)

; (s/def :version/id ::ax/iri)
(s/def :version/was-revision-of (s/coll-of ::ax/iri
                                           :type vector? :min-count 1))
(s/def :version/gen-at-time ::ax/timestamp)
(s/def ::versions (s/coll-of (s/keys :req-un [::object/id
                                              :version/gen-at-time]
                                     :opt-un [:version/was-revision-of])
                             :type vector? :min-count 1))

; (s/def :author/type #{"Organization" "Person"})
(s/def :author/name ::ax/string)
(s/def :author/url ::ax/url)
(s/def ::author (s/keys :req-un [::object/type
                                 :author/name]
                        :opt-un [:author/url]))

(defmethod object/object? "Organization" [_] ::author)
(defmethod object/object? "Person" [_] ::author)

; (s/def ::concepts (s/coll-of (s/or :verbs ::cv/verb
;                                    :extensions ::ce/extensions
;                                    :doc-resources ::cd/resources
;                                    :activities ::ca/activities)
;                              :type vector?))

; (s/def ::templates (s/coll-of ::t/template :type vector?))
; (s/def ::patterns (s/coll-of ::p/pattern :type vector?))

; (s/def ::profile (s/keys :req [::id
;                                ::context
;                                ::type
;                                ::conforms-to
;                                ::pref-label
;                                ::definition
;                                ::versions
;                                ::author]
;                          :opt [::see-also
;                                ::concepts
;                                ::templates
;                                ::patterns]))

(s/def :profile/profile
  (s/merge :object/common
           (s/keys :req-un [::context
                            ::conforms-to
                            ::object/pref-label
                            ::object/definition]
                   :opt-un [::see-also
                            ::versions
                            ::author
                            ::concepts
                            ::templates
                            ::patterns])))

(defmethod object/object? "Profile" [_] ::profile)

(defmethod object/foo "hello" [s] s)
(defmethod object/foo "goodbye" [s] "TATA")
