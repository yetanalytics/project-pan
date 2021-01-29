(ns com.yetanalytics.pan-test.graph-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.yetanalytics.pan.graph :as graph]))

(def ex-graph
  (-> (graph/new-digraph)
      (graph/add-nodes [["foo" {:num 0}]])
      (graph/add-nodes [["bar" {:num 1}]])
      (graph/add-edges [["foo" "bar" {:weight 1.2}]])))

(deftest graph-test
  (testing "graph creation and operations"
    (is (= "foo" (graph/src ["foo" "bar"])))
    (is (= "bar" (graph/dest ["foo" "bar"])))
    (is (= #{"foo" "bar"} (set (graph/nodes ex-graph))))
    (is (= ["foo" "bar"] (first (graph/edges ex-graph))))
    (is (= 0 (graph/attr ex-graph "foo" :num)))
    (is (= 1 (graph/attr ex-graph "bar" :num)))
    (is (= 1.2 (graph/attr ex-graph ["foo" "bar"] :weight)))
    (is (= 0 (graph/in-degree ex-graph "foo")))
    (is (= 1 (graph/in-degree ex-graph "bar")))
    (is (= 1 (graph/out-degree ex-graph "foo")))
    (is (= 0 (graph/out-degree ex-graph "bar")))
    (is (= [["foo"] ["bar"]] (graph/scc ex-graph)))))