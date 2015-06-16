(ns snap-spot.helpers.trip-helper
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [snap-spot.core :refer :all]
            [snap-spot.helper :as helper]
            [snap-spot.controllers 
             (trip :as trip)]
            [clojure.data.json :as json]))

(defn create [& {:keys [duration] :or {duration 15}}]
  (let [trip {:id (helper/generate-uuid) :duration duration :start (t/now)}
        secret (json/read-str (trip/create {:params trip}) :key-fn keyword)
        trip (assoc trip :secret (:secret secret))]
    trip))
