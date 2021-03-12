(ns com.yetanalytics.pan.errors
  (:require #?(:clj [clojure.core.match :as m]
               :cljs [cljs.core.match :as m])
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [expound.alpha :as exp]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.pan.context :as ctx]
            [com.yetanalytics.pan.identifiers :as id]
            [com.yetanalytics.pan.graph :as g]
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
;; Syntax spec messages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(exp/defmsg ::u/inline-or-iri "cannot contain both linked and inline JSON schema")
(exp/defmsg ::u/related-only-deprecated "related can only be used on deprecated concepts")
(exp/defmsg ::ae/no-recommended-verbs "only allowed on context or result extensions")
(exp/defmsg ::ce/no-recommended-activity-types "only allowed on activity extensions")
(exp/defmsg ::re/no-recommended-activity-types "only allowed on activity extensions")
(exp/defmsg ::act/activityDefinition "invalid activity definition")

(exp/defmsg ::t/type-or-reference "cannot contain both objectActivityType and objectStatementRefTemplate")

(exp/defmsg ::p/pattern-clause "pattern contains too many properties")
(exp/defmsg ::p/is-primary-true "primary is not true")
(exp/defmsg ::p/is-primary-false "primary is not false nor nil")

;; ID spec messages
(exp/defmsg ::id/one-count "the id value is not unique")
(exp/defmsg ::id/in-scheme "the inScheme value is not a valid version ID")

;; Graph spec messages
(exp/defmsg ::g/not-self-loop "object cannot refer to itself")

(exp/defmsg ::c/valid-dest "linked concept does not exist")
(exp/defmsg ::c/relatable-src "should be type: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/relatable-dest "should link to type: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/aext-src "should be type: \"ActivityExtension\"")
(exp/defmsg ::c/crext-src "should be type: \"ContextExtension\" or \"ResultExtension\"")
(exp/defmsg ::c/at-dest "should link to type: \"ActivityType\"")
(exp/defmsg ::c/verb-dest "should link to type: \"Verb\"")
(exp/defmsg ::c/same-concepts "the concepts are not the same type")
(exp/defmsg ::c/same-version "inScheme values do not match")

(exp/defmsg ::t/template-src "should be type: \"StatementTemplate\"")
(exp/defmsg ::t/valid-dest "linked concept or template does not exist")
(exp/defmsg ::t/verb-dest "should link to type: \"Verb\"")
(exp/defmsg ::t/at-dest "should link to type: \"ActivityType\"")
(exp/defmsg ::t/aut-dest "should link to type: \"AttachmentUsageType\"")
(exp/defmsg ::t/template-dest "should link to type: \"StatementTemplate\"")
(exp/defmsg ::t/same-version "inScheme values do not match")

(exp/defmsg ::p/valid-dest "linked template or pattern does not exist")
(exp/defmsg ::p/pattern-src "should be type: \"Pattern\"")
(exp/defmsg ::p/pattern-dest "should link to type: \"Pattern\"")
(exp/defmsg ::p/template-dest "should link to type: \"StatementTemplate\"")
(exp/defmsg ::p/non-opt-dest "alternate pattern cannot contain an optional or zeroOrMore pattern")
(exp/defmsg ::p/singleton-src "primary sequence pattern has multiple links")
(exp/defmsg ::p/not-singleton-src "sequence pattern must have at least two links")
(exp/defmsg ::p/primary-pattern "pattern is not primary")
(exp/defmsg ::p/zero-indegree-src "pattern must not be used elsewhere")

(exp/defmsg ::p/singleton-scc "cyclical reference detected")

;; Context errors
(exp/defmsg ::ctx/simple-term-def "simple term definition does not have valid prefix")
(exp/defmsg ::ctx/expanded-term-def "expanded term definition does not have valid prefix")

(exp/defmsg ::ctx/contexed-key "key cannot be expanded into absolute IRI")
(exp/defmsg ::ctx/is-at-context "key is not JSON-LD keyword")

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

(defn- pluralize
  "Make the word plural based off of a count if needed, by adding a plural s
  at the end."
  [word cnt]
  (if (not= 1 cnt) (str word "s") word))

