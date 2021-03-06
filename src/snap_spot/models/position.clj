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
(defn trip->last-updated-key [trip] (str (:id trip) "-last-updated"))

(def validations {:lat [v/required v/number] 
                  :lon [v/required v/number]})
(defn validate [position]
  (let [errs (first (b/validate position validations))]
    (if (nil? errs) [] (flatten (vals errs)))))

(defn last-updated [trip] 
  (or 
    (redis/wcar* (car/get (trip->last-updated-key trip)))
    (t/epoch)))

(defn seconds-since-update [trip]
  "push new position to subscribed channels and persist to redis if last update was older then a second ago"
  (t/in-seconds (t/interval (last-updated trip) (t/now))))

(defn schedule-expiry [trip]
  (redis/wcar* (car/expire (car/ttl (redis-key trip)) (redis-key trip))))

(defn fetch-all [trip]
  (redis/wcar* (car/lrange (redis-key trip) 0 -1)))

(defn add [t position]
  "add a position to a trip and schedule expiry to match trip"
  (if (> (seconds-since-update t) 1)
    (let [[errs trip] ((resolve 'snap-spot.models.trip/fetch) t)
          errs (concat errs (validate position))]
      (if-not (empty? errs)
        errs
        (let [position-count (redis/wcar*
                (car/setex 10 (trip->last-updated-key trip) (t/now))
                (car/lpush (redis-key trip) position))]
          (if (= 1 position-count) (schedule-expiry trip))
          [])))
      ["trip updates can not be pushed faster then once a second."]))

