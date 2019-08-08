(ns com.yetanalytics.axioms-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.test-utils.clj :refer :all]))

;; String
(deftest test-string
  (testing "string spec"
    (is (s/valid? ::ax/string "foo bar"))
    (is (s/valid? ::ax/string "F43(#)*@ba 3$0s"))
    (is (not (s/valid? ::ax/string "")))
    (is (not (s/valid? ::ax/string 74)))))

;; RFC 2046 media types tests
(deftest test-media-types
  (testing "RFC 2046 media types"
    (is (s/valid? ::ax/media-type "application/json"))
    (is (s/valid? ::ax/media-type "audio/mpeg")) ;; MP3
    (is (s/valid? ::ax/media-type "image/png"))
    (is (s/valid? ::ax/media-type "text/plain"))
    (is (s/valid? ::ax/media-type "video/mp4"))
    (is (s/valid? ::ax/media-type "video/vnd.youtube.yt"))
    (is (not (s/valid? ::ax/media-type "application")))
    (is (not (s/valid? ::ax/media-type "application/json/ld")))
    (is (not (s/valid? ::ax/media-type "application/this-does-not-exist")))
    (is (not (s/valid? ::ax/media-type 74)))
    (is (not (s/valid? ::ax/media-type "what the pineapple")))))

;; Language Maps
;; TODO Language tag spec is broken; fix in xapi-schema
(deftest test-language-maps
  (testing "Language maps"
    (is (s/valid? ::ax/language-map {"en" "Foo Bar"}))
    (is (s/valid? ::ax/language-map {:en "Foo Bar"}))
    (is (s/valid? ::ax/language-map {:en ""}))
    (is (not (s/valid? ::ax/language-map {116 "Foo Bar"})))
    (is (not (s/valid? ::ax/language-map {"hello bubble" "Foo Bar"})))))

;; JSONPath tests

;; Basic unit tests
(deftest test-jsonpath
  (testing "JSONPath string"
    (is (s/valid? ::ax/json-path "$.store"))
    (is (s/valid? ::ax/json-path "$.store.book"))
    (is (s/valid? ::ax/json-path "$['store']['book'][0]"))
    (is (s/valid? ::ax/json-path "$.store['book'][ * ]"))
    (is (s/valid? ::ax/json-path "$['store\\|$io'] | $.foo.bar"))
    (is (not (s/valid? ::ax/json-path "what the pineapple")))))

;; Test in bulk
(deftest test-jsonpath-dot
  (testing "JSONPath strings with dot child notation"
    (should-satisfy+ ::ax/json-path
                     "$"              ; Jayway only
                     "$.store"
                     "$.store.book"
                     "$..book"
                     "$.*"
                     "$.*.*"
                     "$.store.*"
                     "$.*.book"
                     "$..*"
                     "$.**"
                     "$.store*io"
                     :bad
                     ""
                     ".store"
                     "$."
                     "$.."
                     "$store"
                     "$.store,expensive"
                     "$.store, expensive"
                     "$.store expensive"
                     "$.store[expensive"
                     "$. store"
                     "what the pineapple")))

