(ns com.yetanalytics.pan-test.objects-test.pattern-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils
             :refer [should-satisfy should-satisfy+ should-not-satisfy]]
            [com.yetanalytics.pan.objects.pattern :as pattern]))

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

(deftest is-primary-test
  (testing "is-primary-true spec"
    (should-satisfy+ ::pattern/is-primary-true
                     {:primary true}
                     :bad
                     {:primary false}
                     {}))
  (testing "is-primary-false spec"
    (should-satisfy+ ::pattern/is-primary-false
                     {:primary false}
                     {}
                     :bad
                     {:primary true})))

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
                    "https://w3id.org/xapi/catch/templates#view-rubric")
    (should-not-satisfy ::pattern/optional
                        {:id "https://w3id.org/xapi/catch/templates#view-rubric"})
    (should-not-satisfy ::pattern/optional
                        {:foo "https://w3id.org/xapi/catch/templates#view-rubric"})
    (should-not-satisfy ::pattern/optional
                        ["https://w3id.org/xapi/catch/templates#view-rubric"])
    (should-not-satisfy ::pattern/optional "")))

(deftest one-or-more-test
  (testing "oneOrMore (A+) property"
    (should-satisfy ::pattern/oneOrMore
                    "https://w3id.org/xapi/catch/patterns#f1-2-01-scored")
    (should-not-satisfy ::pattern/oneOrMore
                        {:id "https://w3id.org/xapi/catch/patterns#f1-2-01-scored"})
    (should-not-satisfy ::pattern/oneOrMore
                        {:foo "https://w3id.org/xapi/catch/patterns#f1-2-01-scored"})
    (should-not-satisfy ::pattern/oneOrMore
                        ["https://w3id.org/xapi/catch/patterns#f1-2-01-scored"])
    (should-not-satisfy ::pattern/oneOrMore "")))

(deftest zero-or-more-test
  (testing "zeroOrMore (A*) property"
    (should-satisfy ::pattern/zeroOrMore
                        "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete")
    (should-not-satisfy ::pattern/zeroOrMore
                        {:id "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"})
    (should-not-satisfy ::pattern/zeroOrMore
                        {:foo "https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"})
    (should-not-satisfy ::pattern/zeroOrMore
                        ["https://w3id.org/xapi/catch/patterns#system-updates-status-incomplete"])
    (should-not-satisfy ::pattern/zeroOrMore "")))

(deftest pattern-clause-test
  (testing "ensure that only one of the five regex properties are included"
    (should-satisfy+ ::pattern/pattern-clause
                     {:alternates "foo"}
                     {:optional "foo"}
                     {:oneOrMore "foo"}
                     {:sequence "foo"}
                     {:zeroOrMore "foo"}
                     :bad
                     ;; 32 possible combos in total - impractical to test all
                     {}
                     {:alternates "foo" :optional "bar"}
                     {:optional "foo" :oneOrMore "bar"}
                     {:oneOrMore "foo" :sequence "bar"}
                     {:sequence "foo" :zeroOrMore "bar"}
                     {:alternates "foo" :optional "baz" :oneOrMore "bar"}
                     {:optional "foo" :oneOrMore "baz" :zeroOrMore "bar"}
                     {:alternates "foo" :optional "baz" :oneOrMore "goop"
                      :sequence "durr" :zeroOrMore "bar"})))

(deftest primary-pattern-test
  (testing "primary pattern"
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/catch/patterns#system-progress-response"
                   :type "Pattern"
                   :primary true
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "system response progress"}
                   :definition {"en" "System responses to progress within an activity"}
                   :sequence ["https://w3id.org/xapi/catch/templates#system-notification-submission"
                              "https://w3id.org/xapi/catch/templates#system-notification-progression"]}))
    ; Patterns require one regex rule
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/catch/patterns#pattern"
                        :type "Pattern"
                        :primary true
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :prefLabel {"en" "pattern"}
                        :definition {"en" "pattern definition"}})))
    ; Patterns cannot have two or more regex rules
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/catch/patterns#pattern"
                        :type "Pattern"
                        :primary true
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :prefLabel {"en" "pattern"}
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
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :optional "https://w3id.org/xapi/catch/templates#view-rubric"})))))

