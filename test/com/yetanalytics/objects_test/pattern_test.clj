(ns com.yetanalytics.objects-test.pattern-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.pprint :as pprint]
            [ubergraph.core :as uber]
            [com.yetanalytics.util :as u]
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
                                     "https://w3id.org/xapi/catch/templates#four"]})))))

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

;; Graph tests

(deftest node-with-attrs
  (testing "Creating node with attributes"
    (is (= (u/node-with-attrs {:id "https://foo.org/pattern1"
                               :type "Pattern"
                               :primary true
                               :alternates ["https://foo.org/p1"
                                            "https://foo.org/p2"]})
           ["https://foo.org/pattern1" {:type "Pattern"
                                        :primary true
                                        :property :alternates}]))
    (is (= (u/node-with-attrs {:id "https://foo.org/pattern1"
                               :type "Pattern"
                               :primary false
                               :optional {:id "https://foo.org/p3"}})
           ["https://foo.org/pattern1" {:type "Pattern"
                                        :primary false
                                        :property :optional}]))))

(deftest edge-with-attrs-test
  (testing "Creating vector of edges"
    (is (= (u/edges-with-attrs {:id "https://foo.org/pattern1"
                                :type "Pattern"
                                :alternates ["https://foo.org/p1"
                                             "https://foo.org/p2"]})
           [["https://foo.org/pattern1" "https://foo.org/p1" {:type :alternates}]
            ["https://foo.org/pattern1" "https://foo.org/p2" {:type :alternates}]]))
    (is (= (u/edges-with-attrs {:id "https://foo.org/pattern2"
                                :type "Pattern"
                                :sequence ["https://foo.org/p1"
                                           "https://foo.org/p2"]})
           [["https://foo.org/pattern2" "https://foo.org/p1" {:type :sequence}]
            ["https://foo.org/pattern2" "https://foo.org/p2" {:type :sequence}]]))
    (is (= (u/edges-with-attrs {:id "https://foo.org/pattern3"
                                :type "Pattern"
                                :optional {:id "https://foo.org/p0"}})
           [["https://foo.org/pattern3" "https://foo.org/p0" {:type :optional}]]))
    (is (= (u/edges-with-attrs {:id "https://foo.org/pattern4"
                                :type "Pattern"
                                :one-or-more {:id "https://foo.org/p0"}})
           [["https://foo.org/pattern4" "https://foo.org/p0" {:type :one-or-more}]]))
    (is (= (u/edges-with-attrs {:id "https://foo.org/pattern5"
                                :type "Pattern"
                                :zero-or-more {:id "https://foo.org/p0"}})
           [["https://foo.org/pattern5" "https://foo.org/p0" {:type :zero-or-more}]]))))

(deftest alternates-test
  (testing "Alternates pattern MUST NOT include optional or zeroOrMore directly."
    (should-satisfy+
     ::pattern/valid-edge
     {:src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :alternates}
     {:src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :sequence}
     {:src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :one-or-more}
     {:src-type "Pattern" :dest-type "StatementTemplate"
      :type :alternates :dest-property nil}
     :bad
     {:src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :optional}
     {:src-type "Pattern" :dest-type "Pattern"
      :type :alternates :dest-property :zero-or-more}
     {:src-type "Pattern" :dest-type "Verb"
      :type :alternates :dest-property nil})))

(deftest sequence-test
  (testing "Sequence pattern MUST include at least two members, unless pattern is a primary pattern not used elsewhere that contains one StatementTemplate."
    (should-satisfy+
     ::pattern/valid-edge
     {:src-type "Pattern" :dest-type "Pattern" :type :sequence
      :src-indegree 1 :src-outdegree 2 :src-primary false}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary true}
     :bad
     {:src-type "Pattern" :dest-type "Pattern" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary true}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 1 :src-outdegree 1 :src-primary true}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :sequence
      :src-indegree 0 :src-outdegree 1 :src-primary false})))

