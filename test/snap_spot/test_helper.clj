(ns snap-spot.test-helper
  (:require [snap-spot.redis :as redis]
            [taoensso.carmine :as car :refer (wcar)]))

(defn stateless-redis-fixture [f]
  (redis/wcar* (car/flushdb))
  (f))
