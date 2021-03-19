(ns com.yetanalytics.pan-test
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan :refer [validate-profile]]
            [com.yetanalytics.pan.objects.profile :as profile]
            [com.yetanalytics.pan.errors :as e]
            [com.yetanalytics.pan.utils.json :as json])
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

(def catch-err-msg
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

(def cmi-err-msg
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

(def acrossx-err-msg
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"https://w3id.org/xapi/acrossx/verbs/liked\",
 :type \"Verb\",
 :inScheme \"https://w3id.org/xapi/acrossx/v1.0.1\",
 :prefLabel {:en \"liked\"},
 :definition
 {:en
  \"Indicates that the actor approves of, recommends, or endorses the object or activity.\"},
 :related [\"http://activitystrea.ms/schema/1.0/like\"],
 :exactMatch [\"http://activitystrea.ms/schema/1.0/like\"]}

should not use related property in a non-deprecated Concept

-------------------------
Detected 1 error
")

(def activity-stream-err-msg
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"http://activitystrea.ms/like\",
 :type \"Verb\",
 :inScheme \"http://activitystrea.ms/schema/1.0.0\",
 :prefLabel {:en \"liked\"},
 :definition
 {:en
  \"Indicates that the actor marked the object as an item of special interest. The like verb is considered to be an alias of favorite. The two verb are semantically identical.\"},
 :related [\"https://w3id.org/xapi/acrossx/verbs/liked\"]}

should not use related property in a non-deprecated Concept

-------------------------
Detected 1 error
")

(def scorm-err-msg
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"https://w3id.org/xapi/scorm#generalrestrictions\",
 :type \"StatementTemplate\",
 :prefLabel {:en \"general restrictions on statements\"},
 :definition
 {:en
  \"This is the general template that defines restrictions for all statements conforming to the SCORM profile.\"},
 :rules
 [{:location \"context.contextActivities.grouping[*].definition.type\",
   :any [\"http://adlnet.gov/expapi/activities/attempt\"]}
  {:location \"context.contextActivities.grouping[*].definition.type\",
   :any [\"http://adlnet.gov/expapi/activities/course\"]}
  {:location \"timestamp\", :presence \"included\"}]}

should contain key: :inScheme

| key       | spec                                        |
|===========+=============================================|
| :inScheme | (and                                        |
|           |  :com.yetanalytics.pan.axioms/string        |
|           |  (partial                                   |
|           |   re-matches                                |
|           |   xapi-schema.spec.regex/AbsoluteIRIRegEx)) |

-- Spec failed --------------------

Value:
\"context.contextActivities.grouping[*].definition.type\"

of property:
:location

in object:
{:id \"https://w3id.org/xapi/scorm#generalrestrictions\",
 :type \"StatementTemplate\",
 :prefLabel {:en \"general restrictions on statements\"},
 :definition
 {:en
  \"This is the general template that defines restrictions for all statements conforming to the SCORM profile.\"},
 :rules
 [{:location \"context.contextActivities.grouping[*].definition.type\",
   :any [\"http://adlnet.gov/expapi/activities/attempt\"]}
  {:location \"context.contextActivities.grouping[*].definition.type\",
   :any [\"http://adlnet.gov/expapi/activities/course\"]}
  {:location \"timestamp\", :presence \"included\"}]}

should be a valid JSONPath string

-- Spec failed --------------------

Value:
\"http://adlnet.gov/expapi/activities/lesson\"

of property:
:contextParentActivityType

in object:
{:id \"https://w3id.org/xapi/scorm#otheractivity\",
 :type \"StatementTemplate\",
 :prefLabel {:en \"other activity\"},
 :definition
 {:en \"The statement template used for other types of activities.\"},
 :contextParentActivityType
 \"http://adlnet.gov/expapi/activities/lesson\",
 :rules []}

should be an array of IRIs

-------------------------
Detected 3 errors
")

;; ID error messages

(def catch-id-err-msg
  "
**** ID Errors ****

-- Spec failed --------------------

Identifier:
\"https://w3id.org/xapi/catch/patterns#community-engagement-completion\"

which occurs 2 times in the Profile

should be a unique identifier value

-------------------------
Detected 1 error
")

(def cmi-id-err-msg
"
**** ID Errors ****

-- Spec failed --------------------

Identifier:
nil

which occurs 5 times in the Profile

should be a unique identifier value

-------------------------
Detected 1 error
")

(def activity-stream-id-err-msg
"
**** ID Errors ****

-- Spec failed --------------------

Identifier:
\"http://activitystrea.ms/schema/\"

which occurs 2 times in the Profile

should be a unique identifier value

-------------------------
Detected 1 error

**** Version Errors ****

-- Spec failed --------------------

InScheme IRI:
\"http://activitystrea.ms/schema/1.0.0\"

associated with the identifier:
\"http://activitystrea.ms/accept\"

in a Profile with the following version IDs:
\"http://activitystrea.ms/schema/\"

should be a valid version ID

-------------------------
Detected 1 error
")

;; Graph/relation errors
;; TODO: Fix concept errors for broadMatch, narrowMatch, relatedMatch, and exactMatch

(def catch-graph-err-msg
  "
**** Concept Edge Errors ****

-- Missing spec -------------------

Cannot find spec for

Concept:
{:id \"https://w3id.org/xapi/catch/verbs/finished\",
 :type \"Verb\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"http://adlnet.gov/expapi/verbs/completed\",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:broadMatch

with

 Spec multimethod:      `com.yetanalytics.pan.objects.concept/valid-edge?`
 Dispatch value:        `:broadMatch`

-- Spec failed --------------------

Concept:
{:id \"https://w3id.org/xapi/catch/context-extensions/advocacy-event\",
 :type \"ContextExtension\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"http://adlnet.gov/expapi/verbs/attended\",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:recommendedVerbs

should not link to a non-existent Concept

-------------------------
Detected 2 errors

**** Template Edge Errors ****

-- Spec failed --------------------

Statement Template:
{:id \"https://w3id.org/xapi/catch/templates#system-notification-submission\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://w3id.org/xapi/catch/verbs/notified\",
 :type nil,
 :inScheme nil,
 ...}

via the property:
:verb

should not link to non-existent Concept or Template

-- Spec failed --------------------

Statement Template:
{:id \"https://w3id.org/xapi/catch/templates#evidence-community-activity-provide\",
 :type \"StatementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

that links to object:
{:id \"https://w3id.org/xapi/catch/templates#community-activity\",
 :type \"statementTemplate\",
 :inScheme \"https://w3id.org/xapi/catch/v1\",
 ...}

via the property:
:contextStatementRefTemplate

should link to type: \"StatementTemplate\"

-------------------------
Detected 2 errors

**** Pattern Edge Errors ****

-- Spec failed --------------------

Pattern:
{:id \"https://w3id.org/xapi/catch/patterns#reflection\",
 :type \"Pattern\",
 :primary false,
 ...}

that links to object:
{:id {:id \"https://w3id.org/xapi/catch/templates#wrote-reflection\"},
 :type nil,
 nil ...,
 ...}

via the property:
:optional

and is used 15 times to link out to 1 object

should not link to non-existent Template or Pattern

-- Spec failed --------------------

Pattern:
{:id \"https://w3id.org/xapi/catch/patterns#community-engagement-completion\",
 :type \"Pattern\",
 :primary false,
 ...}

that links to object:
{:id \"https://w3id.org/xapi/catch/patterns#community-engagement-completion\",
 :type \"Pattern\",
 :sequence ...,
 ...}

via the property:
:sequence

and is used 1 time to link out to 4 objects

should not refer to itself

-- Spec failed --------------------

Pattern:
{:id \"https://w3id.org/xapi/catch/patterns#f2-1-01-iteration\",
 :type \"Pattern\",
 :primary true,
 ...}

that links to object:
{:id \"https://w3id.org/xapi/catch/templates#reflection-viewed-article\",
 :type \"statementTemplate\",
 nil ...,
 ...}

via the property:
:sequence

and is used 1 time to link out to 4 objects

should link to type: \"Pattern\"

or

should link to type: \"StatementTemplate\"

or

should only link to one other object

-------------------------
Detected 3 errors
")

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
    (is (= catch-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false))))
    (is (= acrossx-err-msg
           (expound-to-str (validate-profile acrossx-profile-raw
                                             :print-errs? false))))
    (is (= activity-stream-err-msg
           (expound-to-str (validate-profile activity-stream-profile-raw
                                             :print-errs? false))))
    (is (= scorm-err-msg
           (expound-to-str (validate-profile scorm-profile-raw
                                             :print-errs? false))))
    ;; cljs err msg tables are wider by one column
    #?(:clj (is (= cmi-err-msg
                   (expound-to-str (validate-profile cmi-profile-raw
                                                     :print-errs? false))))))
  (testing "id error messages"
    (is (= catch-id-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true))))
    (is (= cmi-id-err-msg
           (expound-to-str (validate-profile cmi-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true))))
    (is (= activity-stream-id-err-msg
           (expound-to-str (validate-profile activity-stream-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :ids? true)))))
  (testing "edge error messages"
    (is (= catch-graph-err-msg
           (expound-to-str (validate-profile will-profile-raw
                                             :print-errs? false
                                             :syntax? false
                                             :relations? true
                                             :external-iris? false))))))
