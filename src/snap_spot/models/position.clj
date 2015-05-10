(ns snap-spot.models.position
  (:require 
    [snap-spot.redis :as redis]
    [snap-spot.helper :as helper]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clj-time.core :as t]
    [clojure.data.json :as json]))

(timbre/refer-timbre)

(defn redis-key [trip] (str (:id trip) "-positions"))
(defn redis-last-updated-key [trip] (str (:id trip) "-last-updated"))

(defn last-updated [trip] 
  (or 
    (redis/wcar* (car/get (redis-last-updated-key trip)))
    (java.time.Instant/EPOCH)))

(defn fetch-all [trip]
  (redis/wcar* (car/lrange (redis-key trip) 0 -1)))

(def validations {:lat [v/required v/number] 
                  :lon [v/required v/number]
                  :order [v/required]})

(defn validate [position]
  (first (b/validate position validations)))

(defn add [trip position]
  (redis/wcar*
    (car/lpush (redis-key trip) position)
    (car/set (redis-last-updated-key trip) (java.time.Instant/now))))

