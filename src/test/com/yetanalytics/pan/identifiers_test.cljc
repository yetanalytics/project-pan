(ns com.yetanalytics.pan.identifiers-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.identifiers :as id]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Util Tests 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def number-list
  [{:id 1}
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

(def snsd-ot8-coll
  ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon" "Yoona" "Yuri" "Sunny" "Sooyoung"])

(def snsd-ot9
  (merge snsd-ot8
         {:not-id "Jessica" :another-key "Krystal"}))

;; Util tests

(deftest objs->ids-test
  (testing "objs->ids function"
    (is (= [1 3 5 7 9]
           (id/objs->ids number-list)))
    (is (= snsd-ot8-coll
           (id/objs->ids snsd-ot8)))
    (is (= (conj snsd-ot8-coll nil)
           (id/objs->ids snsd-ot9)))))

(deftest objs->out-ids-test
  (testing "objs->out-ids function"
    (is (= #{"Yet Analytics"}
           (id/objs->out-ids snsd-ot8 [:some-key])))))

(deftest count-ids-test
  (testing "count-ids function"
    (is (= {1 1
            3 1
            5 1
            7 1
            9 1}
           (id/count-ids (id/objs->ids number-list))))
    (is (= {"Taeyeon"  1
            "Tiffany"  1
            "Seohyun"  1
            "Hyoyeon"  1
            "Yoona"    1
            "Yuri"     1
            "Sunny"    1
            "Sooyoung" 1}
           (id/count-ids (id/objs->ids snsd-ot8))))
    (is (= {"Taeyeon"  2
            "Tiffany"  2
            "Seohyun"  2
            "Hyoyeon"  2
            "Yoona"    2
            "Yuri"     2
            "Sunny"    2
            "Sooyoung" 2
            nil        1}
           (id/count-ids (mapcat id/objs->ids [snsd-ot8 snsd-ot9]))))))


(deftest filter-test
  (testing "filter-by-ids function"
    (is (= [{:id "Taeyeon"}]
           (id/filter-by-ids #{"Taeyeon" "Taeyang"} snsd-ot8))))
  (testing "filter-by-ids-kv function"
    (is (= {"Taeyeon" 1}
           (id/filter-by-ids-kv #{"Taeyeon" "Taeyang"}
                                (id/count-ids (id/objs->ids snsd-ot8)))))))

(deftest dedupe-profile-objects-test
  (testing "dedupe-profile-objects function"
    (is (= {:id "https://w3id.org/xapi/catch"
            :concepts [{:id "https://w3id.org/catch/v1/some-verb"}
                       {:id "https://w3id.org/catch/v2/some-verb"
                        :broader ["https://w3id.org/catch/a-verb"]}]
            :templates [{:id "https://w3id.org/catch/v1/some-template"
                         :verb ["https://w3id.org/catch/a-verb"]}
                        {:id "https://w3id.org/catch/v2/some-template"
                         :verb ["https://w3id.org/catch/a-verb"]
                         :rules [{:location "$.foo"
                                  :presence "included"}]}]
            :patterns [{:id "https://w3id.org/catch/v1/some-pattern"
                        :alternates ["https://w3id.org/catch/a-template"]}
                       {:id "https://w3id.org/catch/v2/some-pattern"
                        :alternates ["https://w3id.org/catch/a-template"]}]}
           (id/dedupe-profile-objects
            {:id "https://w3id.org/xapi/catch"
             :concepts [{:id "https://w3id.org/catch/v1/some-verb"
                         :inScheme "https://w3id.org/catch/v1"
                         :deprecated false}
                        {:id "https://w3id.org/catch/v2/some-verb"
                         :inScheme "https://w3id.org/catch/v2"
                         :deprecated true
                         :broader ["https://w3id.org/catch/a-verb"]}]
             :templates [{:id "https://w3id.org/catch/v1/some-template"
                          :inScheme "https://w3id.org/catch/v1"
                          :deprecated false
                          :verb ["https://w3id.org/catch/a-verb"]}
                         {:id "https://w3id.org/catch/v2/some-template"
                          :inScheme "https://w3id.org/catch/v2"
                          :deprecated true
                          :verb ["https://w3id.org/catch/a-verb"]
                          :rules [{:location "$.foo"
                                   :presence "included"
                                   :scopeNote ["Scope Note One"]}]}]
             :patterns [{:id "https://w3id.org/catch/v1/some-pattern"
                         :inScheme "https://w3id.org/catch/v1"
                         :deprecated false
                         :alternates ["https://w3id.org/catch/a-template"]}
                        {:id "https://w3id.org/catch/v2/some-pattern"
                         :inScheme "https://w3id.org/catch/v2"
                         :deprecated true
                         :alternates ["https://w3id.org/catch/a-template"]}]})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec Tests 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
                             :type "Verb"
                             :broader ["https://w3id.org/xapi/catch/verb#1"]}]})))
    (testing "- except when only inScheme or deprecated change"
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :concepts [{:id "https://w3id.org/xapi/catch/verb#duplicate"
                              :type "Verb"}
                             {:id "https://w3id.org/xapi/catch/verb#duplicate"
                              :type "Verb"}]})))
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :concepts [{:id "https://w3id.org/xapi/catch/verb#duplicate"
                              :type "Verb"
                              :inScheme "https://w3id.org/xapi/catch/v1"
                              :deprecated false}
                             {:id "https://w3id.org/xapi/catch/verb#duplicate"
                              :type "Verb"
                              :inScheme "https://w3id.org/xapi/catch/v2"
                              :deprecated true}]})))))
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
                              :type "StatementTemplate"
                              :verb ["https://w3id.org/xapi/catch/verb#1"]}]})))
    (testing "- except when inScheme, deprecated, or scopeNotes change"
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :templates [{:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"}
                              {:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"}]})))
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :templates [{:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"
                               :inScheme "https://w3id.org/xapi/catch/v1"
                               :deprecated false}
                              {:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"
                               :inScheme "https://w3id.org/xapi/catch/v2"
                               :deprecated true}]})))
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :templates [{:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"
                               :rules [{:location "$.foo"
                                        :presence "included"
                                        :scopeNote ["Scope Note One"]}]}
                              {:id "https://w3id.org/xapi/catch/template#dup"
                               :type "StatementTemplate"
                               :rules [{:location "$.foo"
                                        :presence "included"
                                        :scopeNote ["Scope Note Two"]}]}]})))))
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
                             :type "Pattern"
                             :sequence ["https://w3id.org/xapi/catch/pattern#1"]}]})))
    (testing "- except when inScheme and deprecated change"
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :patterns [{:id "https://w3id.org/xapi/catch/pattern#dup"
                              :type "Pattern"}
                             {:id "https://w3id.org/xapi/catch/pattern#dup"
                              :type "Pattern"}]})))
      (is (nil? (id/validate-ids
                 {:id "https://w3id.org/xapi/catch"
                  :patterns [{:id "https://w3id.org/xapi/catch/pattern#dup"
                              :type "Pattern"
                              :inScheme "https://w3id.org/xapi/catch/v1"
                              :deprecated false}
                             {:id "https://w3id.org/xapi/catch/pattern#dup"
                              :type "Pattern"
                              :inScheme "https://w3id.org/xapi/catch/v2"
                              :deprecated true}]})))))
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
                            :inScheme "https://w3id.org/catch/v2"}]})))
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :versions [{:id "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/v2"}
                           {:id "https://w3id.org/catch/v3"}]
                :concepts [{:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v2"}
                           {:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v3"}]
                :templates [{:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v2"}
                            {:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v3"}]
                :patterns [{:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v2"}
                           {:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v3"}]})))
    ;; Properties are the same
    (is (nil? (id/validate-ids
               {:id "https://w3id.org/xapi/catch"
                :versions [{:id "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/v2"}]
                :concepts [{:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v1"
                            :broader ["http://example.org/verb-a"]}
                           {:id "https://w3id.org/catch/some-verb"
                            :inScheme "https://w3id.org/catch/v2"
                            :broader ["http://example.org/verb-a"]}]
                :templates [{:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v1"
                             :verb ["http://example.org/verb-a"]}
                            {:id "https://w3id.org/catch/some-template"
                             :inScheme "https://w3id.org/catch/v2"
                             :verb ["http://example.org/verb-a"]}]
                :patterns [{:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v1"}
                           {:id "https://w3id.org/catch/some-pattern"
                            :inScheme "https://w3id.org/catch/v2"}]})))
    ;; Properties are different
    (is (some? (id/validate-ids
                {:id "https://w3id.org/xapi/catch"
                 :versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :concepts [{:id "https://w3id.org/catch/some-verb"
                             :inScheme "https://w3id.org/catch/v1"
                             :broader ["http://example.org/verb-a"]}
                            {:id "https://w3id.org/catch/some-verb"
                             :inScheme "https://w3id.org/catch/v2"
                             :broader ["http://example.org/verb-b"]}]
                 :templates [{:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v1"
                              :verb ["http://example.org/verb-a"]}
                             {:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v2"
                              :verb ["http://example.org/verb-b"]}]
                 :patterns [{:id "https://w3id.org/catch/some-pattern"
                             :inScheme "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/some-pattern"
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
    ;; inSchemes and other props are NOT ignored when comparing
    ;; across profiles
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
                 :templates []
                 :patterns []})))
    (is (some? (id/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :templates [{:id "https://w3id.org/catch/some-template"
                              :inScheme "https://w3id.org/catch/v3"}]
                 :concepts []
                 :patterns []})))
    (is (some? (id/validate-in-schemes
                {:versions [{:id "https://w3id.org/catch/v1"}
                            {:id "https://w3id.org/catch/v2"}]
                 :patterns [{:id "https://w3id.org/catch/some-pattern"
                             :inScheme "https://w3id.org/catch/v3"}]
                 :concepts []
                 :templates []})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(deftest test-unique-versions
  (testing "validate-unique-versions function"
    (is (nil? (id/validate-unique-versions
               {:id       "http://example.com"
                :versions [{:id "http://example.com/v1"}
                           {:id "http://example.com/v2"}
                           {:id "http://example.com/v3"}]})))
    (is (some? (id/validate-unique-versions
                {:id       "http://example.com"
                 :versions [{:id "http://example.com/v1"}
                            {:id "http://example.com/v1"}
                            {:id "http://example.com/v3"}]})))
    (is (some? (id/validate-unique-versions
                {:id       "http://example.com/v1"
                 :versions [{:id "http://example.com/v1"}
                            {:id "http://example.com/v2"}
                            {:id "http://example.com/v3"}]})))
    (is (some? (id/validate-unique-versions
                {:id       "http://example.com/v3"
                 :versions [{:id "http://example.com/v3"}
                            {:id "http://example.com/v3"}
                            {:id "http://example.com/v3"}]})))
    
    ))

#_(deftest test-valid-object-ids
  (testing "validate-object-ids function"
    (is (nil? (id/validate-object-ids
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (testing "- reused profile ID"
      (is (some? (id/validate-object-ids
                  {:id        "http://example.com"
                   :versions  [{:id "http://example.com/v1"}]
                   :concepts  [{:id       "http://example.com"
                                :inScheme "http://example.com/v1"}]
                   :templates [{:id       "http://example.com/template"
                                :inScheme "http://example.com/v1"}]
                   :patterns  [{:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v1"}]})))
      (is (some? (id/validate-object-ids
                  {:id        "http://example.com"
                   :versions  [{:id "http://example.com/v1"}]
                   :concepts  [{:id       "http://example.com/concept"
                                :inScheme "http://example.com/v1"}]
                   :templates [{:id       "http://example.com"
                                :inScheme "http://example.com/v1"}]
                   :patterns  [{:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v1"}]})))
      (is (some? (id/validate-object-ids
                  {:id        "http://example.com"
                   :versions  [{:id "http://example.com/v1"}]
                   :concepts  [{:id       "http://example.com/concept"
                                :inScheme "http://example.com/v1"}]
                   :templates [{:id       "http://example.com/template"
                                :inScheme "http://example.com/v1"}]
                   :patterns  [{:id       "http://example.com"
                                :inScheme "http://example.com/v1"}]}))))
    (testing "- reused version ID"
      (is (some? (id/validate-object-ids
                  {:id        "http://example.com"
                   :versions  [{:id "http://example.com/v1"}]
                   :concepts  [{:id       "http://example.com/v1"
                                :inScheme "http://example.com/v1"}]
                   :templates [{:id       "http://example.com/template"
                                :inScheme "http://example.com/v1"}]
                   :patterns  [{:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v1"}]}))))
    (testing "- inScheme not in version IDs"
      (is (some? (id/validate-object-ids
                  {:id        "http://example.com"
                   :versions  [{:id "http://example.com/v1"}]
                   :concepts  [{:id       "http://example.com/concept"
                                :inScheme "http://example.com/v4"}]
                   :templates [{:id       "http://example.com/template"
                                :inScheme "http://example.com/v1"}]
                   :patterns  [{:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v1"}]}))))))

(deftest validate-ids-globally-test
  (testing "validate-ids-globally function"
    (is (nil? (id/validate-ids-globally
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (testing "- profile and version IDs not unique"
      (let [spec-ed (id/validate-ids-globally
                     {:id       "http://example.com"
                      :versions [{:id "http://example.com/v1"}
                                 {:id "http://example.com/v1"}
                                 {:id "http://example.com/v3"}]
                      :concepts [{:id       "http://example.com/concept"
                                  :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"         1
                 "http://example.com/v1"      2
                 "http://example.com/v3"      1
                 "http://example.com/concept" 1}}
               (::s/value spec-ed))))
      (let [spec-ed (id/validate-ids-globally
                     {:id       "http://example.com/v1"
                      :versions [{:id "http://example.com/v1"}
                                 {:id "http://example.com/v2"}
                                 {:id "http://example.com/v3"}]
                      :concepts [{:id       "http://example.com/concept"
                                  :inScheme "http://example.com/v1"}]})]
        (is (= {"http://example.com/v1"
                {"http://example.com/v1"      2
                 "http://example.com/v2"      1
                 "http://example.com/v3"      1
                 "http://example.com/concept" 1}}
               (::s/value spec-ed))))
      (let [spec-ed (id/validate-ids-globally
                     {:id       "http://example.com/v3"
                      :versions [{:id "http://example.com/v3"}
                                 {:id "http://example.com/v3"}
                                 {:id "http://example.com/v3"}]
                      :concepts [{:id       "http://example.com/concept"
                                  :inScheme "http://example.com/v1"}]})]
        (is (= {"http://example.com/v1"
                {"http://example.com/v3"      4
                 "http://example.com/concept" 1}}
               (::s/value spec-ed)))))
    (testing "- reused profile ID"
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"          2
                 "http://example.com/v1"       1
                 "http://example.com/template" 1
                 "http://example.com/pattern"  1}}
               (::s/value spec-ed))))
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v1"}]})]
        (is (= {"http://example.com/v1"
                {"http://example.com"         2
                 "http://example.com/v1"      1
                 "http://example.com/concept" 1
                 "http://example.com/pattern" 1}}
               (::s/value spec-ed))))
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com"
                                   :inScheme "http://example.com/v1"}]})]
        (is (= {"http://example.com/v1"
                {"http://example.com"          2
                 "http://example.com/v1"       1
                 "http://example.com/concept"  1
                 "http://example.com/template" 1}}
               (::s/value spec-ed)))))
    (testing "- reused version ID"
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/v1"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"          1
                 "http://example.com/v1"       2
                 "http://example.com/template" 1
                 "http://example.com/pattern"  1}}
               (::s/value spec-ed)))))
    (testing "- duplicate object IDs"
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"    1
                 "http://example.com/v1" 1
                 "http://example.com/x"  3}}
               (::s/value spec-ed)))))
    (testing "- more than one inScheme in Profile objects"
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}
                                  {:id "http://example.com/v2"}
                                  {:id "http://example.com/v3"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v2"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v3"}]})]
        (is (some? spec-ed))
        (is (= 3
               (count (::s/value spec-ed)))))
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}
                                  {:id "http://example.com/v2"}
                                  {:id "http://example.com/v3"}]
                      :concepts  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v2"}]
                      :patterns  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v3"}]})]
        (is (= 3
               (count (::s/value spec-ed))))))
    (testing "- with extra profiles"
      (is (nil? (id/validate-ids-globally
                 {:id       "http://example.com"
                  :versions [{:id "http://example.com/v1"}]
                  :concepts [{:id       "http://example.com/concept"
                              :inScheme "http://example.com/v1"}]}
                 [{:id       "http://example2.com"
                   :versions [{:id "http://example2.com/v1"}]
                   :concepts [{:id       "http://example2.com/concept"
                               :inScheme "http://example2.com/v1"}]}])))
      (let [spec-ed (id/validate-ids-globally
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]}
                     [{:id        "http://example.com"
                       :versions  [{:id "http://example.com/v1"}]
                       :concepts  [{:id       "http://example.com/concept-2"
                                    :inScheme "http://example.com/v1"}]
                       :templates [{:id       "http://example.com/template"
                                    :inScheme "http://example.com/v1"}]}])]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"          2
                 "http://example.com/v1"       2
                 "http://example.com/concept"  1
                 ;; concept-2 not included in the map
                 "http://example.com/template" 2}}
               (::s/value spec-ed)))))))

(deftest validate-same-inschemes-test
  (testing "validate-same-inscheme function"
    (is (nil? (id/validate-same-inschemes
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (testing "- inScheme not in version IDs"
      (let [spec-ed (id/validate-same-inschemes
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v4"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (->> spec-ed
                 ::s/problems
                 (every? #(= `id/has-inscheme? (:pred %)))))
        (is (= #{{:id         "http://example.com/template"
                  :inScheme   "http://example.com/v1"
                  :versionIds #{"http://example.com/v1"}}
                 {:id         "http://example.com/pattern"
                  :inScheme   "http://example.com/v1"
                  :versionIds #{"http://example.com/v1"}}}
               (-> spec-ed ::s/value (get "http://example.com/v1") set)))
        (is (= #{{:id         "http://example.com/concept"
                  :inScheme   "http://example.com/v4"
                  :versionIds #{"http://example.com/v1"}}}
               (-> spec-ed ::s/value (get "http://example.com/v4") set)))))
    (testing "- not same inScheme"
      (let [spec-ed (id/validate-same-inschemes
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}
                                  {:id "http://example.com/v2"}
                                  {:id "http://example.com/v3"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template-1"
                                   :inScheme "http://example.com/v2"}
                                  {:id       "http://example.com/pattern-2"
                                   :inScheme "http://example.com/v2"}]})]
        (is (some? spec-ed))
        (is (->> spec-ed
                 ::s/problems
                 (every? #(not= `id/has-inscheme? (:pred %)))))
        (is (= 2
               (-> spec-ed ::s/value count))))
      (let [spec-ed (id/validate-same-inschemes
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}
                                  {:id "http://example.com/v2"}
                                  {:id "http://example.com/v3"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v2"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v3"}]})]
        (is (= 3
               (-> spec-ed ::s/value count)))))))

(deftest validate-ids-by-inscheme-test
  (testing "validate-ids-by-inscheme function"
    (is (nil? (id/validate-ids-by-inscheme
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (is (nil? (id/validate-ids-by-inscheme
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}
                            {:id "http://example.com/v2"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}
                            {:id       "http://example.com/concept"
                             :inScheme "http://example.com/v2"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}
                            {:id       "http://example.com/template"
                             :inScheme "http://example.com/v2"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}
                            {:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v2"}]})))
    (testing "- duplicate IDs"
      (let [spec-ed (id/validate-ids-by-inscheme
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"    1
                 "http://example.com/v1" 1
                 "http://example.com/x"  3}}
               (::s/value spec-ed))))
      (let [spec-ed (id/validate-ids-by-inscheme
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v1"}]
                      :templates [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v2"}]
                      :patterns  [{:id       "http://example.com/x"
                                   :inScheme "http://example.com/v2"}]})]
        (is (some? spec-ed))
        (is (= {"http://example.com/v1"
                {"http://example.com"    1
                 "http://example.com/v1" 1
                 "http://example.com/x"  1}
                "http://example.com/v2"
                {"http://example.com"    1
                 "http://example.com/v1" 1
                 "http://example.com/x"  2}}
               (::s/value spec-ed)))))))

(deftest validate-inschemes-test
  (testing "validate-inschemes function"
    (is (nil? (id/validate-inschemes
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (is (nil? (id/validate-inschemes
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}
                            {:id "http://example.com/v2"}
                            {:id "http://example.com/v3"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v2"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v3"}]})))
    (testing "- inScheme not in version IDs"
      (let [spec-ed (id/validate-same-inschemes
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}]
                      :concepts  [{:id       "http://example.com/concept"
                                   :inScheme "http://example.com/v4"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"}]
                      :patterns  [{:id       "http://example.com/pattern"
                                   :inScheme "http://example.com/v1"}]})]
        (is (some? spec-ed))
        (is (= #{{:id         "http://example.com/template"
                  :inScheme   "http://example.com/v1"
                  :versionIds #{"http://example.com/v1"}}
                 {:id         "http://example.com/pattern"
                  :inScheme   "http://example.com/v1"
                  :versionIds #{"http://example.com/v1"}}}
               (-> spec-ed ::s/value (get "http://example.com/v1") set)))
        (is (= #{{:id         "http://example.com/concept"
                  :inScheme   "http://example.com/v4"
                  :versionIds #{"http://example.com/v1"}}}
               (-> spec-ed ::s/value (get "http://example.com/v4") set)))))))

(deftest validate-version-change
  (testing "validate-version-change function"
    (is (nil? (id/validate-version-change
               {:id        "http://example.com"
                :versions  [{:id "http://example.com/v1"}]
                :concepts  [{:id       "http://example.com/concept"
                             :inScheme "http://example.com/v1"}]
                :templates [{:id       "http://example.com/template"
                             :inScheme "http://example.com/v1"}]
                :patterns  [{:id       "http://example.com/pattern"
                             :inScheme "http://example.com/v1"}]})))
    (is (nil? (id/validate-version-change
               {:id        "http://example.com"
                :versions [{:id "http://example.com/v1"}
                           {:id "http://example.com/v2"}]
                :concepts [{:id         "http://example.com/concept"
                            :inScheme   "http://example.com/v1"
                            :deprecated false}
                           {:id         "http://example.com/concept"
                            :inScheme   "http://example.com/v2"
                            :deprecated true}]})))
    (let [spec-ed (id/validate-version-change
                   {:id        "http://example.com"
                    :versions [{:id "http://example.com/v1"}
                               {:id "http://example.com/v2"}]
                    :concepts [{:id       "http://example.com/concept"
                                :inScheme "http://example.com/v1"
                                :broader  ["http://example.org/concept-2"]}
                               {:id       "http://example.com/concept"
                                :inScheme "http://example.com/v2"
                                :broader  ["http://example.org/concept-3"]}]
                    :patterns [{:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v1"}
                               {:id       "http://example.com/pattern"
                                :inScheme "http://example.com/v2"}]})]
      (is (some? spec-ed))
      (is (= #{[{:id "http://example.com/concept",
                 :inScheme "http://example.com/v1",
                 :broader ["http://example.org/concept-2"]}
                {:id "http://example.com/concept",
                 :inScheme "http://example.com/v2",
                 :broader ["http://example.org/concept-3"]}]
               [{:id       "http://example.com/pattern"
                 :inScheme "http://example.com/v1"}
                {:id       "http://example.com/pattern"
                 :inScheme "http://example.com/v2"}]}
             (set (::s/value spec-ed)))))
    (testing "- template-specific properties"
      (is (nil? (id/validate-version-change
                 {:id        "http://example.com"
                  :versions  [{:id "http://example.com/v1"}
                              {:id "http://example.com/v2"}]
                  :templates [{:id       "http://example.com/template"
                               :inScheme "http://example.com/v1"
                               :rules    [{:location  "$.foo"
                                           :presence  "included"}]}
                              {:id       "http://example.com/template"
                               :inScheme "http://example.com/v2"
                               :rules    [{:location  "$.foo"
                                           :presence  "included"
                                           :scopeNote "bar"}]}
                              {:id       "http://example.com/template-other"
                               :inScheme "http://example.com/v2"
                               :verb     "http://example.org/verb"}]})))
      (let [spec-ed (id/validate-version-change
                     {:id        "http://example.com"
                      :versions  [{:id "http://example.com/v1"}
                                  {:id "http://example.com/v2"}]
                      :templates [{:id       "http://example.com/template"
                                   :inScheme "http://example.com/v1"
                                   :rules    [{:location  "$.foo"
                                               :presence  "included"}]}
                                  {:id       "http://example.com/template"
                                   :inScheme "http://example.com/v2"
                                   :rules    [{:location  "$.baz"
                                               :presence  "included"
                                               :scopeNote "bar"}]}
                                  {:id       "http://example.com/template-other"
                                   :inScheme "http://example.com/v2"
                                   :verb     "http://example.org/verb"}]})]
        (is (some? spec-ed))
        (is (= #{[{:id       "http://example.com/template"
                   :inScheme "http://example.com/v1"
                   :rules    [{:location  "$.foo"
                               :presence  "included"}]}
                  {:id       "http://example.com/template"
                   :inScheme "http://example.com/v2"
                   :rules    [{:location  "$.baz"
                               :presence  "included"
                               :scopeNote "bar"}]}]
                 [{:id       "http://example.com/template-other"
                   :inScheme "http://example.com/v2"
                   :verb     "http://example.org/verb"}]}
               (set (::s/value spec-ed))))))))
