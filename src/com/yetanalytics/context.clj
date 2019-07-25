(ns com.yetanalytics.context
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as w]
            [clojure.zip :as zip]
            [cheshire.core :as cheshire]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]))

;; This library takes a Profile (which is simply a JSON-LD file) and does a
;; recursive depth-first search in order to ensure that all properties can be
;; expanded to IRIs using contexts. 
;;
;; JSON-LD @context validation is based off of the JSON-LD 1.1 specification
;; found at https://www.w3.org/2018/jsonld-cg-reports/json-ld/
;; This is not a comprehensive JSON-LD validator suite; more comprehensive
;; validation may be added in a later iteration.

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
    :else nil ;; TODO get other contexts from the Internet 
))

(defn get-context
  "Get a raw context, then parse it from JSON to EDN.
  Return the JSON object given by the @context key"
  [context-uri]
  (-> context-uri get-raw-context (util/convert-json "at/") :at/context))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; JSON-LD node objects may be aliased to the following keywords.
;; This spec checks if a @context key is an alias to a keyword value.
;; See section 6.2: Node Objects in the JSON-LD grammar.
(s/def ::keyword
  (fn [k]
    (contains? #{"@context" "@id" "@graph" "@nest" "@type" "@reverse" "@index"}
               k)))

;; Regular expressions
(def gen-delims-regex #".*(?:\:|\/|\?|\#|\[|\]|\@)$")
(def prefix-regex #"(.*\:)")

;; JSON-LD 1.1 prefixes may be a simple term definition that ends in a URI
;; general delimiter char or an expanded term definition with @prefix set to
;; true. See section 4.4: Compact IRIs in the JSON-LD grammar.
(s/def ::prefix
  (s/or :simple-term-def
        (s/and ::ax/iri #(->> % (re-matches gen-delims-regex) some?))
        :expanded-term-def
        (s/and map?  #(-> % :prefix true?))))

(defn collect-prefixes
  "Returns all the prefixes in a context."
  [context]
  (reduce-kv (fn [accum k v]
               (if (s/valid? ::prefix v)
                 (assoc accum k v)
                 accum))
             {} context))

(defn compact-iri?
  "Validates whether this is a compact iri that has a valid prefix."
  [prefixes value]
  (and (string? value)
       (contains? prefixes (-> value (string/split #"\:") first keyword))))

(defn context-map?
  "Validates whether this is a JSON object with a @id that has a valid prefix."
  [prefixes value]
  (and (map? value)
       (compact-iri? prefixes (:at/id value))))

;; A term definition may either have a string as a value (ie. a simple term
;; definition) or a map (ie. an expanded term definition).
(defn value-spec
  "Create a spec that validates a single JSON value in the context."
  [prefixes]
  (s/or :keyword ::keyword
        :prefix ::prefix
        :simple-term-def (partial compact-iri? prefixes)
        :expanded-term-def (partial context-map? prefixes)))

(defn context-spec
  "Creates a spec that validates an entire context."
  [context]
  (let [v-spec (-> context collect-prefixes value-spec)]
    (s/map-of keyword? v-spec)))

;; TODO Handle inline @context values?
(defn create-context
  "Create a valid context from a @context IRI.
  Throws an exception if the context is invalid."
  [context-iri]
  (let [context (get-context context-iri)
        errors (s/explain-data (context-spec context) context)]
    (if (every? nil? errors)
      context
      (throw (ex-info (str "Failure to validate @context " context-iri)
                      errors)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate profile against context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn profile-to-zipper
  "Create a zipper from a profile structure."
  [profile]
  (letfn [(children [node]
            (reduce-kv
             (fn [accum k v]
               (cond ;; TODO Get a better solution to the lang map issue?
                 (and (map? v)
                      (not (#{:prefLabel :definition :scopeNote
                              :name :description} k)))
                 (conj accum v)
                 (s/valid? (s/coll-of map? :type vector?) v)
                 (concat accum v)
                 :else accum))
             (list) node))]
    (zip/zipper map? children nil profile)))

(defn subvec?
  "True if v1 is a subvector of v2, false otherwise."
  [v1 v2]
  (and (<= (count v1) (count v2))
       (= v1 (subvec v2 0 (count v1)))))

(defn pop-context
  "Pop the last-added context if the path is not located at the current
  location nor a parent of the current location."
  [context-stack location]
  (if-not (subvec? (-> context-stack peek :path)
                   (vec (zip/path location)))
    (pop context-stack)
    context-stack))

(defn pop-contexts
  "Repeatedly pop the latest-added contexts until the context is located at
  the current location or a parent thereof."
  [context-stack location]
  (loop [old-stack context-stack]
    (if (empty? old-stack)
      old-stack
      (let [new-stack (pop-context old-stack location)]
        (if (= new-stack old-stack)
          new-stack
          (recur new-stack))))))

(defn push-context
  "If there exists a @context key at the current node, add it to the stack.
  Recall that @context may be either a URI string or an array of URIs."
  [context-stack location]
  (let [curr-path (-> location zip/path vec)
        context-iris (-> location zip/node :context)]
    (cond
      (s/valid? ::ax/iri context-iris)
      (conj context-stack {:context (create-context context-iris)
                           :path curr-path})
      (s/valid? (s/coll-of ::ax/iri :kind vector?) context-iris)
      (concat context-stack (mapv (fn [iri]
                                    {:context (create-context iri)
                                     :path curr-path})
                                  context-iris))
      (nil? context-iris) context-stack)))

(defn update-contexts
  "Update the contexts stack on every node (ie. pop if we are no longer a 
  child of the latest @contexts, push if we see a new @context)."
  [context-stack location]
  (-> context-stack (pop-contexts location) (push-context location)))

(defn search-contexts
  "Given a key, search in all the contexts in the stack for it. Return true
  if the key is found, false otherwise (as that indicates that the key cannot
  be expanded to an IRI)."
  [contexts k]
  (loop [context-stack contexts]
    (if (empty? context-stack)
      false
      (let [curr (peek context-stack)]
        (if (-> curr :context (contains? k))
          true
          (recur (pop context-stack)))))))

(defn validate-all-contexts
  "Validate all the contexts in a Profile."
  [profile]
  (loop [profile-loc (profile-to-zipper profile)
         context-stack []]
    (if (zip/end? profile-loc)
      true
      (let [curr-node (zip/node profile-loc)
            new-stack (update-contexts context-stack profile-loc)
            error-seq (map (partial search-contexts new-stack)
                           (keys (dissoc curr-node :context)))]
        (if (every? true? error-seq)
          (recur (zip/next profile-loc) new-stack)
          curr-node)))))
