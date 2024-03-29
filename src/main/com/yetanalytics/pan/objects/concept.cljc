(ns com.yetanalytics.pan.objects.concept
  (:require
   [clojure.spec.alpha               :as s]
   [com.yetanalytics.pan.graph       :as graph]
   [com.yetanalytics.pan.identifiers :as ids]
   [com.yetanalytics.pan.objects.concepts.verb                       :as v]
   [com.yetanalytics.pan.objects.concepts.activity                   :as a]
   [com.yetanalytics.pan.objects.concepts.activity-type              :as at]
   [com.yetanalytics.pan.objects.concepts.attachment-usage-type      :as aut]
   [com.yetanalytics.pan.objects.concepts.extensions.result          :as re]
   [com.yetanalytics.pan.objects.concepts.extensions.context         :as ce]
   [com.yetanalytics.pan.objects.concepts.extensions.activity        :as ae]
   [com.yetanalytics.pan.objects.concepts.resources.state            :as sr]
   [com.yetanalytics.pan.objects.concepts.resources.agent-profile    :as agr]
   [com.yetanalytics.pan.objects.concepts.resources.activity-profile :as acr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concept Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn valid-concept-type?
  [{:keys [type]}]
  (#{"Verb"
     "Activity"
     "ActivityType"
     "AttachmentUsageType"
     "ActivityExtension"
     "ContextExtension"
     "ResultExtension"
     "ActivityProfileResource"
     "AgentProfileResource"
     "StateResource"}
   type))

(defmulti concept? :type)

(defmethod concept? "Verb" [_] ::v/verb)
(defmethod concept? "Activity" [_] ::a/activity)
(defmethod concept? "ActivityType" [_] ::at/activity-type)
(defmethod concept? "AttachmentUsageType" [_] ::aut/attachment-usage-type)
(defmethod concept? "ActivityExtension" [_] ::ae/extension)
(defmethod concept? "ContextExtension" [_] ::ce/extension)
(defmethod concept? "ResultExtension" [_] ::re/extension)
(defmethod concept? "ActivityProfileResource" [_] ::acr/document-resource)
(defmethod concept? "AgentProfileResource" [_] ::agr/document-resource)
(defmethod concept? "StateResource" [_] ::sr/document-resource)

;; This weird arrangement is so that spec gen can be easily
;; performed while preserving the correct error message
(s/def ::concept
  (s/with-gen (s/and valid-concept-type?
                     (s/multi-spec concept? :type))
    #(s/gen (s/and (s/multi-spec concept? :type)
                   valid-concept-type?))))

