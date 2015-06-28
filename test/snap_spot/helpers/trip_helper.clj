(ns snap-spot.helpers.trip-helper
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [snap-spot.core :refer :all]
            [snap-spot.helper :as helper]
            [snap-spot.models 
             (trip :as trip)]
            [clojure.data.json :as json]))

(comment "Move te test helper")
(defn create [& {:keys [remaining-minutes] :or {remaining-minutes 15}}]
  (let [trip {:id (helper/generate-uuid) :secret (helper/generate-uuid) :remaining-minutes remaining-minutes :start (t/now)}
        errs (trip/create trip)]
    (if (empty? errs)
      trip
      (println "ERROR creating trip " errs))))

