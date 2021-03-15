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
  "cannot contain both linked and inline JSON schema")
(exp/defmsg ::u/related-only-deprecated
  "related can only be used on deprecated concepts")

(exp/defmsg ::ae/no-recommended-verbs
  "only allowed on context or result extensions")
(exp/defmsg ::ce/no-recommended-activity-types
  "only allowed on activity extensions")
(exp/defmsg ::re/no-recommended-activity-types
  "only allowed on activity extensions")
(exp/defmsg ::act/activityDefinition
  "invalid activity definition")

(exp/defmsg ::t/type-or-reference
  "cannot contain both objectActivityType and objectStatementRefTemplate")

(exp/defmsg ::p/pattern-clause
  "pattern contains too many properties")
(exp/defmsg ::p/is-primary-true
  "primary is not true")
(exp/defmsg ::p/is-primary-false
  "primary is not false nor nil")

;; ID spec messages

(exp/defmsg ::id/one-count
  "the id value is not unique")
(exp/defmsg ::id/in-scheme
  "the inScheme value is not a valid version ID")

;; Graph spec messages

(exp/defmsg ::graph/not-self-loop
  "object cannot refer to itself")
(exp/defmsg ::graph/singleton-scc
  "cyclical reference detected")

(exp/defmsg ::c/valid-dest
  "linked concept does not exist")
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
  "the concepts are not the same type")
(exp/defmsg ::c/same-version
  "inScheme values do not match")

(exp/defmsg ::t/template-src
  "should be type: \"StatementTemplate\"")
(exp/defmsg ::t/valid-dest
  "linked concept or template does not exist")
(exp/defmsg ::t/verb-dest
  "should link to type: \"Verb\"")
(exp/defmsg ::t/activity-type-dest
  "should link to type: \"ActivityType\"")
(exp/defmsg ::t/attachment-use-type-dest
  "should link to type: \"AttachmentUsageType\"")
(exp/defmsg ::t/template-dest
  "should link to type: \"StatementTemplate\"")
(exp/defmsg ::t/same-version
  "inScheme values do not match")

(exp/defmsg ::p/valid-dest
  "linked template or pattern does not exist")
(exp/defmsg ::p/pattern-src
  "should be type: \"Pattern\"")
(exp/defmsg ::p/pattern-dest
  "should link to type: \"Pattern\"")
(exp/defmsg ::p/template-dest
  "should link to type: \"StatementTemplate\"")
(exp/defmsg ::p/non-opt-dest
  "alternate pattern cannot contain an optional or zeroOrMore pattern")
(exp/defmsg ::p/singleton-src
  "primary sequence pattern has multiple links")
(exp/defmsg ::p/not-singleton-src
  "sequence pattern must have at least two links")
(exp/defmsg ::p/primary-pattern
  "pattern is not primary")
(exp/defmsg ::p/zero-indegree-src
  "pattern must not be used elsewhere")

;; Context spec messages

(exp/defmsg ::ctx/context-keyword
  "should be a JSON-LD context keyword")
(exp/defmsg ::ctx/context-prefix
  "should be a JSON-LD prefix")
(exp/defmsg ::ctx/simple-term-def
  "simple term definition does not have valid prefix")
(exp/defmsg ::ctx/expanded-term-def
  "expanded term definition does not have valid prefix")

(exp/defmsg ::ctx/iri-key
  "key cannot be expanded into absolute IRI")
