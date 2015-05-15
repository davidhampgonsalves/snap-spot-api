(ns snap-spot.models.trip
  (:require 
    [snap-spot.redis :as redis]
    [snap-spot.helper :as helper]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clj-time.core :as t]
    [clojure.data.json :as json]
    [snap-spot.models.position :as position :only delete]))

(timbre/refer-timbre)

(def key-prefix "trip:")
(defn id->key [id] (str key-prefix id))
(defn redis-key [trip] (id->key (:id trip)))
(defn key? [k] 
  "check if string is a redis trip key"
  (.startsWith k key-prefix))

(defn remaining-duration [trip]
  "calculate the remaing duration for a trip(duration must be set)"
  (let [duration-seconds (* (:duration trip) 1000)
        elapsed-seconds  (t/in-seconds (t/interval (:start trip) (t/now)))]
    (- duration-seconds elapsed-seconds)))

(defn fetch-by-key [k] 
  "lookup the trip based on its key"
  (redis/wcar* (car/get k)))

(defn fetch [id] 
  "lookup the trip based on id"
  (fetch-by-key (id->key id)))

(defn create [trip]
  "write trip to redis"
  (redis/wcar* 
    (car/setex (redis-key trip) (.intValue (* (:duration trip) 60)) (dissoc trip :duration))))

(defn update [trip]
  "update a trip based on its id"
  (redis/wcar* 
    (car/setex (redis-key trip) (remaining-duration trip) (dissoc trip :duration)))) 

(defn seconds-since-update [trip]
  "push new position to subscribed channels and persist to redis if last update was older then a second ago"
  (let [last-updated (position/last-updated trip)]
    (t/in-seconds (t/interval last-updated (t/now)))))

(defn has-valid-secret? [trip existing-trip]
  (if-not (nil? existing-trip)
    (= (:secret existing-trip) (:secret trip))
    false))

(defn valid-or-throw [trip]
  "throw error if trip is invalid"
  (if (nil? trip) 
    (helper/throw-exception "trip is nil."))
  (let [existing-trip (fetch (:id trip))]
    (if (nil? existing-trip)
      (helper/throw-exception "trip does not exist."))
    (if-not (has-valid-secret? trip existing-trip) 
      (helper/throw-exception "invalid secret."))))

(comment "logically this should be in position but it causes a cyclical dependancy")
(defn redis-position-key [trip] (str (:id trip) "-positions"))
(defn redis-last-updated-key [trip] (str (:id trip) "-last-updated"))

(defn delete [trip]
  "delete a trip and all assoicated data"
  (redis/wcar* 
    (car/del (redis-key trip) (redis-position-key trip) (redis-last-updated-key trip))))

(defn delete-if-aged [k]
  "delete trip if it is really old" 
  (let [trip (fetch-by-key k)]
    (if (> (t/in-days (t/interval (:start trip) (t/now))) 0)
      (delete trip))))

(def validations {:id [v/required] 
                  :secret [v/required]
                  :duration [v/required v/number v/positive]
                  :start [v/required]})

(comment "TODO: validate trip duration and start time(custom validator for datetime which should be used on position/instant)")
(defn validate [trip]
  "checks if a trip has all necessry attributes"
  (first (b/validate trip validations)))
