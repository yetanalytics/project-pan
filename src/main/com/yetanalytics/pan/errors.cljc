(ns com.yetanalytics.pan.errors
  (:require #?(:clj [clojure.core :refer [format]]
               :cljs [goog.string :as gstring]
               [goog.string.format :as format])
            [clojure.spec.alpha :as s]
            [clojure.pprint     :as pprint]
            [clojure.string     :as cstr]
            [expound.alpha      :as exp]
            [com.yetanalytics.pan.axioms      :as ax]
            [com.yetanalytics.pan.context     :as ctx]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concept                      :as c]
            [com.yetanalytics.pan.objects.concepts.util                :as cu]
            [com.yetanalytics.pan.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.pan.objects.concepts.extensions.context  :as ce]
            [com.yetanalytics.pan.objects.concepts.extensions.result   :as re]
            [com.yetanalytics.pan.objects.concepts.activity            :as act]
            [com.yetanalytics.pan.objects.template                     :as t]
            [com.yetanalytics.pan.objects.templates.rule               :as r]
            [com.yetanalytics.pan.objects.pattern                      :as p]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec messages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Syntax spec messages

(exp/defmsg ::ax/boolean "should be a boolean")
(exp/defmsg ::ax/string "should be a non-empty string")
(exp/defmsg ::ax/timestamp "should be a valid timestamp")
(exp/defmsg ::ax/language-map "should be a valid language map")
(exp/defmsg ::ax/language-tag "should be a valid language tag")
(exp/defmsg ::ax/lang-map-string "should be a valid string")
(exp/defmsg ::ax/media-type "should be a RFC 2046 media type")
(exp/defmsg ::ax/json-path "should be a valid JSONPath string")
(exp/defmsg ::ax/json-schema "should be a valid JSON schema")
(exp/defmsg ::ax/iri "should be a valid IRI")
(exp/defmsg ::ax/irl "should be a valid IRL")
(exp/defmsg ::ax/uri "should be a valid URI")
(exp/defmsg ::ax/url "should be a valid URL")
(exp/defmsg ::ax/array-of-iri "should be an array of IRIs")
(exp/defmsg ::ax/array-of-uri "should be an array of URIs")

(exp/defmsg ::cu/inline-or-iri
  "should not contain both linked and inline JSON schema")
(exp/defmsg ::cu/related-only-deprecated
  "should not use related property in a non-deprecated Concept")

(exp/defmsg ::c/concept
  "should have a valid Concept type")
(exp/defmsg ::ae/no-recommended-verbs
  "should not use recommended verb property on an Activity Extension")
(exp/defmsg ::ce/no-recommended-activity-types
  "should not use recommended activity type on a Context Extension")
(exp/defmsg ::re/no-recommended-activity-types
  "should not use recommended activity type on a Result Extension")
(exp/defmsg ::act/activityDefinition
  "should be a valid activity definition")
(exp/defmsg ::act/extensions
  "should be valid Activity extensions")
(exp/defmsg ::act/extension
  "should be a valid Activity extension")

(exp/defmsg ::t/type-or-reference
  "should not contain both objectActivityType and objectStatementRefTemplate")
(exp/defmsg ::r/value-array
  "should be an non-empty array of values")
(exp/defmsg ::r/rule-keywords
  "should contain one of presence, any, all, or none")

(exp/defmsg ::p/pattern-clause
  "should only have one of sequences, alternates, etc")
(exp/defmsg ::p/is-primary-true
  "should be a primary pattern")
(exp/defmsg ::p/is-primary-false
  "should not be a primary Pattern")

;; ID spec messages

(exp/defmsg ::id/one-count
  "should be a unique identifier value")
(exp/defmsg ::id/in-scheme
  "should be a valid version ID")

;; Graph spec messages

(exp/defmsg ::graph/not-self-loop
  "should not refer to itself")
(exp/defmsg ::graph/singleton-scc
  "should not contain cyclical references")

(exp/defmsg ::c/valid-dest
  "should not link to a non-existent Concept")
