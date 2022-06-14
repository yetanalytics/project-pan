(ns com.yetanalytics.pan-test
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.spec.alpha                   :as s]
            [com.yetanalytics.pan                 :as p]
            [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.errors          :as e]
            [com.yetanalytics.pan-test-fixtures   :as fix])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-resource
                             read-json-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-resource
                                     read-json-resource]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profiles to test
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def will-profile-raw
  (read-json-resource "sample_profiles/catch.json"))
(def cmi-profile-raw
  (read-json-resource "sample_profiles/cmi5.json"))
(def acrossx-profile-raw
  (read-json-resource "sample_profiles/acrossx.json"))
(def activity-stream-profile-raw
  (read-json-resource "sample_profiles/activity_stream.json"))
(def tincan-profile-raw
  (read-json-resource "sample_profiles/tincan.json"))
(def video-profile-raw
  (read-json-resource "sample_profiles/video.json"))
(def mom-profile-raw
  (read-json-resource "sample_profiles/mom.json"))
(def scorm-profile-raw
  (read-json-resource "sample_profiles/scorm.json"))

(def acrossx-multi-version-raw
  (read-json-resource "sample_profiles/acrossx-multi-version.json"))
(def video-multi-version-raw
  (read-json-resource "sample_profiles/video-multi-version.json"))

(def will-profile-fix
  (read-json-resource "sample_profiles/catch-fixed.json"))
