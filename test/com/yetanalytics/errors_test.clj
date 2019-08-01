(ns com.yetanalytics.errors-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.pprint :as pprint]
            [expound.alpha :as exp]
            [xapi-schema.spec :as xs]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.errors :as e]
            [com.yetanalytics.profile :as p]
            [com.yetanalytics.profiles.author :as ah]
            [com.yetanalytics.objects.template :as t]
            [com.yetanalytics.identifiers :as id]))

;; Sample Profiles

;; Metadata-only profile


(def good-profile-1
  {:id "https://w3id.org/xapi/catch"
   :type "Profile"
   :prefLabel {"en" "Catch"}
   :definition {"en" "The profile for the trinity education application CATCH"}
   :_context "https://w3id.org/xapi/profiles/context"
   :versions [{:id "https://w3id.org/xapi/catch/v1"
               :generatedAtTime "2017-12-22T22:30:00-07:00"}]
   :author {:url "https://www.yetanalytics.io"
            :type "Organization"
            :name "Yet Analytics"}
   :conformsTo "https://w3id.org/xapi/profiles#1.0"})

;; context properties is wrong
(def bad-profile-1a (assoc good-profile-1
                           :context "what the pineapple"))

;; id and type property are wrong
(def bad-profile-1b (assoc good-profile-1
                           :id "not an id"
                           :type "FooBar"))

;; id and author properties are wrong
(def bad-profile-1c (assoc good-profile-1
                           :id "not an id"
                           :author {:url "bad url" :type "Organization" :name "Yet Analytics"}))

;; Everything is just wrong
(def bad-profile-1d (-> good-profile-1
                        (assoc
                         :id "invalid"
                         :type "FooBar"
                         :prefLabel {"es" "uno"
                                     "en" false}
                         :definition {74 "definition"}
                         :versions [{:id "invalid"
                                     :generatedAtTime "not-a-timestamp"}]
                         :author {:url "bad"
                                  :type "BarFoo"
                                  :name 12345}
                         :conformsTo "stan loona")
                        (dissoc :_context)))

;; Profile with some Templates


(def good-profile-2
  (assoc good-profile-1
         :templates [{:id "https://foo.org/template"
                      :type "StatementTemplate"
                      :inScheme "https://w3id.org/xapi/catch/v1"
                      :prefLabel {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :verb "https://foo.org/verb"
                      :rules [{:location "$.actor.mbox"
                               :all ["yet@yetanalytics.io"]}]}
                     {:id "https://foo.org/template2"
                      :type "StatementTemplate"
                      :inScheme "https://w3id.org/xapi/catch/v1"
                      :prefLabel {"en" "Template"}
                      :definition {"en" "This StatementTemplate is only used for example purposes."}
                      :deprecated false
                      :verb "https://foo.org/verb"
                      :rules [{:location "$.actor.name"
                               :all ["Yet Analytics"]}]}]))

;; Nuke the first Template
(def bad-profile-2a
  (assoc-in good-profile-2 [:templates 0]
            {:id "this-template-is-invalid"}))

;; Make IDs duplicate
(def bad-profile-2b
  (assoc-in good-profile-2 [:templates 1 :id]
            "https://foo.org/template"))

;; Invalidate inScheme values
(def bad-profile-2c
  (-> good-profile-2
      (assoc-in [:templates 0 :inScheme] "https://foo.org/invalid")
      (assoc-in [:templates 1 :inScheme] "https://foo.org/also-invalid")))

;; Tests


(deftest group-by-in-test
  (testing "group-by-in function"
    (is (= (p/validate bad-profile-1a)
           (first (e/group-by-in (p/validate bad-profile-1a)))))
    (is (= (-> bad-profile-1a p/validate e/group-by-in first ::s/problems first :in)
           (-> bad-profile-1a p/validate e/group-by-in first ::s/problems second :in)))))

(deftest compare-properties-test
  (testing "compare-properties function"
    (is (neg? (e/compare-properties :id :aaaaaaah)))
    (is (neg? (e/compare-properties :id :type)))
    (is (neg? (e/compare-properties :aaah :aarg)))
    (is (zero? (e/compare-properties :same :same)))
    (is (zero? (e/compare-properties :id :id)))
    (is (= (sort e/compare-properties
                 '(:prefLabel :definition :inScheme :id :deprecated :type))
           '(:id :type :inScheme :prefLabel :definition :deprecated)))))

(deftest compare-arrs-test
  (testing "compare-arrs function"
    (is (= (sort '([:z] [:a :b] [:a])) '([:a] [:z] [:a :b])))
    (is (= (sort e/compare-arrs '([:z] [:a :b] [:a])) '([:a] [:a :b] [:z])))
    (is (= (e/compare-arrs [:author] [:template])
           (compare :author :template)))
    (is (neg? (e/compare-arrs [:author] [:author :url])))
    (is (neg? (e/compare-arrs [:author] [:template :id])))
    (is (neg? (e/compare-arrs [:author :url] [:template])))
    (is (neg? (e/compare-arrs [] [:id])))))

(s/explain ::p/profile bad-profile-2a)
(deftest sort-by-path-test
  (testing "sort-by-path function"
    (is (= [:id]
           (-> bad-profile-1b p/validate e/group-by-in e/sort-by-path first ::s/problems first :path)))
    (is (= [:type]
           (-> bad-profile-1b p/validate e/group-by-in e/sort-by-path second ::s/problems first :path)))
    (is (= [:id]
           (-> bad-profile-1c p/validate e/group-by-in e/sort-by-path first ::s/problems first :path)))
    (is (= [:author :url]
           (-> bad-profile-1c p/validate e/group-by-in e/sort-by-path second ::s/problems first :path)))
    (is (= (->> bad-profile-1d p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :path)))
           [[] [:id] [:type] [:conformsTo] [:prefLabel 1] [:definition 0]
            [:author :type] [:author :name] [:author :url]
            [:versions :id] [:versions :generatedAtTime]]))
    (is (= (->> bad-profile-2a p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :path)))))))

(deftest spec-test
  (testing "ensure that correct specs are returned in problem maps"
    (is (= (->> bad-profile-1d p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))
           [::p/profile ::ax/iri ::p/type ::ax/uri ::ax/lang-map-string ::ax/language-tag
            ::ah/type ::ax/string ::ax/url ::ax/iri ::ax/timestamp]))
    (is (= (->> bad-profile-2a p/validate e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))
           [::t/template ::ax/uri]))
    (is (= (->> bad-profile-2b id/validate-ids e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))
           [::id/one-count]))
    (is (= (->> bad-profile-2c id/validate-in-schemes e/group-by-in e/sort-by-path
                (mapv #(-> % ::s/problems first :via last)))
           [::id/in-scheme ::id/in-scheme]))))
