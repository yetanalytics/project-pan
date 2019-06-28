(ns com.yetanalytics.objects.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [clojure.set :refer [rename-keys]]
            [clojure.walk :refer [stringify-keys]]
            [camel-snake-kebab.core :as csk]
            [xapi-schema.spec]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as u]
            [com.yetanalytics.objects.concepts.util :as cu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activity
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Activity"})
(s/def ::in-scheme ::ax/iri)
(s/def ::deprecated ::ax/boolean)
(def context-url "https://w3id.org/xapi/profiles/activity-context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))

;; Rename keys (as xapi-scheme uses camelCase instead of kebab-case
(defn camel-case-keys
  [kmap]
  (let [keys-kebab (keys kmap)
        keys-camel (map csk/->camelCase keys-kebab)]
    (rename-keys kmap (zipmap keys-kebab keys-camel))))

;; Turn language map keys back into strings
(defn stringify-lang-keys
  [kmap]
  (let [stringify-name (update kmap :name stringify-keys)]
    (update stringify-name :description stringify-keys)))

;; Need to use this function instead of s/merge because of restrict-keys in
;; xapi-schema function.
(s/def ::activity-definition
  (s/and (s/keys :req-un [::context])
         (fn [adef]
           (s/valid? :activity/definition
                     (stringify-lang-keys
                      (camel-case-keys (dissoc adef :context)))))))

(s/def ::activity
  (s/keys :req-un [::id ::type ::in-scheme ::activity-definition]
          :opt-un [::deprecated]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-profile validation+ helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::activity-basic
  (fn [{:keys [object]}] (s/valid? ::activity object)))

(s/def ::activity+
  (s/and ::activity-basic
         ::u/in-scheme-valid?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; advanced processing for spec MUSTS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: spec MUSTs activity definition w/ extension
;; - MUST include JSON-LD @context in all top-level objects of extensions
;; - MUST ensure every extension @context is sufficient to guareantee all 
;;   properties in the extension expand to absolute IRIs during JSON-LD 
;;   processing
;; - see Profile Authors: bellow table at:
;; https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#74-activities
