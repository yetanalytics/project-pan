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

(defn pluralize
  "Make the word plural based off of a count if needed, by adding a plural s
  at the end."
  [word cnt]
  (if (not= 1 cnt) (str word "s") word))

;; Needed to get around Issue #110 in Expound
(defn default-printer
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
      "id"
      (exp/custom-printer {:value-str-fn value-str-id :print-specs? false})
      "in-scheme"
      (exp/custom-printer {:value-str-fn value-str-ver :print-specs? false})
      "edge"
      (exp/custom-printer {:value-str-fn value-str-edge :print-specs? false})
      "cycle"
      (exp/custom-printer {:value-str-fn value-str-scc :print-specs? false})
      :else
      default-printer
      #_(exp/custom-printer {:value-str-fn value-str-def :print-specs? false}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property ordering in error messages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def property-order
  {;; Profile metadata
   :id          0
   :type        1
   :inScheme    2
   :_context    3
   :conformsTo  4
   :prefLabel   5
   :definition  6
   :deprecated  7
   :author      8
   :versions    9
   :concepts    10
   :templates   11
   :patterns    12
   ;; Profile version objects
   :generatedAtTime 13
   :wasRevisionOf   14
   ;; Profile author objects
   :name  15
   :url   16
   ;; Concept properties
   :broader      17
   :broadMatch   18
   :narrower     19
   :narrowMatch  20
   :related      21
   :relatedMatch 22
   :exactMatch   23
   ;; Extension and Resource properties
   :recommendedActivityTypes  24
   :recommendedVerbs          25
   :context                   26
   :contentType               27
   :schema                    28
   :inlineSchema              29
   ;; Activity properties
   :activityDefinition        30
   :description               31
   :moreInfo                  32
   :interactionType           33
   :correctResponsesPattern   34
   :choices     35
   :scale       36
   :source      37
   :target      38
   :steps       39
   :extensions  40
   ;; Statement Template properties
   :verb                          41
   :objectActivityType            42
   :contextGroupingActivityType   43
   :contextParentActivityType     44
   :contextOtherActivityType      45
   :contextCategoryActivityType   46
   :attachmentUsageType           47
   :objectStatementRefTemplate    48
   :contextStatementRefTemplate   49
   :rules                         50
   ;; Statement Template rules properties
   :location  51
   :selector  52
   :presence  53
   :any       54
   :all       55
   :none      56
   :scopeNote 57
   ;; Pattern properties
   :primary    58
   :alternates 59
   :optional   60
   :oneOrMore  61
   :sequence   62
   :zeroOrMore 63})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Error map manipulation functions
;;
;; XXX: Expound can act pretty funny when it comes to s/keys and s/coll-of
;; specs. See Issue #165: Improve grouping of spec errors on Expound's Github.
;; This is why we need the functions expound-error-list and expound-error-map
;; and the helper functions in this section.
;;
;; TODO: Issue #165 has been marked as a bug so it should be fixed at some
;; point. When that happens, rework this namespace to rely on Expound's native
;; error grouping functionality so it doesn't have to do awkward map manips.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compare-properties
  "Get the order of two properties based on how it was listed in the xAPI
  Profile spec. Properties not listed in the property-order map will be pushed
  to the end of the error list and sorted alphabetically.
  - neg number: prop1 comes before prop2
  - pos number: prop1 comes after prop2
  - zero: prop1 and prop2 are equal"
  [prop1 prop2]
  (let [order1 (get property-order prop1)
        order2 (get property-order prop2)]
    (cond
      (and (nil? prop1) (nil? prop2)) 0
      (and (nil? prop1) (some? prop2)) -1
      (and (some? prop1) (nil? prop2)) 1
      (and (some? order1) (nil? order2)) -1
      (and (nil? order1) (some? order2)) 1
      (and (some? order1) (some? order2)) (compare order1 order2)
      (and (nil? order1) (nil? order2)) (compare prop1 prop2))))

