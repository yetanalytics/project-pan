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
(s/def ::context
  (s/or :context ::ax/uri
        :context-array (s/and (s/coll-of ::ax/uri :type vector?)
                              (partial some #(= context-url %)))))

;; Overall profile ID MUST NOT be any of the version IDs
(s/def ::id-distinct
  (fn [{:keys [id versions]}]
    (let [version-ids (util/only-ids versions)]
      (nil? (some #(= id %) version-ids)))))

(s/def ::profile
  (s/and
   (s/keys :req-un [::id ::context ::type ::conformsTo ::prefLabel
                    ::definition ::author/author ::versions/versions]
           :opt-un [::see-also
                    ::concept/concepts
                    ::template/templates
                    ::pattern/patterns])))

; (defn validate
;   "Syntax-only validation.
;   Returns true if validation succeeds, and false otherwise."
;   [profile] (s/valid? ::profile profile))

(defn validate
  "Syntax-only validation.
  Returns a spec trace if validation fails, or nil if successful."
  [profile] (s/explain-data ::profile profile))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; In-scheme validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate all the inSchemes
(defn in-schemes-spec
  [versions]
  (let [version-ids (-> versions util/only-ids set)
        in-scheme? (fn [{:keys [inScheme]}]
                     (contains? version-ids inScheme))]
    (s/coll-of in-scheme?)))

;; Validate profile
(defn validate-ids
  [{:keys [id versions] :as profile}]
  (let [concepts (get profile :concepts [])
        templates (get profile :templates [])
        patterns (get profile :patterns [])
        ;; All IDs in one collection
        all-ids (concat [id] (util/only-ids-multiple [versions concepts
                                                      templates patterns]))])
  (s/explain-data ::util/distinct-ids (util/count-ids)))

;; Validate profile
(defn validate-in-schemes
  "Validation of all object inScheme properties, which MUST be one of the
  versioning IRIs given by the ID of some Profile version object.
  Returns an empty sequence if validation is successful, else a sequence of
  spec errors if validation fails."
  [{:keys [versions concepts templates patterns] :as profile}]
  (let [in-schemes? (in-schemes-spec versions)]
    (concat (s/explain-data in-schemes? concepts)
            (s/explain-data in-schemes? templates)
            (s/explain-data in-schemes? patterns))))

(defn validate-all-ids
  [profile]
  (concat (validate-ids profile)
          (validate-in-schemes profile)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IRI validation (via graphs)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn validate-graphs
  ;; Version for one collection (concept graphs only)
  ([create-graph-fn explain-graph-fn coll]
   (let [id-counts (-> coll util/only-ids util/count-ids)
         id-errors (s/explain-data ::util/distinct-ids id-counts)]
     (if (empty? id-errors)
       (-> coll create-graph-fn explain-graph-fn)
       id-errors)))
  ;; Version for two collections (template and pattern graphs)
  ([create-graph-fn explain-graph-fn coll1 coll2]
   (let [id-counts (-> [coll1 coll2] util/only-ids-multiple util/count-ids)
         id-errors (s/explain-data ::util/distinct-ids id-counts)]
     (if (empty? id-errors)
       (explain-graph-fn (create-graph-fn coll1 coll2))
       id-errors))))

;; Validate profile
(defn validate-iris
  "Validate all profile IRIs by creating a graph data structure out of the
  Profile. Returns an empty sequence if validation is successful, else a 
  sequence of spec errors if validation fails."
  [{:keys [concepts templates patterns]}]
  (let [concepts (if-not (some? concepts) [] concepts)
        templates (if-not (some? templates) [] templates)
        patterns (if-not (some? patterns) [] patterns)]
    (concat (validate-graphs concept/create-concept-graph concept/explain-concept-graph concepts)
            (validate-graphs template/create-template-graph template/explain-template-graph concepts templates)
            (validate-graphs pattern/create-pattern-graph pattern/explain-pattern-graph templates patterns))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; @context validation 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate profile


(defn validate-context
  [profile]
  (if (context/validate-all-contexts profile)
    '()
    '("@context error")))
