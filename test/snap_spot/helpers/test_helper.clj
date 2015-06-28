(ns snap-spot.helpers.test-helper
  (:require [snap-spot.helper :as helper]
            [snap-spot.redis :as redis]
            [snap-spot.models.trip :as trip]
            [clj-time.core :as t]
            [taoensso.carmine :as car :refer (wcar)]))

(defn stateless-redis-fixture [f]
  (redis/wcar* (car/flushdb))
  (f))

(defn create-trip [& {:keys [remaining-minutes] :or {remaining-minutes 15}}]
  (let [trip {:id (helper/generate-uuid) :secret (helper/generate-uuid) :remaining-minutes remaining-minutes :start (t/now)}
        errs (trip/create trip)]
    (if (empty? errs)
      trip
      (println "ERROR creating trip " errs))))
