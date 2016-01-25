;; Copyright © Paolo Estrella.
;; Authors: Paolo Estrella

(ns continuo.core-test
  (:require [clojure.test :refer :all]
            [continuo.core :refer [gen-id]]))

(deftest test-ids
  (testing "Unique IDs"
    (let [n 100]
      (letfn [(ids [] (cons (gen-id) (lazy-seq (ids))))]
        (is (= n (count (into #{} (take n (ids)))))))))

  (testing "Sequenced IDs"
    (let [n 100]
      (loop [prev (gen-id)
             total 0]
        (let [id (gen-id)]
          (when (< total n)
            (do
              (is (< prev id))
              (recur id (inc total)))))))))