(exp/defmsg ::c/relatable-src
  "should be type: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/relatable-dest
  "should link to type: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/activity-ext-src
  "should be type: \"ActivityExtension\"")
(exp/defmsg ::c/ctxt-result-ext-src
  "should be type: \"ContextExtension\" or \"ResultExtension\"")
(exp/defmsg ::c/activity-type-dest
  "should link to type: \"ActivityType\"")
(exp/defmsg ::c/verb-dest
  "should link to type: \"Verb\"")
(exp/defmsg ::c/same-concepts
  "should link to a Concept of the same type")
(exp/defmsg ::c/same-version
  "should link to an object with matching inScheme value")

(exp/defmsg ::t/valid-dest
  "should not link to non-existent Concept or Template")
(exp/defmsg ::t/template-src
  "should be type: \"StatementTemplate\"")
(exp/defmsg ::t/verb-dest
  "should link to type: \"Verb\"")
(exp/defmsg ::t/activity-type-dest
  "should link to type: \"ActivityType\"")
(exp/defmsg ::t/attachment-use-type-dest
  "should link to type: \"AttachmentUsageType\"")
(exp/defmsg ::t/template-dest
  "should link to type: \"StatementTemplate\"")
(exp/defmsg ::t/same-version
  "should link to an object with matching inScheme value")

(exp/defmsg ::p/valid-dest
  "should not link to non-existent Template or Pattern")
(exp/defmsg ::p/pattern-src
  "should be type: \"Pattern\"")
(exp/defmsg ::p/pattern-dest
  "should link to type: \"Pattern\"")
(exp/defmsg ::p/template-dest
  "should link to type: \"StatementTemplate\"")
(exp/defmsg ::p/non-opt-dest
  "should not link to an optional or zeroOrMore Pattern")
(exp/defmsg ::p/singleton-src
  "should only link to one other object")
(exp/defmsg ::p/not-singleton-src
  "should link to at least two other objects")
(exp/defmsg ::p/primary-pattern
  "should be a primary Pattern")
(exp/defmsg ::p/zero-indegree-src
  "should not be used elsewhere in the Profile")

;; Context spec messages

(exp/defmsg ::ctx/_context
  "should be a valid inline context")
(exp/defmsg ::ctx/language-tag
  "should be a language tag")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property orderings
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def property-order
  {;; Common object properties
   :id          0
   :type        1
   :inScheme    2
   :prefLabel   3
   :definition  4
   :deprecated  5
   :_context    6
   ;; Profile metadata
   :conformsTo  7
   :seeAlso     8
   :author      9
   :versions    10
   ;; Profile version objects
   :wasRevisionOf   11
   :generatedAtTime 12
   ;; Profile author objects
   :name  13
   :url   14
   ;; Concept properties
   :broader      15
   :broadMatch   16
   :narrower     17
   :narrowMatch  18
   :related      19
   :relatedMatch 20
   :exactMatch   21
   ;; Extension and Document Resource properties
   :context                   22
   :recommendedActivityTypes  23
   :recommendedVerbs          24
   :contentType               25
   :schema                    26
   :inlineSchema              27
   ;; Activity properties
   :activityDefinition        28
   :description               29
   :moreInfo                  30
   :interactionType           31
   :correctResponsesPattern   32
   :choices     33
   :scale       34
   :source      35
   :target      36
   :steps       37
   :extensions  38
   ;; Statement Template properties
   :verb                          39
   :objectActivityType            40
   :contextGroupingActivityType   41
   :contextParentActivityType     42
   :contextOtherActivityType      43
   :contextCategoryActivityType   44
   :attachmentUsageType           45
   :objectStatementRefTemplate    46
   :contextStatementRefTemplate   47
   :rules                         48
   ;; Statement Template rules properties
   :location  49
   :selector  50
   :presence  51
   :any       52
   :all       53
   :none      54
   :scopeNote 55
   ;; Pattern properties
   :primary    56
   :alternates 57
   :optional   58
   :oneOrMore  59
   :sequence   60
   :zeroOrMore 61
   ;; Top-level object lists
   :concepts    62
   :templates   63
   :patterns    64})

