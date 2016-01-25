;; Copyright © Paolo Estrella.
;; Authors: Paolo Estrella

(ns continuo.core
  (:require [clojure.tools.logging :refer [warn]]))

;; TODO: make configurable by the client
(def ^:dynamic *instance-id* nil)
(def ^:dynamic *ms-offset* 631152000000)

;; bitwise operates, more conveniently named
(def >>> unsigned-bit-shift-right)
(def << bit-shift-left)
(def | bit-or)
(def & bit-and)

(def counter-mask 0x00ffffff)
(def counter-bits (Long/bitCount counter-mask))
(def inst-mask 0x000ff)
(def inst-bits (Long/bitCount inst-mask))

(defn- next-id
  "Returns the next unique, 64-bit, sequenced, sortable ID."
  [millis counter instance-id]
  (let [id (-> (>>> millis 3)
               (- (>>> *ms-offset* 3)))
        counter+inst (| (<< counter inst-bits)
                        (& instance-id inst-mask))]
    (| (<< id counter-bits) (& counter+inst counter-mask))))

(defn- tolerate-time-shift?
  "Returns true if the system clock went backwards a tolerated amount.
   While unlikely, the system clock can get out of whack; this function
   will tolerate 100ms of backward time changes. Non-unique IDs can
   result if the system clock alters significantly."
  [t]
  (let [tdiff (- t (System/currentTimeMillis))]
    (< tdiff 100)))

(def previous-time (ref 0))
(def counter (ref 0))

(defn gen-id []
  (when (not *instance-id*)
    (warn (str "*instance-id* is not set; "
               "non-unique IDs may occur across a network.")))
  (when (not (tolerate-time-shift? @previous-time))
    (throw (ex-info (str "Clock has altered a significant amount; "
                         "aborting in order to avoid non-unique IDs.")
                    {:previous-time @previous-time})))
  (let [t (System/currentTimeMillis)]
    (dosync
     (alter previous-time (fn [_ x] x) t)
     (alter counter + 1))
    (next-id t @counter (or *instance-id* 1))))
