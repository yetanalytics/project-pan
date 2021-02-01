(ns com.yetanalytics.pan-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.yetanalytics.pan :refer [validate-profile]]
            [com.yetanalytics.pan.util :as util]))

;; Profiles to test
(def will-profile-raw
  (util/read-resource "sample_profiles/will-profile-raw.json"))
(def will-profile-fix
  (util/read-resource "sample_profiles/will-profile-reduced.json"))
(def cmi-profile-raw
  (util/read-resource "sample_profiles/cmi5.json"))
(def cmi-profile-fix
  (util/read-resource "sample_profiles/cmi5-fixed.json"))

; ;; Raw profile
(deftest will-raw-test
  (testing "Tests on Will's CATCH profile, unfixed"
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw :syntax? false :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw :syntax? false :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-raw :syntax? false :contexts? true))))
    ;; Testing print-errs being off
    (is (some? (validate-profile will-profile-raw :print-errs? false)))
    (is (contains? (validate-profile will-profile-raw :print-errs? false)
                   :syntax-errors))
    (is (nil? (validate-profile will-profile-raw :syntax? false :contexts? true
                                :print-errs? false)))))

;; Fixed profile
(deftest will-fix-test
  (testing "Tests on Will's CATCH profile, fixed"
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-fix))))
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-fix :ids? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-fix :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-fix :contexts? true))))))

;; Raw profile
(deftest cmi-profile-test
  (testing "Tests on the cmi5 profile"
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw :syntax? false :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw :syntax? false :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-raw :syntax? false :contexts? true))))))

;; Fixed profile
(deftest cmi-fixed-test
  (testing "Tests on the cmi5 profile after it has been fixed"
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix :syntax? false :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-fix :syntax? false :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix :syntax? false :contexts? true))))))
