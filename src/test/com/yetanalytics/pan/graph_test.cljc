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

(defn- transpose
  [adj]
  (reduce-kv (fn [m in outs]
               (reduce (fn [m* out]
                         (update m* out (fnil conj #{in}) in))
                       m
                       outs))
             {}
             adj))

;; Examples taken from the TC3 pofile
(def ex-scc-graph-4-nodes
  #{"soft-buddy-looped-after-init-prior-termination"
    "soft-buddy-looped-optional-played"
    "soft-buddy-looped-played"
    "soft-buddy-looped-after-played-branch"
    "soft-buddy-looped-with-completion"
    "soft-buddy-looped-without-completion"
    "completed:soft_buddy_looped"
    "soft-buddy-looped-after-branch-prior-completion-zero+"
    "soft-buddy-looped-after-branch-prior-completion"
    "soft-buddy-looped-pause"
    "paused:soft-buddy-looped"
    "soft-buddy-looped-maybe-resume"
    "soft-buddy-looped-maybe-any-time"
    "played:soft_buddy_looped"
    "soft-buddy-looped-any-time-after-init-but-before-termination"
    "volumechange:soft_buddy_looped"
    "seeked:soft_buddy_looped"})

(def ex-scc-graph-4-adj
  {"soft-buddy-looped-after-init-prior-termination"
   #{"soft-buddy-looped-optional-played"
     "soft-buddy-looped-maybe-any-time"}
   "soft-buddy-looped-optional-played"
   #{"soft-buddy-looped-played"}
   "soft-buddy-looped-played"
   #{"soft-buddy-looped-after-played-branch"
     "soft-buddy-looped-maybe-any-time"
     "played:soft_buddy_looped"}
   "soft-buddy-looped-after-played-branch"
   #{"soft-buddy-looped-with-completion"
     "soft-buddy-looped-without-completion"}
   "soft-buddy-looped-with-completion"
   #{"soft-buddy-looped-without-completion"
     "completed:soft_buddy_looped"}
   "completed:soft_buddy_looped"
   #{}
   "soft-buddy-looped-without-completion"
   #{"soft-buddy-looped-after-branch-prior-completion-zero+"
     "soft-buddy-looped-maybe-any-time"}
   "soft-buddy-looped-after-branch-prior-completion-zero+"
   #{"soft-buddy-looped-after-branch-prior-completion"}
   "soft-buddy-looped-after-branch-prior-completion"
   #{"soft-buddy-looped-any-time-after-init-but-before-termination"
     "soft-buddy-looped-pause"}
   "soft-buddy-looped-pause"
   #{"paused:soft-buddy-looped"
     "soft-buddy-looped-maybe-resume"
     "soft-buddy-looped-maybe-any-time"}
   "paused:soft-buddy-looped"
   #{}
   "soft-buddy-looped-maybe-resume"
   #{"played:soft_buddy_looped"}
   "played:soft_buddy_looped"
   #{}
   "soft-buddy-looped-maybe-any-time"
   #{"soft-buddy-looped-any-time-after-init-but-before-termination"}
   "soft-buddy-looped-any-time-after-init-but-before-termination"
   #{"volumechange:soft_buddy_looped"
     "seeked:soft_buddy_looped"}
   "volumechange:soft_buddy_looped"
   #{}
   "seeked:soft_buddy_looped"
   #{}})

(def ex-scc-graph-4
  {:nodeset ex-scc-graph-4-nodes
   :adj     ex-scc-graph-4-adj
   :in      (transpose ex-scc-graph-4-adj)})

(def ex-scc-graph-5-nodes ex-scc-graph-4-nodes)

(def ex-scc-graph-5-adj
  (-> ex-scc-graph-4-adj
      (update "volumechange:soft_buddy_looped" conj "seeked:soft_buddy_looped")
      (update "seeked:soft_buddy_looped" conj "volumechange:soft_buddy_looped")))

(def ex-scc-graph-5
  {:nodeset ex-scc-graph-5-nodes
   :adj     ex-scc-graph-5-adj
   :in      (transpose ex-scc-graph-5-adj)})

(deftest scc-test
  (testing "Kosaraju's algorithm"
    (is (= #{#{:a :b :c} #{:d :e :f}}
           (->> (graph/scc ex-scc-graph-1) (map set) set)))
    ;; Copied from https://github.com/aysylu/loom/blob/master/test/loom/test/alg.cljc#L450
    (is (= #{#{2 4 10} #{1 3 5 6} #{11} #{7 8 9}}
           (->> (graph/scc ex-scc-graph-2) (map set) set)))
    (is (= [[0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]]
           (->> (graph/scc ex-scc-graph-3) sort vec)))
    ;; Previous bug where false positive cycles were detected, due to the
    ;; finishing order potentially having duplicate nodes from multiple visits
    (is (->> (graph/scc ex-scc-graph-4)
             (every? #(= 1 (count %)))))
    (is (->> (graph/scc ex-scc-graph-5)
             (some #{["volumechange:soft_buddy_looped" "seeked:soft_buddy_looped"]
                     ["seeked:soft_buddy_looped" "volumechange:soft_buddy_looped"]})
             some?))))
