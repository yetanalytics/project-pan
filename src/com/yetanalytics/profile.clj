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
(s/def ::has-context-url (partial some #(= context-url %)))
(s/def ::context
  (s/or :context-iri ::ax/uri
        :context-array (s/and ::ax/array-of-uri
                              ::has-context-url)))

(s/def ::profile
  (s/keys :req-un [::id ::context ::type ::conformsTo ::prefLabel
                   ::definition ::author/author ::versions/versions]
          :opt-un [::seeAlso
                   ::concept/concepts
                   ::template/templates
                   ::pattern/patterns]))

(defn validate
  "Syntax-only validation.
  Returns a spec trace if validation fails, or nil if successful."
  [profile] (s/explain-data ::profile profile))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; In-scheme validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (defn normalize-nil [value]
;   (if (nil? value) [] value))

; (defn normalize-profile
;   [profile]
;   (-> profile
;       (update :versions normalize-nil)
;       (update :concepts normalize-nil)
;       (update :templates normalize-nil)
;       (update :patterns normalize-nil)))

; ;; Validate all the inSchemes
; (defn in-schemes-spec
;   [versions]
;   (let [version-ids (-> versions util/only-ids set)
;         in-scheme? (fn [{:keys [inScheme]}]
;                      (contains? version-ids inScheme))]
;     (s/coll-of in-scheme?)))

; ;; Validate profile
; (defn validate-ids
;   [{:keys [id versions concepts templates patterns] :as profile}]
;   (let [ids (concat [id] (util/only-ids-multiple [versions concepts
;                                                   templates patterns]))]
;     (s/explain-data ::util/distinct-ids (util/count-ids ids))))

; ;; Validate profile
; (defn validate-in-schemes
;   "Validation of all object inScheme properties, which MUST be one of the
;   versioning IRIs given by the ID of some Profile version object.
;   Returns an empty sequence if validation is successful, else a sequence of
;   spec errors if validation fails."
;   [{:keys [versions concepts templates patterns] :as profile}]
;   (let [in-schemes? (in-schemes-spec versions)]
;     (concat (s/explain-data in-schemes? concepts)
;             (s/explain-data in-schemes? templates)
;             (s/explain-data in-schemes? patterns))))

; (defn validate-all-ids
;   [profile]
;   (let [profile (normalize-profile profile)]
;     (concat (validate-ids profile)
;             (validate-in-schemes profile))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IRI validation (via graphs)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (defn validate-graphs
;   [create-graph-fn explain-graph-fn coll1 & [coll2]]
;   (let [id-errors (->> (concat coll1 coll2) util/only-ids util/count-ids
;                        (s/explain-data ::util/distinct-ids))]
;     (if (empty? id-errors)
;       (if (some? coll2)
;         (explain-graph-fn (create-graph-fn coll1 coll2))
;         (explain-graph-fn (create-graph-fn coll1)))
;       (throw (ex-info "Cannot create graph due to duplicate IDs!"
;                       id-errors)))))

; ;; Validate profile
; ;; TODO Add try-catch blocks to each validate-graphs line
; (defn validate-iris
;   "Validate all profile IRIs by creating a graph data structure out of the
;   Profile. Returns an empty sequence if validation is successful, else a 
;   sequence of spec errors if validation fails."
;   [profile]
;   (let [{:keys [concepts templates patterns] :as profile}
;         (normalize-profile profile)]
;     (concat (validate-graphs concept/create-graph concept/explain-graph
;                              concepts)
;             (validate-graphs template/create-graph template/explain-graph
;                              concepts templates)
;             (validate-graphs pattern/create-graph pattern/explain-graph
;                              templates patterns))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; @context validation 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Validate profile


; (defn validate-context
;   [profile]
;   (if (context/validate-all-contexts profile)
;     '()
;     '("@context error")))