(deftest edge-test
  (testing "Optional, oneOrMore, or zeroOrMore pattern edges"
    (should-satisfy+
     ::pattern/valid-edge
     {:src-type "Pattern" :dest-type "Pattern" :type :optional}
     {:src-type "Pattern" :dest-type "Pattern" :type :one-or-more}
     {:src-type "Pattern" :dest-type "Pattern" :type :zero-or-more}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :optional}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :one-or-more}
     {:src-type "Pattern" :dest-type "StatementTemplate" :type :zero-or-more}
     {:src-type "Pattern" :dest-type "Pattern" :type :optional
      :src-primary true :src-indegree 0 :src-outdegree 1 :dest-property :one-or-more}
     :bad
     {:src-type "Pattern" :dest-type "Verb" :type :optional}
     {:src-type "Pattern" :dest-type "Verb" :type :one-or-more}
     {:src-type "Pattern" :dest-type "Verb" :type :zero-or-more}
     {:src-type "Pattern" :dest-type nil :type :optional}
     {:src-type "Pattern" :dest-type nil :type :one-or-more}
     {:src-type "Pattern" :dest-type nil :type :zero-or-more})))

(def ex-templates
  [{:id "https://foo.org/template1"
    :type "StatementTemplate" :in-scheme "https://foo.org/v1"}
   {:id "https://foo.org/template2"
    :type "StatementTemplate" :in-scheme "https://foo.org/v1"}
   {:id "https://foo.org/template3"
    :type "StatementTemplate" :in-scheme "https://foo.org/v1"}
   {:id "https://foo.org/template4"
    :type "StatementTemplate" :in-scheme "https://foo.org/v1"}
   {:id "https://foo.org/template5"
    :type "StatementTemplate" :in-scheme "https://foo.org/v1"}])

(def ex-patterns
  [{:id "https://foo.org/pattern1" :type "Pattern"
    :in-scheme "https://foo.org/v1" :primary true
    :alternates ["https://foo.org/pattern2"
                 "https://foo.org/template1"]}
   {:id "https://foo.org/pattern2" :type "Pattern"
    :in-scheme "https://foo.org/v1" :primary true
    :sequence ["https://foo.org/pattern3"
               "https://foo.org/template2"]}
   {:id "https://foo.org/pattern3" :type "Pattern"
    :in-scheme "https://foo.org/v1" :primary true
    :optional {:id "https://foo.org/template3"}}
   {:id "https://foo.org/pattern4" :type "Pattern"
    :in-scheme "https://foo.org/v1" :primary true
    :one-or-more {:id "https://foo.org/template4"}}
   {:id "https://foo.org/pattern5" :type "Pattern"
    :in-scheme "https://foo.org/v1" :primary true
    :zero-or-more {:id "https://foo.org/template5"}}])

(def pgraph
  (let [graph (uber/digraph)
        ;; Nodes
        tnodes (mapv (partial u/node-with-attrs) ex-templates)
        pnodes (mapv (partial u/node-with-attrs) ex-patterns)
        ;; Edges
        tedges (reduce concat (mapv (partial u/edges-with-attrs) ex-templates))
        pedges (reduce concat (mapv (partial u/edges-with-attrs) ex-patterns))]
    (-> graph
        (uber/add-nodes-with-attrs* tnodes)
        (uber/add-nodes-with-attrs* pnodes)
        (uber/add-directed-edges* tedges)
        (uber/add-directed-edges* pedges))))

(deftest graph-test
  (testing "Pattern graph should satisfy various properties"
    (is (= 10 (count (uber/nodes pgraph))))
    (is (= 7 (count (uber/edges pgraph))))
    (is (= 7 (count (pattern/get-edges pgraph))))
    (should-satisfy ::pattern/valid-edges (pattern/get-edges pgraph))
    (should-satisfy ::pattern/acyclic-graph pgraph)
    (should-satisfy ::pattern/pattern-graph pgraph)))
