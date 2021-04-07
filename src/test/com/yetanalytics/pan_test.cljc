(ns com.yetanalytics.pan-test
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan :refer [validate-profile]]
            [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.utils.json :as json]
            [com.yetanalytics.pan-test-fixtures :as fix])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-resource]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profiles to test
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def will-profile-raw
  (read-resource "sample_profiles/catch.json"))
(def cmi-profile-raw
  (read-resource "sample_profiles/cmi5.json"))
(def acrossx-profile-raw
  (read-resource "sample_profiles/acrossx.json"))
(def activity-stream-profile-raw
  (read-resource "sample_profiles/activity_stream.json"))
(def tincan-profile-raw
  (read-resource "sample_profiles/tincan.json"))
(def video-profile-raw
  (read-resource "sample_profiles/video.json"))
(def mom-profile-raw
  (read-resource "sample_profiles/mom.json"))
(def scorm-profile-raw
  (read-resource "sample_profiles/scorm.json"))

(def will-profile-fix
  (read-resource "sample_profiles/catch-fixed.json"))
(def cmi-profile-fix
  (read-resource "sample_profiles/cmi5-fixed.json"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile error tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest error-tests
  (are [profile-name profile res]
       (testing (str "the " profile-name ", without printing")
         (let [[correct-syntax? correct-ids? correct-graph? correct-ctxt?]
               res
               syntax-errs (validate-profile profile
                                             :print-errs? false)
               id-errs     (validate-profile profile
                                             :syntax? false
                                             :ids? true
                                             :print-errs? false)
               graph-errs  (validate-profile profile
                                             :syntax? false
                                             :relations? true
                                             :print-errs? false)
               ctxt-errs   (validate-profile profile
                                             :syntax? false
                                             :context? true
                                             :print-errs? false)]
           (if correct-syntax?
             (is (nil? syntax-errs))
             (is (some? syntax-errs)))
           (if correct-ids?
             (is (nil? id-errs))
             (is (some? id-errs)))
           (if correct-graph?
             (is (nil? graph-errs))
             (is (some? graph-errs)))
           (if correct-ctxt?
             (is (nil? ctxt-errs))
             (is (some? ctxt-errs)))))
    "CATCH" will-profile-raw [false false false true]
    "cmi5" cmi-profile-raw [false false false true]
    "AcrossX" acrossx-profile-raw [false true false true]
    "Activity Stream" activity-stream-profile-raw [false false false true]
    "Tin Can" tincan-profile-raw [true false false true]
    "Video" video-profile-raw [true true false true]
    "MOM" mom-profile-raw [true false false true]
    "Scorm" scorm-profile-raw [false false false true] ; may be outdated
    ;; Fixed profiles (note that we can't completely fix cmi5 yet)
    "CATCH (fixed)" will-profile-fix [true true true true]
    "cmi5 (fixed)" cmi-profile-fix [true true false true]))

(deftest catch-err-data-test
  (testing "the CATCH profile error data"
    (is (= 24 (-> (validate-profile will-profile-raw :print-errs? false)
                  :syntax-errors
                  ::s/problems
                  count)))
    (is (= ::profile/profile
           (-> (validate-profile will-profile-raw :print-errs? false)
               :syntax-errors
               ::s/spec)))
    (is (= (json/convert-json will-profile-raw "_")
           (-> (validate-profile will-profile-raw :print-errs? false)
               :syntax-errors
               ::s/value)))
    (is (nil? (validate-profile will-profile-raw
                                :syntax? false
                                :contexts? true
                                :print-errs? false)))))

(deftest cmi5-err-data-test
  (testing "the cmi5 profile error data"
    (is (= 32 (-> (validate-profile cmi-profile-raw :print-errs? false)
                  :syntax-errors
                  ::s/problems
                  count)))
    (is (= ::profile/profile
           (-> (validate-profile cmi-profile-raw :print-errs? false)
               :syntax-errors
               ::s/spec)))
    (is (= (json/convert-json cmi-profile-raw "_")
           (-> (validate-profile cmi-profile-raw :print-errs? false)
               :syntax-errors
               ::s/value)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Error message tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- distinct-problems
  [err-map]
  (reduce-kv
   (fn [err-map err-keyword err-data]
     (assoc err-map
            err-keyword
            (when err-data
              (update err-data
                      ::s/problems
                      (fn [p] (->> p (group-by :pred) vals (map first)))))))
   {}
   err-map))

(defn- expound-to-str
  [err-data]
  (-> err-data distinct-problems e/expound-errors with-out-str))

(deftest err-msg-tests
  (testing "syntax error messages"
    (is (= fix/catch-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false))))
    (is (= fix/acrossx-err-msg
           (expound-to-str (validate-profile acrossx-profile-raw
                                             :print-errs? false))))
    (is (= fix/activity-stream-err-msg
           (expound-to-str (validate-profile activity-stream-profile-raw
                                             :print-errs? false))))
    (is (= fix/scorm-err-msg
           (expound-to-str (validate-profile scorm-profile-raw
                                             :print-errs? false))))
    ;; cljs err msg tables are wider by one column
    #?(:clj (is (= fix/cmi-err-msg
                   (expound-to-str (validate-profile cmi-profile-raw
                                                     :print-errs? false))))))
  (testing "id error messages"
    (is (= fix/catch-id-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true))))
    (is (= fix/cmi-id-err-msg
           (expound-to-str (validate-profile cmi-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true))))
    (is (= fix/activity-stream-id-err-msg
           (expound-to-str (validate-profile activity-stream-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true)))))
  (testing "edge error messages"
    (is (= fix/catch-graph-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :relations? true
                                             :external-iris? false))))))

(deftest success-msg-test
  (testing "error messages on fixed profiles"
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-fix
                                           :syntax? true
                                           :relations? true
                                           :context? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix
                                           :syntax? true
                                           :context? true))))))
