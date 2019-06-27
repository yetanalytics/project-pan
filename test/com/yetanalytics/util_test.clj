(ns com.yetanalytics.util_test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            ;; Not to be confused with util.clj in src
            [com.yetanalytics.utils :refer :all]
            [com.yetanalytics.util :as util]))

;; Specs we will be using for our tests

(s/def ::id-spec #(contains? % :id))

(s/def ::id-spec+
  (fn [{:keys [obj bool]}]
    (and (s/valid? ::id-spec obj)
         (= true bool))))

(s/def ::map-id-spec+
  (fn [{obj :obj :as args}]
    (util/spec-map+ ::id-spec+ :obj obj args)))

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

(deftest only-ids-test
  (testing "only-ids function"
    (is (= (util/only-ids number-list) [1 3 5 7 9]))
    (is (= (util/only-ids snsd-ot8)
           ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung"]))
    (is (= (util/only-ids snsd-ot9)
           ["Taeyeon" "Tiffany" "Seohyun" "Hyoyeon"
            "Yoona" "Yuri" "Sunny" "Sooyoung" nil]))))

(deftest id-map-test
  (testing "id-map function"
    (let [snsd-map (util/id-map snsd-ot9)]
      (is (= (snsd-map "Taeyeon") {:id "Taeyeon"}))
      (is (= (snsd-map "Tiffany") {:id "Tiffany"}))
      (is (= (snsd-map "Seohyun") {:id "Seohyun"}))
      (is (= (snsd-map "Hyoyeon") {:id "Hyoyeon"}))
      (is (= (snsd-map "Yoona") {:id "Yoona"}))
      (is (= (snsd-map "Yuri") {:id "Yuri"}))
      (is (= (snsd-map "Sunny") {:id "Sunny" :some-key "Yet Analytics"}))
      (is (= (snsd-map "Sooyoung") {:id "Sooyoung" :another-key true}))
      (is (not= (snsd-map "Jessica") {:not-id "Jessica" :another-key "Krystal"}))
      (is (not= (snsd-map nil) {:not-id "Jessica" :another-key "Krystal"}))
      (is (= (snsd-map "Jessica") nil))
      (is (= (snsd-map "Foo Bar") nil)))))

(deftest explain-spec-map-test
  (testing "explain-spec-map function"
    (is (= [] (util/explain-spec-map ::id-spec snsd-ot8)))
    (is (= 0 (count (util/explain-spec-map ::id-spec snsd-ot8))))
    (is (= 1 (count (util/explain-spec-map ::id-spec snsd-ot9))))
    (is (= 8 (count (util/explain-spec-map ::id-spec
                                           [{:not-id 1} {:not-id 2}
                                            {:not-id 3} {:not-id 4}
                                            {:not-id 5} {:not-id 6}
                                            {:not-id 7} {:not-id 8}]))))))

(s/def ::map-id-spec+
  (fn [{obj :obj :as args}]
    (util/spec-map+ ::id-spec+ :obj obj args)))

(deftest spec-map-plus-test
  (testing "spec-map+ function"
    (is (s/valid? ::map-id-spec+ {:obj snsd-ot8 :boolean true}))
    (is (not (s/valid? ::map-id-spec+ {:obj snsd-ot9 :boolean true})))
    (is (not (s/valid? ::map-id-spec+ {:obj snsd-ot8 :boolean false})))))

(fn [{:keys [obj bool]}]
  (print obj)
  (s/valid? ::id-spec obj))

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

(deftest id-map-test
  (testing "id-map function"
    (let [snsd-map (util/id-map snsd-ot9)]
      (is (= (snsd-map "Taeyeon") {:id "Taeyeon"}))
      (is (= (snsd-map "Tiffany") {:id "Tiffany"}))
      (is (= (snsd-map "Seohyun") {:id "Seohyun"}))
      (is (= (snsd-map "Hyoyeon") {:id "Hyoyeon"}))
      (is (= (snsd-map "Yoona") {:id "Yoona"}))
      (is (= (snsd-map "Yuri") {:id "Yuri"}))
      (is (= (snsd-map "Sunny") {:id "Sunny" :some-key "Yet Analytics"}))
      (is (= (snsd-map "Sooyoung") {:id "Sooyoung" :another-key true}))
      (is (not= (snsd-map "Jessica") {:not-id "Jessica" :another-key "Krystal"}))
      (is (not= (snsd-map nil) {:not-id "Jessica" :another-key "Krystal"}))
      (is (= (snsd-map "Jessica") nil))
      (is (= (snsd-map "Foo Bar") nil)))))

(deftest explain-spec-map-test
  (testing "explain-spec-map function"
    (is (= [] (util/explain-spec-map ::id-spec snsd-ot8)))
    (is (= 0 (count (util/explain-spec-map ::id-spec snsd-ot8))))
    (is (= 1 (count (util/explain-spec-map ::id-spec snsd-ot9))))
    (is (= 8 (count (util/explain-spec-map ::id-spec
                                           [{:not-id 1} {:not-id 2}
                                            {:not-id 3} {:not-id 4}
                                            {:not-id 5} {:not-id 6}
                                            {:not-id 7} {:not-id 8}]))))))

(deftest spec-map-plus-test
  (testing "spec-map+ function"
    (is (s/valid? ::map-id-spec+ {:obj snsd-ot8 :bool true}))
    (is (not (s/valid? ::map-id-spec+ {:obj snsd-ot9 :bool true})))
    (is (not (s/valid? ::map-id-spec+ {:obj snsd-ot8 :bool false})))))

(deftest explain-spec-map-plus-test
  (testing "explain-spec-map+ function"
    (is (= 0 (count (util/explain-spec-map+
                     ::id-spec+ :obj snsd-ot8 {:obj snsd-ot8 :bool true}))))
    (is (= 1 (count (util/explain-spec-map+
                     ::id-spec+ :obj snsd-ot9 {:obj snsd-ot9 :bool true}))))
    (is (= 8 (count (util/explain-spec-map+
                     ::id-spec+ :obj snsd-ot8 {:obj snsd-ot8 :bool false}))))))
