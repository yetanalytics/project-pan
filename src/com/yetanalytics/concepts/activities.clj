(ns com.yetanalytics.concepts.activities
  (:require [clojure.spec.alpha :as s]
            [com.yetanalytics.axioms :as ax]
            [com.yetanalytics.concept :as concept]))

;; Basic properties

; (s/def ::id ::ax/iri)
; (s/def ::type #{"Activity"})
; (s/def ::in-scheme ::ax/iri)
; (s/def ::deprecated ::ax/boolean)

;; Advanced properties


(s/def ::context (s/or :single-val ::ax/uri
                       :array-val (s/coll-of ::ax/uri :type vector?)))
;; TODO Include Activity defintion from basic spec
(s/def ::activity-def (s/keys :req [::context ::ax/boolean])) ;; TODO Placeholder

(s/def ::activity (s/keys :req [::id
                                ::type
                                ::in-scheme
                                ::activity-def]
                          :opt [:deprecated]))

(s/def ::activity
  (s/merge ::object/common
           (s/keys :req-un [::object/in-scheme
                            ::activity-definition]
                   :opt-un [:deprecated])))

(defmethod concept/concept? "Activity" [_] ::activity)
