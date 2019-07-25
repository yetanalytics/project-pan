(ns com.yetanalytics.project-pan-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.project-pan :refer :all]))

;; Profiles to test
(def will-profile (slurp "resources/sample_profiles/will-profile.json"))
(def dod-profile (slurp "resources/sample_profiles/dod-isd.json"))
(def scorm-profile (slurp "resources/sample_profiles/scorm.json"))

(deftest will-profile-integration
  (testing "Integration test of Will's CATCH profile, basic validation"
    (is (validate-profile will-profile))
    (is (validate-profile will-profile :in-scheme true))
    (is (validate-profile will-profile :at-context true))))