;; Needed to get around Issue #110 in Expound
(defn- default-printer
  "Return a default printer with :show-valid-values? and :print-specs? both
  preset to false. Also truncate lines that only contain elipses."
  [explain-data]
  (let [unformed-str
        (with-out-str ;; Pretty hacky, but it works
          ((exp/custom-printer {:show-valid-values? false
                                :print-specs? false}) explain-data))
        formed-str
        (string/replace unformed-str #"(?<=\n)\s+\.\.\.\n" "")]
    formed-str))

;; TODO Current default-printer function is quite unhelpful in locating errors.
;; May look into this again.
#_(defn value-str-def
    "Custom value string for syntax validation error messages. Takes the form:
  > Invalid value: <value>
  > At path: profile -> <key1> -> <key2>"
    [_ form path value]
    (let [tag (if (-> path peek int?)
                (-> path pop peek name (string/split #"s") first keyword)
                (peek path))
          path-arr (mapv strv path)
          val-str (if (map? value)
                    (-> value pr-str (string/replace #"(?<!\\), " ",\n   "))
                    (pr-str value))]
      (str tag " " val-str "\n"
           "  at path: " (string/join " > " path-arr))))

(defn value-str-id
  "Custom value string for duplicate ID error messages. Takes the form:
  > Duplicate id: <identifier>
  >  with count: <int>"
  [_ _ path value]
  (str "Duplicate id: " (-> path last strv) "\n"
       " with count: " (strv value)))

(defn value-str-ver
  "Custom value string for inScheme error messages. Takes the form:
  > Invalid inScheme: <in-scheme>
  >  at object: <identifier>
  >  profile version ids:
  >   <version-id-1>
  >   <version-id-2>
  >   ..."
  [_ _ _ value]
  (str "Invalid inScheme: " (-> value :inScheme strv) "\n"
       " at object: " (-> value :id strv) "\n"
       " profile version ids:\n  "
       (->> value :version-ids sequence sort reverse (string/join "\n  "))))

#_{:clj-kondo/ignore [:unresolved-symbol]} ; kondo doesn't recognize core.match
(defn value-str-edge
  "Custom value string for IRI link error messages. Takes the form:
  > Invalid <property> identifier:
  >  <identifier>
  > at object:
  >  <object map>
  > linked object:
  >  <object map>"
  [_ _ _ value]
  (let [attrs-list
        (m/match [value]
          ;; Patterns
          [{:src-primary   src-primary
            :src-indegree  src-indegree
            :src-outdegree src-outdegree
            :dest-property dest-property}]
          (str " at object:\n"
               "  {:id " (-> value :src strv) ",\n"
               "   :type " (-> value :src-type strv) ",\n"
               "   :primary " (strv src-primary) ",\n"
               "   ...}\n"
               "\n"
               " linked object:\n"
               "   {:id " (-> value :dest strv) ",\n"
               "    :type " (-> value :dest-type strv) ",\n"
               "    :" (strv dest-property) " ...,\n"
               "    ...}\n"
               "\n"
               ;; Add text that says:
               ;; "pattern is used # time(s) in the profile
               ;; and links out to # other object(s)."
               " pattern is used " (strv src-indegree) " "
               (pluralize "time" src-indegree) " in the profile\n"
               " and links out to " (strv src-outdegree) " other "
               (pluralize "object" src-outdegree) ".")
          ;; Concepts and Templates
          [{:src-version  src-version
            :dest-version dest-version}]
          (str " at object:\n"
               "  {:id " (-> value :src strv) ",\n"
               "   :type " (-> value :src-type strv) ",\n"
               "   :inScheme " (strv src-version) ",\n"
               "   ...}\n"
               "\n"
               " linked object:\n"
               "  {:id " (-> value :dest strv) ",\n"
               "   :type " (-> value :dest-type strv) ",\n"
               "   :inScheme " (strv dest-version) ",\n"
               "   ...}")
          ;; Shouldn't happen but just in case
          :else "")]
    (str "Invalid " (-> value :type strv) " identifier:\n"
         " " (-> value :dest strv) "\n"
         "\n" attrs-list)))

(defn value-str-scc
  "Custom value string for strongly connected component error messages (if a
  digraph has a cycle). Takes the form:
  > Cycle detected in the following nodes:
  >   <identifier>
  >   <identifier>"
  [_ _ _ value]
  (str "Cycle detected involving the following nodes:\n  "
       (->> value sort (string/join "\n  "))))

;; TODO Possibly make custom error messages for @context errors?
(defn custom-printer
  "Returns a printer based on the error-type argument. A nil error-type will
  result in the default Expound printer (except with :print-specs? set to
  false). error-types:
  - id: Duplicate ID errors
  - in-scheme: InScheme property errors
  - edge: Concept, Template and Pattern link errors
  - cycle: Pattern cycle errors
  - no arg: Basic syntax + @context validation"
  [& [error-type]]
  (let [error-type (if (nil? error-type) :else error-type)]
    (case error-type
      :id
      (exp/custom-printer {:value-str-fn value-str-id :print-specs? false})
      :in-scheme
      (exp/custom-printer {:value-str-fn value-str-ver :print-specs? false})
      :edge
      (exp/custom-printer {:value-str-fn value-str-edge :print-specs? false})
      :cycle
      (exp/custom-printer {:value-str-fn value-str-scc :print-specs? false})
      :else
      #_default-printer
      (exp/custom-printer {:show-valid-values? false :print-specs? false})
      #_(exp/custom-printer {:value-str-fn value-str-def :print-specs? false}))))

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
           concept-errors
           template-errors
           pattern-errors
           pattern-cycle-errors
           context-errors
           context-key-errors]}]
  (when syntax-errors
    (expound-error syntax-errors "Syntax Errors"))
  (when id-errors
    (expound-error id-errors "ID Errors" :id))
  (when in-scheme-errors
    (expound-error in-scheme-errors "Version Errors" :in-scheme))
  (when concept-errors
    (expound-error concept-errors "Concept Edge Errors" :edge))
  (when template-errors
    (expound-error template-errors "Template Edge Errors" :edge))
  (when pattern-errors
    (expound-error pattern-errors "Pattern Edge Errors" :edge))
  (when pattern-cycle-errors
    (expound-error pattern-cycle-errors "Cycle Errors" :cycle))
  (when context-errors
    (expound-error context-errors "Context Errors"))
  (when context-key-errors
    (expound-error context-key-errors "Context Key Errors")))
