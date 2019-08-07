(ns com.yetanalytics.project-pan-test
  (:require [clojure.pprint :as pprint]
            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.profile :as profile]
            [com.yetanalytics.objects.concept :as c]
            [com.yetanalytics.objects.template :as t]
            [com.yetanalytics.objects.pattern :as p]
            [com.yetanalytics.project-pan :refer :all]))

;; XXX This is a demo suite

;; Profiles to test
(def will-profile-raw (slurp "resources/sample_profiles/will-profile-raw.json"))
(def will-profile-fix (slurp "resources/sample_profiles/will-profile-reduced.json"))
(def cmi-profile-raw (slurp "resources/sample_profiles/cmi5.json"))

;; Raw profile
(validate-profile will-profile-raw)
(validate-profile will-profile-raw :ids true)
(validate-profile will-profile-raw :relations true)
(validate-profile will-profile-raw :contexts true)

;; Fixed profile
(validate-profile will-profile-fix)
(validate-profile will-profile-fix :ids true)
(validate-profile will-profile-fix :relations true)
(validate-profile will-profile-fix :contexts true)

;; Raw profile
