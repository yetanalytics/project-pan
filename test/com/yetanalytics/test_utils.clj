(ns com.yetanalytics.test-utils
  (:require [clojure.test :refer [is] :include-macros true]
            [clojure.spec.alpha :as s :include-macros true]))

;; This is a copy-paste of the xapi-schema.support.spec namespace defined in
;; the xapi-schema project. Distribution from the original project is
;; permitted under the Eclipse Public License 1.0.
;; Author: Milt Reder

;; Tests one data point to see if it conforms to spec.
(defn should-satisfy [spec data]
  (is (nil? (s/explain-data spec data))))

;; Tests one data point to see if it does NOT conform to spec.
(defn should-not-satisfy [spec data]
  (is (not (nil? (s/explain-data spec data)))))

;; Tests multiple data points against a spec.
(defn should-satisfy+
  [spec & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        checked-bad-spec (when bads (map (partial s/explain-data spec)
                                         bads))]
    (is (nil? (s/explain-data (s/coll-of spec) goods)))
    (when bads
      (is (not (some nil? checked-bad-spec))))))

;; Tests multiple key-value pairs against a spec.
;; Params:
;;     - spec: spec to test against
;;     - base: the minimal Object that conforms to the spec, on which you add
;;             the key-value pair
;;     - key: the key to test
;;     - goods-bads: the values to test
(defn key-should-satisfy+ [spec
                           base
                           key
                           & goods-bads]
  (let [[goods _ bads] (partition-by #(= :bad %) goods-bads)
        gbs  (concat
              (map
               #(assoc base key %)
               goods)
              '(:bad)
              (map
               #(assoc base key %)
               bads))]
    (apply
     should-satisfy+
     spec
     gbs)))
