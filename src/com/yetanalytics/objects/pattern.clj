(ns com.yetanalytics.objects.pattern
  (:require [clojure.spec.alpha :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as algo]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.axioms :as ax]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Patterns 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Basic properties

(s/def ::id ::ax/uri)
(s/def ::type #{"Pattern"})
(s/def ::primary ::ax/boolean)
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

(s/def ::alternates (s/coll-of ::ax/iri :type vector? :min-count 2))
(s/def ::optional (s/keys :req-un [::id]))
(s/def ::one-or-more (s/keys :req-un [::id]))
(s/def ::sequence (s/coll-of ::ax/iri :type vector? :min-count 1))
(s/def ::zero-or-more (s/keys :req-un [::id]))

;; Check if primary is true or false

(s/def ::is-primary-true
  (fn [p] (:primary p)))

(s/def ::is-primary-false
  (fn [p] (not (:primary p))))

;; Ensure that only one of the five regex properties are included in pattern.
;; Including two or more properties should fail the spec. 
(s/def ::pattern-clause
  (fn [p]
    (let [alt? (contains? p :alternates)
          opt? (contains? p :optional)
          oom? (contains? p :one-or-more)
          sqn? (contains? p :sequence)
          zom? (contains? p :zero-or-more)]
      (cond
        alt? (not (or opt? oom? sqn? zom?))
        opt? (not (or alt? oom? sqn? zom?))
        oom? (not (or alt? opt? sqn? zom?))
        sqn? (not (or alt? opt? oom? zom?))
        zom? (not (or alt? opt? oom? sqn?))))))

(s/def ::primary-pattern
  (s/and (s/keys :req-un [::id ::type ::pref-label ::definition ::primary]
                 :opt-un [::in-scheme ::deprecated ::alternates ::optional
                          ::one-or-more ::sequence ::zero-or-more])
         ::pattern-clause
         ::is-primary-true))

(s/def ::reg-pattern
  (s/and (s/keys :req-un [::id ::type]
                 :opt-un [::primary ::in-scheme ::pref-label ::definition
                          ::deprecated ::alternates ::optional ::one-or-more
                          ::sequence ::zero-or-more])
         ::pattern-clause
         ::is-primary-false))

(s/def ::pattern
  (s/or :no-primary ::reg-pattern
        :primary ::primary-pattern))

(s/def ::patterns
  (s/coll-of ::pattern :kind vector? :min-count 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::pattern-basic
  (fn [{:keys [object]}] (s/valid? ::pattern object)))

;; Get the IRIs of a Pattern, depending on its property
(defmulti get-iris (fn [p] (keys (dissoc p :id :type :pref-label :definition
                                         :primary :in-scheme :deprecated))))

(defmethod get-iris '(:alternates) [p] (:alternates p))
(defmethod get-iris '(:sequence) [p] (:sequence p))
(defmethod get-iris '(:optional) [p] [(:id (:optional p))])
(defmethod get-iris '(:one-or-more) [p] [(:id (:one-or-more p))])
(defmethod get-iris '(:zero-or-more) [p] [(:id (:zero-or-more p))])
(defmethod get-iris :default [_] nil)

(defn pattern-graph [patterns-table]
  "Create a graph from a table between pattern IDs and patterns."
  (let [adjacency-map
        (reduce-kv (fn [m id pattern]
                     (assoc m id (get-iris pattern)))
                   {} patterns-table)]
    (uber/digraph adjacency-map)))

;; MUST be valid IRIs that point to Templates and Patterns
(s/def ::valid-iris
  (fn [{:keys [object templates-table patterns-table]}]
    (every? #(or (contains? templates-table %)
                 (contains? patterns-table %)) (get-iris object))))

;; MUST NOT put optinal or zeroOrMore directly inside alternates
(s/def ::no-zero-nests
  (fn [{:keys [object patterns-table]}]
    (if (contains? object :alternates)
      (every? #(not (or (contains? (patterns-table %) :optional)
                        (contains? (patterns-table %) :zero-or-more)))
              (get-iris object))
      true)))

;; MUST include at least two members of sequence, unless
;; 1. The member of sequence is a single Statement Template
;; 2. The Pattern is a primary pattern
;; 3. The Pattern is not used elsewhere 
(s/def ::min-sequence-count
  (fn [{:keys [object templates-table patterns-table patterns-graph]}]
    (if (contains? object :sequence)
      (let [seq-list (:sequence object)]
        (or (<= 2 (count seq-list))
            (and (contains? templates-table (first seq-list))
                 (true? (:primary object))
                 (= 0 (uber/in-degree patterns-graph (:id object))))))
      true)))

;; MUST NOT include any Pattern within itself or any Pattern in it.
;; In other words, no cycles.
(s/def ::no-cycles
  (fn [args-map]
    (let [pgraph (:patterns-graph (first args-map))]
      (algo/dag? pgraph))))

(s/def ::pattern+
  (s/and ::pattern-basic
         ::valid-iris
         ::no-zero-nests
         ::min-sequence-count))

(s/def ::patterns+
  (s/and
   (s/coll-of ::pattern+ :kind vector? :min-count 1)
   ::no-cycles))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; validation which requires external calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: MAY re-use Statement Templates and Patterns from other Profiles
