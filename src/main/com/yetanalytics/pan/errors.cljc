(ns com.yetanalytics.pan.errors
  (:require [clojure.core :refer [format]]
            #_[clojure.spec.alpha :as s]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [expound.alpha :as exp]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.pan.objects.concept :as c]
            [com.yetanalytics.pan.objects.template :as t]
            [com.yetanalytics.pan.objects.pattern :as p]
            [com.yetanalytics.pan.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.pan.objects.concepts.extensions.context :as ce]
            [com.yetanalytics.pan.objects.concepts.extensions.result :as re]
            [com.yetanalytics.pan.objects.concepts.activities :as act]
            [com.yetanalytics.pan.utils.spec :as u]))

;; TODO: Expound can act pretty funny sometimes and we have to use some hacks
;; to avoid said funniness. Look into creating an in-house error message lib.

;; Error map reference:
;; :path = Path of map + spec keywords
;; :pred = Spec predicate
;; :val = Value being specced
;; :via = Path of spec keywords
;; :in = Path of map keys + array entry numbers

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
(exp/defmsg ::ax/json-path "should be a valid JSON schema")
(exp/defmsg ::ax/iri "should be a valid IRI")
(exp/defmsg ::ax/irl "should be a valid IRL")
(exp/defmsg ::ax/uri "should be a valid URI")
(exp/defmsg ::ax/url "should be a valid URL")
(exp/defmsg ::ax/array-of-iri "should be an array of IRIs")
(exp/defmsg ::ax/array-of-uri "should be an array of URIs")

(exp/defmsg ::u/inline-or-iri
  "should not contain both linked and inline JSON schema")
(exp/defmsg ::u/related-only-deprecated
  "should not use related property in a non-deprecated Concept")

(exp/defmsg ::ae/no-recommended-verbs
  "should not use recommended verb property on an Activity Extension")
(exp/defmsg ::ce/no-recommended-activity-types
  "should not use recommended activity type on a Context Extension")
(exp/defmsg ::re/no-recommended-activity-types
  "should not use recommended activity type on a Result Extension")
(exp/defmsg ::act/activityDefinition
  "should be a valid activity definition")

(exp/defmsg ::t/type-or-reference
  "should not contain both objectActivityType and objectStatementRefTemplate")

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

(exp/defmsg ::ctx/context-keyword
  "should be a JSON-LD context keyword")
(exp/defmsg ::ctx/context-prefix
  "should be a JSON-LD prefix")
(exp/defmsg ::ctx/simple-term-def
  "should be a simple term definition with a valid prefix")
(exp/defmsg ::ctx/expanded-term-def
  "should be an expanded term definition with a valid prefix")

(exp/defmsg ::ctx/iri-key
  "should be expandable into an absolute IRI")
(exp/defmsg ::ctx/keyword-key
  "should be a JSON-LD keyword")

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
  "Get the order of two properties based on how it was listed in the xAPI
  Profile spec. Properties not listed in the property-order map will be pushed
  to the end of the error list and sorted alphabetically.
  - neg number: prop1 comes before prop2
  - pos number: prop1 comes after prop2
  - zero: prop1 and prop2 are equal"
  [p1 p2]
  (let [n1 (get property-order p1)
        n2 (get property-order p2)]
    (cond
      ;; Compare properties' existence
      (and (nil? p1) (nil? p2)) 0
      (and (nil? p1) (some? p2)) -1
      (and (some? p1) (nil? p2)) 1
      ;; Compare properties' orders
      (and (some? n1) (nil? n2)) -1
      (and (nil? n1) (some? n2)) 1
      (and (some? n1) (some? n2)) (compare n1 n2)
      (and (nil? n1) (nil? n2)) (compare p1 p2))))

