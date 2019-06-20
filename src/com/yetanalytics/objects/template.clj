(ns com.yetanalytics.objects.template
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.objects.object :as object]))

;; Basic properties

(s/def ::id ::ax/uri)
(s/def ::type #{"StatementTemplate"})

;; Content of template
(s/def ::verb ::ax/iri)
(s/def ::object-activity-type ::ax/iri)
(s/def ::context-grouping-activity-type
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::context-parent-activity-type
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::context-other-activity-type
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::context-category-activity-type
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::attachment-usage-type
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::object-statement-ref-template
  (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::context-statement-ref-template
  (s/coll-of ::ax/iri :type vector? :min-count 1))

;; Ensure that any Statement Template does not have both objectActivityType
;; and objectStatementRefTemplate at the same time
(defn object-nand? [st]
  (let [otype? (contains? st :object-activity-type)
        otemp? (contains? st :object-statement-ref-template)]
    (not (and otype? otemp?))))

;; Rules and their properties


(s/def :rule/location ::ax/json-path)
(s/def :rule/selector ::ax/json-path)
(s/def :rule/presence #{"included" "excluded" "recommended"})
(s/def :rule/any (s/coll-of any? :type vector? :min-count 1))
(s/def :rule/all (s/coll-of any? :type vector? :min-count 1))
(s/def :rule/none (s/coll-of any? :type vector? :min-count 1))
(s/def :rule/scope-note ::ax/language-map)

; The "or" accounts for the fact that any of "presence", "any", "all" or "none"
; can be included in a rule (though there must always be at least one in the
; map).
(s/def ::rule (s/keys :req-un [:rule/location
                               (or :rule/presence
                                   :rule/any
                                   :rule/all
                                   :rule/none)]
                      :opt-un [:rule/selector
                               :rule/scope-note]))
(s/def ::rules (s/coll-of ::rule :type vector?))

(s/def ::template
  (s/and
   (s/merge ::object/common
            (s/keys :req-un [::id
                             ::type]
                    :opt-un [::verb
                             ::object-activity-type
                             ::context-grouping-activity-type
                             ::context-parent-activity-type
                             ::context-other-activity-type
                             ::context-category-activity-type
                             ::attachment-usage-type
                             ::object-statement-ref-template
                             ::context-statement-ref-template
                             ::rules]))
   object-nand?))

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))
