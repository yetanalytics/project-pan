(ns com.yetanalytics.objects.template
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.objects.templates.rules :as rules]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
           :opt-un [::deprecated ::rules
                    ::verb
                    ::object-activity-type
                    ::context-grouping-activity-type
                    ::context-parent-activity-type
                    ::context-other-activity-type
                    ::context-category-activity-type
                    ::attachment-usage-type
                    ::object-statement-ref-template
                    ::context-statement-ref-template])
   ::type-or-reference))

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn match-concept
  [c-type c-table iri]
  (if (some? iri)
    (= c-type (:type (c-table iri)))
    true))

(defn match-concepts
  [c-type c-table iri-vec]
  (if (some? iri-vec)
    (every? true? (map #(= c-type (:type (c-table %))) iri-vec))
    true))

(defn match-templates
  [t-version t-table iri-vec]
  (if (some? iri-vec)
    (every? true? (map #(= t-version (:in-scheme (t-table %))) iri-vec))
    true))

(s/def ::template-basic
  (fn [{:keys [object]}] (s/valid? ::template object)))

(s/def ::verb-iri
  (fn [{:keys [object concepts-table]}]
    (match-concept "Verb" concepts-table (:verb object))))

(s/def ::object-activity-type-iri
  (fn [{:keys [object concepts-table]}]
    (match-concept "ActivityType" concepts-table
                   (:object-activity-type object))))

(s/def ::context-grouping-activity-type-iris
  (fn [{:keys [object concepts-table]}]
    (match-concepts "ActivityType" concepts-table
                    (:context-grouping-activity-type object))))

(s/def ::context-parent-activity-type-iris
  (fn [{:keys [object concepts-table]}]
    (match-concepts "ActivityType" concepts-table
                    (:context-parent-activity-type object))))

(s/def ::context-other-activity-type-iris
  (fn [{:keys [object concepts-table]}]
    (match-concepts "ActivityType" concepts-table
                    (:context-other-activity-type object))))

(s/def ::context-category-activity-type-iris
  (fn [{:keys [object concepts-table]}]
    (match-concepts "ActivityType" concepts-table
                    (:context-category-activity-type object))))

(s/def ::attachment-usage-type-iris
  (fn [{:keys [object concepts-table]}]
    (match-concepts "AttachmentUsageType" concepts-table
                    (:attachment-usage-type object))))

(s/def ::object-statement-ref-template-iris
  (fn [{:keys [object templates-table]}]
    (match-templates (:in-scheme object) templates-table
                     (:object-statement-ref-template object))))

(s/def ::context-statement-ref-template-iris
  (fn [{:keys [object templates-table]}]
    (match-templates (:in-scheme object) templates-table
                     (:context-statement-ref-template object))))

(s/def ::template+
  (s/and ::template-basic
         ::util/in-scheme-valid?
         ::verb-iri
         ::object-activity-type-iri
         ::context-grouping-activity-type-iris
         ::context-parent-activity-type-iris
         ::context-other-activity-type-iris
         ::context-category-activity-type-iris
         ::attachment-usage-type-iris
         ::object-statement-ref-template-iris
         ::context-statement-ref-template-iris))

(s/def ::templates+ (s/coll-of ::template+ :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO Fix stuff in the rules
