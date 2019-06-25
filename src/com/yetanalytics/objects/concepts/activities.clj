(ns com.yetanalytics.objects.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [xapi-schema.spec]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Activity"})
(s/def ::in-scheme ::ax/iri)
(s/def ::deprecated ::ax/boolean)
(def context-url "https://w3id.org/xapi/profiles/activity-context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))

;; Need to use this function instead of s/merge because of restrict-keys in
;; xapi-schema function.
(s/def ::activity-definition
  (s/and (s/keys :req-un [::context])
         (fn [adef] (s/valid? :activity/definition (dissoc adef :context)))))

(s/def ::activity
  (s/keys :req-un [::id ::type ::in-scheme ::activity-definition]
          :opt-un [::deprecated]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::activity-in-profile-strict
  (fn [{:keys [activity profile]}]
    (let [{:keys [in-scheme]} activity]
      (s/and (s/valid? ::activity activity)
             (s/valid? ::u/in-scheme-strict-scalar {:in-scheme in-scheme
                                                    :profile profile})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; advanced processing for spec MUSTS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: spec MUSTs activity definition w/ extension
;; - MUST include JSON-LD @context in all top-level objects of extensions
;; - MUST ensure every extension @context is sufficient to guareantee all properties in the extension
;;        expand to absolute IRIs during JSON-LD processing
;; - see Profile Authors: bellow table at:
;; https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#74-activities