(defn compare-arrs
  "Compare two arrays of keywords (or strings), such that lexiographical order
  of corresponding entries takes precedence over array length.
  The conventional compares array length first, then lexicographical order.
  - Sort using compare-arrs: ([:a] [:a :b] [:z])
  - Sort using regular compare: ([:a] [:z] [:a :b])"
  [arr1 arr2]
  (loop [a1 arr1
         a2 arr2]
    (if (and (empty? a1) (empty? a2))
      0
      (let [comp-int (compare-properties (first a1) (first a2))]
        (if (= 0 comp-int)
          (recur (rest a1) (rest a2))
          comp-int)))))

(defn group-by-in
  "Splits up a spec error map by the :in path of an ::s/problems map, which
  lists the keys that lead to the value in a map data structure. For example,
  an invalid version ID would have the path ::v/version ::v/id. Returns a
  sequence of spec error maps (each with ::s/problems, ::s/spec and ::s/value
  keys) group together by :in paths.

  This is required because otherwise Expound will group together random spec
  errors together in ways that don't make sense. By doing this, we group spec
  errors by where they occur in the data structure."
  [error-map]
  (let [{problems ::s/problems spec ::s/spec value ::s/value} error-map
        problems-map (group-by :in problems)]
    (reduce-kv (fn [err-list _ v]
                 (conj err-list (assoc {}
                                       ::s/problems (if (vector? v)
                                                      (sequence v) (list v))
                                       ::s/spec spec
                                       ::s/value value))) (list) problems-map)))

(defn sort-by-path
  "Sort a sequence of spec error maps by the value of the :path key, which is
  a list of all keywords that lead up to the final spec. (Unlike :key, this
  include tagged portions of specs, eg. of s/or.) Useful for keeping specs
  in the same area grouped together"
  [error-map-list]
  (sort-by #(-> % ::s/problems first :path) compare-arrs error-map-list))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Expounding functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn expound-error
  "Print an Expounded error message using a custom printer. The custom printer
  is determined using the error-type arg; if not supplied, it prints a default
  Expound error message (without the Relevant Specs trace)."
  [error-map & {:keys [error-type silent]}]
  (when (not silent)
    (let [print-fn (custom-printer error-type)]
      (print-fn error-map))))

(defn expound-error-list
  "Sort a list of spec error maps using the value of the :path key."
  [error-list & {:keys [error-type silent]}]
  (mapv (fn [error]
          (let [error-text (expound-error error
                                          :error-type error-type
                                          :silent silent)
                error-path (-> error
                               ::s/problems
                               first
                               :path)]
            (when error-text
              (println error-text))
            {:path error-path
             :text error-text}))
        error-list))

(defn expound-error-map
  "Regroup an error map into a sorted list of error maps.
  Used to avoid Issue #165 for Expound."
  [error-map & {:keys [error-type silent]}]
  (-> error-map
      group-by-in
      sort-by-path
      (expound-error-list :error-type error-type
                          :silent silent)))

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
           context-key-errors
           silent]}]
  (cond-> {}
    (some? syntax-errors)
    (assoc :syntax-errors (expound-error-map syntax-errors
                                             :silent silent))
    (some? id-errors)
    (assoc :id-errors (expound-error id-errors
                                     :error-type "id"
                                     :silent silent))
    (some? in-scheme-errors)
    (assoc :in-scheme-errors (expound-error in-scheme-errors
                                            :error-type "in-scheme"
                                            :silent silent))
    (some? concept-errors)
    (assoc :concept-errors (expound-error-map concept-errors
                                              :error-type "edge"
                                              :silent silent))
    (some? template-errors)
    (assoc :template-errors (expound-error-map template-errors
                                               :error-type "edge"
                                               :silent silent))
    (some? pattern-errors)
    (assoc :pattern-errors (expound-error-map pattern-errors
                                              :error-type "edge"
                                              :silent silent))
    (some? pattern-cycle-errors)
    (assoc :pattern-cycle-errors (expound-error pattern-cycle-errors
                                                :error-type "cycle"
                                                :silent silent))
    ;; Context errors are already in list format
    (some? context-errors)
    (assoc :context-errors (expound-error-list context-errors
                                               :silent silent))
    (some? context-key-errors)
    (assoc :context-key-errors (expound-error-list context-key-errors
                                                   :silent silent))))