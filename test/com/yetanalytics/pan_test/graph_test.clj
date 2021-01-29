(ns com.yetanalytics.pan-test.graph-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.yetanalytics.pan.graph :as graph]))

#_(deftest validate-iris
    (testing "graph creation and evaluation"
      (is (empty? (graph/validate-iris
                   {:concepts [{:id "https://foo.org/verb1"
                                :broader ["https://foo.org/verb2"]}
                               {:id "https://foo.org/verb2"}]
                    :templates [{:id "https://foo.org/template"
                                 :verb "https://foo.org/verb1"}]
                    :patterns [{:id "https://foo.org/pattern"
                                :optional "https://foo.org/template"}]})))))
