(ns com.yetanalytics.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [camel-snake-kebab.core :as kebab]
            [cheshire.core :as cheshire]
            [ubergraph.core :as uber]
            [com.yetanalytics.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-nil
  "Turn a nil array into an empty array"
  [value]
  (if (nil? value) [] value))

(defn normalize-profile
  "Turn any nil top-level arrays in a profile (versions, concepts, templates
  and patterns) into empty arrays."
  [profile]
  (-> profile
      (update :versions normalize-nil)
      (update :concepts normalize-nil)
      (update :templates normalize-nil)
      (update :patterns normalize-nil)))

; (defn only-ids
;   "Return a collection of IDs from a collection of objects."
;   [obj-coll] (mapv (fn [{:keys [id]}] id) obj-coll))

; (defn only-ids-multiple
;   "Return a collection of all IDs from multiple collections of objects"
;   [obj-colls]
;   (flatten (mapv only-ids obj-colls)))

; #_(defn id-map
;     "Create a map of IDs to the objects that they identify."
;     [map-with-ids]
;     (dissoc (zipmap (only-ids map-with-ids) map-with-ids) nil))

; #_(defn combine-args
;     "Return a vector of maps that each include an object and additional
;   arguments."
;     [obj-vec args]
;     (mapv #(conj args [:object %]) obj-vec))

; (defn count-ids
;   "Count the number of ID instances by creating a map between IDs and their
;   respective counts. (Ideally theys should all be one, as IDs MUST be unique
;   by definition.)"
;   [ids-coll]
;   (reduce (fn [accum id]
;             (update accum id #(if (nil? %) 1 (inc %)))) {} ids-coll))

; ;; IDs MUST be distinct.
; (defn one? [n] (= 1 n))

; (s/def ::distinct-ids
;   (s/map-of (s/or :iri ::ax/iri :irl ::ax/irl :uri ::ax/uri :url ::ax/url)
;             one?))

;; In Concepts that can contain a schema or an inlineSchema (ie. IRI or string)
;; it MUST NOT contain both
(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inlineSchema)]
      (not (and schema? inline-schema?)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parsing 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-chars
  "Remove chars that are illegal in keywords, ie. spaces and the @ symbol."
  [s] (string/replace s #"@|\s" ""))

(defn replace-at
  "Replace any @ symbols with 'at/' (so that they are all in their own pseudo-
  namespace."
  [s replacement] (string/replace s #"@" replacement))

(defn convert-json
  "Convert a JSON string into an edn data structure."
  [json at-replacement]
  (cheshire/parse-string
   json #(-> % (replace-at at-replacement) keyword)
   #_(fn [k] (-> k remove-chars kebab/->kebab-case-keyword))))
;; ^ example usage of ->

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; ;; Return a node and its attributes
; (defmulti node-with-attrs #(:type %))

; (defmethod node-with-attrs :default [node]
;   (let [node-name (:id node)
;         node-attrs {:type (:type node)
;                     :inScheme (:inScheme node)}]
;     (vector node-name node-attrs)))

; ;; Return a vector of all outgoing edges
; (defmulti edges-with-attrs #(:type %))

; (defmethod edges-with-attrs :default [_] [])

; ;; Flatten a collection of edges so that it is a 1D vector of [src dest attrs]
; ;; vectors
; (defn collect-edges [attr-edges]
;   (reduce concat attr-edges))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Error message formatting 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn explain
  "Works like spec/explain-data, but only returns the ::spec/problems key.
  Useful to isolate the problematic entry in a collection from a spec/coll-of
  spec."
  [spec item]
  (::s/problems (s/explain-data spec item)))

(def error-msgs
  {"boolean" "Not a boolean value."
   "string" "Not a string (or an empty string)."
   "timestamp" "Invalid timestamp."
   "language-map" "Invalid Language Map object."
   "media-type" "Not a RFC 2046 media type."
   "json-path" "Not a valid JSONPath string."
   "json-schema" "Not a valid draft-07 JSON Schema."
   "iri" "Invalid IRI."
   "irl" "Invalid IRL."
   "uri" "Invalid URI."
   "url" "Invalid URL."
   "array-of-iri" "Not a valid array of IRIs."
   "valid-edge" "Invalid relation between two Profile objects."
   "distinct-ids" "Non-distinct ID."
   "in-scheme" "Invalid inScheme IRI (e.g. not a valid version ID)."})

(defn prettify-error
  [{:keys [path] :as problem}]
  (let [spec-pred (-> path last name)]
    (str "Error: " (get error-msgs spec-pred "Invalid value") "\n"
         "Value:\n"
         (:val problem) "\n")))

; (defn phraser
;   [{:keys [path pred via in] :as problems}]
;   (str "Invalid " (-> via last name) " error."))

; (-> (s/explain-data ::ax/iri "foo bar") ::s/problems first phraser)

; (defn error
;   [{:keys [problems] :as spec-data}]
;   (let [{:keys [path pred val via in]} problems]
;     (case (-> via))))


(comment
  "Explain data: {:path :pred :val :via :in}
  Spec error output:
  (val) - failed: (pred) at: [(path)] spec: (spec)
  Our error output (potentially):
  failure to validate at (spec)
    at path:
      (path[0])
      (path[1])
      (...))
    with predicate: (pred)
    on value:
      (val)
  
  For a syntax validation error, we should have
  - OBJECT where the error occured (Profile, Concept, Template or Pattern)
  - VALUE that failed the validation
  - PREDICATE/SPEC that the value didn't conform against

  For an edge validation error, we should have
  - SRC and DEST edges, with attributes
  - PREDICATE/SPEC that the value didn't conform against
  ")

(try
  (throw (ex-info "exception" {:problems [{:pred false?}]}))
  (catch Exception e e))
