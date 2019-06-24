(ns com.yetanalytics.objects.concepts.util
  (:require [com.yetanalytics.util :as u]
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn filter-concepts
  [{:keys [target-type-str concepts ?deprecated]}]
  (if ?deprecated
    (filterv (fn [concept]
               (let [{c-type :type
                      c-dep? :deprecated} concept]
                 (and (= target-type-str c-type) c-dep?))) concepts)
    (filterv (fn [concept] (-> concept :type (= target-type-str))) concepts)))

(defn ids-of-target-concept-type
  [{:keys [target-type-str profile ?deprecated]}]
  (->> {:target-type-str target-type-str
        :concepts (:concepts profile)
        :?deprecated ?deprecated}
       filter-concepts
       u/only-ids))

(defn iri-in-profile-concepts?
  ;; assumes profile is a keywordized map
  [{:keys [iri target-type-str profile ?deprecated]}]
  (let [concepts (ids-of-target-concept-type {:target-type-str target-type-str
                                              :profile profile
                                              :?deprecated ?deprecated})]
    (u/containsv? concepts iri)))

(comment
  ;; Example of how to do more effecient
  (def dummy-profile {:concepts [{:id "https://www.some-target-iri.com/miss"
                                  :type "ActivityType"}
                                 {:id "https://www.some-target-iri.com"
                                  :type "Verb"}
                                 {:id "https://www.some-target-iri.com/another-hit"
                                  :type "Verb"}]
                      :templates []
                      :patterns []})

  (let [valid-targets (ids-of-target-concept-type {:target-type-str "Verb"
                                                   :profile dummy-profile})]
    (mapv (fn [iri] (u/containsv? valid-targets iri)) ["https://www.some-target-iri.com" "https://www.some-target-iri.com/another-hit"]))

  (iri-in-profile-concepts?
   {:iri "https://www.some-target-iri.com"
    :target-type-str "Verb"
    :profile dummy-profile})

  (iri-in-profile-concepts?
   {:iri "https://www.some-target-iri.com/another-hit"
    :target-type-str "Verb"
    :profile dummy-profile})

  ;; example of how to do strict validation at top level
  (s/valid? ::stict-profile-spec profile)
  (s/def ::strict-profile-spec
    (fn [{:keys [concepts patterns templates]}]
      (s/valid? ::strict-concepts concepts)
      (s/valid? ::strict-patterns patterns)
      (s/valid? ::strict-templates templates)))

  (defn get-external-thing
    [id]
    "got the thing")

  (s/def ::dependent-on-external
    (fn [m]
      (let [{thing-to-check :check-me
             to-check-against :resolve-me} m
            data (get-external-thing to-check-against)]
        ;; focus on the predicate instead of the external resolution
        (= thing-to-check data))))

  (defn validate-profile
    [profile]
    (reduce-kv
     (fn [accum k v]
       (let [{profile-id :id} profile]
        (case k
         :id accum ;; no op bc nothing to be more strict about
         :concepts (for [each v] ;; v = concepts in the profile
                     (let [{concept-type :type} each]
                       (reduce-kv ;; think from this persepctive
                        (fn [ack concept-k concept-v]
                          ;; TODO: stepping in pattern matching territory
                          ;; - core.match vs meander comes in
                          (case (and concept-k concept-type)
                            :another-example-key
                            (conj ack (s/valid? :something-needs-val-from-profile
                                                {:value-to-check     concept-v
                                                 :value-from-profile profile-id}))
                            :example-key-with-additional-constraints
                            (conj ack (s/valid? ::u/in-scheme-strict-scalar
                                                {:in-scheme concept-v
                                                 :profile profile}))
                            "...")) [] each)))
         :templates "..."
         :patterns "..."
         "..."))) [] profile))

  {:concept-1 [true true {:SPEC-ERROR "some data"} true {:SPEC-ERROR "some data"}]
   :concept-2 [true true {:SPEC-ERROR "some data"}]}

  (validate-profile dummy-profile)

  (reduce-kv
   (fn [accum k v]
     (if (= k :foo)
       (conj accum v)
       (conj accum (:qux v))))
   []
   {:foo "bar" :baz {:qux "hello world"}})
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::inline-or-iri
  (fn [ext]
    (let [schema? (contains? ext :schema)
          inline-schema? (contains? ext :inline-schema)]
      (not (and schema? inline-schema?)))))
