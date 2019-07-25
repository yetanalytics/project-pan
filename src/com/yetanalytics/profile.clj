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
                    ::pattern/patterns])
   ::id-distinct))

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

;; Validate an individual inScheme
(s/def ::valid-in-scheme
  (fn [{:keys [object vid-set]}] (contains? vid-set (:inScheme object))))

;; Validate all the inSchemes
(s/def ::valid-in-schemes (s/coll-of ::valid-in-scheme))

;; Validate profile
(defn validate-in-schemes
  "Validation of all object inScheme properties, which MUST be one of the
  versioning IRIs given by the ID of some Profile version object.
  Returns an empty sequence if validation is successful, else a sequence of
  spec errors if validation fails."
  [{:keys [versions concepts templates patterns] :as profile}]
  (let [vid-set (versions/version-set versions)
        c-vids (util/combine-args concepts {:vid-set vid-set})
        t-vids (util/combine-args templates {:vid-set vid-set})
        p-vids (util/combine-args patterns {:vid-set vid-set})]
    (concat (s/explain-data ::valid-in-schemes c-vids)
            (s/explain-data ::valid-in-schemes t-vids)
            (s/explain-data ::valid-in-schemes p-vids))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IRI validation (via graphs)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate profile
(defn validate-iris
  "Validate all profile IRIs by creating a graph data structure out of the
  Profile. Returns an empty sequence if validation is successful, else a 
  sequence of spec errors if validation fails."
  [{:keys [concepts templates patterns]}]
  (let [concepts (if-not (some? concepts) [] concepts)
        templates (if-not (some? templates) [] templates)
        patterns (if-not (some? patterns) [] patterns)
        ;; graphs 
        cgraph (concept/create-concept-graph concepts)
        tgraph (template/create-template-graph concepts templates)
        pgraph (pattern/create-pattern-graph templates patterns)]
    (concat (concept/explain-concept-graph cgraph)
            (template/explain-template-graph tgraph)
            (pattern/explain-pattern-graph pgraph))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; @context validation 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate profile
(defn validate-context
  [profile]
  (if (context/validate-all-contexts profile)
    '()
    '("@context error")))
