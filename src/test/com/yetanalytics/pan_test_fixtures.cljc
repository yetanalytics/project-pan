(ns com.yetanalytics.pan-test-fixtures
  (:require [clojure.string :as cstr]))

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

should be a valid IRI

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

| key         | spec                                       |
|=============+============================================|
| :definition | (map-of                                    |
|             |  com.yetanalytics.pan.axioms/language-tag? |
|             |  string?                                   |
|             |  :min-count                                |
|             |  1)                                        |

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

| key       | spec                                 |
|===========+======================================|
| :inScheme | com.yetanalytics.pan.axioms/iri-str? |

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

(def catch-graph-err-msg
  "
**** Concept Edge Errors ****

-- Spec failed --------------------

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

should not link to a non-existent Concept

-------------------------
Detected 1 error

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

;; SCORM profile forgot the "shared" verb
(def catch-graph-err-msg-2
  (-> catch-graph-err-msg
      (cstr/replace
       #?(:clj #"https\:\/\/w3id\.org\/xapi\/catch\/verbs\/finished"
          :cljs #"https\://w3id\.org/xapi/catch/verbs/finished")
       "https://w3id.org/xapi/catch/verbs/provided")
      (cstr/replace
       #?(:clj #"http\:\/\/adlnet\.gov\/expapi\/verbs\/completed"
          :cljs #"http\://adlnet\.gov/expapi/verbs/completed")
       "http://adlnet.gov/expapi/verbs/shared")))

;; Object validation fixtures

(def verb-concept-error
  "
**** Syntax Errors ****

-- Spec failed --------------------

Object:
{:id \"https://w3id.org/xapi/adl/verbs/satisfied\",
 :type \"FooBar\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"satisfied\"},
 :definition
 {:en
  \"Indicates that the authority or activity provider determined the actor has fulfilled the criteria of the object or activity.\"}}

should have a valid Concept type

-------------------------
Detected 1 error
")

(def verb-template-error
  "
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"Verb\"

of property:
:type

in object:
{:id \"https://w3id.org/xapi/adl/verbs/satisfied\",
 :type \"Verb\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"satisfied\"},
 :definition
 {:en
  \"Indicates that the authority or activity provider determined the actor has fulfilled the criteria of the object or activity.\"}}

should be: \"StatementTemplate\"

-------------------------
Detected 1 error
")

(def verb-pattern-error
  "
**** Syntax Errors ****

-- Spec failed --------------------

Value:
\"Verb\"

of property:
:type

in object:
{:id \"https://w3id.org/xapi/adl/verbs/satisfied\",
 :type \"Verb\",
 :inScheme \"https://w3id.org/xapi/cmi5/v1.0\",
 :prefLabel {:en \"satisfied\"},
 :definition
 {:en
  \"Indicates that the authority or activity provider determined the actor has fulfilled the criteria of the object or activity.\"}}

should be: \"Pattern\"

-------------------------
Detected 1 error
")
