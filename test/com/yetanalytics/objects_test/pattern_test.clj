(ns com.yetanalytics.objects-test.pattern-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.objects.pattern :as pattern]))

(deftest id-test
  (testing "id property"
    (should-satisfy ::pattern/id
                    "https://w3id.org/xapi/catch/patterns#view-rubric")
    (should-not-satisfy ::pattern/id "what the pineapple")))

(deftest type-test
  (testing "type property"
    (should-satisfy ::pattern/type "Pattern")
    (should-not-satisfy ::pattern/type "pattern")
    (should-not-satisfy ::pattern/type "stan loona")))

(deftest primary-test
  (testing "primary property"
    (should-satisfy+ ::pattern/primary
                     true
                     false
                     :bad
                     "true"
                     "false"
                     74
                     nil)))

(deftest alternates-test
  (testing "alternates (A | B) property"
    (should-satisfy ::pattern/alternates
                    ["https://w3id.org/xapi/catch/templates#lesson-plan-upload"
                     "https://w3id.org/xapi/catch/templates#lesson-plan-design"])
    (should-not-satisfy ::pattern/alternates
                        ["https://w3id.org/xapi/catch/templates#lesson-plan-upload"])
    (should-not-satisfy ::pattern/alternates
                        "https://w3id.org/xapi/catch/templates#lesson-plan-upload")
    (should-not-satisfy ::pattern/alternates [])))

(deftest sequence-test
  (testing "sequence (ABC) property"
    (should-satisfy ::pattern/sequence
                    ["https://w3id.org/xapi/catch/patterns#f1-1-01-iteration"
                     "https://w3id.org/xapi/catch/templates#system-notification-completion"
                     "https://w3id.org/xapi/catch/templates#activity-completion"])
    (should-not-satisfy ::pattern/sequence
                        "https://w3id.org/xapi/catch/patterns#f1-1-01-iteration")
    (should-not-satisfy ::pattern/sequence [])))

(deftest optional-test
  (testing "optional (A?) property"
    (should-satisfy ::pattern/optional
                    {:id "https://w3id.org/xapi/catch/templates#view-rubric"})
    (should-not-satisfy ::pattern/optional
                        {:foo "https://w3id.org/xapi/catch/templates#view-rubric"})
    (should-not-satisfy ::pattern/optional
                        "https://w3id.org/xapi/catch/templates#view-rubric")
    (should-not-satisfy ::pattern/optional
                        ["https://w3id.org/xapi/catch/templates#view-rubric"])
    (should-not-satisfy ::pattern/optional "")))

(deftest one-or-more-test
  (testing "oneOrMore (A+) property"
    (should-satisfy ::pattern/one-or-more
                    {:id "https://w3id.org/xapi/catch/patterns#f1-2-01-scored"})
    (should-not-satisfy ::pattern/one-or-more
                        {:foo "https://w3id.org/xapi/catch/patterns#f1-2-01-scored"})
    (should-not-satisfy ::pattern/one-or-more
                        "https://w3id.org/xapi/catch/patterns#f1-2-01-scored")
    (should-not-satisfy ::pattern/one-or-more
                        ["https://w3id.org/xapi/catch/patterns#f1-2-01-scored"])
    (should-not-satisfy ::pattern/one-or-more "")))

(deftest zero-or-more-test
  (testing "zeroOrMore (A*) property"
    (should-satisfy ::pattern/zero-or-more
                    {:id "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"})
    (should-not-satisfy ::pattern/zero-or-more
                        {:foo "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"})
    (should-not-satisfy ::pattern/zero-or-more
                        "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete")
    (should-not-satisfy ::pattern/zero-or-more
                        ["https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"])
    (should-not-satisfy ::pattern/zero-or-more "")))

(deftest pattern-clause-test
  (testing "ensure that only one of the five regex properties are included"
    (should-satisfy+ ::pattern/pattern-clause
                     {:alternates "foo"}
                     {:optional "foo"}
                     {:one-or-more "foo"}
                     {:sequence "foo"}
                     {:zero-or-more "foo"}
                     :bad
                     ;; 32 possible combos in total - impractical to test all
                     {}
                     {:alternates "foo" :optional "bar"}
                     {:optional "foo" :one-or-more "bar"}
                     {:one-or-more "foo" :sequence "bar"}
                     {:sequence "foo" :zero-or-more "bar"}
                     {:alternates "foo" :optional "baz" :one-or-more "bar"}
                     {:optional "foo" :one-or-more "baz" :zero-or-more "bar"}
                     {:alternates "foo" :optional "baz" :one-or-more "goop"
                      :sequence "durr" :zero-or-more "bar"})))

