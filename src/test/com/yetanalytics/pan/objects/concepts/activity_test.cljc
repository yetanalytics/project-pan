(ns com.yetanalytics.pan.objects.concepts.activity-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.objects.concepts.activity :as a]
            [com.yetanalytics.pan.objects.concepts.activity.definition :as adef]
            [com.yetanalytics.test-utils :refer [should-satisfy
                                                 should-satisfy+
                                                 should-not-satisfy]]))

(deftest type-test
  (testing "type property"
    (should-satisfy+ ::a/type
                     "Activity"
                     :bad
                     "Profile"
                     "Verb"
                     "StanLoona")))

(deftest context-test
  (testing "@context property"
    (should-satisfy ::adef/_context
                    "https://w3id.org/xapi/profiles/activity-context")
    (should-satisfy ::adef/_context "https://some-other-context")
    (should-satisfy ::adef/_context
                    ["https://w3id.org/xapi/profiles/activity-context"])
    (should-not-satisfy ::adef/_context ["https://some-other-context"])
    (should-not-satisfy ::adef/_context "foo bar")))

(def interaction-type
  [{:id "itype-1" :description {:en-US "Interaction Type 1"}}
   {:id "itype-2" :description {:en-US "Interaction Type 2"}}
   {:id "itype-3" :description {:en-US "Interaction Type 3"}}])

(def bad-interaction-type
  [{:id "itype-1" :description {:en-US "Interaction Type 1A"}}
   {:id "itype-1" :description {:en-US "Interaction Type 1B"}}])

(deftest activity-definition-property-test
  (testing "activityDefinition"
    (testing "name property"
      (should-satisfy+ ::adef/name
                       {:en "Foo"}
                       :bad
                       {:xyzz "Invalid"}))
    (testing "description property"
      (should-satisfy+ ::adef/description
                       {:en "Foo"}
                       :bad
                       {:xyzz "Invalid"}))
    (testing "type property"
      (should-satisfy+ ::adef/type
                       "http://foo.org/activity-type"
                       :bad
                       "123"
                       123))
    (testing "moreInfo property"
      (should-satisfy+ ::adef/type
                       "http://foo.org/more-info"
                       :bad
                       "123"
                       123))
    (testing "interactionType property"
      (should-satisfy+ ::adef/interactionType
                       "true-false"
                       "choice"
                       "fill-in"
                       "long-fill-in"
                       "matching"
                       "performance"
                       "sequencing"
                       "likert"
                       "numeric"
                       "other"
                       :bad
                       "zoo-wee-mama"))
    (testing "correctResponsesPattern property"
      (should-satisfy+ ::adef/correctResponsesPattern
                       ["true"]
                       ["golf[,]tetris"]
                       ["Bob's your uncle"]
                       ["{case_matters=false}{lang=en}Some Interaction"]
                       ["likert_3"]
                       ["ben[.]3[,]chris[.]2[,]troy[.]4[,]freddie[.]1"]
                       :bad
                       []
                       [123]))
    (testing "interaction component properties - valid"
      (should-satisfy ::adef/choices interaction-type)
      (should-satisfy ::adef/scale interaction-type)
      (should-satisfy ::adef/source interaction-type)
      (should-satisfy ::adef/target interaction-type)
      (should-satisfy ::adef/steps interaction-type))
    (testing "interaction component properties - invalid"
      (should-not-satisfy ::adef/choices bad-interaction-type)
      (should-not-satisfy ::adef/scale bad-interaction-type)
      (should-not-satisfy ::adef/source bad-interaction-type)
      (should-not-satisfy ::adef/target bad-interaction-type)
      (should-not-satisfy ::adef/steps bad-interaction-type))))

(deftest activity-definition-test
  (testing "activityDefinition property"
    (is (s/valid?
         ::a/activityDefinition
         {:_context    "https://w3id.org/xapi/profiles/activity-context"
          :name        {:en "Cross Linguistic Connections"}
          :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
          :type        "https://w3id.org/xapi/catch/activitytypes/competency"}))
    (testing "depends on interactionType"
      (is (s/valid?
           ::a/activityDefinition
           {:_context        "https://w3id.org/xapi/profiles/activity-context"
            :interactionType "choice"
            :choices         interaction-type}))
      (is (s/valid?
           ::a/activityDefinition
           {:_context        "https://w3id.org/xapi/profiles/activity-context"
            :interactionType "sequencing"
            :choices         interaction-type}))
      (is (s/valid?
           ::a/activityDefinition
           {:_context        "https://w3id.org/xapi/profiles/activity-context"
            :interactionType "likert"
            :scale           interaction-type}))
      (is (s/valid?
           ::a/activityDefinition
           {:_context        "https://w3id.org/xapi/profiles/activity-context"
            :interactionType "matching"
            :source           interaction-type
            :target           interaction-type}))
      (is (s/valid?
           ::a/activityDefinition
           {:_context        "https://w3id.org/xapi/profiles/activity-context"
            :interactionType "performance"
            :steps           interaction-type}))
      (is (not (s/valid?
                ::a/activityDefinition
                {:_context        "https://w3id.org/xapi/profiles/activity-context"
                 :interactionType "true-false"
                 :steps           interaction-type})))
      (is (not (s/valid?
                ::a/activityDefinition
                {:_context "https://w3id.org/xapi/profiles/activity-context"
                 :correctResponsesPattern ["foo" "bar"]}))))
    (testing "with extension object"
      (is (s/valid?
           ::a/activityDefinition
           {:_context    "https://w3id.org/xapi/profiles/activity-context"
            :name        {:en "Cross Linguistic Connections"}
            :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
            :type        "https://w3id.org/xapi/catch/activitytypes/competency"
            :extensions  {"http://foo.org/extension-1"
                          {:_context {:foo     "http://foo.org/"
                                      :display {:_id        "foo:display"
                                                :_container "@language"}}
                           :display  {:en-US "My Extension"}}
                          "http://foo.org/extension-2" 2}}))
      (is (not (s/valid?
                ::a/activityDefinition
                {:_context    "https://w3id.org/xapi/profiles/activity-context"
                 :name        {:en "Cross Linguistic Connections"}
                 :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
                 :type        "https://w3id.org/xapi/catch/activitytypes/competency"
                 :extensions  {"http://foo.org/extension-1" {:display {:en-US "My Extension"}}
                               "http://foo.org/extension-2" 2}}))))))

(deftest activity-test
  (testing "Activity concept"
    (is (s/valid?
         ::a/activity
         {:id "https://w3id.org/xapi/catch/activities/competency/cross-linguistic-connections"
          :type "Activity"
          :inScheme "https://w3id.org/xapi/catch/v1"
          :activityDefinition
          {:_context "https://w3id.org/xapi/profiles/activity-context"
           :name {:en "Cross Linguistic Connections"}
           :description {"en" "The cross linguistic connections competency as described by the EPISD Dual Language Competency Framework"}
           :type "https://w3id.org/xapi/catch/activitytypes/competency"}}))))
