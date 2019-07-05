(ns com.yetanalytics.objects-test.pattern-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
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

(run-tests)