(deftest primary-pattern-test
  (testing "primary pattern"
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/catch/patterns#system-progress-response"
                   :type "Pattern"
                   :primary true
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "system response progress"}
                   :definition {"en" "System responses to progress within an activity"}
                   :sequence ["https://w3id.org/xapi/catch/templates#system-notification-submission"
                              "https://w3id.org/xapi/catch/templates#system-notification-progression"]}))
    ; Patterns require one regex rule
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/catch/patterns#pattern"
                        :type "Pattern"
                        :primary true
                        :in-scheme "https://w3id.org/xapi/catch/v1"
                        :pref-label {"en" "pattern"}
                        :definition {"en" "pattern definition"}})))
    ; Patterns cannot have two or more regex rules
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/catch/patterns#pattern"
                        :type "Pattern"
                        :primary true
                        :in-scheme "https://w3id.org/xapi/catch/v1"
                        :pref-label {"en" "pattern"}
                        :definition {"en" "pattern definition"}
                        :sequence ["https://w3id.org/xapi/catch/templates#one"
                                   "https://w3id.org/xapi/catch/templates#two"]
                        :alternates ["https://w3id.org/xapi/catch/templates#three"
                                     "https://w3id.org/xapi/catch/templates#four"]})))
    ; Primary patterns require prefLabel and definition
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/minimal/pattern"
                        :type "Pattern"
                        :primary true
                        :in-scheme "https://w3id.org/xapi/catch/v1"
                        :optional {:id "https://w3id.org/xapi/catch/templates#view-rubric"}})))))

(deftest non-primary-pattern-test
  (testing "non-primary pattern"
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/catch/patterns#view-rubric"
                   :type "Pattern"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :pref-label {"en" "view rubric"}
                   :definition {"en" "This is a pattern for someone looking at a rubric"}
                   :optional {:id "https://w3id.org/xapi/catch/templates#view-rubric"}}))
    ; Remove properties that are optional in non-primary patterns
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/minimal/pattern"
                   :type "Pattern"
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :optional {:id "https://w3id.org/xapi/catch/templates#view-rubric"}}))
    ; Add the "primary" property (set to false)
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/minimal/pattern"
                   :type "Pattern"
                   :primary false
                   :in-scheme "https://w3id.org/xapi/catch/v1"
                   :optional {:id "https://w3id.org/xapi/catch/templates#view-rubric"}}))
    ; Pattern require one regex rule
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/minimal/pattern"
                        :type "Pattern"
                        :in-scheme "https://w3id.org/xapi/catch/v1"})))
    ; Patterns cannot have two or more regex rules
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/minimal/pattern"
                        :type "Pattern"
                        :in-scheme "https://w3id.org/xapi/catch/v1"
                        :optional {:id "https://w3id.org/xapi/catch/templates#one"}
                        :zero-or-more {:id "https://w3id.org/xapi/catch/templates#two"}})))))

;; Test semi-strict validation spects

(deftest in-scheme-valid-test
  (testing "inScheme, if present, MUST be a version ID"
    (is (s/valid? ::pattern/in-scheme-valid?
                  {:object {:in-scheme
                            "https://foo.org/version1"}
                   :vid-set {"https://foo.org/version1"
                             "https://foo.org/version2"}}))
    (is (not (s/valid? ::pattern/in-scheme-valid?
                       {:object {:in-scheme "https://foo.org/version0"}
                        :vid-set {"https://foo.org/version1"
                                  "https://foo.org/version2"}})))
    (is (s/valid? ::pattern/in-scheme-valid?
                  {:object {:primary false}
                   :vid-set {"https://foo.org/version1"
                             "https://foo.org/version2"}}))))

(def templates-map {"https://foo.org/statement-one"
                    {:id "https://foo.org/statement-one"
                     :type "StatementTemplate"}
                    "https://foo.org/statement-two"
                    {:id "https://foo.org/statement-two"
                     :type "StatementTemplate"}
                    "https://foo.org/statement-three"
                    {:id "https://foo.org/statement-three"
                     :type "StatementTemplate"}
                    "https://foo.org/statement-four"
                    {:id "https://foo.org/statement-four"
                     :type "StatementTemplate"}
                    "https://foo.org/statement-five"
                    {:id "https://foo.org/statement-five"
                     :type "StatementTemplate"}})

