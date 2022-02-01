(ns com.yetanalytics.pan-test.axioms-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [com.yetanalytics.pan.axioms :as ax]
            [com.yetanalytics.test-utils :refer [should-satisfy+]]))

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
                    \"id\" : {
                        \"type\" : \"number\",
                        \"exclusiveMinimum\": 0
                    } 
                },
                \"required\" : [\"id\"]}")

; Examples from xAPI Profile Spec README
(def schema-3 "{ \"type\": \"object\",
                 \"properties\": {
                   \"rank\": { \"type\": \"number\", \"required\": true },
                   \"medal\": { \"type\": \"string\" }
                 }
               }")

(def schema-4 "{ \"type\": \"object\",
                 \"properties\": { \\
                   \"cut\": {\"enum\": [\"straight\", \"fitted\"], \"required\": true}, \\
                   \"size\": {\"enum\": [\"x-small\", \"small\", \"medium\", \"large\", \"x-large\", \"2x-large\", \"3x-large\"], \"required\": true}}
               }")

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

;; TODO: More complete test coverage
(deftest test-json-schema
  (testing "JSON Schema strings"
    (is (s/valid? ::ax/json-schema "{}"))
    (is (s/valid? ::ax/json-schema "true"))
    (is (s/valid? ::ax/json-schema "false"))
    (is (s/valid? ::ax/json-schema "{\"foo\" : \"bar\"}"))
    (is (s/valid? ::ax/json-schema "{ \"type\": \"number\" }"))
    (is (s/valid? ::ax/json-schema schema-1))
    (is (s/valid? ::ax/json-schema schema-2))
    (is (s/valid? ::ax/json-schema schema-5))
    (is (s/valid? ::ax/json-schema schema-6))
    (is (s/valid? ::ax/json-schema schema-7))
    (is (not (s/valid? ::ax/json-schema 74)))
    (is (not (s/valid? ::ax/json-schema "")))
    (is (not (s/valid? ::ax/json-schema "what the pineapple")))
    ; next two are valid only in draft-03
    (is (not (s/valid? ::ax/json-schema schema-3)))
    (is (not (s/valid? ::ax/json-schema schema-4))))
  (testing "More JSON Schema strings, mostly from the juxt/jinx test suite"
    (testing "$schema property"
      (should-satisfy+ ::ax/json-schema
                       "{\"$schema\": \"http://json-schema.org/draft-07/schema#\"}"
                       :bad
                       "{\"$schema\": \" not an uri \"}"
                       "{\"$schema\": \"alsoNotURI\"}"))
    (testing "$ref property"
      (should-satisfy+ ::ax/json-schema
                       "{\"$ref\": \"#\"}"
                       "{\"$ref\": \"#/definitions/schemaArray\"}"
                       "{\"$ref\": \"https://example.org/ref\"}"
                       "{\"$ref\": \"/relative?path#example\"}"
                       :bad
                       "{\"$ref\": \"not a uri reference\"}"))
    (testing "$comment property"
      (should-satisfy+ ::ax/json-schema
                       "{\"$comment\" : \"74\"}"
                       :bad
                       "{\"$comment\" : 74}"))
    (testing "type property"
      (should-satisfy+ ::ax/json-schema
                       "{\"type\": [\"integer\"]}"
                       :bad
                       "{\"type\": 10}"
                       "{\"type\": \"float\"}"
                       "{\"type\": [\"string\", 10]}"
                       "{\"type\": [null]}"
                       "{\"type\": [\"string\", \"string\"]}"
                       "{\"type\": [\"float\". \"number\"]}"))
    (testing "enum property"
      (should-satisfy+ ::ax/json-schema
                       "{\"enum\": [\"foo\", \"bar\"]}"
                       :bad
                       "{\"enum\": \"foo\"}"
                       "{\"enum\": []}"
                       "{\"enum\": [\"foo\", \"foo\"]}"))
    (testing "const property"
      (should-satisfy+ ::ax/json-schema
                       "{\"const\": \"foo\"}"
                       "{\"const\": []}"
                       "{\"const\": [\"foo\"]}"
                       "{\"const\": null}"
                       :bad
                       "{\"const\": }"))
    (testing "multipleOf property"
      (should-satisfy+ ::ax/json-schema
                       "{\"multipleOf\": 0.1}"
                       :bad
                       "{\"multipleOf\": \"foo\"}"
                       "{\"multipleOf\": 0}"
                       "{\"multipleOf\": -1}"))
    (testing "maximum/exclusiveMaximum properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"maximum\": 10}"
                       "{\"maximum\": 10.5}"
                       "{\"exclusiveMaximum\": 0}"
                       :bad
                       "{\"maximum\": \"foo\"}"
                       "{\"exclusiveMaximum\": \"foo\"}"))
    (testing "mimimum/exclusiveMinimum properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"minimum\": 10}"
                       "{\"minimum\": 10.5}"
                       "{\"exclusiveMinimum\": 0}"
                       :bad
                       "{\"minimum\": \"foo\"}"
                       "{\"exclusiveMinimum\": \"foo\"}"))
    (testing "maxLength/minLength properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"maxLength\": 0}"
                       "{\"maxLength\": 5}"
                       "{\"minLength\": 0}"
                       "{\"minLength\": 5}"
                       :bad
                       "{\"maxLength\": \"foo\"}"
                       "{\"minLength\": \"foo\"}"))
    (testing "pattern property"
      (should-satisfy+ ::ax/json-schema
                       "{\"pattern\": \"foobar.?\"}"
                       :bad
                       "{\"pattern\": 10}"))
    (testing "items property"
      (should-satisfy+ ::ax/json-schema
                       "{\"items\": {\"type\": \"string\"}}"
                       "{\"items\": [{\"type\": \"string\"},
                                     {\"type\": \"number\"}]}"
                       :bad
                       "{\"items\": null}"
                       "{\"items\": {\"type\": \"foo\"}}"
                       "{\"items\": [{\"type\": \"string\"},
                                     {\"type\": \"number\"},
                                     {\"type\": \"float\"}]}"))
    (testing "additionalItems property"
      (should-satisfy+ ::ax/json-schema
                       "{\"additionalItems\": false}"
                       "{\"additionalItems\": true}"
                       :bad
                       "{\"additionalItems\": null}"
                       "{\"additionalItems\": {\"type\": \"foo\"}}"))
    (testing "maxItems/minItems properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"maxItems\": 10}"
                       "{\"maxItems\": 0}"
                       "{\"minItems\": 10}"
                       "{\"minItems\": 0}"
                       :bad
                       "{\"maxItems\": -1}"
                       "{\"maxItems\": 0.5}"
                       "{\"minItems\": -1}"
                       "{\"minItems\": 0.5}"))
    (testing "uniqueItems property"
      (should-satisfy+ ::ax/json-schema
                       "{\"uniqueItems\": true}"
                       "{\"uniqueItems\": false}"
                       :bad
                       "{\"uniqueItems\": 1}"))
    (testing "contains property"
      (should-satisfy+ ::ax/json-schema
                       "{\"contains\": true}"
                       "{\"contains\": false}"
                       "{\"contains\": {\"type\": \"string\"}}"
                       :bad
                       "{\"contains\": {\"type\": \"foo\"}}"))
    (testing "maxProperties/minProperties properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"maxProperties\": 10}"
                       "{\"maxProperties\": 0}"
                       "{\"minProperties\": 10}"
                       "{\"minProperties\": 0}"
                       :bad
                       "{\"maxProperties\": -1}"
                       "{\"maxProperties\": 0.5}"
                       "{\"minProperties\": -1}"
                       "{\"minProperties\": 0.5}"))
    (testing "required property"
      (should-satisfy+ ::ax/json-schema
                       "{\"required\": []}"
                       "{\"required\": [\"foo\", \"bar\"]}"
                       :bad
                       "{\"required\": \"foo\"}"
                       "{\"required\": [\"foo\", 0]}"
                       "{\"required\": [\"foo\", \"foo\"]}"))
    (testing "properties property"
      (should-satisfy+ ::ax/json-schema
                       "{\"properties\": {}}"
                       :bad
                       "{\"properties\": 10}"
                       "{\"properties\": {\"foo\": {\"type\": \"bar\"}}}"))
    (testing "patternProperties property"
      (should-satisfy+ ::ax/json-schema
                       "{\"patternProperties\": {}}"
                       :bad
                       "{\"patternProperties\": 10}"
                       "{\"patternProperties\": {\"foo\": {\"type\": \"bar\"}}}"))
    (testing "additionalProperties property"
      (should-satisfy+ ::ax/json-schema
                       "{\"additionalProperties\": false}"
                       "{\"additionalProperties\": true}"
                       :bad
                       "{\"additionalProperties\": null}"
                       "{\"additionalProperties\": {\"type\": \"foo\"}}"))
    (testing "dependencies property"
      (should-satisfy+ ::ax/json-schema
                       "{\"dependencies\": {}}"
                       "{\"dependencies\": {\"a\": []}}"
                       :bad
                       "{\"dependencies\": true}"
                       "{\"dependencies\": {\"a\": [\"foo\", \"foo\"]}}"
                       "{\"dependencies\": {\"a\": [\"foo\", 10]}}"
                       "{\"dependencies\": {\"a\": {\"type\": \"foo\"}}}"
                       "{\"dependencies\": {\"a\": null}}"))
    (testing "propertyNames property"
      (should-satisfy+ ::ax/json-schema
                       "{\"propertyNames\": true}"
                       "{\"propertyNames\": false}"
                       :bad
                       "{\"propertyNames\": null}"
                       "{\"propertyNames\": {\"type\": \"foo\"}}"))
    (testing "if/then/else/allOf/anyOf/oneOf/not properties"
      (should-satisfy+ ::ax/json-schema
                       "{\"allOf\": [{\"type\": \"string\"}]}"
                       "{\"anyOf\": [{\"type\": \"string\"}]}"
                       "{\"oneOf\": [{\"type\": \"string\"}]}"
                       "{\"not\": {\"type\": \"string\"}}"
                       :bad
                       "{\"if\": {\"type\": \"foo\"}}"
                       "{\"then\": {\"type\": \"foo\"}}"
                       "{\"else\": {\"type\": \"foo\"}}"
                       "{\"allOf\": {\"type\": \"foo\"}}"
                       "{\"allOf\": []}"
                       "{\"allOf\": [{\"type\": \"foo\"}]}"
                       "{\"anyOf\": {\"type\": \"foo\"}}"
                       "{\"anyOf\": []}"
                       "{\"anyOf\": [{\"type\": \"foo\"}]}"
                       "{\"oneOf\": {\"type\": \"foo\"}}"
                       "{\"oneOf\": []}"
                       "{\"oneOf\": [{\"type\": \"foo\"}]}"
                       "{\"not\": {\"type\": \"foo\"}}"))
    (testing "format property"
      (should-satisfy+ ::ax/json-schema
                       "{\"format\": \"regex\"}"
                       :bad
                       "{\"format\": null}"
                       "{\"format\": []}"))))

(deftest test-iri
  (testing "IRIs/IRLs/URIs/URLs"
    (should-satisfy+ ::ax/iri
                     "https://foo.org"
                     "https://foo.org/"
                     :bad
                     "foo.org"
                     "www.foo.org"))
  (testing "IRIs/IRLs vs URIs/URLs"
    (is (s/valid? ::ax/iri "https://안녕하세요.ko"))
    (is (s/valid? ::ax/irl "https://안녕하세요.ko"))
    (is (not (s/valid? ::ax/uri "https://안녕하세요.ko")))
    (is (not (s/valid? ::ax/url "https://안녕하세요.ko")))))