(exp/defmsg ::ctx/keyword-key
  "key is not JSON-LD keyword")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value display
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn strv
  "Short for 'stringify-value.' Like str or pr-str, but makes special cases
  for nils and keywords."
  [value]
  (case value
    nil "nil"
    :_context   "@context"
    :_id        "@id"
    :_type      "@type"
    :_base      "@base"
    :_container "@container"
    :_graph     "@graph"
    :_index     "@index"
    :_language  "@language"
    :_list      "@list"
    :_nest      "@nest"
    :_none      "@none"
    :_prefix    "@prefix"
    :_reverse   "@reverse"
    :_set       "@set"
    :_value     "@value"
    :_version   "@version"
    :_vocab     "@vocab"
    (if (keyword? value) (name value) (pr-str value))))

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
    (let [obj (->> path (get-in profile) elide-arrs)]
      (format (str "Object:\n"
                   "%s")
              (ppr-str obj)))
    (let [obj (->> path butlast (get-in profile) elide-arrs)]
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
  (let [obj (->> path butlast (get-in profile) elide-arrs)]
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
  (format (str "Duplicate id: %s\n"
               " with count:  %d")
          (last path)
          value))

(defn value-str-version
  "Custom value string for inScheme error messages. Takes the form:
   | Invalid inScheme: <in-scheme>
   |  at object: <identifier>
   |  profile version ids:
   |   <version-id-1>
   |   <version-id-2>
   |   ..."
  [_ _ _ {:keys [id inScheme version-ids] :as _value}]
  (format (str "Invalid inScheme: %s\n"
               " at object: %s\n"
               " profile version ids %s")
          inScheme
          id
          (->> version-ids sequence sort reverse (string/join "\n  "))))

#_{:clj-kondo/ignore [:unresolved-symbol]} ; kondo doesn't recognize core.match
(defn value-str-edge
  "Custom value string for IRI link error messages. Takes the form:
   | Invalid <property> identifier:
   |  <identifier>
   | at object:
   |  <object map>
   | linked object:
   |  <object map>"
  [_ _ _ {:keys [type src src-type dest dest-type] :as value}]
  (cond
    (= "Pattern" src-type)
    (let [{:keys [src-primary src-indegree src-outdegree dest-property]}
          value]
      (format (str "Invalid %s identifier:"
                   " %s\n"
                   "\n"
                   " at object:\n"
                   "  {:id \"%s\",\n"
                   "   :type \"%s\",\n"
                   "   :primary %b,\n"
                   "   ...}\n"
                   "\n"
                   " linked object:\n"
                   "   {:id \"%s\",\n"
                   "    :type \"%s\",\n"
                   "    %s ...,\n"
                   "    ...}\n"
                   "\n"
                   " pattern is used %d time%s in the profile and links out to %d other object%s.")
              type
              dest
              src
              src-type
              src-primary
              dest
              dest-type
              dest-property
              src-indegree
              (if (= 1 src-indegree) "" "s")
              src-outdegree
              (if (= 1 src-outdegree) "" "s")))
    (or (= "Concept" src-type) (= "StatementTemplate" src-type))
    (let [{:keys [src-version dest-version]} value]
      (format (str "Invalid %s identifier:"
                   " %s\n"
                   "\n"
                   " at object:\n"
                   "  {:id \"%s\",\n"
                   "   :type \"%s\",\n"
                   "   :inScheme \"%s\",\n"
                   "   ...}\n"
                   "\n"
                   " linked object:\n"
                   "  {:id \"%s\",\n"
                   "   :type \"%s\",\n"
                   "   :inScheme \"%s\",\n"
                   "   ...}")
              type
              dest
              src
              src-type
              src-version
              dest
              dest-type
              dest-version))
    :else
    ""))

(defn value-str-scc
  "Custom value string for strongly connected component error messages (if a
   digraph has a cycle). Takes the form:
   | Cycle detected in the following nodes:
   |   <identifier>
   |   <identifier>"
  [_ _ _ value]
  (format (str "Cycle detected involving the following nodes:\n"
               "  %s")
          (->> value sort (string/join "\n  "))))

(defn value-str-context
  [_ contexts path value]
  (format (str "Value:\n"
               "%s\n"
               "\n"
               "in context:\n"
               "%s")
          (ppr-str value)
          (ppr-str (->> path butlast (get-in contexts)))))

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
