(ns com.yetanalytics.project-pan-test
  (:require [clojure.pprint :as pprint]
            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.project-pan :refer :all]))

;; Profiles to test
(def will-profile-raw (slurp "resources/sample_profiles/will-profile-raw.json"))
(def will-profile-fix (slurp "resources/sample_profiles/will-profile-reduced.json"))
(def dod-profile (slurp "resources/sample_profiles/dod-isd.json"))
(def scorm-profile (slurp "resources/sample_profiles/scorm.json"))

;; Raw profile
(validate-profile will-profile-raw)
; (validate-profile will-profile-raw :ids true)
; (validate-profile will-profile-raw :relations true)
; (validate-profile will-profile-raw :contexts true)

;; Fixed profile
; (validate-profile will-profile-fix)
; (validate-profile will-profile-fix :ids true)
; (validate-profile will-profile-fix :relations true)
; (validate-profile will-profile-fix :contexts true)

; (pprint/pprint (validate-profile will-profile-red :relations true))

; (deftest will-profile-integration
;   (testing "Integration test of Will's CATCH profile"
;     (is (validate-profile will-profile))
;     (is (validate-profile will-profile :ids true))
;     (is (validate-profile will-profile-red :relations true))
;     (is (validate-profile will-profile :contexts true))))

; (deftest dod-profile-integration
;   (testing "Integration test of the DOD profile"
;     (is (validate-profile dod-profile))))

; #_(deftest scorm-profile-integration
;     (testing "Integration test of the SCORM profile"
;       (is (some? (validate-profile scorm-profile)))))
