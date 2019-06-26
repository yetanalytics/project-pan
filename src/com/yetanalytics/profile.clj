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

;; prefLabel + definition
(s/def ::see-also ::ax/url)

(s/def ::profile-top-level
  (s/keys :req-un [::id ::context ::type ::conforms-to ::pref-label
                   ::definition ::author/author ::versions/versions]
          :opt-un [::see-also]))

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
(defn version-set [{:keys [id versions]}]
  (let [vid-vec (util/only-ids versions)]
    (let [vid-set (set vid-vec)]
      (if (and (= (count vid-set) (count vid-vec)) (contains? vid-set id))
        vid-set
        nil))))

(defn id-object-map [profile kword]
  (let [obj-vec (profile kword)]
    (let [id-vec (util/only-ids obj-vec)]
      (zipmap id-vec obj-vec))))

(defn spec-with-args [spec obj-kword obj args-map])
(s/valid? spec (conj args-map [obj-kword obj]))

(defn spec-arr [spec obj-kword obj-arr args-map]
  (mapv (spec-with-args)))

(defn validate-profile
  "Weak validation; only check property types and basic syntax"
  [profile]
  (s/explain-data ::profile profile))

(defn validate-profile+
  "Semi-strict validation; validate local IRIs"
  [profile]
  (let [concepts (:concepts profile)
        templates (:templates template)
        patterns (:templates patterns)
        ;; Spec arguments
        vid-set (version-set profile)
        concepts-map (id-object-map profile :concepts)
        templates-map (id-object-map profile :templates)
        patterns-map (id-object-map pattern :patterns)]
    (s/and (s/valid? ::profile-top-level profile)
           (s/valid? ::concepts+ {:concepts concepts
                                  :vid-set vid-set
                                  :concepts-map concepts-map})
           (s/valid? ::templates+ {:templates templates
                                   :vid-set vid-set
                                   :concepts-map concepts-map
                                   :templates-map templates-map})
           (s/valid? ::patterns+ {:patterns patterns
                                  :vid-set vid-set
                                  :templates-map templates-map
                                  :patterns-map patterns-map}))))

;; Non short-circuit validation

(defn validate-profile-noshort
  "Weak validation without short-circuiting"
  [profile]
  (concat
   (filterv some? [(s/explain-data ::profile-top-level profile)])
   (util/error-map ::concept/concept (profile :concepts))
   (util/error-map ::template/template (profile :templates))
   (util/error-map ::pattern/pattern (profile :pattern))))

(defn validate-profile-noshort+
  "Semi-strict validation without short-circuiting"
  [profile]
  (let [concepts (:concepts profile)
        templates (:templates template)
        patterns (:templates patterns)
        ;; Spec arguments
        vid-set (version-set profile)
        concepts-map (id-object-map profile :concepts)
        templates-map (id-object-map profile :templates)
        patterns-map (id-object-map pattern :patterns)]
    (s/and (s/valid? ::profile-top-level profile)
           (s/valid? ::concepts+ {:concepts concepts
                                  :vid-set vid-set
                                  :concepts-map concepts-map})
           (s/valid? ::templates+ {:templates templates
                                   :vid-set vid-set
                                   :concepts-map concepts-map
                                   :templates-map templates-map})
           (s/valid? ::patterns+ {:patterns patterns
                                  :vid-set vid-set
                                  :templates-map templates-map
                                  :patterns-map patterns-map}))))