(def patterns-map
  {"https://foo.org/primary-pattern-one"
   {:id "https://foo.org/primary-pattern-one"
    :type "Pattern"
    :primary true
    :alternates ["https://foo.org/not-primary-one"
                 "https://foo.org/primary-pattern-two"]}
   "https://foo.org/primary-pattern-two"
   {:id "https://foo.org/primary-pattern-two"
    :type "Pattern"
    :primary true
    :sequence ["https://foo.org/not-primary-three"
               "https://foo.org/not-primary-four"]}
   "https://foo.org/primary-pattern-three"
   {:id "https://foo.org/primary-pattern-three"
    :type "Pattern"
    :primary true
    :one-or-more {:id "https://foo.org/not-primary-five"}}
   "https://foo.org/primary-pattern-four"
   {:id "https://foo.org/primary-pattern-four"
    :type "Pattern"
    :primary true
    :zero-or-more {:id "https://foo.org/not-primary-five"}}
   "https://foo.org/primary-pattern-five"
   {:id "https://foo.org/primary-pattern-five"
    :type "Pattern"
    :primary true
    :optional {:id "https://foo.org/not-primary-five"}}
   "https://foo.org/primary-pattern-six"
   {:id "https://foo.org/primary-pattern-six"
    :type "Pattern"
    :primary true
    :sequence ["https://foo.org/not-primary-four"
               "https://foo.org/statement-one"]}
   "https://foo.org/not-primary-one"
   {:id "https://foo.org/not-primary-one"
    :type "Pattern"
    :primary false
    :alternates ["https://foo.org/statement-one"]}
   "https://foo.org/not-primary-two"
   {:id "https://foo.org/not-primary-two"
    :type "Pattern"
    :primary false
    :sequence ["https://foo.org/statement-two"
               "https://foo.org/statement-three"]}
   "https://foo.org/not-primary-three"
   {:id "https://foo.org/not-primary-three"
    :type "Pattern"
    :primary false
    :optional {:id "https://foo.org/statement-three"}}
   "https://foo.org/not-primary-four"
   {:id "https://foo.org/not-primary-four"
    :type "Pattern"
    :primary false
    :one-or-more {:id "https://foo.org/statement-four"}}
   "https://foo.org/not-primary-five"
   {:id "https://foo.org/not-primary-five"
    :type "Pattern"
    :primary false
    :zero-or-more {:id "https://foo.org/statement-five"}}})

(deftest valid-iris-test
  (testing "IRIs MUST point to StatementTemplates or Patterns"
    (is (s/valid? ::pattern/valid-iris
                  {:object
                   {:id "https://foo.org/primary-pattern-one"
                    :type "Pattern"
                    :primary true
                    :alternates ["https://foo.org/not-primary-one"
                                 "https://foo.org/not-primary-two"]}
                   :patterns-table patterns-map
                   :templates-table templates-map}))
    (is (not (s/valid? ::pattern/valid-iris
                       {:object
                        {:id "https://foo.org/primary-pattern-one"
                         :type "Pattern"
                         :primary true
                         :optional {:id "https://foo.org/not-in-maps"}}
                        :patterns-table patterns-map
                        :templates-table templates-map})))))

(deftest no-zero-nests-test
  (testing "MUST NOT put optional or zerOrMore directly inside alternates"
    (is (not (s/valid? ::pattern/no-zero-nests
                       {:object
                        {:id "https://foo.org/primary-pattern-one"
                         :type "Pattern"
                         :primary true
                         :alternates ["https://foo.org/not-primary-three"]}
                        :patterns-table patterns-map})))
    (is (not (s/valid? ::pattern/no-zero-nests
                       {:object
                        {:id "https://foo.org/primary-pattern-one"
                         :type "Pattern"
                         :primary true
                         :alternates ["https://foo.org/not-primary-five"]}
                        :patterns-table patterns-map})))
    (is (s/valid? ::pattern/no-zero-nests
                  {:object
                   {:id "https://foo.org/primary-pattern-one"
                    :type "Pattern"
                    :primary true
                    :alternates ["https://foo.org/not-primary-four"]}
                   :patterns-table patterns-map}))
    (is (s/valid? ::pattern/no-zero-nests
                  {:object
                   {:id "https://foo.org/primary-pattern-one"
                    :type "Pattern"
                    :primary true
                    :sequence ["https://foo.org/not-primary-three"]}
                   :patterns-table patterns-map}))))

