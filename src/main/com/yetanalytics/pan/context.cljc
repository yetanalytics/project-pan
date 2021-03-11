(ns com.yetanalytics.pan.context
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.zip :as zip]
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
;; Parse context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def profile-context
  (read-json-resource "context/profile-context.json" "at/"))

(def activity-context
  (read-json-resource "context/activity-context.json" "at/"))

(defn- get-context*
  "Get a context and parse it from JSON to EDN.
   If a @context is one of two contexts given by the profile spec, call them
   from local resources."
  [context]
  (case context
    "https://w3id.org/xapi/profiles/context"
    profile-context
    "https://w3id.org/xapi/profiles/activity-context"
    activity-context
    ;; TODO get other contexts from the Internet; currently throws exception
    (throw (ex-info "Unable to read from URL" {:url context}))))

(defn get-context
  "Get a raw context, then parse it from JSON to EDN.
  Return the JSON object given by the @context key"
  [context-uri]
  (-> context-uri get-context* :at/context))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; JSON-LD node objects may be aliased to the following keywords.
;; This spec checks if a @context key is an alias to a keyword value.
;; See section 6.2: Node Objects in the JSON-LD grammar.
(defn jsonld-keyword? [k]
  (contains? #{"@context" "@id" "@graph" "@nest" "@type" "@reverse" "@index"}
             k))

;; Regular expressions
;; Note: Forward slash is not escaped in JS regex

