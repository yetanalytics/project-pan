(ns com.yetanalytics.pan.utils.json-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.yetanalytics.pan.utils.json :as json]))

(deftest convert-json-test
  (testing "convert-json function"
    (is (= {:foo 1 :bar {:baz 2}}
           (json/convert-json "{\"foo\" : 1, \" bar\" : {\"baz \" : 2}}")))
    (is (= {:foo 1 :bar {:baz 2}}
           (json/convert-json "{\"@foo\" : 1, \"@bar\" : {\"@baz\" : 2}}" "")))
    (is (= {:_foo 1 :_bar {:_baz 2}}
           (json/convert-json "{\"@foo\" : 1, \"@bar\" : {\"@baz\" : 2}}" "_")))
    (is (= {:_foo 1 :_bar {:_baz 2}}
           (json/convert-json "{\"@foo\" : 1, \"@bar\" : {\"@baz\" : 2}}")))
    (is (= {"http://foo.org" {"http://bar.com" 123}}
           (json/convert-json "{\"http://foo.org\": {\"http://bar.com\": 123}}")))))