(def pgraph (pattern/pattern-graph patterns-map))

(deftest min-sequence-count-test
  (testing "MUST include at least two members of sequence, unless sequence
           consists of a single Template in a primary pattern not use elsewhere"
    ;; Pattern MUST be primary
    (is (not (s/valid? ::pattern/min-sequence-count
                       {:object
                        {:id "https://foo.org/not-primary-pattern"
                         :type "Pattern"
                         :primary false
                         :sequence ["https://foo.org/statement-two"]}
                        :templates-table templates-map
                        :patterns-table patterns-map
                        :patterns-graph pgraph})))
    ;; Sequence MUST contain a statement template
    (is (not (s/valid? ::pattern/min-sequence-count
                       {:object
                        {:id "https://foo.org/primary-pattern"
                         :type "Pattern"
                         :primary true
                         :sequence ["https://foo.org/primary-pattern-three"]}
                        :templates-table templates-map
                        :patterns-table patterns-map
                        :patterns-graph pgraph})))
    ;; Sequence MUST NOT be used elsehwere in the graph
    (is (not (s/valid? ::pattern/min-sequence-count
                       {:object
                        {:id "https://foo.org/primary-pattern-two"
                         :type "Pattern"
                         :primary true
                         :sequence ["https://foo.org/statement-two"]}
                        :templates-table templates-map
                        :patterns-table patterns-map
                        :patterns-graph pgraph})))
    ;; Valid pattern
    (is (s/valid? ::pattern/min-sequence-count
                  {:object
                   {:id "https://foo.org/primary-pattern-six"
                    :type "Pattern"
                    :primary true
                    :sequence ["https://foo.org/statement-one"]}
                   :templates-table templates-map
                   :patterns-table patterns-map
                   :patterns-graph pgraph}))
    ;; Moot point if sequence has two or more IRIs
    (is (s/valid? ::pattern/min-sequence-count
                  {:object
                   {:id "https://foo.org/not-primary-two"
                    :type "Pattern"
                    :primary false
                    :sequence ["https://foo.org/statement-two"
                               "https://foo.org/statement-three"]}
                   :templates-table templates-map
                   :patterns-table patterns-map
                   :patterns-graph pgraph}))
    ;; Moot point if our pattern is not sequence 
    (is (s/valid? ::pattern/min-sequence-count
                  {:object
                   {:id "https://foo.org/not-primary-two"
                    :type "Pattern"
                    :primary false
                    :alternates ["https://foo.org/statement-two"
                                 "https://foo.org/statement-three"]}
                   :templates-table templates-map
                   :patterns-table patterns-map
                   :patterns-graph pgraph}))))

(def patterns-map-bad
  {"https://foo.org/pattern-one"
   {:id "https://foo.org/pattern-one"
    :type "Pattern"
    :primary true
    :one-or-more {:id "https://foo.org/pattern-two"}}
   "https://foo.org/pattern-two"
   {:id "https://foo.org/pattern-two"
    :type "Pattern"
    :primary true
    :one-or-more {:id "https://foo.org/pattern-one"}}})

(def patterns-map-bad-2
  {"https://foo.org/pattern-three"
   {:id "https://foo.org/pattern-three"
    :type "Pattern"
    :primary true
    :one-or-more {:id "https://foo.org/pattern-three"}}})

(def pgraph-bad (pattern/pattern-graph patterns-map-bad))
(def pgraph-bad-2 (pattern/pattern-graph patterns-map-bad-2))

(deftest no-cycles-test
  (testing "MUST not have any cycles in graph"
    (is (s/valid? ::pattern/no-cycles
                  [{:patterns-graph pgraph}
                   {:patterns-graph pgraph}]))
    ;; No cycles
    (is (not (s/valid? ::pattern/no-cycles
                       [{:patterns-graph pgraph-bad}
                        {:patterns-graph pgraph-bad}])))
    ;; No self loops
    (is (not (s/valid? ::pattern/no-cycles
                       [{:patterns-graph pgraph-bad-2}])))))