;; General IRI delimiters as defined in RFC 3986
(def gen-delims-regex
  #?(:clj #".*(?:\:|\/|\?|\#|\[|\]|\@)$"
     :cljs #".*(?:\:|/|\?|\#|\[|\]|\@)$"))

(def prefix-regex #"(.*\:)")

;; JSON-LD 1.1 prefixes may be a simple term definition that ends in a URI
;; general delimiter char or an expanded term definition with @prefix set to
;; true. See section 4.4: Compact IRIs in the JSON-LD grammar.
(defn jsonld-prefix? [x]
  (or (and (s/valid? ::ax/iri x) ; simple term definition
           (some? (re-matches gen-delims-regex x)))
      (and map? ; expanded term definition
           (true? (:prefix x)))))

(defn collect-prefixes
  "Returns all the prefixes in a context."
  [context]
  (reduce-kv (fn [accum k v]
               (if (jsonld-prefix? v) (assoc accum k v) accum))
             {}
             context))

(defn dissoc-prefixes
  "Returns all terms in a context that are not prefixes nor keyword aliases."
  [context]
  (reduce-kv (fn [accum k v]
               (if (or (jsonld-keyword? v) (jsonld-prefix? v))
                 accum
                 (assoc accum k v)))
             {}
             context))

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

(s/def ::simple-term-def
  (fn prefixes-simple-term-def-pair?
    [[prefixes x]] (compact-iri? prefixes x)))

(s/def ::expanded-term-def
  (fn prefixes-expanded-term-def-pair?
    [[prefixes x]] (context-map? prefixes x)))

;; A term definition may either have a string as a value (ie. a simple term
;; definition) or a map (ie. an expanded term definition).
(s/def ::term-def
  (s/or :simple-term-def   ::simple-term-def
        :expanded-term-def ::expanded-term-def))

(s/def ::term-defs (s/coll-of ::term-def))

(defn- conform-context
  [context]
  (let [prefixes  (collect-prefixes context)
        term-defs (dissoc-prefixes context)]
    (map (fn [td] [prefixes td]) (vals term-defs))))

(s/def ::jsonld-context
  (s/and (s/conformer conform-context) ::term-defs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate profile against context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Exclude language maps or inlined @context maps
(def excluded-nodes
  #{:prefLabel :definition :scopeNote :name :description :_context})

(defn children
  "Zipper function for returning the children at a location in a Profile.
  Returns nil if there are no children at the location."
  [node]
  (not-empty (reduce-kv
              (fn [accum k v]
                (cond
                  (and (map? v) (not (contains? excluded-nodes k)))
                  (conj accum v)
                  (s/valid? (s/coll-of map? :type vector?) v)
                  (concat accum v)
                  :else accum))
              '()
              node)))

(defn profile-to-zipper
  "Create a zipper from a profile structure."
  [profile]
  (zip/zipper map? children nil profile))

(defn subvec?
  "True if v1 is a subvector of v2, false otherwise."
  [v1 v2]
  (and (<= (count v1) (count v2))
       (= v1 (subvec v2 0 (count v1)))))

(defn pop-context
  "Pop the last-added context if the path is not located at the current
  location nor a parent of the current location."
  [context-stack location]
  (if-not (subvec? (-> context-stack peek :path) (-> location zip/path vec))
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
  (let [curr-path   (-> location zip/path vec)
        context-val (-> location zip/node :_context)]
    (letfn [(push-to-stack [context-stack context]
              (if-let [errors (s/explain-data ::jsonld-context context)]
                (conj context-stack {:context nil
                                     :errors  errors
                                     :path    curr-path})
                (conj context-stack {:context context
                                     :errors  nil
                                     :path    curr-path})))]
      (cond
        ;; @context is IRI valued
        (s/valid? ::ax/iri context-val)
        (push-to-stack context-stack (get-context context-val))
        ;; @context is array valued
        (s/valid? ::ax/array-of-iri context-val)
        (reduce push-to-stack context-stack (map get-context context-val))
        ;; @context is inline (not allowed by spec, but good for debug)
        (map? context-val)
        (push-to-stack context-stack context-val)
        ;; No @context key at this location
        (nil? context-val)
        context-stack))))

(defn update-context-errors
  "Update the list of context errors if a new erroneous context had been
  added to the stack."
  [old-stack new-stack context-errors]
  (if (not= old-stack new-stack)
    (conj context-errors (-> new-stack peek :errors))
    context-errors))

(defn search-contexts
  "Given a key, search in all the contexts in the stack for it. Return true
  if the key is found, false otherwise (as that indicates that the key cannot
  be expanded to an IRI)."
  [contexts k]
  (boolean (some (fn [context] (contains? context k)) contexts)))

(s/def ::iri-key
  (fn contexted-key? [[contexts k]] (search-contexts contexts k)))

;; Cannot use set as predicate, or else Expound overrides custom error msg.
;; List of keywords taken from Section 1.7 of the JSON-LD spec.
(s/def ::keyword-key
  (fn special-key? [[_ k]]
    (or (= :_context k)
        (= :_id k)
        (= :_type k)
        (= :_base k)
        (= :_container k)
        (= :_graph k)
        (= :_index k)
        (= :_language k)
        (= :_list k)
        (= :_nest k)
        (= :_none k)
        (= :_prefix k)
        (= :_reverse k)
        (= :_set k)
        (= :_value k)
        (= :_version k)
        (= :_vocab k))))

(s/def ::jsonld-key
  (s/coll-of (s/or :jsonld-iri ::iri-key
                   :jsonld-keyword ::keyword-key)))

(defn- conform-context-node-pair
  [[contexts node]]
  (map (fn [k] [contexts k]) (keys node)))

(s/def ::jsonld-keys
  (s/and (s/conformer conform-context-node-pair)
         ::jsonld-key))

(defn- update-profile-errors
  [contexts curr-node profile-errors]
  (let [contexts   (map :context contexts)
        new-errors (s/explain-data ::jsonld-keys [contexts curr-node])]
    (if (some? new-errors)
      (conj profile-errors new-errors)
      profile-errors)))

(defn validate-contexts
  "Validate all the contexts in a Profile. Returns spec error data if
   any context-related errors are present."
  [profile]
  (loop [profile-loc    (profile-to-zipper profile)
         context-stack  []
         ;; For contexts themselves that are bad
         context-errors '()
         ;; When the profile doesn't follow the context 
         profile-errors '()]
    (if-not (zip/end? profile-loc)
      (let [curr-node    (zip/node profile-loc)
            popped-stack (pop-contexts context-stack profile-loc)
            pushed-stack (push-context context-stack profile-loc)]
        (if (-> pushed-stack peek :context nil?)
          ;; If latest context is erroneous
          (let [context-errors' (update-context-errors popped-stack
                                                       pushed-stack
                                                       context-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack
                   context-errors'
                   profile-errors))
          ;; If latest context is valid => validate map keys
          (let [profile-errors' (update-profile-errors pushed-stack
                                                       curr-node
                                                       profile-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack
                   context-errors
                   profile-errors'))))
      (let [context-err-seq (filter some? context-errors)
            profile-err-seq (filter some? profile-errors)
            ;; Combine the seq of error data into one big error data
            ;; NOTE: `:in` key of each problem gets outdated so we dissoc it
            context-err-map (reduce
                             (fn [acc {probs ::s/problems value ::value}]
                               (let [problems (map #(dissoc % :in) probs)]
                                 (-> acc
                                     (update ::s/problems concat problems)
                                     (update ::s/value conj value))))
                             {::s/problems '()
                              ::s/spec     ::jsonld-context
                              ::s/value    #{}}
                             context-err-seq)
            profile-err-map (reduce
                             (fn [acc {probs ::s/problems}]
                               (let [problems (map #(dissoc % :in) probs)]
                                 (update acc ::s/problems concat problems)))
                             {::s/problems '()
                              ::s/spec     ::jsonld-keys
                              ::s/value    profile}
                             profile-err-seq)]
        {:context-errors
         (when (not-empty context-err-seq) context-err-map)
         :context-key-errors
         (when (not-empty profile-err-seq) profile-err-map)}))))
