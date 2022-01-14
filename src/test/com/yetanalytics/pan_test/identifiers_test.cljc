(ns com.yetanalytics.pan-test.identifiers-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.identifiers :as id]))

;; Data that we will be using of our tests

(def number-list [{:id 1}
                  {:id 3}
                  {:id 5 :other-key "Foo Bar"}
                  {:id 7 :another-key true}
                  {:id 9 :some-key 100}])

(def snsd-ot8 [{:id "Taeyeon"}
               {:id "Tiffany"}
               {:id "Seohyun"}
               {:id "Hyoyeon"}
               {:id "Yoona"}
               {:id "Yuri"}
               {:id "Sunny" :some-key "Yet Analytics"}
               {:id "Sooyoung" :another-key true}])

(def snsd-ot9 (merge snsd-ot8 {:not-id "Jessica" :another-key "Krystal"}))

;; Util tests

(deftest only-ids-test
  (testing "only-ids function"
    (is (= [1 3 5 7 9] (id/only-ids number-list)))
    (is (= ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung"]
           (id/only-ids snsd-ot8)))
    (is (= ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung" nil]
           (id/only-ids snsd-ot9)))))

(deftest only-ids-multiple-test
  (testing "only-ids-multiple function"
    (is (= [1 3 5 7 9]
           (id/only-ids-multiple [number-list])))
    (is (= ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon" "Yoona" "Yuri" "Sunny"
            "Sooyoung" "Taeyeon" "Tiffany" "Seohyun" "Hyoyeon" "Yoona" "Yuri"
            "Sunny" "Sooyoung" nil]
           (id/only-ids-multiple [snsd-ot8 snsd-ot9])))))

(deftest count-ids-test
  (testing "count-ids function"
    (is (= {1 1, 3 1, 5 1, 7 1, 9 1}
           (id/count-ids (id/only-ids number-list))))
    (is (= {"Taeyeon" 1 "Tiffany" 1 "Seohyun" 1 "Hyoyeon" 1
            "Yoona" 1 "Yuri" 1 "Sunny" 1 "Sooyoung" 1}
           (id/count-ids (id/only-ids snsd-ot8))))
    (is (= {"Taeyeon" 2 "Tiffany" 2 "Seohyun" 2 "Hyoyeon" 2
            "Yoona" 2 "Yuri" 2 "Sunny" 2 "Sooyoung" 2 nil 1}
           (id/count-ids (id/only-ids-multiple [snsd-ot8 snsd-ot9]))))))

(deftest distinct-ids-test
  (testing "distinct-ids spec"
    (is (s/valid? ::id/distinct-ids {"https://foo.org" 1
                                     "https://bar.org" 1}))
    (is (s/valid? ::id/distinct-ids {"https://foo.org" 1
                                     "https://bar.org" 1
                                     "what the pineapple" 1}))
    (is (not (s/valid? ::id/distinct-ids {"https://foo.org" 1
                                          "https://bar.org" 2})))))

;; ID tests
(deftest validate-ids-test
  (testing "id ID MUST be distinct from version IDs"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :versions [{:id "https://w3id.org/xapi/catch/v2"}
                           {:id "https://w3id.org/xapi/catch/v1"}]})))
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/xapi/catch"}
                            {:id "https://w3id.org/xapi/catch/v1"}]}))))
  (testing "Every Profile version ID MUST be distinct"
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/xapi/catch/v1"}
                            {:id "https://w3id.org/xapi/catch/v1"}]}))))
  (testing "Concept IDs MUST be distinct"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :concepts [{:id "https://w3id.org/xapi/catch/verb#1"
                            :type "Verb"}
                           {:id "https://w3id.org/xapi/catch/verb#2"
                            :type "Verb"}]})))
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :concepts [{:id "https://w3id.org/xapi/catch/verb#duplicate"
                             :type "Verb"}
                            {:id "https://w3id.org/xapi/catch/verb#duplicate"
                             :type "Verb"}]}))))
  (testing "Statement Template IDs MUST be distinct"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :templates [{:id "https://w3id.org/xapi/catch/template#1"
                             :type "StatementTemplate"}
                            {:id "https://w3id.org/xapi/catch/template#2"
                             :type "StatementTemplate"}]})))
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :templates [{:id "https://w3id.org/xapi/catch/template#dup"
                              :type "StatementTemplate"}
                             {:id "https://w3id.org/xapi/catch/template#dup"
                              :type "StatementTemplate"}]}))))
  (testing "Pattern IDs MUST be distinct"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :patterns [{:id "https://w3id.org/xapi/catch/pattern#1"
                            :type "Pattern"}
                           {:id "https://w3id.org/xapi/catch/pattern#2"
                            :type "Pattern"}]})))
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :patterns [{:id "https://w3id.org/xapi/catch/pattern#dup"
                             :type "Pattern"}
                            {:id "https://w3id.org/xapi/catch/pattern#dup"
                             :type "Pattern"}]}))))
  (testing "All IDs MUST be distinct"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :versions [{:id "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/v2"}]
                :concepts [{:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v1"}]
                :templates [{:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v1"}]
                :patterns [{:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v2"}]}))))
  (testing "All IDs MUST be distinct across different profiles"
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :concepts [{:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v1"}]
                :templates [{:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v1"}]
                :patterns [{:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v2"}]}
               [{:concepts [{:id "https://w3id.org/catch/some-verb-x"
                             :inScheme "https://w3id.org/catch/v3"}]}
                {:templates [{:id "https://w3id.org/catch/some-template-x"
                              :inScheme "https://w3id.org/catch/v4"}]}
                {:patterns [{:id "https://w3id.org/catch/some-pattern-x"
                             :inScheme "https://w3id.org/catch/v5"}]}])))
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :concepts [{:id "https://w3id.org/catch/some-verb"
                             :inScheme "https://w3id.org/catch/v1"}]
                 :templates [{:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v1"}]
                 :patterns [{:id "https://w3id.org/catch/some-pattern"
                             :inScheme "https://w3id.org/catch/v2"}]}
                [{:id "https://w3id.org/xapi/catch"
                  :versions [{:id "https://w3id.org/catch/v1"}
                             {:id "https://w3id.org/catch/v2"}]
                  :concepts [{:id "https://w3id.org/catch/some-verb"
                              :inScheme "https://w3id.org/catch/v1"}]
                  :templates [{:id "https://w3id.org/catch/some-template"
                               :inScheme "https://w3id.org/catch/v1"}]
                  :patterns [{:id "https://w3id.org/catch/some-pattern"
                              :inScheme "https://w3id.org/catch/v2"}]}])))))

(deftest in-scheme-test
  (testing "object inScheme MUST be a valid Profile version"
    (is (nil? (id/validate-in-schemes
               {:versions [{:id "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/v2"}]
                :concepts [{:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v1"}]
                :templates [{:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v1"}]
                :patterns [{:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v2"}]})))
    (is (some? (id/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :concepts [{:id "https://w3id.org/catch/some-verb"
                             :inScheme "https://w3id.org/catch/v3"}]
                 :templates [] :patterns []})))
    (is (some? (id/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :templates [{:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v3"}]
                 :concepts [] :patterns []})))
    (is (some? (id/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :patterns [{:id "https://w3id.org/catch/some-pattern"
                             :inScheme "https://w3id.org/catch/v3"}]
                 :concepts [] :templates []})))))
