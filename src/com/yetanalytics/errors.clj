(ns com.yetanalytics.errors
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as exp]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.identifiers :as id]
            [com.yetanalytics.graph :as g]
            [com.yetanalytics.objects.concept :as c]
            [com.yetanalytics.objects.template :as t]
            [com.yetanalytics.objects.pattern :as p]
            [com.yetanalytics.objects.concepts.extensions.activity :as ae]
            [com.yetanalytics.objects.concepts.extensions.context :as ce]
            [com.yetanalytics.objects.concepts.extensions.result :as re]
            [com.yetanalytics.objects.concepts.activities :as act]
            [com.yetanalytics.util :as u]
            [xapi-schema.spec :as xs]))

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
(exp/defmsg ::c/relatable-src "concept should be: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/relatable-dest "should link to: \"ActivityType\", \"AttachmentUsageType\" or \"Verb\"")
(exp/defmsg ::c/aext-src "concept should be: \"ActivityExtension\"")
(exp/defmsg ::c/crext-src "concept should be: \"ContextExtension\" or \"ResultExtension\"")
(exp/defmsg ::c/at-dest "should link to: \"ActivityType\"")
(exp/defmsg ::c/verb-dest "should link to: \"Verb\"")
(exp/defmsg ::c/same-concepts "the concepts are not of the same type")
(exp/defmsg ::c/same-version "the concepts do not share the same version")

(exp/defmsg ::t/template-src "template should be: \"StatementTemplate\"")
(exp/defmsg ::t/valid-dest "linked object does not exist")
(exp/defmsg ::t/verb-dest "should link to: \"Verb\"")
(exp/defmsg ::t/at-dest "should link to: \"ActivityType\"")
(exp/defmsg ::t/aut-dest "should link to: \"AttachmentUsageType\"")
(exp/defmsg ::t/template-dest "should link to: \"StatementTemplate\"")
(exp/defmsg ::t/same-version "inScheme version IDs differ")

(exp/defmsg ::p/valid-dest "linked object does not exist")
(exp/defmsg ::p/pattern-src "pattern should be: \"Pattern\"")
(exp/defmsg ::p/pattern-dest "should link to: \"Pattern\"")
(exp/defmsg ::p/template-dest "should link to:\"StatementTemplate\"")
(exp/defmsg ::p/non-opt-dest "alternate pattern cannot directly contain an optional or zeroOrMore pattern")
(exp/defmsg ::p/singleton-src "pattern cannot contain multiple links")
(exp/defmsg ::p/not-singleton-src "pattern can only contain one link")
(exp/defmsg ::p/primary-pattern "pattern is not primary")
(exp/defmsg ::p/zero-indegree-src "pattern must not be used elsewhere")

(exp/defmsg ::p/singleton-scc "pattern is involved in a cyclical reference")

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
   :broader 17
   :broadMatch 18
   :narrower 19
   :narrowMatch 20
   :related 21
   :relatedMatch 22
   :exactMatch 23
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
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compare-properties
  "Get the order of two properties based on how it was listed in the xAPI 
  Profile spec. Properties not listed in the property-order map will be pushed 
  to the end of the error list and sorted alphabetically.
  - neg number: prop1 comes before prop2
  - pos number: prop1 comes after prop2
  - zero: prop1 and prop2 are equal; arbitrarily put prop1 before prop2"
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
    (reduce-kv (fn [err-list k v]
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

(defn expound-error [error-map]
  (let [print-fn (exp/custom-printer {:print-specs? false})]
    (print-fn error-map)))

(defn expound-error-list
  [error-list]
  (doseq [error error-list]
    (do (expound-error error) (println))))

(defn expound-error-map
  [error-map]
  (-> error-map group-by-in sort-by-path expound-error-list))

(defn expound-errors
  [{:keys [syntax-errors]}]
  (do
    (if (some? syntax-errors) (expound-error-map syntax-errors))))
