(ns com.yetanalytics.context
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as w]
            [clojure.zip :as zip]
            [cheshire.core :as cheshire]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Parse context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-raw-context
  "If a @context is one of two contexts given by the profile spec, call them
  from local resources."
  [context]
  (case context
    "https://w3id.org/xapi/profiles/context"
    (slurp "resources/context/profile-context.json")
    "https://w3id.org/xapi/profiles/activity-context"
    (slurp "resources/context/activity-context.json")
    :else nil ;; TODO do something
))

(defn get-context
  "Get a raw context, then parse it from JSON to EDN.
  Return the JSON object given by the @context key"
  [context-uri]
  (-> context-uri get-raw-context util/convert-json :context))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Regular expressions
(def gen-delims-regex #".*(?:\:|\/|\?|\#|\[|\]|\@)$")
(def prefix-regex #"(.*\:)")

(s/def ::prefix
  (s/or :simple-term-def
        (s/and ::ax/iri
               #(->> % (re-matches gen-delims-regex) some?))
        :expanded-term-def
        (s/and map?
               #(-> % :prefix true?))))

(defn collect-prefixes
  "Returns all the prefixes in a context."
  [context]
  (reduce-kv (fn [accum k v]
               (if (is-prefix? v) (assoc accum k v) accum)) {} context))

(defn compact-iri?
  "Validates whether this is a compact iri that has a valid prefix."
  [prefixes value]
  (and (string? value)
       (contains? prefixes (-> value (string/split #"\:") first keyword))))

(defn context-map?
  "Validates whether this is a JSON object with a @id that has a valid prefix."
  [prefixes value]
  (and (map? value)
       (compact-iri? prefixes (:id value))))

(defn value-spec
  [prefixes]
  (s/or :prefix ::prefix
        :compact-iri (partial compact-iri? prefixes)
        :object (partial context-map? prefixes)))

(defn context-spec
  [context]
  (let [v-spec (-> context collect-prefixes value-spec)]
    (s/map-of keyword? v-spec)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate profile against context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn profile-to-zipper
  [profile]
  (letfn [(children [node]
            (reduce-kv [accum k child]
                       (cond
                         (map? child)
                         (conj accum child)
                         ((s/coll-of map? :type vector?) child)
                         (concat accum child)
                         :else accum))
            (list) node)]
    (zip/zipper map? children nil profile)))

(defn create-context
  [location]
  (let [node (zip/node location)
        new-context (-> curr-node :context get-context)
        error-msg (s/explain-data ::context new-context)]
    (if (nil? error-msg)
      {:path (zip/path location)
       :context new-context}
      (throw (ex-info "Failure to validate @context" error-msg)))))

(defn subvec?
  "True if v1 is a subvector of v2, false otherwise."
  [v1 v2]
  (and (<= (count v1) (count v2))
       (= v1 (subvec v2 0 (count v1)))))

(defn update-contexts
  [context-stack location]
  (let [stack' (if-not (subvec? (zip/path location)
                                (-> context-stack peek :path))
                 (pop context-stack)
                 context-stack)
        stack'' (if (contains? curr-node :context)
                  (conj stack' (create-context (:context curr-node)))
                  stack')]
    stack''))

(defn search-contexts
  [contexts k]
  (not (every? false? (map #(-> % :context (contains? k)) contexts))))

(defn foo-bar
  [profile]
  (loop [profile-loc (profile-to-zipper profile)
         context-stack []]
    (if (zip/end? profile-loc)
      true
      (let [curr-node (zip/node profile-loc)
            new-stack (update-contexts context-stack profile-loc)
            error-seq (map (partial search-contexts new-stack) (keys curr-node))]
        (if (every? true? error-seq)
          (recur (zip/next profile-loc) new-stack))))))