(deftest non-primary-pattern-test
  (testing "non-primary pattern"
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/catch/patterns#view-rubric"
                   :type "Pattern"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :prefLabel {"en" "view rubric"}
                   :definition {"en" "This is a pattern for someone looking at a rubric"}
                   :optional "https://w3id.org/xapi/catch/templates#view-rubric"}))
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/catch/patterns#cross-linguistic-connections-completion"
                        :type "Pattern"
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :prefLabel {:en "complete Two-Way Sheltered Instruction"}
                        :oneOrMore ["https://w3id.org/xapi/catch/patterns#f3-2-01-completion"]})))
    ; Remove properties that are optional in non-primary patterns
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/minimal/pattern"
                   :type "Pattern"
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :optional "https://w3id.org/xapi/catch/templates#view-rubric"}))
    ; Add the "primary" property (set to false)
    (is (s/valid? ::pattern/pattern
                  {:id "https://w3id.org/xapi/minimal/pattern"
                   :type "Pattern"
                   :primary false
                   :inScheme "https://w3id.org/xapi/catch/v1"
                   :optional "https://w3id.org/xapi/catch/templates#view-rubric"}))
    ; Pattern require one regex rule
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/minimal/pattern"
                        :type "Pattern"
                        :inScheme "https://w3id.org/xapi/catch/v1"})))
    ; Patterns cannot have two or more regex rules
    (is (not (s/valid? ::pattern/pattern
                       {:id "https://w3id.org/xapi/minimal/pattern"
                        :type "Pattern"
                        :inScheme "https://w3id.org/xapi/catch/v1"
                        :optional "https://w3id.org/xapi/catch/templates#one"
                        :zeroOrMore "https://w3id.org/xapi/catch/templates#two"})))))

;; Graph tests

(deftest node-with-attrs
  (testing "Creating node with attributes"
    (is (= (graph/node-with-attrs {:id "https://foo.org/pattern1"
                                   :type "Pattern"
                                   :primary true
                                   :alternates ["https://foo.org/p1"
                                                "https://foo.org/p2"]})
           ["https://foo.org/pattern1" {:type "Pattern"
                                        :primary true
                                        :property :alternates}]))
    (is (= (graph/node-with-attrs {:id "https://foo.org/pattern1"
                                   :type "Pattern"
                                   :primary false
                                   :optional "https://foo.org/p3"})
           ["https://foo.org/pattern1" {:type "Pattern"
                                        :primary false
                                        :property :optional}]))))

(deftest edge-with-attrs-test
  (testing "Creating vector of edges"
    (is (= (graph/edges-with-attrs {:id "https://foo.org/pattern1"
                                    :type "Pattern"
                                    :alternates ["https://foo.org/p1"
                                                 "https://foo.org/p2"]})
           [["https://foo.org/pattern1" "https://foo.org/p1" {:type :alternates}]
            ["https://foo.org/pattern1" "https://foo.org/p2" {:type :alternates}]]))
    (is (= (graph/edges-with-attrs {:id "https://foo.org/pattern2"
                                    :type "Pattern"
                                    :sequence ["https://foo.org/p1"
                                               "https://foo.org/p2"]})
           [["https://foo.org/pattern2" "https://foo.org/p1" {:type :sequence}]
            ["https://foo.org/pattern2" "https://foo.org/p2" {:type :sequence}]]))
    (is (= (graph/edges-with-attrs {:id "https://foo.org/pattern3"
                                    :type "Pattern"
                                    :optional "https://foo.org/p0"})
           [["https://foo.org/pattern3" "https://foo.org/p0" {:type :optional}]]))
    (is (= (graph/edges-with-attrs {:id "https://foo.org/pattern4"
                                    :type "Pattern"
                                    :oneOrMore "https://foo.org/p0"})
           [["https://foo.org/pattern4" "https://foo.org/p0" {:type :oneOrMore}]]))
    (is (= (graph/edges-with-attrs {:id "https://foo.org/pattern5"
                                    :type "Pattern"
                                    :zeroOrMore "https://foo.org/p0"})
           [["https://foo.org/pattern5" "https://foo.org/p0" {:type :zeroOrMore}]]))))

