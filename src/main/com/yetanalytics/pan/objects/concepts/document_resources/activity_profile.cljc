(ns com.yetanalytics.pan.objects.concepts.document-resources.activity-profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.utils.spec :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity Profile Resource
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"ActivityProfileResource"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::contentType ::ax/media-type)
(s/def ::deprecated ::ax/boolean)
(s/def ::context ::ax/iri)
(s/def ::schema ::ax/iri)
(s/def ::inlineSchema ::ax/json-schema)

(s/def ::document-resource
  (s/and (s/keys
          :req-un [::id ::type ::inScheme ::prefLabel ::definition
                   ::contentType]
          :opt-un [::deprecated ::context ::schema ::inlineSchema])
         ::util/inline-or-iri))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Currently does nothing
(defmethod graph/edges-with-attrs "ActivityProfileResource" [_] nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: json-ld context validation
;; context - valid json-ld context

;; TODO: get string from iri
;; schema - json-schema string at other end of iri

;; TODO: LRS clients sending Document resources checks required by the spec
;; - id
;; - contentType
