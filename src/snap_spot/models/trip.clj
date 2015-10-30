(ns snap-spot.models.trip
  (:require 
    [snap-spot.redis :as redis]
    [snap-spot.helper :as helper]
    [taoensso.carmine :as car :refer (wcar)]
    [taoensso.timbre :as timbre]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [clojure.data.json :as json]
    [snap-spot.models.position :as position :only delete]))

(timbre/refer-timbre)

(def key-prefix "trip:")
(defn id->key [id] (str key-prefix id))
(defn redis-key [trip] (id->key (:id trip)))

(def validations {:id [v/required] 
                  :secret [v/required]
                  :remaining-minutes [v/required v/number [v/in-range [0 120]]]
                  :start [v/required]})
(defn validate [trip]
  "checks if a trip has all necessry attributes"
  (let [errs (first (b/validate trip validations))]
    (if (nil? errs) [] (flatten (vals errs)))))

(defn fetch [params] 
  "lookup the trip based on id and secret"
  (let [trip (redis/wcar* (car/get (id->key (:id params))))]
    (if-not (= (:secret trip) (:secret params))
      [["trip not found."], nil]
      [[], trip])))

(comment "TODO: need to expire the positions as well")
(defn create [trip]
  "write trip to redis"
  (let [errs (validate trip)]
    (if-not (empty? errs)
      errs
      (if (= 0 (redis/wcar* (car/setnx (redis-key trip) (dissoc trip :remaining-minutes))))
        ["trip already exists."]
        (do
          (redis/wcar* (car/expire (redis-key trip) (* (:remaining-minutes trip) 60)))
          nil)))))

(comment "TODO: need to expire the positions as well")
(defn update [params]
  "update a trip based on its id/secret and return errors, allow remining time to be extended indefinately \\
  , if trip is really old(more then a day) it will be destroyed by cleanup task anyway"
  (let [[errs trip] (fetch params)]
    (if-not (empty? errs)
      errs
      (let [remaining-minutes (helper/str->int (:remaining-minutes params))
            updated-trip (assoc trip :remaining-minutes remaining-minutes)
            errs (concat errs (validate updated-trip))]
        (if-not (empty? errs)
          errs
          (do 
            (redis/wcar* (car/expire (redis-key trip) (* remaining-minutes 60)))
            nil))))))

