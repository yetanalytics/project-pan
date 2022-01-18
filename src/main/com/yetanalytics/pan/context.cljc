(ns com.yetanalytics.pan.context
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [com.yetanalytics.pan.axioms :as ax])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-json-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-json-resource]])))

;; This library takes a Profile (which is simply a JSON-LD file) and does a
;; recursive depth-first search in order to ensure that all properties can be
;; expanded to IRIs using contexts. 
;;
;; JSON-LD @context validation is based off of the JSON-LD 1.1 specification
;; found at https://www.w3.org/2018/jsonld-cg-reports/json-ld/
;; This is not a comprehensive JSON-LD validator suite; more comprehensive
;; validation may be added in a later iteration.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Inits 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def profile-context
  (:_context (read-json-resource "context/profile-context.json" "_")))

(def activity-context
  (:_context (read-json-resource "context/activity-context.json" "_")))

(def default-context-map
  {"https://w3id.org/xapi/profiles/context"          profile-context
   "https://w3id.org/xapi/profiles/activity-context" activity-context})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context Spec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def :context.term-def/_id (s/and string? (partial re-matches #".*:.*")))
(s/def :context.term-def/_container #{"@list" "@set" "@language"})

;; Unimplemented context keyword specs, but kept here for reference
(s/def ::_base ::ax/iri)
(s/def ::_import ::ax/iri)
(s/def ::_language ::ax/language-tag)
(s/def ::_propagate ::ax/boolean)
(s/def ::_protected ::ax/boolean)
(s/def ::_container #{"@set"})
(s/def ::_type (s/keys :req-un [::_container]))
(s/def ::_version #{1.1})
(s/def ::_vocab ::ax/iri)

(def context-spec
  (s/map-of
   keyword?
   (s/or
    :keyword #{"@id" "@type"}
    :iri ::ax/iri
    :simple-term-def :context.term-def/_id
    :expanded-term-def (s/keys :req-un [:context.term-def/_id]
                               :opt-un [:context.term-def/_container]))))

(s/def ::_context
  (s/or :iri ::ax/iri
        :inline context-spec
        :array (s/coll-of (s/or :iri ::ax/iri
                                :inline context-spec)
                          :min-count 1)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context Expansion and Validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- expand-compact-iri
  [context compact-iri]
  (when-some [[_ pre post] (re-matches #"(.*):(.*)" compact-iri)]
    (when-some [exp-pre (get context (keyword pre))]
      (str exp-pre post))))

(defn- expand-key
  [context k]
  (cond
      ;; compact or expanded IRI
    (or (s/valid? ::ax/iri k)
        (and (string? k) (string/includes? k ":")))
    (or (expand-compact-iri context k)
        k)
      ;; simple or expanded term definition
    (contains? context k)
    (let [term-def (get context k)]
      (or (and (string? term-def)
               (first (re-matches #"(@.*)" term-def)))
          (and (string? term-def)
               (expand-compact-iri context term-def))
          (and (map? term-def)
               (expand-compact-iri context (:_id term-def)))
          ;; fail
          k))
      ;; fail
    :else
    k))

(defn- lang-map-key?
  [context k]
  (let [term-def (get context k)]
    (boolean (and (map? term-def)
                  (#{"@language"} (:_container term-def))))))

(defn- new-context
  [contexts-map context]
  (cond
    (string? context)
    (get contexts-map context)
    (map? context)
    context
    (seq? context)
    (->> context
         (map (partial new-context contexts-map))
         (apply merge))
    :else
    (throw (ex-info "Invalid @context value!"
                    {:type    ::invalid-context
                     :context context}))))

(defn expand-profile-keys*
  [contexts-map context profile]
  (let [context* (if-some [ctx (some->> profile
                                        :_context
                                        (new-context contexts-map))]
                   (merge context ctx)
                   context)]
    (reduce-kv (fn [m k v]
                 (let [k* (expand-key context* k)
                       v* (cond
                            ;; Need to treat language maps specially
                            ;; Otherwise they'd be treated as node objects
                            (lang-map-key? context* k)
                            (into {}
                                  (map (fn [[ltag lval]]
                                         [(keyword (str "_LANGTAG_" (name ltag)))
                                          lval])
                                       v))
                            (map? v)
                            (expand-profile-keys* contexts-map
                                                  context*
                                                  v)
                            (coll? v)
                            (mapv (fn [x]
                                    (if (map? x)
                                      (expand-profile-keys*
                                       contexts-map
                                       context*
                                       x)
                                      x))
                                  v)
                            :else
                            v)]
                   (assoc m k* v*)))
               {}
               (dissoc profile :_context))))

(defn expand-profile-keys
  ([profile]
   (expand-profile-keys* default-context-map {} profile))
  ([profile contexts-map]
   (expand-profile-keys* (merge default-context-map contexts-map) {} profile)))

;; All JSON-LD keywords except those exclusive to context maps
;; https://www.w3.org/TR/json-ld11/#keywords
(def jsonld-keywords
  #{:_context
    :_direction
    :_graph
    :_id
    :_included
    :_index
    :_json
    :_language
    :_list
    :_nest
    :_none
    :_reverse
    :_set
    :_type
    :_value})

(s/def ::expanded-key
  (s/or :iri ::ax/iri
        :keyword #{"@id" "@type"}
        :lang-tag (s/and keyword?
                         #(->> % name (re-matches #"_LANGTAG_.*")))))

(s/def ::expanded-key-profile
  (s/map-of ::expanded-key
            (s/or :object ::expanded-key-profile
                  :array (s/coll-of ::expanded-key-profile)
                  :scalar any?)))

(defn validate-contexts
  "Validate that all keys in `profile` are able to be expanded into IRIs
   using \"@context\" maps. NOTE: Does not attempt to validate the values."
  ([profile]
   (->> (expand-profile-keys profile)
        (s/explain-data ::expanded-key-profile)))
  ([profile contexts-map]
   (->> (expand-profile-keys profile contexts-map)
        (s/explain-data ::expanded-key-profile))))
