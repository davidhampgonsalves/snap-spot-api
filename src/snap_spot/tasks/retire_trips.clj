(ns snap-spot.tasks.retire-trips
  (:gen-class)
  (:require
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [clj-time.core :as t]
    [snap-spot.models 
     (trip :as trip)
     (position :as position)]))

(timbre/refer-timbre)

(defn delete [trip]
  "delete a trip and all assoicated data"
  (redis/wcar* 
    (car/del 
      (trip/redis-key trip) 
      (position/redis-key trip) 
      (position/trip->last-updated-key trip))))

(defn delete-if-aged [k]
  "delete trip if it is really old" 
  (let [trip (redis/wcar* (car/get k))]
    (if (> (t/in-days (t/interval (:start trip) (t/now))) 0)
      (delete trip))))

(defn key? [k] 
  "check if string is a redis trip key"
  (.startsWith k trip/key-prefix))

(defn delete-expired-trips []
  (loop [index "0"]
    (let [[index all-keys] (redis/wcar* (car/scan index))]
      (doseq [trip-key all-keys]
        (if (key? trip-key)
          (delete-if-aged trip-key)))
      (if-not (= index "0")
        (recur index)))))

(defn -main [& args]
  (delete-expired-trips))
