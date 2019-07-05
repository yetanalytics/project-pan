(ns com.yetanalytics.objects.template
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
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

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))

(defmethod util/edges-with-attrs "StatementTemplate"
  [{:keys [id
           verb
           object-activity-type
           context-grouping-activity-type
           context-parent-activity-type
           context-other-activity-type
           context-category-activity-type
           attachment-usage-type
           object-statement-ref-template
           context-statement-ref-template]}]
  (into [] (filter #(some? (second %))
                   (concat
                    (vector (vector id verb {:type :verb}))
                    (vector (vector id object-activity-type
                                    {:type :object-activity-type}))
                    (map #(vector id % {:type :context-grouping-activity-type})
                         context-grouping-activity-type)
                    (map #(vector id % {:type :context-parent-activity-type})
                         context-parent-activity-type)
                    (map #(vector id % {:type :context-other-activity-type})
                         context-other-activity-type)
                    (map #(vector id % {:type :context-category-activity-type})
                         context-category-activity-type)
                    (map #(vector id % {:type :attachment-usage-type})
                         attachment-usage-type)
                    (map #(vector id % {:type :object-statement-ref-template})
                         object-statement-ref-template)
                    (map #(vector id % {:type :context-statement-ref-template})
                         context-statement-ref-template)))))

(defn get-edges
  [tgraph]
  (let [edges (uber/edges tgraph)]
    (mapv #({:src-id (uber/src %)
             :src-type (uber/attr tgraph (uber/src %) :type)
             :src-version (uber/attr tgraph (uber/src %) :in-scheme)
             :dest-id (uber/dest %)
             :dest-type (uber/attr tgraph (uber/dest %) :type)
             :dest-version (uber/attr tgraph (uber/src % :in-scheme))
             :type (:type (uber/attr tgraph %))})
          edges)))

(defmulti valid-edge? #(:type %))

(defmethod valid-edge? :verb [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"Verb"} dest-type)))

(defmethod valid-edge? :object-activity-type [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"ActivityType"} dest-type)))

(defmethod valid-edge? :context-grouping-activity-type
  [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"ActivityType"} dest-type)))

(defmethod valid-edge? :context-parent-activity-type
  [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"ActivityType"} dest-type)))

(defmethod valid-edge? :context-other-activity-type
  [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"ActivityType"} dest-type)))

(defmethod valid-edge? :attachment-usage-type
  [{:keys src-type dest-type}]
  (and (#{"StatementTemplate"} src-type)
       (#{"AttachmentUsageType"} dest-type)))

(defmethod valid-edge? :object-statement-ref-template
  [{:keys src-type dest-type src-version dest-version}]
  (and (#{"StatementTemplate"} src-type)
       (#{"StatementTemplate"} dest-type)
       (= src-version dest-version)))

(defmethod valid-edge? :context-statement-ref-template
  [{:keys src-type dest-type src-version dest-version}]
  (and (#{"StatementTemplate"} src-type)
       (#{"StatementTemplate"} dest-type)
       (= src-version dest-version)))

(defmethod valid-edge? :default [_] false)

(s/def ::valid-edge valid-edge?)

(s/def ::valid-edges (s/coll-of ::valid-edge))

(s/def ::template-graph
  (fn [tgraph] (s/valid? ::valid-edges (get-edges tgraph))))

(defn explain-template-graph [tgraph]
  (s/explain-data ::valid-edges (get-edges tgraph)))
