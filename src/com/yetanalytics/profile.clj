(ns com.yetanalytics.profile
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.util :as util]
            [com.yetanalytics.profiles.versions :as versions]
            [com.yetanalytics.profiles.author :as author]
            [com.yetanalytics.objects.concept :as concept]
            [com.yetanalytics.objects.template :as template]
            [com.yetanalytics.objects.pattern :as pattern]))

(s/def ::id ::ax/iri)
(def context-url "https://w3id.org/xapi/profiles/context")
(s/def ::context (s/or :context ::ax/uri
                       :context-array (s/and
                                       (s/coll-of ::ax/uri :type vector?)
                                       (partial some #(= context-url %)))))
(s/def ::type #{"Profile"})
(s/def ::conforms-to ::ax/uri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::see-also ::ax/url)

;; Check that the overall profile ID is not any of the version IDs
(s/def ::id-distinct
  (fn [{:keys [id versions]}]
    (let [version-ids (util/only-ids versions)]
      (nil? (some #(= id %) version-ids)))))

(s/def ::profile-top-level
  (s/and (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                          ::definition ::author/author ::versions/versions]
                 :opt-un [::see-also])
         ::id-distinct))

(s/def ::profile
  (s/merge ::profile-top-level
           (s/keys :opt-un [::concept/concepts
                            ::template/templates
                            ::pattern/patterns])))

; (defn vids [versions prof-id]
;   (let [vid-vec (only-ids versions)]
;     (let [vid-set (set vid-vec)]
;       (if (and (not= (count vid-vec) (count vid-set))
;                (not (contains? vid-set prof-id)))
;         vid-set
;         nil))))

(s/def ::profile
  (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                   ::definition ::author/author ::versions/versions]
          :opt-un [::see-also
                   ::concept/concepts
                   ::template/templates
                   ::pattern/patterns]))

;; TODO: stricter validation levels
(defn version-set [{:keys [versions]}]
  (-> versions util/only-ids set))

(defn id-object-map [profile kword]
  (let [obj-vec (profile kword)
        id-vec (util/only-ids obj-vec)]
    (zipmap id-vec obj-vec)))

; (defn spec-with-args [spec obj-kword obj args-map])
; (s/valid? spec (conj args-map [obj-kword obj]))

; (defn spec-arr [spec obj-kword obj-arr args-map]
;   (mapv (spec-with-args)))

(defn validate-profile
  "Weak validation; only check property types and basic syntax"
  [profile]
  (s/explain-data ::profile profile))

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
                                 :patterns-table patterns-map})]
    ;; TODO Combine the explain-data into a single array
    (and (s/valid? ::profile-top-level profile)
         (s/valid? ::concepts+ concepts-args)
         (s/valid? ::templates+ templates-args)
         (s/valid? ::patterns+ patterns-args))))

; (defn validate-profile+
;   "Semi-strict validation; validate local IRIs"
;   [profile]
;   (let [concepts (:concepts profile)
;         templates (:templates template)
;         patterns (:templates patterns)
;         ;; Spec arguments
;         vid-set (version-set profile)
;         concepts-map (id-object-map profile :concepts)
;         templates-map (id-object-map profile :templates)
;         patterns-map (id-object-map pattern :patterns)]
;     (s/and (s/valid? ::profile-top-level profile)
;            (s/valid? ::concepts+ {:concepts concepts
;                                   :vid-set vid-set
;                                   :concepts-map concepts-map})
;            (s/valid? ::templates+ {:templates templates
;                                    :vid-set vid-set
;                                    :concepts-map concepts-map
;                                    :templates-map templates-map})
;            (s/valid? ::patterns+ {:patterns patterns
;                                   :vid-set vid-set
;                                   :templates-map templates-map
;                                   :patterns-map patterns-map}))))

;; Non short-circuit validation

; (defn validate-profile-noshort
;   "Weak validation without short-circuiting"
;   [profile]
;   (concat
;    (filterv some? [(s/explain-data ::profile-top-level profile)])
;    (concept/explain-concepts (profile :concepts))
;    (template/explain-templates (profile :templates))
;    (pattern/explain-patterns (profile :templates))))

; (defn validate-profile-noshort+
;   "Semi-strict validation without short-circuiting"
;   [profile]
;   (let [concepts (:concepts profile)
;         templates (:templates template)
;         patterns (:templates patterns)
;         ;; Spec arguments
;         vid-set (version-set profile)
;         concepts-map (id-object-map profile :concepts)
;         templates-map (id-object-map profile :templates)
;         patterns-map (id-object-map pattern :patterns)]
;     (concat
;      (filterv some? [(s/explain-data ::profile-top-level profile)])
;      (concept/explain-concepts+ {:concepts concepts
;                                  :vid-set vid-set
;                                  :concepts-map concepts-map})
;      (template/explain-templates+ {:templates templates
;                                    :vid-set vid-set
;                                    :concepts-map concepts-map
;                                    :templates-map templates-map})
;      (pattern/explain-patterns+ {:patterns patterns
;                                  :vid-set vid-set
;                                  :templates-map templates-map
;                                  :patterns-map patterns-map}))))