(deftest alternates-test-2
  (testing "Alternates pattern MUST NOT include optional or zeroOrMore directly."
    (should-satisfy+
     ::pattern/valid-edge
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :alternates}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :sequence}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :oneOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate"
      :type :alternates :dest-property nil}
     :bad
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :optional}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :zeroOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Verb"
      :type :alternates :dest-property nil})))

#_(s/explain ::pattern/valid-edge
           {:src "https://foo.org/p1" :dest "https://foo.org/p2"
            :src-type "Pattern" :dest-type "Pattern"
            :type :alternates :dest-property :alternates})

(deftest sequence-test-2
  (testing "Sequence pattern MUST include at least two members, unless pattern is a primary pattern not used elsewhere that contains one StatementTemplate."
    (should-satisfy+
     ::pattern/valid-edge
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :sequence
      :src-indegree 1 :src-outdegree 2 :src-primary false}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary true}
     :bad
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary true}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 1 :src-outdegree 1 :src-primary true}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary false})))

(deftest edge-test
  (testing "Optional, oneOrMore, or zeroOrMore pattern edges"
    (should-satisfy+
     ::pattern/valid-edge
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :optional}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :oneOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :zeroOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :optional}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :oneOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "StatementTemplate" :type :zeroOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Pattern" :type :optional
      :src-primary true :src-indegree 0 :src-outdegree 1 :dest-property :oneOrMore}
     :bad
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Verb" :type :optional}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Verb" :type :oneOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type "Verb" :type :zeroOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type nil :type :optional}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type nil :type :oneOrMore}
     {:src "https://foo.org/p1" :dest "https://foo.org/p2"
      :src-type "Pattern" :dest-type nil :type :zeroOrMore})))

(def ex-templates
  [{:id "https://foo.org/template1"
    :type "StatementTemplate" :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/template2"
    :type "StatementTemplate" :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/template3"
    :type "StatementTemplate" :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/template4"
    :type "StatementTemplate" :inScheme "https://foo.org/v1"}
   {:id "https://foo.org/template5"
    :type "StatementTemplate" :inScheme "https://foo.org/v1"}])

(def ex-patterns
  [{:id "https://foo.org/pattern1" :type "Pattern"
    :inScheme "https://foo.org/v1" :primary true
    :alternates ["https://foo.org/pattern2"
                 "https://foo.org/template1"]}
   {:id "https://foo.org/pattern2" :type "Pattern"
    :inScheme "https://foo.org/v1" :primary true
    :sequence ["https://foo.org/pattern3"
               "https://foo.org/template2"]}
   {:id "https://foo.org/pattern3" :type "Pattern"
    :inScheme "https://foo.org/v1" :primary true
    :optional "https://foo.org/template3"}
   {:id "https://foo.org/pattern4" :type "Pattern"
    :inScheme "https://foo.org/v1" :primary true
    :oneOrMore "https://foo.org/template4"}
   {:id "https://foo.org/pattern5" :type "Pattern"
    :inScheme "https://foo.org/v1" :primary true
    :zeroOrMore "https://foo.org/template5"}])

(def pgraph (pattern/create-graph ex-templates ex-patterns))

