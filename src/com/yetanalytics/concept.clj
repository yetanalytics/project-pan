(ns com.yetanalytics.concept
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.object :as object]))

(s/def ::common
  (s/merge ::object/common
           (s/keys :req-un [::object/in-scheme
                            ::object/pref-label
                            ::object/definition]
                   :opt-un [::object/deprecated])))

(s/def ::schema ::ax/iri)
(s/def ::inline-schema ::ax/json-schema)

(defn extension-xor? [ext]
  (let [schema? (contains? ext :schema)
        inline-schema? (contains? ext :inline-schema)]
    (or (and schema? (not inline-schema?))
        (and inline-schema? (not inline-schema?)))))

(defmulti concept? :object/type)

(s/def ::concept (s/multi-spec concept? :object/type))

(defmethod object/object? :default [_] ::concept)
