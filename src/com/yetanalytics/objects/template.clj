(ns com.yetanalytics.objects.template
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.objects.templates.rules :as rules]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/uri)
(s/def ::type #{"StatementTemplate"})
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
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

(s/def ::type-or-reference
  (fn [st]
    (let [otype? (contains? st :object-activity-type)
          otemp? (contains? st :object-statement-ref-template)]
      (not (and otype? otemp?)))))

(s/def ::rules (s/coll-of ::rules/rule :type vector?))

(s/def ::template
  (s/and
   (s/keys :req-un [::id ::type ::in-scheme ::pref-label ::definition]
           :opt-un [::deprecated ::verb ::object-activity-type ::rules
                    ::context-grouping-activity-type ::context-parent-activity-type
                    ::context-other-activity-type ::context-category-activity-type
                    ::attachment-usage-type ::object-statement-ref-template
                    ::context-statement-ref-template])
   ::type-or-reference))

(s/def ::template+
  (fn [{:keys [template vid-set concepts-map templates-map]}])
  (and (s/valid? ::template template)
       (comment "Some other thing")))

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))

(s/def ::templates+ (s/coll-of ::template+ :kind vector? :min-count 1))

; (defn explain-templates
;   [templates]
;   (util/explain-spec-map templates))

; (s/def ::templates+
;   (fn [{templates :templates :as args}]
;     (util/spec-map+ ::template+ :template templates args)))

; (defn explain-templates+
;   [{templates :templates :as args}]
;   (util/explain-spec-map+ ::template+ :template templates args))
