(ns com.yetanalytics.pan.context
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.zip :as zip]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.utils.spec :as util])
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

(defn- uri->context*
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
    (throw (ex-info "Unable to read from URI"
                    {:type ::unknown-context-uri
                     :url   context}))))

(defn uri->context
  "Get a raw context, then parse it from JSON to EDN.
  Return the JSON object given by the @context key"
  [context-uri]
  (-> context-uri uri->context* :at/context))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; JSON-LD node objects may be aliased to the following keywords.
;; This spec checks if a @context key is an alias to a keyword value.
;; See section 6.2: Node Objects in the JSON-LD grammar.
(s/def ::context-keyword
  #{"@context" "@id" "@graph" "@nest" "@type" "@reverse" "@index"})

;; Regular expressions
;; Note: Forward slash is not escaped in JS regex
(def gen-delims-regex
  #?(:clj #".*(?:\:|\/|\?|\#|\[|\]|\@)$"
     :cljs #".*(?:\:|/|\?|\#|\[|\]|\@)$"))

(def prefix-regex #"(.*\:)")

;; JSON-LD 1.1 prefixes may be a simple term definition that ends in a URI
;; general delimiter char or an expanded term definition with @prefix set to
;; true. See section 4.4: Compact IRIs in the JSON-LD grammar.
(s/def ::context-prefix
  (s/or :simple-term-def
        (s/and ::ax/iri #(->> % (re-matches gen-delims-regex) some?))
        :expanded-term-def
        (s/and map? #(-> % :prefix true?))))

(defn collect-prefixes
  "Returns all the prefixes in a context."
  [context]
  (reduce-kv (fn [accum k v]
               (if (s/valid? ::context-prefix v) (assoc accum k v) accum))
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

;; TODO: Add "!" due to side effect on spec registry
(defn validate-context
  [context]
  (let [prefixes (collect-prefixes context)]
    ;; Hack the global spec registry by redefining these specs w/ new prefixes
    (s/def ::simple-term-def (partial compact-iri? prefixes))
    (s/def ::expanded-term-def (partial context-map? prefixes))
    (s/def ::jsonld-context (s/map-of
                             any?
                             (s/or :keyword ::context-keyword
                                   :prefix  ::context-prefix
                                   :simple-term-def   ::simple-term-def
                                   :expanded-term-def ::expanded-term-def)))
    (s/explain-data ::jsonld-context context)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate profile against context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Exclude language maps or inline @context maps as node children
(def excluded-props
  #{:prefLabel :definition :scopeNote :name :description :_context})

(defn- children
  "Zipper function for returning the children at a location in a Profile.
  Returns nil if there are no children at the location."
  [node]
  (not-empty
   (reduce-kv
    (fn [accum k v]
      (cond
        ;; Objects
        (and (map? v) (not (excluded-props k)))
        (conj accum v)
        ;; Arrays of objects
        (and (vector? v) (every? map? v))
        (concat accum v)
        ;; Don't consider anything else as children
        :else
        accum))
    '()
    node)))

(defn profile-to-zipper
  "Create a zipper from a profile structure."
  [profile]
  (zip/zipper map? children nil profile))

(defn pop-context
  "Pop the last-added context if the path is not located at the current
  location nor a parent of the current location."
  [context-stack location]
  (if-not (util/subvec? (-> context-stack peek :path)
                        (-> location zip/path vec))
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
        context-val (-> location zip/node :_context)]
    (letfn [(push-context*
              [context-stack context]
              (if-let [errors (validate-context context)]
                (conj context-stack {:context nil
                                     :errors  errors
                                     :path    curr-path})
                (conj context-stack {:context context
                                     :errors  nil
                                     :path    curr-path})))]
      (cond
        ;; @context is URI valued
        (s/valid? ::ax/iri context-val)
        (push-context* context-stack (uri->context context-val))
        ;; @context is array valued
        (s/valid? ::ax/array-of-iri context-val)
        (reduce push-context* context-stack (map uri->context context-val))
        ;; @context is inline (not allowed by spec, but good for debug)
        (map? context-val)
        (push-context* context-stack context-val)
        ;; No @context key at this location
        (nil? context-val)
        context-stack
        :else
        (throw (ex-info "Unknown @context value"
                        {:type  ::unknown-context-val
                         :value context-val}))))))

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
  (loop [context-stack contexts]
    (if (empty? context-stack)
      false ;; We searched through all the contexts
      (let [curr (peek context-stack)]
        (if (-> curr :context (contains? k))
          true ;; We found the key at the curr context!
          (recur (pop context-stack)))))))

;; List of keywords taken from Section 1.7 of the JSON-LD spec.
(s/def ::keyword-key
  #{:_context
    :_id
    :_type
    :_base
    :_container
    :_graph
    :_index
    :_language
    :_list
    :_nest
    :_none
    :_prefix
    :_reverse
    :_set
    :_value
    :_version
    :_vocab})

;; At this point all @ signs are replaced by underscores
;; TODO: Add "!" to denote side effect
(defn validate-keys
  [contexts curr-node]
  ;; Bind key to registry as side effect.
  (s/def ::iri-key
    (partial search-contexts contexts))
  (s/def ::jsonld-node
    (s/map-of (s/or :iri ::iri-key :keyword ::keyword-key) any?))
  (s/explain-data ::jsonld-node curr-node))

(defn update-profile-errors
  [contexts curr-node profile-errors]
  (let [new-errors (validate-keys contexts curr-node)]
    (if (some? new-errors)
      (conj profile-errors new-errors)
      profile-errors)))

(defn- error-seq->map
  "Returns the combined error map, or nil if `error-seq` is empty.
   Updates `:in` to reflect conslidated values."
  [spec-kw error-seq]
  (when (not-empty error-seq)
    (-> (reduce
         (fn [{index :index :as acc}
              {probs ::s/problems
               value ::s/value}]
           (let [probs' (map (fn [p] (update p :in #(into [index] %))) probs)]
             (-> acc
                 (update ::s/problems concat probs')
                 (update ::s/value conj value)
                 (update :index inc))))
         {::s/problems '()
          ::s/spec     spec-kw
          ::s/value    '()
          :index       0}
         error-seq)
        (dissoc :index)
        (update ::s/value (comp vec reverse)))))

(defn validate-contexts
  "Validate all the contexts in a Profile."
  [profile]
  (loop [profile-loc (profile-to-zipper profile)
         context-stack []
         ;; For contexts themselves that are bad
         context-errors '()
         ;; When the profile doesn't follow the context 
         profile-errors '()]
    (if-not (zip/end? profile-loc)
      (let [curr-node (zip/node profile-loc)
            popped-stack (pop-contexts context-stack profile-loc)
            pushed-stack (push-context context-stack profile-loc)]
        (if (-> pushed-stack peek :context nil?)
          ;; If latest context is erroneous
          (let [new-cerrors (update-context-errors popped-stack
                                                   pushed-stack
                                                   context-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack
                   new-cerrors
                   profile-errors))
          ;; If latest context is valid => validate map keys
          (let [new-perrors (update-profile-errors pushed-stack
                                                   curr-node
                                                   profile-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack
                   context-errors
                   new-perrors))))
      (let [context-err-seq (filter some? context-errors)
            profile-err-seq (filter some? profile-errors)
            context-err-map (error-seq->map ::jsonld-context context-err-seq)
            profile-err-map (error-seq->map ::jsonld-node profile-err-seq)]
        {:context-errors     context-err-map
         :context-key-errors profile-err-map}))))
