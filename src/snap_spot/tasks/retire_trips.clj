(ns snap-spot.tasks.retire-trips
  (:gen-class)
  (:require
    [snap-spot.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [snap-spot.models 
     (trip :as trip)]))

(timbre/refer-timbre)

(defn delete-expired-trips []
  (loop [index "0"]
    (let [[index all-keys] (redis/wcar* (car/scan index))]
      (doseq [trip-key all-keys]
        (if (trip/key? trip-key)
          (trip/delete-if-expired trip-key)))
      (if-not (= index "0")
        (recur index)))))

(defn -main [& args]
  (delete-expired-trips))