(defn- cmp-properties
  "Compare two Profile property keys, mostly based on how it was
   listed in the xAPI Profile spec. Properties not listed in the
   property-order map will be pushed to the end of the error list
   and sorted alphabetically.

   Return value meaning:
   - neg number: prop1 comes before prop2
   - pos number: prop1 comes after prop2
   - zero: prop1 and prop2 are equal"
  [p1 p2]
  (let [n1 (get property-order p1)
        n2 (get property-order p2)]
    (cond
      ;; Compare properties' existences
      (and (nil? p1) (nil? p2)) 0
      (and (nil? p1) (some? p2)) -1
      (and (some? p1) (nil? p2)) 1
      ;; Compare properties' orders
      (and (some? n1) (nil? n2)) -1
      (and (nil? n1) (some? n2)) 1
      (and (some? n1) (some? n2)) (compare n1 n2)
      (and (nil? n1) (nil? n2)) (compare p1 p2))))

(defn- map->sorted-map
  "Sort the keys of a Profile object."
  [m]
  (into (sorted-map-by cmp-properties) m))

(defn- expanded-map->sorted-map
  "Sort the keys of an object with nameable keys (strings, keywords, etc)."
  [m]
  (into (sorted-map-by (fn [x y] (compare (name x) (name y))))
        m))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value display
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fmt #?(:clj format :cljs gstring/format))