(deftest test-jsonpath-bracket
  (testing "JSONPath strings with bracket child notation"
    (should-satisfy+ ::ax/json-path
                     "$['store']"
                     "$['store shop']"
                     "$['store']['book']"
                     "$[   ' store '   ]"   ; Jayway supports
                     "$.store['book']"
                     "$['store'].book"
                     "$..['book']"
                     "$..['author']"
                     "$[*]"
                     "$[*][*]"
                     "$[    *    ]"
                     "$['*']"               ; Is a string, not a wildcard in Jayway
                     "$['store'][*]"
                     "$['store']['book']['*']"
                     "$[*]['books']"
                     "$['store\\'s']"        ; Jayway supports escaped chars
                     "$['store\\,s']"        ; Jayway supports escaped chars 
                     "$['store']['book*']"  ; All chars except ',' and ''' supported
                     "$['store','expensive']"
                     "$['store', 'expensive']"
                     "$['store',           'expensive']"
                     :bad
                     "['store']"
                     "$[]"
                     "$['store"
                     "$[**]"
                     "$[store]"             ; Jayway does not support
                     "$['store'] ['book']"
                     "$['']"                ; No empty strings in xAPI Profile spec
                     "$['store,expensive']"
                     "$['store']book"
                     "$['store's']"
                     "$['store''expensive']"
                     "$['store' 'expensive']")))

(deftest test-jsonpath-array
  (testing "JSONPath strings that use array notation"
    (should-satisfy+ ::ax/json-path
                     "$..[0]"
                     "$..book[0]"
                     "$..book[1234]"
                     "$.store.book[0]"
                     "$.store.book[0, 1]"
                     "$.store.book[*]"
                     "$['store']['book'][0]"
                     "$['store']['book'][0, 1]"
                     "$['store']['book'][*]"
                     "$['store']['book'][0]['author']"
                     "$['store']['book']['*'][0]"
                     :bad
                     "$.store.book[[0]"
                     "$.store.book[0]]"
                     "$['store']['book'][0][0]"
                     "$..book[?(@isbn)]"  ; Filter functions not supported in xAPI Profile spec
                     "$..book[-1]"        ; Negative numbers not supported in xapi Profile spec
                     "$..book[0:6:2]"     ; Array slicing is not supported in xapi Profile spec
                     "$..book[l33t]")))

(deftest test-jsonpath-joined
  (testing "JSONPath strings that have been joined using the '|' char"
    (should-satisfy+ ::ax/json-path
                     "$.store.book|$.results.extensions"
                     "$.store.book  |$.results.extensions"
                     "$.store.book|$.results.extensions"
                     "$.store.book | $.results.extensions"
                     "$['store']['book']|$['results']['extensions']"
                     "$['store']['book'] | $['results']['extensions']"
                     "$['store']['book'][0] | $.results.extensions['https://foo.org']"
                     "$['store\\|$.io']"
                     "$['store   \\|$.io']"
                     "$['store|$.io']"
                     "$.store[*] | $.results['b|ah']"
                     :bad
                     "$.store.book $.results.extension"
                     "$.store.book | what the pineapple"
                     "$['store|$['io']]")))

;; JSON Schema tests


(def schema-1 "{\"$schema\" : \"https://foo.org/\",
                \"$comment\" : \"stan loona\"}")

; Example from luposlip/json-schema README 
(def schema-2 "{\"$schema\" : \"http://json-schema.org/draft-07/schema#\",
                \"id\" : \"https://luposlip.com/some-schema.json\",
                \"type\" : \"object\",
                \"properties\" : {
                    \"id\" :{
                        \"type\" : \"number\",
                        \"exclusiveMinimum\": 0} 
                },
                \"required\" : [\"id\"]}")

; Examples from xAPI Profile Spec README
(def schema-3 "{ \"type\": \"object\", \"properties\":{
              \"rank\": {\"type\": \"number\", \"required\": true},
              \"medal\": {\"type\": \"string\"}}}")

(def schema-4 "{ \"type\": \"object\", \"properties\": { \\
                 \"cut\": {\"enum\": [\"straight\", \"fitted\"], \"required\": true}, \\
                 \"size\": {\"enum\": [\"x-small\", \"small\", \"medium\", \"large\", \\
                 \"x-large\", \"2x-large\", \"3x-large\"], \"required\": true}}}")

; Examples from cmi5 profile
(def schema-5 "{ \"type\": \"number\", \"maximum\": 100, \"minimum\": 0, \"multipleOf\": 1.0 }")

(def schema-6 "{ \"enum\": [\"Passed\", \"Completed\", \"CompletedAndPassed\", \"CompletedOrPassed\", \"NotApplicable\"] }")

; Example from SCORM profile
(def schema-7 "{
    \"id\": \"https://w3id.org/xapi/scorm/activity-state/scorm.profile.activity.state.schema\",
    \"description\": \"State ID: https://w3id.org/xapi/scorm/activity-state. See: https://github.com/adlnet/xAPI-SCORM-Profile/blob/master/xapi-scorm-profile.md#scorm-activity-state\",
    \"type\": \"object\",
    \"additionalProperties\": false,
    \"required\": [\"attempts\"],
    \"properties\": {
        \"attempts\": {
            \"type\": \"array\",
            \"items\": {
                \"type\": \"string\",
                \"format\": \"uri\"
            }
        }
    }
}")

(deftest test-json-schema
  (testing "JSON Schema strings"
    (is (s/valid? ::ax/json-schema "{}"))
    (is (s/valid? ::ax/json-schema "{\"foo\" : \"bar\"}"))
    (is (s/valid? ::ax/json-schema schema-1))
    (is (s/valid? ::ax/json-schema schema-2))
    (is (s/valid? ::ax/json-schema schema-5))
    (is (s/valid? ::ax/json-schema schema-6))
    (is (s/valid? ::ax/json-schema schema-7))
    (is (not (s/valid? ::ax/json-schema 74)))
    (is (not (s/valid? ::ax/json-schema "")))
    (is (not (s/valid? ::ax/json-schema "what the pineapple")))
    (is (not (s/valid? ::ax/json-schema "{\"$schema\" : \" not a uri \"}")))
    (is (not (s/valid? ::ax/json-schema "{\"$schema\" : \"alsoNotURI\"}")))
    (is (not (s/valid? ::ax/json-schema "{\"$comment\" : 74}")))
    (is (not (s/valid? ::ax/json-schema schema-3))) ; valid only in draft-03
    (is (not (s/valid? ::ax/json-schema schema-4))) ; valid only in draft-03
))

;; TODO Should check that IRIs with non-ASCII chars pass (currently they don't)
(deftest test-iri
  (testing "IRIs/IRLs/URIs/URLs"
    (should-satisfy+ ::ax/iri
                     "https://foo.org"
                     "https://foo.org/"
                     :bad
                     "foo.org"
                     "www.foo.org")))
