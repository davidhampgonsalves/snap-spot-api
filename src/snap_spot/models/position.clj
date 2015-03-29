(ns snap-spot.models.position
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clj-time.core :as t]
    [clojure.data.json :as json]))

(timbre/refer-timbre)

(defn trip-positions-key [trip] (str (:id trip) "-positions"))

(defn fetch-positions [trip]
  (redis/wcar* (car/lrange (trip-positions-key trip) 0 -1)))

(def validations {:lat [v/required v/number] 
                  :lon [v/required v/number]
                  :instant [v/required]})

(defn validate [position]
  (first (b/validate position validations)))

(defn add [trip position]
  (redis/wcar* (car/lpush (trip-positions-key trip) position)))

