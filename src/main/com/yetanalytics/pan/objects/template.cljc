(ns com.yetanalytics.pan.objects.template
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.templates.rules :as rules]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/uri)
(s/def ::type #{"StatementTemplate"})
(s/def ::inScheme ::ax/iri)
(s/def ::prefLabel ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)
(s/def ::verb ::ax/iri)
(s/def ::objectActivityType ::ax/iri)
(s/def ::contextGroupingActivityType ::ax/array-of-iri)
(s/def ::contextParentActivityType ::ax/array-of-iri)
(s/def ::contextOtherActivityType ::ax/array-of-iri)
(s/def ::contextCategoryActivityType ::ax/array-of-iri)
(s/def ::attachmentUsageType ::ax/array-of-iri)
(s/def ::objectStatementRefTemplate ::ax/array-of-iri)
(s/def ::contextStatementRefTemplate ::ax/array-of-iri)

;; A StatementTemplate MUST NOT have both objectStatementRefTemplate and
;; objectActivityType at the same time.
(defn- type-or-ref? [st]
  (let [otype? (contains? st :objectActivityType)
        otemp? (contains? st :objectStatementRefTemplate)]
    (not (and otype? otemp?))))

(s/def ::type-or-reference type-or-ref?)

(s/def ::rules (s/coll-of ::rules/rule :type vector?))

(s/def ::template-keys
  (s/keys :req-un [::id ::type ::inScheme ::prefLabel ::definition]
          :opt-un [::deprecated
                   ::rules
                   ::verb
                   ::objectActivityType
                   ::contextGroupingActivityType
                   ::contextParentActivityType
                   ::contextOtherActivityType
                   ::contextCategoryActivityType
                   ::attachmentUsageType
                   ::objectStatementRefTemplate
                   ::contextStatementRefTemplate]))

(s/def ::template (s/and ::template-keys ::type-or-reference))

(s/def ::templates (s/coll-of ::template :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template Graph Creation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; From a single StatementTemplate, return a 1D vector of edge vectors of 
;; form [src dest {:type type-kword}]
(defmethod graph/edges-with-attrs "StatementTemplate"
  [{:keys [id
           verb
           objectActivityType
           contextGroupingActivityType
           contextParentActivityType
           contextOtherActivityType
           contextCategoryActivityType
           attachmentUsageType
           objectStatementRefTemplate
           contextStatementRefTemplate]}]
  (into [] (filter #(some? (second %))
                   (concat
                    (vector (vector id verb {:type :verb}))
                    (vector (vector id objectActivityType
                                    {:type :objectActivityType}))
                    (map #(vector id % {:type :contextGroupingActivityType})
                         contextGroupingActivityType)
                    (map #(vector id % {:type :contextParentActivityType})
                         contextParentActivityType)
                    (map #(vector id % {:type :contextOtherActivityType})
                         contextOtherActivityType)
                    (map #(vector id % {:type :contextCategoryActivityType})
                         contextCategoryActivityType)
                    (map #(vector id % {:type :attachmentUsageType})
                         attachmentUsageType)
                    (map #(vector id % {:type :objectStatementRefTemplate})
                         objectStatementRefTemplate)
                    (map #(vector id % {:type :contextStatementRefTemplate})
                         contextStatementRefTemplate)))))

(def template-ext-keys
  [:verb
   :objectActivityType
   :contextGroupingActivityType
   :contextParentActivityType
   :contextOtherActivityType
   :attachmentUsageType
   :objectStatementRefTemplate
   :contextStatementRefTemplate])

(defn- collect-template
  [acc template]
  (-> template
      (select-keys template-ext-keys)
      vals
      flatten
      (concat acc)))

(defn get-graph-concept-templates
  [profile extra-profiles]
  (let [templates (:templates profile)
        ext-ids   (set (reduce collect-template [] templates))
        concepts  (->> (concat [profile] extra-profiles)
                       (mapcat :concepts)
                       (filter (fn [{id :id}] (contains? ext-ids id))))
        ext-tmps  (->> extra-profiles
                       (mapcat :templates)
                       (filter (fn [{id :id}] (contains? ext-ids id))))]
    {:concepts      concepts
     :templates     templates
     :ext-templates ext-tmps}))

(defn create-graph
  ([profile]
   (let [{:keys [concepts
                 templates]} profile
         tnodes (->> (concat concepts templates)
                     (mapv graph/node-with-attrs))
         tedges (->> templates
                     (mapv graph/edges-with-attrs)
                     graph/collect-edges)]
     (graph/create-graph tnodes tedges)))
  ([profile extra-profiles]
   (let [{:keys [concepts
                 templates
                 ext-templates]} (get-graph-concept-templates
                                  profile
                                  extra-profiles)
         tnodes (->> (concat concepts templates ext-templates)
                     (mapv graph/node-with-attrs))
         tedges (->> templates
                     (mapv graph/edges-with-attrs)
                     graph/collect-edges)]
     (graph/create-graph tnodes tedges))))

(comment
  (create-graph
   {:templates [{:id "https://foo.org/template1"
                 :type "StatementTemplate"
                 :inScheme "https://foo.org/v1"
                 :verb "https://foo.org/verb"
                 :objectActivityType "https://foo.org/activity-type"
                 :attachmentUsageType ["https://foo.org/attachmentUsageType"]
                 :contextStatementRefTemplate ["https://foo.org/template2"]}
                {:id "https://foo.org/template2"
                 :type "StatementTemplate"
                 :inScheme "https://foo.org/v1"
                 :objectStatementRefTemplate ["https://foo.org/template1"]}]}
   [])
  )

(defn get-edges
  "Return a sequence of edge maps, with the following keys:
  - src: source node ID
  - dest: destination node ID
  - src-type: source node 'type' property
  - dest-type: destination node 'type' property
  - src-version: source node 'inScheme' property
  - dest-version: destination node 'inScheme property
  - type: corresponding property in the source node (ie. the Determining
  Property or Statement Ref Template type)"
  [tgraph]
  (map (fn [edge]
         (let [src  (graph/src edge)
               dest (graph/dest edge)]
           {:src          src
            :src-type     (graph/attr tgraph src :type)
            :src-version  (graph/attr tgraph src :inScheme)
            :dest         dest
            :dest-type    (graph/attr tgraph dest :type)
            :dest-version (graph/attr tgraph dest :inScheme)
            :type         (graph/attr tgraph edge :type)}))
       (graph/edges tgraph)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statement Template Graph Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Is the source a Statement Template?
(s/def ::template-src
  (fn template-src? [{:keys [src-type]}]
    (contains? #{"StatementTemplate"} src-type)))

;; Is the destination not nil?
(s/def ::valid-dest
  (fn valid-dest? [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

;; Is the destination a Verb?
(s/def ::verb-dest
  (fn verb-dest? [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

;; Is the destination an Activity Type?
(s/def ::activity-type-dest
  (fn at-dest? [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

;; Is the destination an Attachment Usage Type?
(s/def ::attachment-use-type-dest
  (fn aut-dest? [{:keys [dest-type]}]
    (contains? #{"AttachmentUsageType"} dest-type)))

;; Is the destination another Statement Template?
(s/def ::template-dest
  (fn template-dest? [{:keys [dest-type]}]
    (contains? #{"StatementTemplate"} dest-type)))

;; Are both the source and destination in the same version?
(s/def ::same-version
  (fn same-version? [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Edge validation multimethod

(defmulti valid-edge? :type)

;; verb MUST point to a Verb Concept
(defmethod valid-edge? :verb [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::verb-dest))

;; objectActivityType MUST point to an object ActivityType
(defmethod valid-edge? :objectActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; contextGroupingActivityType MUST point to grouping ActivityTypes
(defmethod valid-edge? :contextGroupingActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; contextParentActivityType MUST point to parent ActivityTypes
(defmethod valid-edge? :contextParentActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; contextOtherActivityType MUST point to other ActivityTypes
(defmethod valid-edge? :contextOtherActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; contextCategoryActivityType MUST point to category ActivityTypes
(defmethod valid-edge? :contextCategoryActivityType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; attachmentUsageType MUST point to AttachmentUsageType Concepts
(defmethod valid-edge? :attachmentUsageType [_]
  (s/and ::template-src
         ::valid-dest
         ::graph/not-self-loop
         ::attachment-use-type-dest))

;; objectStatementRefTemplate MUST point to Statement Templates from this
;; profile version
(defmethod valid-edge? :contextStatementRefTemplate [_]
  (s/and ::template-src
         ::valid-dest
         ::template-dest
         ::same-version))

;; contextStatementRefTemplate MUST point to Statement Templates from this
;; profile version 
(defmethod valid-edge? :objectStatementRefTemplate [_]
  (s/and ::template-src
         ::valid-dest
         ::template-dest
         ::same-version))

;; Validate a single edge
(s/def ::template-edge (s/multi-spec valid-edge? :type))

;; Validate all the edges
(s/def ::template-edges (s/coll-of ::template-edge))

;; Putting it all together
(defn validate-template-edges [tgraph]
  (s/explain-data ::template-edges (get-edges tgraph)))
