(ns com.yetanalytics.pan.utils.resources
  (:require [clojure.java.io :refer [resource]]
            [clojure.edn :as edn]
            [com.yetanalytics.pan.utils.json :as json]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource IO macros
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; File reading macro inspired by:
;; https://github.com/yetanalytics/xapi-schema/blob/master/test/xapi_schema/support/data.cljc

(defmacro read-resource
  "Read a file from \"resources/\" during compilation. Returns a string.
   NOTE: The resulting string must be assigned to a def, or else it will nil
   out for some mysterious reason."
  [path]
  (-> path resource slurp))

(defmacro read-edn-resource
  "Read a file from \"resources/\" during compilation. Returns EDN."
  [path]
  (-> path resource slurp edn/read-string))

(defmacro read-json-resource
  "Read a file from \"resources/\" during compilation. Returns JSON.
   Optional at-replacement arg is what replaces the \"@\" symbol during JSON
   parsing. Removes spaces in keywords."
  ([path]
   (-> path resource slurp json/convert-json))
  ([path at-replacement]
   (-> path resource slurp (json/convert-json at-replacement))))
