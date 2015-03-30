(ns snap-spot.models.trip
  (:require 
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clj-time.core :as t]
    [clojure.data.json :as json]))

(timbre/refer-timbre)

(defn fetch [id] 
  "lookup the trip based on id"
  (redis/wcar* (car/get id)))

(defn persist [trip]
  "write trip redis"
  (redis/wcar* (car/set (:id trip) trip)))

(defn update [trip]
  (redis/wcar* (car/set (:id trip) trip)))
  

(defn active? [trip] 
  "checks if a trip is still active"
  (t/after? (t/plus (:start trip) (t/minutes (:duration trip))) (t/now)))

(def validations {:id [v/required] 
                  :secret [v/required]
                  :duration [v/required v/number v/positive]
                  :start [v/required]})

(comment "TODO: validate trip duration and start time(custom validator for datetime which should be used on position/instant)")
(defn validate [trip]
  "checks if a trip has all necessry attributes and is still active"
  (first (b/validate trip validations)))
