(ns com.yetanalytics.project-pan-test
  (:require [clojure.test :refer :all]
            [com.yetanalytics.project-pan :refer :all]))

(def will-profile (slurp "resources/sample_profiles/will-profile.json"))

(validate-profile will-profile)

(validate-profile will-profile :validation-level 1)

(def dod-profile (slurp "resources/sample_profiles/dod-isd.json"))

(validate-profile dod-profile)

;; TODO Fix validator if concepts, templates or pattern array is nil
; (validate-profile dod-profile :validation-level 1)

(def scorm-profile (slurp "resources/sample_profiles/scorm.json"))

(validate-profile scorm-profile)