(defn- elide-arrs
  "Elide concept, template, and pattern arrays as to not distract
   from showing top-level Profile properties."
  [obj]
  (cond-> obj
    (:concepts obj) (assoc :concepts ['...])
    (:templates obj) (assoc :templates ['...])
    (:patterns obj) (assoc :patterns ['...])))

(defn- elide-arrs-2
  [obj]
  (reduce-kv (fn [m k v]
               (if (and (coll? v)
                        (every? coll? v))
                 (assoc m k '[...])
                 (assoc m k v)))
             {}
             obj))

(defn- ppr-str
  "Format `obj` according to clojure.core/pprint and returns a
   string."
  [obj]
  (pprint/write obj :stream nil))

(defn- get-prop-from-path
  "Get the erroneous property from a error data path."
  [path]
  (->> path reverse (some (fn [x] (when (keyword? x) x)))))

(defn- value-str-obj
  "Custom value string fn for errors on objects."
  [_ profile path value]
  (cond
    ;; Error occured over the entire Profile, Concept, Template, or Pattern
    (or (= [] path)
        (#{[:concepts] [:templates] [:patterns]} (butlast path)))
    (let [obj (-> value elide-arrs map->sorted-map)]
      (fmt (str "Object:\n"
                "%s")
           (ppr-str obj)))
    ;; Error occured inside a Concept, Template, or Pattern
    (#{:concepts :templates :patterns} (first path))
    (let [path' (subvec path 0 2)
          obj   (->> path' (get-in profile) map->sorted-map)]
      (fmt (str "Value:\n"
                "%s\n"
                "\n"
                "of property:\n"
                "%s\n"
                "\n"
                "in object:\n"
                "%s")
           (ppr-str value)
           (pr-str (get-prop-from-path path))
           (ppr-str obj)))
    ;; Error occured on Profile metadata or some other nested object
    :else
    (let [obj (-> profile elide-arrs map->sorted-map)]
      (fmt (str "Value:\n"
                "%s\n"
                "\n"
                "of property:\n"
                "%s\n"
                "\n"
                "in object:\n"
                "%s")
           (ppr-str value)
           (pr-str (get-prop-from-path path))
           (ppr-str obj)))))

(defn- value-str-id
  "Custom value string fn for duplicate ID errors"
  [_ _ path value]
  (fmt (str "Identifier:\n"
            "%s\n"
            "\n"
            "which occurs %d time%s in the Profile")
       (-> path last pr-str)
       value
       (if (= 1 value) "" "s")))

(defn- value-str-version
  "Custom value string fn for inScheme error messages."
  [_ _ _ {:keys [id inScheme version-ids] :as _value}]
  (fmt (str "InScheme IRI:\n"
            "%s\n"
            "\n"
            "associated with the identifier:\n"
            "%s\n"
            "\n"
            "in a Profile with the following version IDs:\n"
            "%s")
       (pr-str inScheme)
       (pr-str id)
       (->> version-ids sort (map pr-str) (cstr/join "\n"))))

(defn- value-str-edge
  "Custom value string fn for IRI link error messages."
  [_ _ _ {:keys [src src-type dest dest-type] :as value}]
  (if (= "Pattern" src-type)
    ;; Patterns
    (let [{:keys [src-primary src-indegree src-outdegree dest-property]}
          value]
      (fmt (str "Pattern:\n"
                "{:id %s,\n"
                " :type %s,\n"
                " :primary %s,\n"
                " ...}\n"
                "\n"
                "that links to object:\n"
                "{:id %s,\n"
                " :type %s,\n"
                " %s ...,\n"
                " ...}\n"
                "\n"
                "via the property:\n"
                "%s\n"
                "\n"
                "and is used %d time%s to link out to %d object%s")
           (pr-str src)
           (pr-str src-type)
           (pr-str src-primary) ; cljs does not support "%b"
           (pr-str dest)
           (pr-str dest-type)
           (pr-str dest-property)
           (pr-str (:type value)) ; Don't shadow clojure.core/type
           src-indegree
           (if (= 1 src-indegree) "" "s")
           src-outdegree
           (if (= 1 src-outdegree) "" "s")))
    ;; Concepts and Statement Templates
    (let [{:keys [src-version dest-version]} value]
      (fmt (str "%s:\n"
                "{:id %s,\n"
                " :type %s,\n"
                " :inScheme %s,\n"
                " ...}\n"
                "\n"
                "that links to object:\n"
                "{:id %s,\n"
                " :type %s,\n"
                " :inScheme %s,\n"
                " ...}\n"
                "\n"
                "via the property:\n"
                "%s")
           (if (= "StatementTemplate" src-type) "Statement Template" "Concept")
           (pr-str src)
           (pr-str src-type)
           (pr-str src-version)
           (pr-str dest)
           (pr-str dest-type)
           (pr-str dest-version)
           (pr-str (:type value))))))

(defn- value-str-scc
  "Custom value string fn for strongly connected component errors.
   Used for pattern cycle errors."
  [_ _ _ value]
  (fmt (str "The following Patterns:\n"
            "%s")
       (->> value sort (map pr-str) (cstr/join "\n"))))

(defn- value-str-context-key
  "Custom value string fn to print errors on context expanded keys."
  [_ profile path value]
  (let [object (->> path
                    butlast
                    (get-in profile)
                    elide-arrs-2
                    expanded-map->sorted-map)]
    (fmt (str "Key:\n"
              "%s\n"
              "\n"
              "in object:\n"
              "%s")
         (ppr-str value)
         (ppr-str object))))

(defn- custom-printer
  "Returns a printer based on `error-type`. A `nil` value will
  result in the default return value of `value-str-obj`."
  [& [error-type]]
  (let [error-type (if (nil? error-type) :else error-type)
        make-opts  (fn [f] {:print-specs? false :value-str-fn f})]
    (case error-type
      ;; New
      :syntax-errors
      (exp/custom-printer (make-opts value-str-obj))
      :id-errors
      (exp/custom-printer (make-opts value-str-id))
      :in-scheme-errors
      (exp/custom-printer (make-opts value-str-version))
      :concept-edge-errors
      (exp/custom-printer (make-opts value-str-edge))
      :template-edge-errors
      (exp/custom-printer (make-opts value-str-edge))
      :pattern-edge-errors
      (exp/custom-printer (make-opts value-str-edge))
      :pattern-cycle-errors
      (exp/custom-printer (make-opts value-str-scc))
      :context-errors
      (exp/custom-printer (make-opts value-str-context-key))
      ;; Old
      :id
      (exp/custom-printer (make-opts value-str-id))
      :in-scheme
      (exp/custom-printer (make-opts value-str-version))
      :edge
      (exp/custom-printer (make-opts value-str-edge))
      :cycle
      (exp/custom-printer (make-opts value-str-scc))
      :context
      (exp/custom-printer (make-opts value-str-context-key))
      :else
      (exp/custom-printer (make-opts value-str-obj)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transform error map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn error->str
  [error-map error-type]
  (let [print-fn (custom-printer error-type)]
    (with-out-str (print-fn error-map))))

(defn- regroup-exp-data
  "Convert the explain-data map into a list of mini-explain-data maps,
   grouped together by the `:in` key (a simplified version of how
   Expound groups related errors)."
  [{problems ::s/problems :as exp-data-map}]
  (->> problems
       (group-by :in)
       (reduce-kv (fn [m k v]
                    (->> v
                         (assoc exp-data-map ::s/problems)
                         (assoc m k)))
                  {})))

(defn- kw->header
  [k]
  (case k
    :id-errors "ID Errors"
    :in-scheme-errors "Version Errors"
    (->> (-> k name (cstr/split #"-"))
         (map cstr/capitalize)
         (cstr/join " "))))

(defn errors->type-path-str-m
  "Given a map `{:err-type spec-err}`, return a map of the form
   `{:err-type {:spec-path err-str}}`"
  [spec-errs-map]
  (reduce-kv (fn [m k v]
               (if (some? v)
                 (->> (regroup-exp-data v)
                      (reduce-kv (fn [m' k' v']
                                   (assoc m' k' (error->str v' k)))
                                 {})
                      (assoc m k))
                 m))
             {}
             spec-errs-map))

(defn errors->type-str-m
  "Given a map `{:err-type spec-err}`, return a map of the form
   `{:err-type err-str}`."
  [spec-errs-map]
  (reduce-kv (fn [m k v]
               (cond-> m
                 (some? v)
                 (assoc k (-> v (error->str k)))))
             {}
             spec-errs-map))

(defn errors->string
  "Given a map `{:err-type spec-err}`, return an error string."
  [spec-errors-map]
  (reduce-kv (fn [s k v]
               (cond-> s
                 (some? v)
                 (str "\n**** " (kw->header k) " ****\n\n" (error->str v k))))
             ""
             spec-errors-map))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Expounding entire error map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn expound-error
  "Print an Expounded error message using a custom printer. The custom
   printer is determined via `error-type`; if not supplied, it assumes
   that it is a syntax error. Prints `error-label` as the header."
  ([error-map error-label]
   (expound-error error-map error-label nil))
  ([error-map error-label error-type]
   (let [print-fn (custom-printer error-type)]
     (println (str "\n**** " error-label " ****\n"))
     (print-fn error-map))))

(defn print-errors
  "Print errors from profile validation using Expound. Available keys:
  :syntax-errors         Basic syntax validation (always present)
  :id-errors             Duplicate ID errors
  :in-scheme-errors      inScheme property validation
  :concept-errors        Concept relation/link errors
  :template-errors       Template relation/link errors
  :pattern-errors        Pattern relation/link errors
  :pattern-cycle-errors  Cyclical pattern errors
  :context-errors        Errors in expanding keys via @context maps"
  [{:keys [syntax-errors
           id-errors
           in-scheme-errors
           concept-edge-errors
           template-edge-errors
           pattern-edge-errors
           pattern-cycle-errors
           context-errors]}]
  (when syntax-errors
    (expound-error syntax-errors "Syntax Errors"))
  (when id-errors
    (expound-error id-errors "ID Errors" :id))
  (when in-scheme-errors
    (expound-error in-scheme-errors "Version Errors" :in-scheme))
  (when concept-edge-errors
    (expound-error concept-edge-errors "Concept Edge Errors" :edge))
  (when template-edge-errors
    (expound-error template-edge-errors "Template Edge Errors" :edge))
  (when pattern-edge-errors
    (expound-error pattern-edge-errors "Pattern Edge Errors" :edge))
  (when pattern-cycle-errors
    (expound-error pattern-cycle-errors "Pattern Cycle Errors" :cycle))
  (when context-errors
    (expound-error context-errors "Context Errors" :context)))
