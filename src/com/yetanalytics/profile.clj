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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Profile"})
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::see-also ::ax/url)

;; @context SHOULD be the following URI and MUST contain it if URI valued
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))

;; Overall profile ID MUST NOT be any of the version IDs
(s/def ::id-distinct
  (fn [{:keys [id versions]}]
    (let [version-ids (util/only-ids versions)]
      (nil? (some #(= id %) version-ids)))))

(s/def ::versions (s/and ::versions/versions ::id-distinct))

(s/def ::profile
  (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                   ::definition ::author/author ::versions]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Strict validation 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; In-scheme validation

;; Validate an individual in-scheme
(s/def ::valid-in-scheme
  (fn [{:keys [object vid-set]}] (contains? vid-set (:in-scheme object))))

(s/def ::valid-in-schemes (s/coll-of ::valid-in-scheme))

;; IRI validation (via graphs)

(defn validate+
  "Validation of in-profile identifiers and locators.
  Returns true if valid, false otherwise."
  [profile]
  (let [concepts (get profile :concepts [])
        templates (get profile :templates [])
        patterns (get profile :patterns [])
        ;; in-schemes
        vid-set (versions/version-set (:versions profile))
        c-vids (util/combine-args concepts {:vid-set vid-set})
        t-vids (util/combine-args templates {:vid-set vid-set})
        p-vids (util/combine-args patterns {:vid-set vid-set})
        ;; graphs 
        cgraph (concept/create-concept-graph concepts)
        tgraph (template/create-template-graph concepts templates)
        pgraph (pattern/create-pattern-graph templates patterns)]
    (and (s/valid? ::profile profile)
         (s/valid? ::valid-in-schemes c-vids)
         (s/valid? ::valid-in-schemes t-vids)
         (s/valid? ::valid-in-schemes p-vids)
         (s/valid? ::concept/concept-graph cgraph)
         (s/valid? ::template/template-graph tgraph)
         (s/valid? ::pattern/pattern-graph pgraph))))

(defn validate-explain+
  "Validation of in-profile identifiers and locators.
  Returns a spec trace if validation fails, or nil if successful."
  [profile]
  (let [concepts (get profile :concepts [])
        templates (get profile :templates [])
        patterns (get profile :patterns [])
        ;; in-schemes
        vid-set (versions/version-set (:versions profile))
        c-vids (util/combine-args concepts {:vid-set vid-set})
        t-vids (util/combine-args templates {:vid-set vid-set})
        p-vids (util/combine-args patterns {:vid-set vid-set})
        ;; graphs 
        cgraph (concept/create-concept-graph concepts)
        tgraph (template/create-template-graph concepts templates)
        pgraph (pattern/create-pattern-graph templates patterns)]
    (concat (s/explain-data ::profile profile)
            (s/explain-data ::valid-in-schemes c-vids)
            (s/explain-data ::valid-in-schemes t-vids)
            (s/explain-data ::valid-in-schemes p-vids)
            (concept/explain-concept-graph cgraph)
            (template/explain-template-graph tgraph)
            (pattern/explain-pattern-graph pgraph))))
