(ns com.yetanalytics.pan.objects.concepts.activity.definition.interaction-type
  (:require [clojure.spec.alpha          :as s]
            [clojure.spec.gen.alpha      :as sgen]
            [xapi-schema.spec            :as xs]
            [com.yetanalytics.pan.axioms :as ax]))

(s/def ::id
  ::ax/string)
(s/def ::description
  ::ax/language-map)

(def interaction-component-spec
  (s/and (s/keys :req-un [::id]
                 :opt-un [::description])
         (xs/restrict-keys :id :description)))

(def interaction-component-coll-spec
  (s/with-gen
    (s/and (s/coll-of interaction-component-spec
                      :kind vector?
                      :into []
                      :min-count 1)
           ;; IDs must be distinct
           (fn [icomps] (->> icomps (map :id) (apply distinct?))))
    #(->> interaction-component-spec
          s/gen
          sgen/vector-distinct
          sgen/not-empty)))
