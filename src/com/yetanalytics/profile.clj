(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [com.yetanalytics.profiles.versions :as versions]
            [com.yetanalytics.profiles.author :as author]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]))

(s/def ::id ::ax/iri)
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))
(s/def ::type #{"Profile"})
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)

;; prefLabel + definition
(s/def ::see-also ::ax/url)

(s/def ::profile
  (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                   ::definition ::author/author ::versions/versions]
          :opt-un [::see-also
                   ::concept/concepts
                   ::template/templates
                   ::pattern/patterns]))

(defn validate
  "Syntax-only validation.
  Returns true if validation succeeds, and false otherwise."
  [profile] (s/valid? ::profile profile))

(defn validate-explain
  "Syntax-only validation.
  Returns a spec trace if validation fails, or nil if successful."
  [profile] (s/explain-data ::profile profile))

;; TODO: stricter validation levels

;; In-scheme validation

(defn zip-vids [object-coll vid-set]
  (mapv #({:object % :vid-set vid-set}) object-coll))

(s/def ::valid-in-scheme
  (fn [{:keys object vid-set}]
    (vid-set (:in-scheme object))))

(s/def ::valid-in-schemes (s/coll-of ::valid-in-scheme))

;; IRI validation (via graphs)

(defn create-concept-graph [{:keys concepts}]
  (let [cgraph (uber/digraph)
        cnodes (mapv (partial util/node-with-attrs) concepts)
        cedges (reduce concat (mapv (partial util/edges-with-attrs) concepts))]
    (-> cgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-directed-edges* cedges))))

(defn create-template-graph [{:keys concepts templates}]
  (let [tgraph (uber/digraph)
        ;; Nodes
        cnodes (mapv (partial util/node-with-attrs) concepts)
        tnodes (mapv (partial util/node-with-attrs) templates)
        ;; Edges
        cedges (reduce concat (mapv (partial util/edges-with-attrs) concepts))
        tedges (reduce concat (mapv (partial util/edges-with-attrs) templates))]
    (-> tgraph
        (uber/add-nodes-with-attrs* cnodes)
        (uber/add-nodes-with-attrs* tnodes)
        (uber/add-directed-edges* cedges)
        (uber/add-directed-edges* tedges))))

(defn create-pattern-graph [{:keys templates patterns}]
  (let [pgraph (uber/digraph)
        ;; Nodes
        tnodes (mapv (partial util/node-with-attrs) templates)
        pnodes (mapv (partial util/node-with-attrs) patterns)
        ;; Edges
        tedges (reduce concat (mapv (partial util/edges-with-attrs) templates))
        pedges (reduce concat (mapv (partial util/edges-with-attrs) patterns))]
    (-> tgraph
        (uber/add-nodes-with-attrs* tnodes)
        (uber/add-nodes-with-attrs* pnodes)
        (uber/add-directed-edges* tedges)
        (uber/add-directed-edges* pedges))))

(defn validate+
  "Validation of in-profile identifiers and locators.
  Returns true if valid, false otherwise."
  [profile]
  (let [concepts (:concepts profile)
        templates (:templates profile)
        patterns (:patterns profile)
        vid-set (-> profile :versions util/only-ids)
        cgraph (create-concept-graph profile)
        tgraph (create-template-graph profile)
        pgraph (create-pattern-graph profile)]
    (and (s/valid? ::profile profile)
         (s/valid? ::valid-in-schemes (zip-vids concepts vid-set))
         (s/valid? ::valid-in-schemes (zip-vids templates vid-set))
         (s/valid? ::valid-in-schemes (zip-vids patterns vid-set))
         (s/valid? ::concept/concept-graph cgraph)
         (s/valid? ::template/template-graph tgraph)
         (s/valid? ::pattern/pattern-graph pgraph))))

(defn validate-explain+
  "Validation of in-profile identifiers and locators.
  Returns a spec trace if validation fails, or nil if successful."
  [profile]
  (let [concepts (:concepts profile)
        templates (:templates profile)
        patterns (:patterns profile)
        vid-set (-> profile :versions util/only-ids)
        cgraph (create-concept-graph profile)
        tgraph (create-template-graph profile)
        pgraph (create-pattern-graph profile)]
    (concat (s/explain-data ::profile profile)
            (s/explain-data ::valid-in-schemes (zip-vids concepts vid-set))
            (s/explain-data ::valid-in-schemes (zip-vids templates vid-set))
            (s/explain-data ::valid-in-schemes (zip-vids patterns vid-set))
            (concept/explain-concept-graph cgraph)
            (template/explain-template-graph tgraph)
            (pattern/explain-pattern-graph pgraph))))
