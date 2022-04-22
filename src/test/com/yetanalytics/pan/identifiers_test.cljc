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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Util Tests 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(deftest dissoc-properties-test
  (testing "dissoc-properties function"
    (is (= {:id "https://w3id.org/catch/v2/some-verb"
            :broader ["https://w3id.org/catch/a-verb"]}
           (id/dissoc-properties
            {:id "https://w3id.org/catch/v2/some-verb"
             :inScheme "https://w3id.org/catch/v2"
             :deprecated true
             :broader ["https://w3id.org/catch/a-verb"]})))
    (is (= {:id "https://w3id.org/catch/v2/some-template"
            :verb ["https://w3id.org/catch/a-verb"]
            :rules [{:location "$.foo"
                     :presence "included"}]}
           (id/dissoc-properties
            {:id "https://w3id.org/catch/v2/some-template"
             :inScheme "https://w3id.org/catch/v2"
             :deprecated true
             :verb ["https://w3id.org/catch/a-verb"]
             :rules [{:location "$.foo"
                      :presence "included"
                      :scopeNote ["Scope Note One"]}]})))))

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
