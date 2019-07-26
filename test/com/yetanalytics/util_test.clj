(ns com.yetanalytics.util_test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            ;; Not to be confused with util.clj in src
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.util :as util]))

;; Specs we will be using for our tests

(s/def ::id-spec #(contains? % :id))

(s/def ::id-spec+
  (s/and (fn [m] (s/valid? ::id-spec (m :object)))
         (fn [m] (true? (m :bool)))))

(s/def ::map-id-spec+
  (s/coll-of ::id-spec+ :type vector?))

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

(def snsd-ot9 (conj snsd-ot8 {:not-id "Jessica" :another-key "Krystal"}))

;; Our tests relating to custom specs

(deftest only-ids-test
  (testing "only-ids function"
    (is (= (util/only-ids number-list) [1 3 5 7 9]))
    (is (= (util/only-ids snsd-ot8)
           ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung"]))
    (is (= (util/only-ids snsd-ot9)
           ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung" nil]))))

(deftest only-ids-multiple-test
  (testing "only-ids-multiple function"
    (is (= (util/only-ids-multiple [number-list]) [1 3 5 7 9]))
    (is (= (util/only-ids-multiple [snsd-ot8 snsd-ot9])
           ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon" "Yoona" "Yuri" "Sunny"
            "Sooyoung" "Taeyeon" "Tiffany" "Seohyun" "Hyoyeon" "Yoona" "Yuri"
            "Sunny" "Sooyoung" nil]))))

(deftest count-ids-test
  (testing "count-ids function"
    (is (= (util/count-ids (util/only-ids number-list))
           {1 1, 3 1, 5 1, 7 1, 9 1}))
    (is (= (util/count-ids (util/only-ids snsd-ot8))
           {"Taeyeon" 1 "Tiffany" 1 "Seohyun" 1 "Hyoyeon" 1
            "Yoona" 1 "Yuri" 1 "Sunny" 1 "Sooyoung" 1}))
    (is (= (util/count-ids (util/only-ids-multiple [snsd-ot8 snsd-ot9]))
           {"Taeyeon" 2 "Tiffany" 2 "Seohyun" 2 "Hyoyeon" 2
            "Yoona" 2 "Yuri" 2 "Sunny" 2 "Sooyoung" 2 nil 1}))))

(deftest distinct-ids-test
  (testing "distinct-ids spec"
    (is (s/valid? ::util/distinct-ids {"https://foo.org" 1
                                       "https://bar.org" 1}))
    (is (not (s/valid? ::util/distinct-ids {"https://foo.org" 1
                                            "https://bar.org" 2})))
    (is (not (s/valid? ::util/distinct-ids {"https://foo.org" 1
                                            "https://bar.org" 1
                                            "what the pineapple" 1})))))

; #_(deftest id-map-test
;   (testing "id-map function"
;     (let [snsd-map (util/id-map snsd-ot9)]
;       (is (= (snsd-map "Taeyeon") {:id "Taeyeon"}))
;       (is (= (snsd-map "Tiffany") {:id "Tiffany"}))
;       (is (= (snsd-map "Seohyun") {:id "Seohyun"}))
;       (is (= (snsd-map "Hyoyeon") {:id "Hyoyeon"}))
;       (is (= (snsd-map "Yoona") {:id "Yoona"}))
;       (is (= (snsd-map "Yuri") {:id "Yuri"}))
;       (is (= (snsd-map "Sunny") {:id "Sunny" :some-key "Yet Analytics"}))
;       (is (= (snsd-map "Sooyoung") {:id "Sooyoung" :another-key true}))
;       (is (not= (snsd-map "Jessica") {:not-id "Jessica" :another-key "Krystal"}))
;       (is (not= (snsd-map nil) {:not-id "Jessica" :another-key "Krystal"}))
;       (is (= (snsd-map "Jessica") nil))
;       (is (= (snsd-map "Foo Bar") nil)))))

; #_(deftest combine-args-test
;   (testing "combine-args function"
;     (is (= (util/combine-args snsd-ot8 {:bool true})
;            [{:object {:id "Taeyeon"} :bool true}
;             {:object {:id "Tiffany"} :bool true}
;             {:object {:id "Seohyun"} :bool true}
;             {:object {:id "Hyoyeon"} :bool true}
;             {:object {:id "Yoona"} :bool true}
;             {:object {:id "Yuri"} :bool true}
;             {:object {:id "Sunny" :some-key "Yet Analytics"} :bool true}
;             {:object {:id "Sooyoung" :another-key true} :bool true}]))))

; #_(deftest map-spec-test
;   (testing "the correctness of specs that utilize util/combine-args"
;     (is (s/valid? ::map-id-spec+ (util/combine-args snsd-ot8 {:bool true})))
;     (is (not (s/valid? ::map-id-spec+ (util/combine-args snsd-ot9 {:bool true}))))
;     (is (not (s/valid? ::map-id-spec+ (util/combine-args snsd-ot8 {:bool false}))))
;     (is (= 0 (count (::s/problems (s/explain-data ::map-id-spec+ (util/combine-args snsd-ot8 {:bool true}))))))
;     (is (= 1 (count (::s/problems (s/explain-data ::map-id-spec+ (util/combine-args snsd-ot9 {:bool true}))))))
;     (is (= 8 (count (::s/problems (s/explain-data ::map-id-spec+ (util/combine-args snsd-ot8 {:bool false}))))))))

; (deftest in-scheme-valid-test
;   (testing "inScheme property MUST point to a valid version ID"
;     (is (s/valid? ::util/in-scheme-valid?
;                   {:object {:in-scheme
;                             "https://foo.org/version1"}
;                    :vid-set {"https://foo.org/version1"
;                              "https://foo.org/version2"}}))
;     (is (not (s/valid? ::util/in-scheme-valid?
;                        {:object {:in-scheme "https://foo.org/version0"}
;                         :vid-set {"https://foo.org/version1"
;                                   "https://foo.org/version2"}})))))
