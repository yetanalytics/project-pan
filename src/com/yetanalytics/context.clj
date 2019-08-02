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
    ;; TODO get other contexts from the Internet; return nil on failure 
    nil))

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

(defn dissoc-prefixes
  "Returns all terms in a context that are not prefixes nor keyword aliases."
  [context]
  (reduce-kv (fn [accum k v]
               (if (or (s/valid? ::keyword v) (s/valid? ::prefix v))
                 accum
                 (assoc accum k v))) {} context))

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

(defn simple-term-spec
  [prefixes]
  (s/def ::simple-term-def (partial compact-iri? prefixes)))

(defn expanded-term-spec
  [prefixes]
  (s/def ::expanded-term-def (partial context-map? prefixes)))

;; A term definition may either have a string as a value (ie. a simple term
;; definition) or a map (ie. an expanded term definition).
(defn value-spec
  [prefixes]
  (s/def ::value
    (s/or :simple-term-def (simple-term-spec prefixes)
          :expanded-term-def (expanded-term-spec prefixes))))

(defn values-spec
  [prefixes]
  (let [v-spec (value-spec prefixes)]
    (s/def ::values (s/map-of any? v-spec))))

(defn validate-context
  [context]
  (let [prefixes (collect-prefixes context)
        term-defs (dissoc-prefixes context)
        vals-spec (values-spec prefixes)]
    (s/explain-data vals-spec term-defs)))

(s/def ::context-iri-fail (constantly false))

(defn create-context
  [context-iri]
  (if-let [context (get-context context-iri)]
    (if-let [errors (validate-context context)]
      {:context nil :errors errors}
      {:context context :errors nil})
    {:context nil :errors (s/explain-data ::context-iri-fail context-iri)}))

; (defn value-spec
;   "Create a spec that validates a single JSON value in the context."
;   [prefixes]
;   (s/or :keyword ::keyword
;         :prefix ::prefix
;         :simple-term-def (partial compact-iri? prefixes)
;         :expanded-term-def (partial context-map? prefixes)))

; (defn context-spec
;   "Creates a spec that validates an entire context."
;   [context]
;   (let [v-spec (-> context collect-prefixes value-spec)]
;     (s/map-of keyword? v-spec)))

; (defn create-context
;   "Create a valid context from a @context IRI.
;   Throws an exception if the context is invalid."
;   [context-iri]
;   (let [context (get-context context-iri)
;         errors (s/explain-data (context-spec context) context)]
;     (if (every? nil? errors)
;       context
;       (throw (ex-info (str "Failure to validate @context " context-iri)
;                       errors)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validate profile against context 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO Get a better solution to the lang map issue? 
(defn children
  "Zipper function for returning the children at a location in a Profile.
  Returns nil if there are no children at the location."
  [node]
  (not-empty
   (reduce-kv (fn [accum k v]
                (cond
                  (and (map? v) (not (#{:prefLabel :definition :scopeNote
                                        :name :description} k)))
                  (conj accum v)
                  (s/valid? (s/coll-of map? :type vector?) v)
                  (concat accum v)
                  :else accum))
              (list) node)))

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
        context-iris (-> location zip/node :_context)]
    (cond
      ;; @context is URI valued
      (s/valid? ::ax/iri context-iris)
      (conj context-stack (assoc (create-context context-iris)
                                 :path curr-path))
      ;; @context is array valued
      (s/valid? (s/coll-of ::ax/iri :kind vector?) context-iris)
      (concat context-stack (mapv (fn [iri]
                                    (assoc (create-context iri)
                                           :path curr-path))
                                  context-iris))
      (nil? context-iris) context-stack)))

#_(defn update-contexts
    "Update the contexts stack on every node (ie. pop if we are no longer a 
  child of the latest @contexts, push if we see a new @context)."
    [context-stack location]
    (-> context-stack (pop-contexts location) (push-context location)))

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

(defn contexted-key-spec
  [contexts]
  (s/def ::contexed-key (partial search-contexts contexts)))

;; TODO Dissassociate other JSON-LD keywords besides @context
;; At this point all @ signs are replaced by underscores
(defn validate-keys
  [contexts curr-node]
  (let [k-spec (contexted-key-spec contexts)
        ks-spec (s/def ::contexted-keys (s/coll-of k-spec))]
    (s/explain-data ks-spec
                    (keys (dissoc curr-node :_context)))))

(defn update-profile-errors
  [contexts curr-node profile-errors]
  (let [new-errors (validate-keys contexts curr-node)]
    (if (some? new-errors)
      (conj profile-errors new-errors)
      profile-errors)))

(defn validate-contexts
  "Validate all the contexts in a Profile."
  [profile]
  (loop [profile-loc (profile-to-zipper profile)
         context-stack []
         ;; For contexts themselves that are bad 
         context-errors (list)
         ;; When the profile doesn't follow the context 
         profile-errors (list)]
    (if (zip/end? profile-loc)
      (-> {} (assoc :context-errors (not-empty context-errors)
                    :context-key-errors (not-empty profile-errors)))
      (let [curr-node (zip/node profile-loc)
            popped-stack (pop-contexts context-stack profile-loc)
            pushed-stack (push-context context-stack profile-loc)]
        (if (-> pushed-stack peek :context nil?)
          ;; If latest context is erroneous
          (let [new-cerrors (update-context-errors popped-stack pushed-stack
                                                   context-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack new-cerrors profile-errors))
          ;; If latest context is valid => validate map keys
          (let [new-perrors (update-profile-errors pushed-stack curr-node
                                                   profile-errors)]
            (recur (zip/next profile-loc)
                   pushed-stack context-errors new-perrors)))))))