(deftest graph-test
  (testing "Pattern graph should satisfy various properties"
    (is (= 10 (count (graph/nodes pgraph))))
    (is (= 7 (count (graph/edges pgraph))))
    (is (= 7 (count (pattern/get-edges pgraph))))
    (is (= (set (graph/nodes pgraph))
           #{"https://foo.org/pattern1" "https://foo.org/pattern2"
             "https://foo.org/pattern3" "https://foo.org/pattern4"
             "https://foo.org/pattern5" "https://foo.org/template1"
             "https://foo.org/template2" "https://foo.org/template3"
             "https://foo.org/template4" "https://foo.org/template5"}))
    (is (= (set (pattern/get-edges pgraph))
           #{{:src "https://foo.org/pattern1" :src-type "Pattern"
              :src-primary true :src-indegree 0 :src-outdegree 2
              :dest "https://foo.org/pattern2" :dest-type "Pattern"
              :dest-property :sequence :type :alternates}
             {:src "https://foo.org/pattern1" :src-type "Pattern"
              :src-primary true :src-indegree 0 :src-outdegree 2
              :dest "https://foo.org/template1" :dest-type "StatementTemplate"
              :dest-property nil :type :alternates}
             {:src "https://foo.org/pattern2" :src-type "Pattern"
              :src-primary true :src-indegree 1 :src-outdegree 2
              :dest "https://foo.org/pattern3" :dest-type "Pattern"
              :dest-property :optional :type :sequence}
             {:src "https://foo.org/pattern2" :src-type "Pattern"
              :src-primary true :src-indegree 1 :src-outdegree 2
              :dest "https://foo.org/template2" :dest-type "StatementTemplate"
              :dest-property nil :type :sequence}
             {:src "https://foo.org/pattern3" :src-type "Pattern"
              :src-primary true :src-indegree 1 :src-outdegree 1
              :dest "https://foo.org/template3" :dest-type "StatementTemplate"
              :dest-property nil :type :optional}
             {:src "https://foo.org/pattern4" :src-type "Pattern"
              :src-primary true :src-indegree 0 :src-outdegree 1
              :dest "https://foo.org/template4" :dest-type "StatementTemplate"
              :dest-property nil :type :oneOrMore}
             {:src "https://foo.org/pattern5" :src-type "Pattern"
              :src-primary true :src-indegree 0 :src-outdegree 1
              :dest "https://foo.org/template5" :dest-type "StatementTemplate"
              :dest-property nil :type :zeroOrMore}}))
    (should-satisfy ::pattern/valid-edges (pattern/get-edges pgraph))
    (should-satisfy ::pattern/singleton-sccs (graph/scc pgraph))
    (is (nil? (pattern/explain-graph pgraph)))
    (is (nil? (pattern/explain-graph-cycles pgraph)))))

(def cyclic-patterns-1
  [{:id "https://foo.org/pattern-one"
    :type "Pattern"
    :primary true
    :oneOrMore "https://foo.org/pattern-two"}
   {:id "https://foo.org/pattern-two"
    :type "Pattern"
    :primary true
    :oneOrMore "https://foo.org/pattern-one"}])

(def cyclic-patterns-2
  [{:id "https://foo.org/pattern-three"
    :type "Pattern"
    :primary true
    :oneOrMore "https://foo.org/pattern-three"}])

(def cyclic-pgraph-1 (pattern/create-graph [] cyclic-patterns-1))

(def cyclic-pgraph-2 (pattern/create-graph [] cyclic-patterns-2))

(deftest no-cycles-test
  (testing "MUST not have any cycles in graph"
    ;; No cycles
    (is (some? (pattern/explain-graph-cycles cyclic-pgraph-1)))
    ;; No self loops
    ;; Note: Self loops are NOT caught by explain-graph-cycles, but are
    ;; caught by the edge validation specs
    (is (some? (pattern/explain-graph cyclic-pgraph-2)))
    (is (nil? (pattern/explain-graph-cycles cyclic-pgraph-2)))))
