(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.context :as context]
            [com.yetanalytics.profiles.versions :as versions]
            [com.yetanalytics.profiles.author :as author]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Profile"})
(s/def ::conformsTo ::ax/uri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::seeAlso ::ax/url)

;; @context SHOULD be the following URI and MUST contain it if URI valued
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::has-context-url (partial some #(= context-url %)))
(s/def ::_context
  (s/or :context-iri ::ax/uri
        :context-array (s/and ::ax/array-of-uri
                              ::has-context-url)))

(s/def ::profile
  (s/keys :req-un [::id ::_context ::type ::conformsTo ::prefLabel
                   ::definition ::author/author ::versions/versions]
          :opt-un [::seeAlso
                   ::concept/concepts
                   ::template/templates
                   ::pattern/patterns]))

(defn validate
  "Syntax-only validation.
  Returns a spec trace if validation fails, or nil if successful."
  [profile] (s/explain-data ::profile profile))
