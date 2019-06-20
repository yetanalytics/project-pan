(ns com.yetanalytics.templates
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.object :as object]))

;; TODO Add more requirements

;; Basic properties


; (s/def ::id ::ax/iri)
; (s/def ::type #{"StatementTemplate"})
; (s/def ::in-scheme ::ax/iri)
; (s/def ::pref-label ::ax/language-map)
; (s/def ::definition ::ax/language-map)
; (s/def ::deprecated ::ax/boolean)

;; Content of template
(s/def ::verb ::ax/iri)
(s/def ::obj-activity-type ::ax/iri)
(s/def ::context-grouping-activity-type (s/coll-of ::ax/iri :type vector?))
(s/def ::context-parent-activity-type (s/coll-of ::ax/iri :type vector?))
(s/def ::context-other-activity-type (s/coll-of ::ax/iri :type vector?))
(s/def ::context-category-activity-type (s/coll-of ::ax/iri :type vector?))
(s/def ::attachment-usage-type (s/coll-of ::ax/iri :type vector?))
(s/def ::object-statement-ref-temp (s/coll-of ::ax/iri :type vector?))
(s/def ::context-statement-ref-temp (s/coll-of ::ax/iri :type vector?))

;; Rules
(s/def :rule/location ::ax/json-path)
(s/def :rule/selector ::ax/json-path)
(s/def :rule/presence #{"included" "excluded" "recommended"})
(s/def :rule/any (s/coll-of any? :type vector?))
(s/def :rule/all (s/coll-of any? :type vector?))
(s/def :rule/none (s/coll-of any? :type vector?))
(s/def :rule/scope-note ::ax/language-map)

(s/def ::rule (s/keys :req-un [:rule/location
                               (or :rule/presence
                                   :rule/any
                                   :rule/all
                                   :rule/none)]
                      :opt-un [:rule/selector
                               :rule/scope-note]))
(s/def ::rules (s/coll-of ::rule :type vector?))

; (s/def ::template (s/keys :req [::id
;                                 ::type
;                                 ::in-scheme
;                                 ::pref-label
;                                 ::definition]
;                           :opt [::deprecated
;                                 ::verb
;                                 ::obj-activity-type
;                                 ::context-grouping-activity-type
;                                 ::context-parent-activity-type
;                                 ::context-other-activity-type
;                                 ::context-category-activity-type
;                                 ::attachment-usage-type
;                                 ::object-statement-ref-temp
;                                 ::context-statement-ref-temp]))

(s/def ::template
  (s/merge ::object/common
           (s/keys :req-un [::object/in-scheme
                            ::object/pref-label
                            ::object/definition]
                   :opt-un [::object/deprecated
                            ::verb
                            ::object-activity-type
                            ::context-grouping-activity-type
                            ::context-parent-activity-type
                            ::context-other-activity-type
                            ::context-category-activity-type
                            ::attachment-usage-type
                            ::object-statement-ref-template
                            ::context-statement-ref-template
                            ::rules])))

(s/def ::templates (s/coll-of ::template :kind vector?))

(defmethod object/object? "StatementTemplate" [_] ::template)
