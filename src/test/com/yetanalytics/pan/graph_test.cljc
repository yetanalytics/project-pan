(ns com.yetanalytics.pan.graph-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.yetanalytics.pan.graph :as graph]
            [com.yetanalytics.test-utils :refer [instrumentation-fixture]]))

(use-fixtures :once instrumentation-fixture)

(def ex-graph
  (-> (graph/new-digraph)
      (graph/add-nodes [["foo" {:num 0}]])
      (graph/add-nodes [["bar" {:num 1}]])
      (graph/add-edges [["foo" "bar" {:weight 1.2}]])))

(deftest graph-test
  (testing "graph creation and operations"
    (is (some? (graph/new-digraph)))
    (is (= #{"foo" "bar"} (set (graph/nodes ex-graph))))
    (is (= ["foo" "bar"] (first (graph/edges ex-graph))))
    (is (= 0 (graph/attr ex-graph "foo" :num)))
    (is (= 1 (graph/attr ex-graph "bar" :num)))
    (is (= 1.2 (graph/attr ex-graph ["foo" "bar"] :weight)))
    (is (= 0 (graph/in-degree ex-graph "foo")))
    (is (= 1 (graph/in-degree ex-graph "bar")))
    (is (= 1 (graph/out-degree ex-graph "foo")))
    (is (= 0 (graph/out-degree ex-graph "bar")))
    (is (= [["foo"] ["bar"]] (graph/scc ex-graph)))
    (is (= [["bar" "foo"]]
           (graph/scc (graph/add-edges ex-graph [["bar" "foo"]])))))
  (testing "src and dest functions on lone edges"
    (is (= "foo" (graph/src ["foo" "bar"])))
    (is (= "foo" (graph/src {:src "foo" :dest "bar"})))
    (is (= "bar" (graph/dest ["foo" "bar"])))
    (is (= "bar" (graph/dest {:src "foo" :dest "bar"})))))

;; 2 strongly connected components:
;; SCC 1 is :a -> :b -> :c
;; SCC 2 is :d -> :e -> :f
;; :c -> :d connects the two SCCs
(def ex-scc-graph-1
  (-> (graph/new-digraph)
      (graph/add-nodes (map (fn [x] [x {}]) [:a :b :c :d :e :f]))
      (graph/add-edges (map (fn [[x y]] [x y {}])
                            [[:a :b] [:b :c] [:c :a] [:c :d]
                             [:d :e] [:e :f] [:f :d]]))))

;; 4 strongly connected components
;; Copied from https://github.com/aysylu/loom/blob/master/test/loom/test/alg.cljc#L107
(def ex-scc-graph-2
  (-> (graph/new-digraph)
      (graph/add-edges (map (fn [[n1 n2]] [n1 n2 {}])
                            [[1 5]
                             [2 4]
                             [3 1]
                             [3 2]
                             [3 6]
                             [4 10]
                             [5 3]
                             [6 1]
                             [6 10]
                             [7 8]
                             [8 9]
                             [8 11]
                             [9 3]
                             [9 5]
                             [9 7]
                             [10 2]
                             [11 2]
                             [11 4]]))))

;; Graph with no edges (every node is its own SCC)
(def ex-scc-graph-3
  (-> (graph/new-digraph)
      (graph/add-nodes (map (fn [n] [n]) (range 0 11)))))

(deftest scc-test
  (testing "Kosaraju's algorithm"
    (is (= #{#{:a :b :c} #{:d :e :f}}
           (->> (graph/scc ex-scc-graph-1) (map set) set)))
    ;; Copied from https://github.com/aysylu/loom/blob/master/test/loom/test/alg.cljc#L450
    (is (= #{#{2 4 10} #{1 3 5 6} #{11} #{7 8 9}}
           (->> (graph/scc ex-scc-graph-2) (map set) set)))
    (is (= [[0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]]
           (->> (graph/scc ex-scc-graph-3) sort vec)))))
