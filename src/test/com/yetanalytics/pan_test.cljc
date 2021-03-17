(ns com.yetanalytics.pan-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan :refer [validate-profile]]
            [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.utils.json :as json])
  #?(:clj (:require [com.yetanalytics.pan.utils.resources
                     :refer [read-resource]])
     :cljs (:require-macros [com.yetanalytics.pan.utils.resources
                             :refer [read-resource]])))

;; Profiles to test
(def will-profile-raw
  (read-resource "sample_profiles/will-profile-raw.json"))
(def will-profile-fix
  (read-resource "sample_profiles/will-profile-reduced.json"))
(def cmi-profile-raw
  (read-resource "sample_profiles/cmi5.json"))
(def cmi-profile-fix
  (read-resource "sample_profiles/cmi5-fixed.json"))

; ;; Raw profile
(deftest will-raw-test
  (testing "Will's unfixed CATCH profile with printing"
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw
                                              :syntax? false
                                              :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile will-profile-raw
                                              :syntax? false
                                              :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile will-profile-raw
                                           :syntax? false
                                           :contexts? true)))))
  (testing "Will's unfixed CATCH profile without printing"
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

;; Fixed profile
(deftest will-fix-test
  (testing "Will's CATCH profile, fixed"
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
  (testing "the unfixed cmi5 profile with printing"
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw
                                              :syntax? false
                                              :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-raw
                                              :syntax? false
                                              :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-raw
                                           :syntax? false
                                           :contexts? true)))))
  (testing "the unfixed cmi5 profile without printing"
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

;; Fixed profile
(deftest cmi-fixed-test
  (testing "the cmi5 profile, fixed"
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix :syntax? false :ids? true))))
    (is (not= "Success!\n"
              (with-out-str (validate-profile cmi-profile-fix :syntax? false :relations? true))))
    (is (= "Success!\n"
           (with-out-str (validate-profile cmi-profile-fix :syntax? false :contexts? true))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Error message tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def will-profile-err-msg
"
**** Syntax Errors ****

-- Spec failed --------------------

Value:
{:id \"https://w3id.org/xapi/catch/templates#view-rubric\"}

of property:
:optional

in object:
{:id \"https://w3id.org/xapi/catch/patterns#view-rubric\",
 :type \"Pattern\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :prefLabel {:en \"view rubric\"},
 :definition {:en \"This is a pattern for someone looking at a rubric\"},
 :optional {:id \"https://w3id.org/xapi/catch/templates#view-rubric\"}}

should be a non-empty string

-- Spec failed --------------------

Object:
{:id \"https://w3id.org/xapi/catch/verbs/uploaded\",
 :type \"Verb\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :prefLabel {:en \"provided\"},
 :definition {:en \"Uploading a resource from a local file system\"},
 :related
 [\"https://w3id.org/xapi/catch/verbs/submitted\"
  \"https://w3id.org/xapi/catch/verbs/provided\"]}

should not use related property in a non-deprecated Concept

-- Spec failed --------------------

Value:
\"statementTemplate\"

of property:
:type

in object:
{:id \"https://w3id.org/xapi/catch/templates#reflection-viewed-article\",
 :type \"statementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 :prefLabel {:en \"viewed article\"},
 :definition
 {:en
  \"This statement template is for describing the action of clicking on a link which takes the learner to an article.  The object should be the article itself with the object id being the url of the article.  This template should be used for each article viewed this way\"},
 :verb \"http://id.tincanapi.com/verb/viewed\",
 :objectActivityType
 \"https://w3id.org/xapi/catch/activitytypes/article\",
 :contextGroupingActivityType
 [\"https://w3id.org/xapi/catch/activitytypes/domain\"],
 :contextParentActivityType
 [\"https://w3id.org/xapi/catch/activitytypes/competency\"],
 :rules
 [{:location \"$.timestamp\",
   :presence \"included\",
   :scopeNote {:en \"When the learner clicks the link to the article\"}}
  {:location
   \"$.context.contextActivities.category['https://w3id.org/xapi/catch/v1']\",
   :presence \"included\",
   :scopeNote
   {:en \"this states the the statement conforms to this profile\"}}
  {:location \"$.object.definition.name\",
   :presence \"included\",
   :scopeNote {:en \"The name of the article\"}}]}

should be: \"StatementTemplate\"

-------------------------
Detected 3 errors
")

(def cmi-profile-err-msg
"
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"typicalsessions\"

of property:
:sequence

in object:
{:id \"https://w3id.org/xapi/cmi5#toplevel\",
 :type \"Pattern\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"General Pattern\"},
 :definition
 {:en
  \"This pattern describes the sequence of statements sent over the an entire course registration.\"},
 :primary true,
 :sequence [\"https://w3id.org/xapi/cmi5#satisfieds\" \"typicalsessions\"]}

should be a valid IRI

-- Spec failed --------------------

Object:
{:inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"abandoned\"},
 :definition
 {:en
  \"Indicates that the AU session was abnormally terminated by a learner's action (or due to a system failure).\"},
 :_id \"https://w3id.org/xapi/adl/verbs/abandoned\",
 :_type \"Verb\"}

should have a valid Concept type

-- Spec failed --------------------

Object:
{:id \"https://w3id.org/xapi/cmi5#generalrestrictions\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"Restrictions for all cmi5-defined Statements\"},
 :rules
 [{:location \"$.id\", :presence \"included\"}
  {:location \"$.timestamp\", :presence \"included\"}
  {:location \"$.context.contextActivities.grouping[*]\",
   :presence \"included\",
   :scopeNote
   \"An Activity object with an 'id' property whose value is the unaltered value of the AU's id attribute from the course structure (See Section 13.1.4 AU Metadata – id) MUST be included in the 'grouping' context activities.\"}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/sessionid']\",
   :presence \"included\"}]}

should contain key: :definition

| key         | spec                                          |
|=============+===============================================|
| :definition | (map-of                                       |
|             |  :com.yetanalytics.pan.axioms/language-tag    |
|             |  :com.yetanalytics.pan.axioms/lang-map-string |
|             |  :min-count                                   |
|             |  1)                                           |

-- Spec failed --------------------

Value:
\"An Activity object with an 'id' property whose value is the unaltered value of the AU's id attribute from the course structure (See Section 13.1.4 AU Metadata – id) MUST be included in the 'grouping' context activities.\"

of property:
:scopeNote

in object:
{:id \"https://w3id.org/xapi/cmi5#generalrestrictions\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"Restrictions for all cmi5-defined Statements\"},
 :rules
 [{:location \"$.id\", :presence \"included\"}
  {:location \"$.timestamp\", :presence \"included\"}
  {:location \"$.context.contextActivities.grouping[*]\",
   :presence \"included\",
   :scopeNote
   \"An Activity object with an 'id' property whose value is the unaltered value of the AU's id attribute from the course structure (See Section 13.1.4 AU Metadata – id) MUST be included in the 'grouping' context activities.\"}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/sessionid']\",
   :presence \"included\"}]}

should be a valid language map

-- Spec failed --------------------

Value:
\"https://w3id.org/xapi/cmi5/context/categories/moveon\"

of property:
:none

in object:
{:id \"https://w3id.org/xapi/cmi5#launched\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"Launched\"},
 :verb \"http://adlnet.gov/expapi/verbs/launched\",
 :rules
 [{:location \"$.result.score\", :presence \"excluded\"}
  {:location \"$.result.success\", :presence \"excluded\"}
  {:location \"$.result.completion\", :presence \"excluded\"}
  {:location \"$.context.contextActivities.category[*].id\",
   :none \"https://w3id.org/xapi/cmi5/context/categories/moveon\"}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/launchmode']\",
   :presence \"included\",
   :all [\"Normal\" \"Browse\" \"Review\"]}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/launchurl']\",
   :presence \"included\",
   :scopeNote
   \"The LMS MUST put a fully qualified URL equivalent to the one that the LMS used to launch the AU without the name/value pairs included as defined in section 8.1 in the context extensions of the 'Launched' statement.\"}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/moveon']\",
   :presence \"included\",
   :all
   [\"Passed\"
    \"Completed\"
    \"CompletedAndPassed\"
    \"CompletedOrPassed\"
    \"NotApplicable\"]}
  {:location
   \"$.context.extensions['https://w3id.org/xapi/cmi5/context/extensions/launchparameters']\",
   :presence \"included\"}]}

should be an non-empty array of values

-------------------------
Detected 5 errors
")

(defn- distinct-problems
  [err-map]
  (update-in err-map
             [:syntax-errors ::s/problems]
             (fn [probs] (->> probs (group-by :pred) vals (map first)))))

(defn- expound-to-str
  [profile]
  (-> (validate-profile profile :print-errs? false)
      distinct-problems
      e/expound-errors
      with-out-str))

(deftest err-msg-tests
  (testing "syntax error messages"
    (is (= will-profile-err-msg (expound-to-str will-profile-raw)))
    ;; cljs err msg tables are wider by one column
    #?(:clj (is (= cmi-profile-err-msg (expound-to-str cmi-profile-raw))))))
