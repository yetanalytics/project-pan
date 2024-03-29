(ns com.yetanalytics.pan.utils.resources
  (:require [clojure.java.io :refer [resource]]
            [clojure.edn     :as edn]
            [clojure.string  :as string]
            [com.yetanalytics.pan.utils.json :as json]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource IO macros
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; File reading macro inspired by:
;; https://github.com/yetanalytics/xapi-schema/blob/master/test/xapi_schema/support/data.cljc

(defn- assert-extension
  [path ext]
  (if (string/ends-with? path (str "." ext))
    nil
    (let [msg (str "read-" ext "-resource: file does not end in ." ext)]
      (throw (Exception. msg)))))

(defmacro read-resource
  "Read a file from \"resources/\" during compilation. Returns a string.
   NOTE: The resulting string must be assigned to a def, or else it will nil
   out for some mysterious reason."
  [path]
  (-> path resource slurp))

(defmacro read-edn-resource
  "Read a file from \"resources/\" during compilation. Returns EDN."
  [path]
  (assert-extension path "edn")
  (-> path resource slurp edn/read-string))

(defmacro read-json-resource
  "Read a file from \"resources/\" during compilation. Returns EDN.
   Optional at-replacement arg is what replaces the `@` symbol during JSON
   parsing (`_` by default). Removes spaces in keywords."
  ([path]
   (assert-extension path "json")
   (-> path resource slurp json/convert-json))
  ([path at-replacement]
   (assert-extension path "json")
   (-> path resource slurp (json/convert-json at-replacement))))
