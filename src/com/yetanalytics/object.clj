(ns com.yetanalytics.object
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]))

;; Basic properties

;; ID and type properties are required by ALL objects, no exceptions
(s/def ::id ::ax/iri) ; Technically Templates and Patterns require URIs
(s/def ::type #{"Profile"
                ; Author types
                "Organization"
                "Person"
                ; Extensions
                "ContextExtension"
                "ResultExtension"
                "ActivityExtension"
                ; Document Resources
                "StateResource"
                "AgentProfileResource"
                "ActivityProfileResource"
                ; Other concepts
                "Verb"
                "ActivityType"
                "AttachmentUsageType"
                "Activity"
                ; Other object types
                "StatementTemplate"
                "Pattern"})

(s/def ::common (s/keys :req-un [::id ::type]))

;; Properties common across all objects; however some objects may not have
;; them (or only have them under certain circumstances).
(s/def ::in-scheme ::ax/iri)
(s/def ::pref-label ::ax/language-map)
(s/def ::definition ::ax/language-map)
(s/def ::deprecated ::ax/boolean)

(defmulti object? ::type)
(defmethod object? "Profile" [_] #(map? %))
;; Profile Top-Level Properties

; (s/def :profile/profile
;   (s/merge :object/common
;            (s/keys :req-un [:profile/context
;                             :profile/conforms-to
;                             :object/pref-label
;                             :object/definition]
;                    :opt-un [:profile/see-also
;                             :profile/versions
;                             :profile/author
;                             :object/concepts
;                             :object/templates
;                             :object/patterns])))

; (s/def :author/author (s/keys :req-un [:object/type
;                                        :author/name
;                                        :author/url]))

; (defmethod object? "Organization" [_] :author/author)
; (defmethod object? "Person" [_] :author/author)

; (defmethod object? "Profile" [_] :profile/profile)

; ;; Statement Template Properties

; (s/def :template/template
;   (s/merge :object/common
;            (s/keys :req-un [:object/in-scheme
;                             :object/pref-label
;                             :object/definition]
;                    :opt-un [:object/deprecated
;                             :template/verb
;                             :template/object-activity-type
;                             :template/context-grouping-activity-type
;                             :template/context-parent-activity-type
;                             :template/context-other-activity-type
;                             :template/context-category-activity-type
;                             :template/attachment-usage-type
;                             :template/object-statement-ref-template
;                             :template/context-statement-ref-template
;                             :template/rules])))

; (s/def :template/rule (s/keys :req-un [:template/location]
;                               :opt-un [:template/selector
;                                        :template/presence
;                                        :template/any
;                                        :template/all
;                                        :template/none
;                                        :template/scope-note]))

; (defmethod object? "StatementTemplate" [_] :template/template)

; ;; Pattern Properties

; (s/def :pattern/common
;   (s/merge :object/common
;            (s/keys :req-un [(or :pattern/alternates
;                                 :pattern/optiona
;                                 :pattern/one-or-more
;                                 :pattern/sequence
;                                 :pattern/zero-or-more)]
;                    :opt-un [:pattern/primary
;                             :object/in-scheme
;                             :object/deprecated])))

; (defmulti pattern? :pattern/primary)
; (defmethod pattern? true [_]
;   (s/merge :pattern/common
;            (s/keys :req-un [:object/pref-label
;                             :object/definition])))
; (defmethod pattern? :default [_]
;   (s/merge :pattern/common
;            (s/keys :opt-un [:object/pref-label
;                             :object/definition])))

; (defmethod object? "Pattern" [_] :pattern/pattern)

; ;; Concepts

; (s/def :concept/common
;   (s/merge :object/common
;            (s/keys :req-un [:object/in-scheme
;                             :object/pref-label
;                             :object/definition]
;                    :opt-un [:object/deprecated])))

; (defmulti concept? :concept/type)

; (s/def :concept/verb-or-type
;   (s/merge :concept/common
;            (s/keys req-un [:verb/broader
;                            :verb/broad-match
;                            :verb/narrower
;                            :verb/narrow-match
;                            :verb/related
;                            :verb/related-match
;                            :verb/exact-match])))

; (defmethod concept? "Verb" [_] :concept/verb-or-type)
; (defmethod concept? "ActivityType" [_] :concept/verb-or-type)
; (defmethod concept? "AttachmentUsageType" [_] :concept/verb-or-type)

; (s/def :concept/activity-extension
;   (s/merge :concept/common
;            (s/keys :req-un [(or :concept/schema
;                                 :concept/inline-schema)]
;                    :opt-un [:extension/recommended-activity-types
;                             :extension/context])))

; (s/def :concept/verb-extension
;   (s/merge :concept/common
;            (s/keys :req-un [(or :concept/schema
;                                 :concept/inline-schema)]
;                    :opt-un [:extension/recommended-verb
;                             :extension/context])))

; (defmethod concept? "ContextExtension" [_] :concept/verb-extension)
; (defmethod concept? "ResultExtension" [_] :concept/verb-extension)
; (defmethod concept? "ActivityExtension" [_] :concept/activity-extension)

; (s/def :concept/document-resource
;   (s/merge :concept/common
;            (s/keys :req-un [:document-resources/content-type
;                             (or :concept/schema
;                                 :concept/inline-schema)]
;                    :opt-un [:document-resources/context])))

; (defmethod concept? "StateResource" [_] :concept/document-resource)
; (defmethod concept? "AgentProfileResource" [_] :concept/document-resource)
; (defmethod concept? "ActivityProfileResource" [_] :concept/document-resource)

; (defmethod concept? "Activity" [_]
;   (s/merge :object/common
;            (s/keys :req-un [:object/in-scheme
;                             :activity/activity-definition]
;                    :opt-un [:deprecated])))

; (s/def :concept/concept (s/multi-spec concept? :concept/type))

; ; (defmethod object? :default [_] :concept/concept)

; (defmethod object? "Person" [_] (true))

(s/def ::object (s/multi-spec object? ::type))

(s/explain ::object {:type "Profile"})
; (s/explain ::object {:url "https://www.yetanalytics.io"
;                      :type "Organization"
;                      :name "Yet Analytics"})

; (defmulti foobar? ::type)

(defn dispatch-fn [x] x)
(defmulti foo dispatch-fn)