(def cmi-profile-fix
  (read-json-resource "sample_profiles/cmi5-fixed.json"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON parse tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def will-profile-json-str
  (read-resource "sample_profiles/catch.json"))

(deftest json-parse-test
  (testing "json-profile->edn function"
    (is (= will-profile-raw
           (p/json-profile->edn will-profile-json-str)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile error tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest error-tests
  (are [profile-name profile res]
       (testing (str "the " profile-name ", without printing")
         (let [[correct-syntax? correct-ids? correct-graph? correct-ctxt?] res
               syntax-errs (p/validate-profile profile)
               id-errs     (p/validate-profile profile
                                               :syntax? false
                                               :ids? true)
               graph-errs  (p/validate-profile profile
                                               :syntax? false
                                               :relations? true)
               ctxt-errs   (p/validate-profile profile
                                               :syntax? false
                                               :context? true)]
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
    (is (= 24 (-> (p/validate-profile will-profile-raw)
                  :syntax-errors
                  ::s/problems
                  count)))
    (is (= ::profile/profile
           (-> (p/validate-profile will-profile-raw)
               :syntax-errors
               ::s/spec)))
    (is (nil? (p/validate-profile will-profile-raw
                                :syntax? false
                                :contexts? true)))))

(deftest cmi5-err-data-test
  (testing "the cmi5 profile error data"
    (is (= 32 (-> (p/validate-profile cmi-profile-raw)
                  :syntax-errors
                  ::s/problems
                  count)))
    (is (= ::profile/profile
           (-> (p/validate-profile cmi-profile-raw)
               :syntax-errors
               ::s/spec)))))

(deftest relation-error-tests
  (testing "Different relation keyword args"
    (is (= #{:concept-edge-errors
             :template-edge-errors
             :pattern-edge-errors
             :pattern-cycle-errors}
           (->> (p/validate-profile will-profile-raw
                                    :syntax?        false
                                    :relations?     true
                                    :concept-rels?  false
                                    :template-rels? false
                                    :pattern-rels?  false)
                keys
                set)
           (->> (p/validate-profile will-profile-raw
                                    :syntax?        false
                                    :relations?     true
                                    :concept-rels?  true
                                    :template-rels? true
                                    :pattern-rels?  true)
                keys
                set)
           (->> (p/validate-profile-coll [will-profile-raw]
                                         :syntax?        false
                                         :relations?     true
                                         :concept-rels?  true
                                         :template-rels? true
                                         :pattern-rels?  true)
                first
                keys
                set)))
    (is (= #{:concept-edge-errors}
           (->> (p/validate-profile will-profile-raw
                                    :syntax?        false
                                    :relations?     false
                                    :concept-rels?  true
                                    :template-rels? false
                                    :pattern-rels?  false)
                keys
                set)
           (->> (p/validate-profile-coll [will-profile-raw]
                                         :syntax?        false
                                         :relations?     false
                                         :concept-rels?  true
                                         :template-rels? false
                                         :pattern-rels?  false)
                first
                keys
                set)))
    (is (= #{:template-edge-errors}
           (->> (p/validate-profile will-profile-raw
                                    :syntax?        false
                                    :relations?     false
                                    :concept-rels?  false
                                    :template-rels? true
                                    :pattern-rels?  false)
                keys
                set)
           (->> (p/validate-profile-coll [will-profile-raw]
                                         :syntax?        false
                                         :relations?     false
                                         :concept-rels?  false
                                         :template-rels? true
                                         :pattern-rels?  false)
                first
                keys
                set)))
    (is (= #{:pattern-edge-errors
             :pattern-cycle-errors}
           (->> (p/validate-profile will-profile-raw
                                    :syntax?        false
                                    :relations?     false
                                    :concept-rels?  false
                                    :template-rels? false
                                    :pattern-rels?  true)
                keys
                set)
           (->> (p/validate-profile-coll [will-profile-raw]
                                         :syntax?        false
                                         :relations?     false
                                         :concept-rels?  false
                                         :template-rels? false
                                         :pattern-rels?  true)
                first
                keys
                set))))
  (testing "Pattern relations only"
    ;; Test case borrowed from DATASIM
    (let [profile-coll
          [cmi-profile-fix
           (-> video-profile-raw
               (assoc-in [:patterns 0 :sequence 0]
                         "https://w3id.org/xapi/cmi5#initialized")
               (assoc-in [:patterns 0 :sequence 2]
                         "https://w3id.org/xapi/cmi5#terminated")
               (assoc-in [:patterns 1 :alternates 6]
                         "https://w3id.org/xapi/cmi5#completed"))]]
      (is (nil? (p/validate-profile-coll profile-coll
                                         :syntax? true
                                         :pattern-rels? true))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Error message tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- distinct-problems
  "Return an keyword-error map where all errors are made distinct on the
   basis of the `:pred` key in `::s/problems`."
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
  (-> err-data distinct-problems (e/errors->string {:print-objects? true})))

(deftest err-msg-tests
  (testing "syntax error messages"
    (is (= fix/catch-err-msg
           (expound-to-str (p/validate-profile will-profile-raw))))
    (is (= fix/acrossx-err-msg
           (expound-to-str (p/validate-profile acrossx-profile-raw))))
    (is (= fix/activity-stream-err-msg
           (expound-to-str (p/validate-profile activity-stream-profile-raw))))
    (is (= fix/scorm-err-msg
           (expound-to-str (p/validate-profile scorm-profile-raw))))
    ;; cljs err msg tables are wider by one column
    #?(:clj (is (= fix/cmi-err-msg
                   (expound-to-str (p/validate-profile cmi-profile-raw))))))
  (testing "id error messages"
    (is (= fix/catch-id-err-msg
           (expound-to-str (p/validate-profile will-profile-raw
                                               :syntax? false
                                               :ids? true))))
    (is (= fix/cmi-id-err-msg
           (expound-to-str (p/validate-profile cmi-profile-raw
                                               :syntax? false
                                               :ids? true))))
    (is (= fix/activity-stream-id-err-msg
           (expound-to-str (p/validate-profile activity-stream-profile-raw
                                               :syntax? false
                                               :ids? true)))))
  (testing "versioning error messages"
    (is (= fix/acrossx-multi-inscheme-err-msg
           (expound-to-str (p/validate-profile acrossx-multi-version-raw
                                               :syntax? false
                                               :ids? true))))
    (is (= fix/video-multi-inscheme-err-msg
           (expound-to-str (p/validate-profile video-multi-version-raw
                                               :syntax? false
                                               :ids? true))))
    (is (empty? (expound-to-str (p/validate-profile acrossx-multi-version-raw
                                                    :syntax? false
                                                    :ids? true
                                                    :multi-version? true))))
    (is (= fix/video-multi-inscheme-err-msg-2
           (expound-to-str (p/validate-profile video-multi-version-raw
                                               :syntax? false
                                               :ids? true
                                               :multi-version? true))))
    ;; The above fixture is misleading since duplicate pred problems
    ;; are deduped
    (is (< 1 (-> (p/validate-profile video-multi-version-raw
                                     :syntax? false
                                     :ids? true
                                     :multi-version? true)
                 :versioning-errors
                 ::s/problems
                 count))))
  (testing "edge error messages"
    (is (= fix/catch-graph-err-msg
           (expound-to-str (p/validate-profile will-profile-raw
                                               :syntax? false
                                               :relations? true))))
    (is (= fix/catch-graph-err-msg-2
           (expound-to-str (p/validate-profile will-profile-raw
                                               :syntax? false
                                               :relations? true
                                               :extra-profiles [scorm-profile-raw]))))
    (is (= fix/catch-graph-err-msg-2
           (-> [will-profile-raw scorm-profile-raw]
               (p/validate-profile-coll :syntax? false
                                        :relations? true)
               first
               expound-to-str))))
  (testing "string and string vec maps"
    (is (every? map? (-> [will-profile-raw scorm-profile-raw]
                         (p/validate-profile-coll :syntax? false
                                                  :relations? true
                                                  :result :type-path-string)
                         first
                         vals)))
    (is (every? string? (-> [will-profile-raw scorm-profile-raw]
                            (p/validate-profile-coll :syntax? false
                                                     :relations? true
                                                     :result :type-string)
                            first
                            vals)))
    (is (string? (-> [will-profile-raw scorm-profile-raw]
                     (p/validate-profile-coll :syntax? false
                                              :relations? true
                                              :result :string)
                     first)))
    (is (not= (-> [will-profile-raw scorm-profile-raw]
                  (p/validate-profile-coll :result :string)
                  first)
              (-> [will-profile-raw scorm-profile-raw]
                  (p/validate-profile-coll :result :string
                                           :error-msg-opts {:print-objects? false})
                  first)))))

(deftest success-msg-test
  (testing "error messages on fixed profiles"
    (is (= "Success!\n"
           (with-out-str (p/validate-profile will-profile-fix
                                             :syntax? true
                                             :ids? true
                                             :relations? true
                                             :context? true
                                             :result :print))))
    (is (= "Success!\n"
           (with-out-str (p/validate-profile cmi-profile-fix
                                             :syntax? true
                                             :ids? true
                                             :context? true
                                             :result :print))))
    (is (= "Success!\n"
           (with-out-str (p/validate-profile-coll [will-profile-fix
                                                   cmi-profile-fix]
                                                  :syntax? true
                                                  :ids? true
                                                  :context? true
                                                  :result :print))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Object Validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Taken from the cmi5 profile
(def sample-verb
  {:id         "https://w3id.org/xapi/adl/verbs/satisfied"
   :inScheme   "https://w3id.org/xapi/cmi5/v1.0"
   :type       "Verb"
   :prefLabel  {:en "satisfied"}
   :definition {:en "Indicates that the authority or activity provider determined the actor has fulfilled the criteria of the object or activity."}})

(deftest validate-object-test
  (testing "error messages on individual objects"
    (is (nil? (p/validate-object sample-verb :type :concept)))
    (is (some? (p/validate-object sample-verb :type :template)))
    (is (some? (p/validate-object sample-verb :type :pattern)))
    (testing "defaults to concept"
      (is (nil? (p/validate-object sample-verb)))
      (is (some? (p/validate-object (assoc sample-verb :type "Pattern")))))
    (is (string? (get (p/validate-object sample-verb
                                         :type :pattern
                                         :result :path-string)
                      [:type])))
    (is (= fix/verb-concept-error
           (p/validate-object (assoc sample-verb :type "FooBar")
                              :type :concept
                              :result :string)
           (p/validate-object (assoc sample-verb :type "FooBar")
                              :result :string)))
    (is (= fix/verb-template-error
           (p/validate-object sample-verb
                              :type :template
                              :result :string)))
    (is (= fix/verb-pattern-error
           (p/validate-object sample-verb
                              :type :pattern
                              :result :string)))
    (is (= fix/verb-pattern-error-no-obj
           (p/validate-object sample-verb
                              :type :pattern
                              :result :string
                              :error-msg-opts {:print-objects? false})))
    (is (= (str (p/validate-object sample-verb
                                   :type :pattern
                                   :result :string)
                "\n") ; extra \n because of println
           (with-out-str
             (p/validate-object sample-verb
                                :type :pattern
                                :result :println))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; External IRI retrieval tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest get-external-iri-test
  (testing "get-external-iris function"
    (is (= {:exactMatch
            #{"https://w3id.org/xapi/profiles/ontology#Profile"
              "https://w3id.org/xapi/cmi5/activities/course"
              "http://activitystrea.ms/schema/terminate"
              "http://activitystrea.ms/schema/complete"}
            :context
            #{"https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/context/attempt-state-context.jsonld"}
            :schema
            #{"https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.agent.profile.schema.json"
              "https://w3id.org/xapi/scorm/activity-profile/scorm.profile.activity.profile.schema"
              "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.attempt.state.schema.json"
              "https://raw.githubusercontent.com/adlnet/xAPI-SCORM-Profile/master/document-schemas/scorm.profile.activity.state.schema.json"}
            :verb
            #{"http://adlnet.gov/expapi/verbs/commented"}
            :objectActivityType
            #{"http://adlnet.gov/expapi/activities/cmi.interaction"}}
           (p/get-external-iris scorm-profile-raw)))))
