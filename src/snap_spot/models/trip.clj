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
  (.startsWith k key-prefix))

(defn fetch-by-key [k] 
  "lookup the trip based on its key"
  (redis/wcar* (car/get k)))

(defn fetch [id] 
  "lookup the trip based on id"
  (fetch-by-key (id->key id)))

(defn create [trip]
  "write trip to redis"
  (redis/wcar* (car/set (redis-key trip) trip)))

(defn update [trip]
  "update a trip based on its id"
  (redis/wcar* (car/set (redis-key trip) trip)))

(defn active? [trip] 
  "checks if a trip is still active"
  (t/after? (t/plus (:start trip) (t/minutes (:duration trip))) (t/now)))

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
      (helper/throw-exception "invalid secret."))
    (comment "start might not be set on updates")
    (if-not (or (nil? (:start trip)) (active? trip))
      (helper/throw-exception "trip does not exist."))))

(comment "logically this should be in position but it causes a cyclical dependancy")
(defn redis-position-key [trip] (str (:id trip) "-positions"))
(defn redis-last-updated-key [trip] (str (:id trip) "-last-updated"))

(defn delete [trip]
  "delete a trip and all assoicated data"
  (redis/wcar* 
    (car/del (redis-key trip))
    (car/del (redis-position-key trip))
    (car/del (redis-last-updated-key trip))))

(defn delete-if-expired [k]
  "delete trip if it has expired (start time + duration) > now "
  (let [trip (fetch-by-key k)]
    (if-not (active? trip)
      (delete trip))))

(def validations {:id [v/required] 
                  :secret [v/required]
                  :duration [v/required v/number v/positive]
                  :start [v/required]})

(comment "TODO: validate trip duration and start time(custom validator for datetime which should be used on position/instant)")
(defn validate [trip]
  "checks if a trip has all necessry attributes"
  (first (b/validate trip validations)))
