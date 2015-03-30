(ns snap-spot.trip-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [ring.mock.request :as mock]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [clojure.data.json :as json]))

(deftest test-create
  (testing "trip/create validation."
    (def trip (json/read-str (trip/create {:params {:id 5 :duration 30}})))
    (contains? trip :secret)))

(deftest test-from-params
  (testing "trip-from-params"
    (= 1 (:duration (trip/trip-from-params {:id "1" :duration "1"})))))