(s/def ::concepts (s/coll-of ::concept :type vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concept Graph Creation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def concept-iri-keys
  [:broader :broadMatch
   :narrower :narrowMatch
   :related :relatedMatch
   :exactMatch
   :recommendedActivityTypes
   :recommendedVerbs])

(def external-iri-keys
  [:broadMatch :narrowMatch :relatedMatch :exactMatch
   :recommendedActivityTypes :recommendedVerbs
   :context :schema])

(defn get-external-iris
  "Return the external IRIs from the Concepts of `profile`."
  [profile]
  (let [{:keys [concepts]} profile
        id-filter-set      (set (ids/objs->ids concepts))]
    (ids/objs->out-ids-map concepts external-iri-keys id-filter-set)))

(defn- get-graph-concepts
  [profile extra-profiles]
  (let [concepts (:concepts profile)
        out-ids  (ids/objs->out-ids concepts concept-iri-keys)
        ext-cons (->> (mapcat :concepts extra-profiles)
                      (ids/filter-by-ids out-ids))]
    {:concepts     concepts
     :ext-concepts ext-cons}))

(defn create-graph
  "Create a graph of Concept relations from `profile` and possibly
   `extra-profiles` that can then be used in validation."
  ([profile]
   (let [{:keys [concepts]} profile]
     (graph/create-graph concepts concepts)))
  ([profile extra-profiles]
   (let [{:keys [concepts ext-concepts]} (get-graph-concepts profile
                                                             extra-profiles)]
     (graph/create-graph (concat concepts ext-concepts)
                         concepts))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concept Graph Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-edges
  "Returns a sequence of edge maps, with the following keys:
  - src: source node ID
  - dest: destination node ID
  - src-type: source node 'type' property
  - dest-type: destination node 'type' property
  - src-version: source node 'inScheme' property
  - dest-version: destination node 'inScheme property
  - type: the property corresponding to this edge (eg. broader, narrower,
  related, etc.)"
  [cgraph]
  (map (fn [edge]
         (let [src  (graph/src edge)
               dest (graph/dest edge)]
           {:src          src
            :src-type     (graph/attr cgraph src :type)
            :src-version  (graph/attr cgraph src :inScheme)
            :dest         dest
            :dest-type    (graph/attr cgraph dest :type)
            :dest-version (graph/attr cgraph dest :inScheme)
            :type         (graph/attr cgraph edge :type)}))
       (graph/edges cgraph)))

;; Is the destination not nil?
(s/def ::valid-dest
  (fn valid-dest? [{:keys [dest-type dest-version]}]
    (and (some? dest-type) (some? dest-version))))

;; Is the source an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-src
  (fn relatable-src? [{:keys [src-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} src-type)))

;; Is the destination an Activity Type, Attachment Usage Type, or a Verb?
(s/def ::relatable-dest
  (fn relatable-dest? [{:keys [dest-type]}]
    (contains? #{"ActivityType" "AttachmentUsageType" "Verb"} dest-type)))

;; Is the source an Activity Extension?
(s/def ::activity-ext-src
  (fn aext-src? [{:keys [src-type]}]
    (contains? #{"ActivityExtension"} src-type)))

;; Is the source a Context or Result Extension?
(s/def ::ctxt-result-ext-src
  (fn crext-src? [{:keys [src-type]}]
    (contains? #{"ContextExtension" "ResultExtension"} src-type)))

;; Is the destination an Activity Type?
(s/def ::activity-type-dest
  (fn at-dest? [{:keys [dest-type]}]
    (contains? #{"ActivityType"} dest-type)))

;; Is the destination a Verb?
(s/def ::verb-dest
  (fn verb-dest? [{:keys [dest-type]}]
    (contains? #{"Verb"} dest-type)))

;; Are both the source and destination the same type of Concept?
(s/def ::same-concept
  (fn same-concept? [{:keys [src-type dest-type]}]
    (= src-type dest-type)))

;; Are both the source and destination of the same version?
(s/def ::same-version
  (fn same-version? [{:keys [src-version dest-version]}]
    (= src-version dest-version)))

;; Are both the source and destination from different version/Profile?
(s/def ::diff-version
  (fn diff-version? [{:keys [src-version dest-version]}]
    (not= src-version dest-version)))

;; Edge validation multimethod 

(defmulti valid-edge? :type)

;; broader, narrower, and related  MUST point to same-type Concepts from the
;; same Profile version

(defmethod valid-edge? :broader [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

(defmethod valid-edge? :narrower [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

(defmethod valid-edge? :related [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::same-version))

;; broadMatch, narrowMatch, relatedMatch, and exactMatch MUST point to same-type
;; Concepts from a different Profile

(defmethod valid-edge? :broadMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :narrowMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :relatedMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

(defmethod valid-edge? :exactMatch [_]
  (s/and ::relatable-src
         ::valid-dest
         ::graph/not-self-loop
         ::relatable-dest
         ::same-concept
         ::diff-version))

;; recommendedActivityTypes MUST point to ActivityType Concepts
(defmethod valid-edge? :recommendedActivityTypes [_]
  (s/and ::activity-ext-src
         ::valid-dest
         ::graph/not-self-loop
         ::activity-type-dest))

;; recommendedVerbs MUST point to Verb Concepts
(defmethod valid-edge? :recommendedVerbs [_]
  (s/and ::ctxt-result-ext-src
         ::valid-dest
         ::graph/not-self-loop
         ::verb-dest))

;; Is one edge valid?
(s/def ::concept-edge (s/multi-spec valid-edge? :type))

;; Are all edges valid?
(s/def ::concept-edges (s/coll-of ::concept-edge))

(defn validate-concept-edges
  "Given the Concept graph `cgraph`, return spec error data if the
   graph edges are invalid according to the xAPI Profile spec, or
   `nil` otherwise."
  [cgraph]
  (s/explain-data ::concept-edges (get-edges cgraph)))
