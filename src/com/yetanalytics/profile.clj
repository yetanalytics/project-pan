(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.profiles.versions :as versions]
            [com.yetanalytics.profiles.author :as author]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Profile 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::id ::ax/iri)
(s/def ::type #{"Profile"})
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::see-also ::ax/url)

(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))

;; Check that the overall profile ID is not any of the version IDs
(s/def ::id-distinct
  (fn [{:keys [id versions]}]
    (let [version-ids (util/only-ids versions)]
      (nil? (some #(= id %) version-ids)))))

;; Spec for profile metadata only
(s/def ::profile-top-level
  (s/and (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                          ::definition ::author/author ::versions/versions]
                 :opt-un [::see-also])
         ::id-distinct))

;; Spec for entire profile
(s/def ::profile
  (s/merge ::profile-top-level
           (s/keys :opt-un [::concept/concepts
                            ::template/templates
                            ::pattern/patterns])))

;; Set of all version IDs
(defn version-set [{:keys [versions]}]
  (-> versions util/only-ids set))

;; Map of object IDs to their respective objects
(defn id-object-map [profile kword]
  (let [obj-vec (profile kword)
        id-vec (util/only-ids obj-vec)]
    (zipmap id-vec obj-vec)))

(defn validate-profile
  "Weak validation; only check property types and basic syntax"
  [profile]
  (s/explain ::profile profile))

(defn validate-profile+
  "Semi-strict validation; validate inScheme property and local IRIs"
  [profile]
  (let [concepts (:concepts profile)
        templates (:templates profile)
        patterns (:templates profile)
        ;; Spec arguments
        vid-set (version-set profile)
        concepts-map (id-object-map profile :concepts)
        templates-map (id-object-map profile :templates)
        patterns-map (id-object-map profile :patterns)
        patterns-graph (pattern/pattern-graph patterns-map)
        ;; Combine objects and arguments
        concepts-args (util/combine-args
                       concepts {:vid-set vid-set
                                 :concepts-table concepts-map})
        templates-args (util/combine-args
                        templates {:vid-set vid-set
                                   :concepts-table concepts-map
                                   :templates-table templates-map})
        patterns-args (util/combine-args
                       patterns {:vid-set vid-set
                                 :templates-table templates-map
                                 :patterns-table patterns-map
                                 :patterns-graph patterns-graph})]
    ;; TODO Combine the explain-data into a single array
    (and (s/valid? ::profile-top-level profile)
         (s/valid? ::concept/concepts+ concepts-args)
         (s/valid? ::template/templates+ templates-args)
         (s/valid? ::pattern/patterns+ patterns-args))))
