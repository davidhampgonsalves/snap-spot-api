(ns snap-spot.redis
  (:require 
    [taoensso.carmine :as car :refer (wcar)]))

(def redis1-conn {:pool {} :spec {:host "localhost" :port 6379}})

(defmacro wcar* [& body] `(car/wcar redis1-conn ~@body))
