(ns com.yetanalytics.pan-test
  (:require [clojure.pprint :as pprint]
            [clojure.test :refer :all]
            [com.yetanalytics.pan :refer :all]))

;; Profiles to test
(def will-profile-raw (slurp "resources/sample_profiles/will-profile-raw.json"))
(def will-profile-fix (slurp "resources/sample_profiles/will-profile-reduced.json"))
(def cmi-profile-raw (slurp "resources/sample_profiles/cmi5.json"))
(def cmi-profile-fix (slurp "resources/sample_profiles/cmi5-fixed.json"))

; ;; Raw profile
(deftest will-raw-test
  (testing "Tests on Will's CATCH profile, unfixed"
    (is (not= (with-out-str (validate-profile will-profile-raw))
              "Success!\n"))
    (is (not= (with-out-str (validate-profile will-profile-raw :syntax false :ids true))
              "Success!\n"))
    (is (not= (with-out-str (validate-profile will-profile-raw :syntax false :relations true))
              "Success!\n"))
    (is (= (with-out-str (validate-profile will-profile-raw :syntax false :contexts true))
           "Success!\n"))
    ;; Testing print-errs being off
    (is (some? (validate-profile will-profile-raw :print-errs false)))
    (is (contains? (validate-profile will-profile-raw :print-errs false)
                   :syntax-errors))
    (is (nil? (validate-profile will-profile-raw :syntax false :contexts true
                                :print-errs false)))))

;; Fixed profile
(deftest will-fix-test
  (testing "Tests on Will's CATCH profile, fixed"
    (is (= (with-out-str (validate-profile will-profile-fix))
           "Success!\n"))
    (is (= (with-out-str (validate-profile will-profile-fix :ids true))
           "Success!\n"))
    (is (= (with-out-str (validate-profile will-profile-fix :relations true))
           "Success!\n"))
    (is (= (with-out-str (validate-profile will-profile-fix :contexts true))
           "Success!\n"))))

;; Raw profile
(deftest cmi-profile-test
  (testing "Tests on the cmi5 profile"
    (is (not= (with-out-str (validate-profile cmi-profile-raw))
              "Success!\n"))
    (is (not= (with-out-str (validate-profile cmi-profile-raw :syntax false :ids true))
              "Success!\n"))
    (is (not= (with-out-str (validate-profile cmi-profile-raw :syntax false :relations true))
              "Success!\n"))
    (is (= (with-out-str (validate-profile cmi-profile-raw :syntax false :contexts true))
           "Success!\n"))))

;; Fixed profile
(deftest cmi-fixed-test
  (testing "Tests on the cmi5 profile after it has been fixed"
    (is (= (with-out-str (validate-profile cmi-profile-fix))
           "Success!\n"))
    (is (= (with-out-str (validate-profile cmi-profile-fix :syntax false :ids true))
           "Success!\n"))
    (is (not= (with-out-str (validate-profile cmi-profile-fix :syntax false :relations true))
              "Success!\n"))
    (is (= (with-out-str (validate-profile cmi-profile-fix :syntax false :contexts true))
           "Success!\n"))))
