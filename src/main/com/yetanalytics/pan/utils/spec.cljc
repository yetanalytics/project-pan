(ns com.yetanalytics.pan.utils.spec)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic functions and specs 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-nil
  "Turn a nil array into an empty array"
  [value]
  (if (nil? value) [] value))

(defn normalize-profile
  "Turn any nil top-level arrays in a profile (versions, concepts, templates
  and patterns) into empty arrays."
  [profile]
  (-> profile
      (update :versions normalize-nil)
      (update :concepts normalize-nil)
      (update :templates normalize-nil)
      (update :patterns normalize-nil)))

(defn subvec?
  "True if v1 is a subvector of v2, false otherwise."
  [v1 v2]
  (let [len1 (count v1) len2 (count v2)]
    (and (<= len1 len2)
         (= v1 (subvec v2 0 len1)))))