(defn- map->sorted-map
  [m]
  (into (sorted-map-by cmp-properties) m))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value display
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- elide-arrs [obj]
  (cond-> obj
    (:concepts obj) (assoc :concepts ['...])
    (:templates obj) (assoc :templates ['...])
    (:patterns obj) (assoc :patterns ['...])))

(defn- ppr-str
  [x]
  (pprint/write x :stream nil))

(defn value-str-obj
  [_ profile path value]
  (if (or (empty? path) (int? (last path)))
    (let [obj (->> path (get-in profile) elide-arrs map->sorted-map)]
      (format (str "Object:\n"
                   "%s")
              (ppr-str obj)))
    (let [obj (->> path butlast (get-in profile) elide-arrs map->sorted-map)]
      (format (str "Value:\n"
                   "%s\n"
                   "\n"
                   "of property:\n"
                   "%s\n"
                   "\n"
                   "in object:\n"
                   "%s")
              (ppr-str value)
              (pr-str (last path))
              (ppr-str obj)))))

(defn value-str-obj-nopath
  [_ profile path value]
  (let [obj (->> path butlast (get-in profile) elide-arrs map->sorted-map)]
    (format (str "Value:\n"
                 "%s\n"
                 "\n"
                 "in object:\n"
                 "%s")
            (ppr-str value)
            (ppr-str obj))))

(defn value-str-id
  "Custom value string for duplicate ID error messages. Takes the form:
   | Duplicate id: <identifier>
   |  with count:  <int>"
  [_ _ path value]
  (format (str "Identifer:\n"
               "%s\n"
               "\n"
               "which occurs %d time%s in the Profile")
          (-> path last pr-str)
          value
          (if (= 1 value) "" "s")))

(defn value-str-version
  "Custom value string for inScheme error messages. Takes the form:
   | Invalid inScheme: <in-scheme>
   |  at object: <identifier>
   |  profile version ids:
   |   <version-id-1>
   |   <version-id-2>
   |   ..."
  [_ _ _ {:keys [id inScheme version-ids] :as _value}]
  (format (str "InScheme IRI:\n"
               "%s\n"
               "\n"
               "associated with the identifier:\n"
               "%s\n"
               "\n"
               "in a Profile with the following version IDs:\n"
               "%s")
          (pr-str inScheme)
          (pr-str id)
          (->> version-ids sort (map pr-str) (string/join "\n"))))

#_{:clj-kondo/ignore [:unresolved-symbol]} ; kondo doesn't recognize core.match
(defn value-str-edge
  "Custom value string for IRI link error messages. Takes the form:
   | Invalid <property> identifier:
   |  <identifier>
   | at object:
   |  <object map>
   | linked object:
   |  <object map>"
  [_ _ _ {:keys [src src-type dest dest-type] :as value}]
  (cond
    (= "Pattern" src-type)
    (let [{:keys [src-primary src-indegree src-outdegree dest-property]}
          value]
      (format (str "Pattern:\n"
                   "{:id %s,\n"
                   " :type %s,\n"
                   " :primary %b,\n"
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
              src-primary
              (pr-str dest)
              (pr-str dest-type)
              dest-property
              (pr-str (:type value)) ; Don't shadow clojure.core/type
              src-indegree
              (if (= 1 src-indegree) "" "s")
              src-outdegree
              (if (= 1 src-outdegree) "" "s")))
    (or (= "Concept" src-type) (= "StatementTemplate" src-type))
    (let [{:keys [src-version dest-version]} value]
      (format (str "%s:\n"
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
              (if (= "Concept" src-type) "Concept" "Statement Template")
              (pr-str src)
              (pr-str src-type)
              (pr-str src-version)
              (pr-str dest)
              (pr-str dest-type)
              (pr-str dest-version)
              (pr-str (:type value))))
    :else
    ""))

(defn value-str-scc
  "Custom value string for strongly connected component error messages (if a
   digraph has a cycle). Takes the form:
   | Cycle detected in the following nodes:
   |   <identifier>
   |   <identifier>"
  [_ _ _ value]
  (format (str "The following Patterns:\n"
               "%s")
          (->> value sort (map pr-str) (string/join "\n"))))

(defn value-str-context
  [_ contexts path value]
  (format (str "Value:\n"
               "%s\n"
               "\n"
               "in context:\n"
               "%s")
          (ppr-str value)
          (ppr-str (->> path butlast (get-in contexts) map->sorted-map))))

(defn custom-printer
  "Returns a printer based on the error-type argument. A nil error-type will
  result in the default Expound printer (except with :print-specs? set to
  false)."
  [& [error-type]]
  (let [error-type (if (nil? error-type) :else error-type)
        make-opts  (fn [f] {:print-specs? false :value-str-fn f})]
    (case error-type
      :id
      (exp/custom-printer (make-opts value-str-id))
      :in-scheme
      (exp/custom-printer (make-opts value-str-version))
      :edge
      (exp/custom-printer (make-opts value-str-edge))
      :cycle
      (exp/custom-printer (make-opts value-str-scc))
      :context
      (exp/custom-printer (make-opts value-str-context))
      :context-key
      (exp/custom-printer (make-opts value-str-obj-nopath))
      :else
      (exp/custom-printer (make-opts value-str-obj)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Expounding functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn expound-error
  "Print an Expounded error message using a custom printer. The custom printer
  is determined using the error-type arg; if not supplied, it prints a default
  Expound error message (without the Relevant Specs trace)."
  ([error-map error-label]
   (expound-error error-map error-label nil))
  ([error-map error-label error-type]
   (let [print-fn (custom-printer error-type)]
     (println (str "\n**** " error-label " ****\n"))
     (print-fn error-map))))

(defn expound-errors
  "Print errors from profile validation using Expound. Available keys:
  :syntax-errors - Basic syntax validation (always present)
  :id-errors - Duplicate ID errors
  :in-scheme-errors - inScheme property validation
  :concept-errors - Concept relation/link errors
  :template-errors - Template relation/link errors
  :pattern-errors - Pattern relation/link errors
  :pattern-cycle-errors - Cyclical pattern errors
  :context-errors - Errors in any @context maps
  :context-key-errors - Errors in expanding any keys via @context maps"
  [{:keys [syntax-errors
           id-errors
           in-scheme-errors
           concept-edge-errors
           template-edge-errors
           pattern-edge-errors
           pattern-cycle-errors
           context-errors
           context-key-errors]}]
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
    (expound-error context-errors "Context Errors" :context))
  (when context-key-errors
    (expound-error context-key-errors "Context Key Errors" :context-key)))
