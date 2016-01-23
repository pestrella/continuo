;; Copyright © Paolo Estrella.
;; Authors: Paolo Estrella

(ns continuo.core)

;; TODO: make configurable by the client
(def ^:dynamic *instance-id* 1)
(def ^:dynamic *date-offset* 1212188400000)

;; bitwise operates, more conveniently named
(def >>> unsigned-bit-shift-right)
(def << bit-shift-left)
(def | bit-or)
(def & bit-and)

(def shift-millis 3)
(def offset (>>> *date-offset* shift-millis))

(def counter-mask 0x00ffffff)
(def counter-bits (Long/bitCount counter-mask))
(def worker-mask 0x000ff)
(def worker-bits (Long/bitCount worker-mask))

(def previous-time (ref 0))
(def counter (ref 0))

(defn- tolerate-time-shift?
  "Returns true if the system clock went backwards a tolerated amount.
   While unlikely, the system clock can get out of whack; this function
   will tolerate 100ms of backward time changes. Non-unique IDs can
   result if the system clock alters significantly."
  [t]
  (let [tdiff (- t (System/currentTimeMillis))]
    (< tdiff 100)))

(defn- get-id
  "Returns a unique, 64-bit, sequenced, sortable ID."
  [millis counter offset]
  (let [id (-> (>>> millis shift-millis)
               (- offset)
               (<< counter-bits))
        counter+worker (| (<< counter worker-bits) (& *instance-id* worker-mask))]
    (| id (& counter+worker counter-mask))))

(defn gen-id []
  (when (not *instance-id*)
    (throw (ex-info "Worker ID must be set" {})))
  (when (not (tolerate-time-shift? @previous-time))
    (throw (ex-info "Clock has altered a significant amount; non-unique IDs may occur"
                    {:previous-time @previous-time})))
  (let [t (System/currentTimeMillis)]
    (dosync
     (alter previous-time (fn [_ x] x) t)
     (alter counter + 1))
    (get-id t @counter offset)))
