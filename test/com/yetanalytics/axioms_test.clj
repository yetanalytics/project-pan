(ns com.yetanalytics.axioms-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.utils :refer :all]))

;; JSONPath tests

;; Basic unit tests
(deftest test-jsonpath
  (testing "JSONPath string"
    (is (s/valid? ::ax/json-path "$.store"))
    (is (s/valid? ::ax/json-path "$.store.book"))
    (is (s/valid? ::ax/json-path "$['store']['book'][0]"))
    (is (s/valid? ::ax/json-path "$.store['book'][ * ]"))
    (is (s/valid? ::ax/json-path "$['store\\|$io'] | $.foo.bar"))
    (is (not (s/valid? ::ax/json-path "what the pineapple")))))

;; Test in bulk

(deftest test-jsonpath-dot
  (testing "JSONPath strings with dot child notation"
    (should-satisfy+ ::ax/json-path
                     "$"              ; Jayway only
                     "$.store"
                     "$.store.book"
                     "$..book"
                     "$.*"
                     "$.*.*"
                     "$.store.*"
                     "$.*.book"
                     "$..*"
                     "$.**"
                     "$.store*io"
                     :bad
                     ""
                     ".store"
                     "$."
                     "$.."
                     "$store"
                     "$.store,expensive"
                     "$.store, expensive"
                     "$.store expensive"
                     "$.store[expensive"
                     "$. store"
                     "what the pineapple")))

(deftest test-jsonpath-bracket
  (testing "JSONPath strings with bracket child notation"
    (should-satisfy+ ::ax/json-path
                     "$['store']"
                     "$['store shop']"
                     "$['store']['book']"
                     "$[   ' store '   ]"   ; Jayway supports
                     "$.store['book']"
                     "$['store'].book"
                     "$..['book']"
                     "$..['author']"
                     "$[*]"
                     "$[*][*]"
                     "$[    *    ]"
                     "$['*']"               ; Is a string, not a wildcard in Jayway
                     "$['store'][*]"
                     "$['store']['book']['*']"
                     "$[*]['books']"
                     "$['store\\'s']"        ; Jayway supports escaped chars
                     "$['store\\,s']"        ; Jayway supports escaped chars 
                     "$['store']['book*']"  ; All chars except ',' and ''' supported
                     "$['store','expensive']"
                     "$['store', 'expensive']"
                     "$['store',           'expensive']"
                     :bad
                     "['store']"
                     "$[]"
                     "$['store"
                     "$[**]"
                     "$[store]"             ; Jayway does not support
                     "$['store'] ['book']"
                     "$['']"                ; No empty strings in xAPI Profile spec
                     "$['store,expensive']"
                     "$['store']book"
                     "$['store's']"
                     "$['store''expensive']"
                     "$['store' 'expensive']")))

(deftest test-jsonpath-array
  (testing "JSONPath strings that use array notation"
    (should-satisfy+ ::ax/json-path
                     "$..[0]"
                     "$..book[0]"
                     "$..book[1234]"
                     "$.store.book[0]"
                     "$.store.book[0, 1]"
                     "$.store.book[*]"
                     "$['store']['book'][0]"
                     "$['store']['book'][0, 1]"
                     "$['store']['book'][*]"
                     "$['store']['book'][0]['author']"
                     "$['store']['book']['*'][0]"
                     :bad
                     "$.store.book[[0]"
                     "$.store.book[0]]"
                     "$['store']['book'][0][0]"
                     "$..book[?(@isbn)]"  ; Filter functions not supported in xAPI Profile spec
                     "$..book[-1]"        ; Negative numbers not supported in xapi Profile spec
                     "$..book[0:6:2]"     ; Array slicing is not supported in xapi Profile spec
                     "$..book[l33t]")))

(deftest test-jsonpath-joined
  (testing "JSONPath strings that have been joined using the '|' char"
    (should-satisfy+ ::ax/json-path
                     "$.store.book|$.results.extensions"
                     "$.store.book  |$.results.extensions"
                     "$.store.book|$.results.extensions"
                     "$.store.book | $.results.extensions"
                     "$['store']['book']|$['results']['extensions']"
                     "$['store']['book'] | $['results']['extensions']"
                     "$['store']['book'][0] | $.results.extensions['https://foo.org']"
                     "$['store\\|$.io']"  ; Need to allow the user to escape
                     "$['store   \\|$.io']"
                     :bad
                     "$.store.book $.results.extension"
                     "$.store.book | what the pineapple"
                     "$['store|$.io']"
                     "$['store|$['io']]")))

;; Test runner
(run-tests)
