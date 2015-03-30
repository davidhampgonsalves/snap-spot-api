(ns snap-spot.position-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [ring.mock.request :as mock]
            [snap-spot.helper :as helper]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [clojure.data.json :as json]))

(defn create-trip []
  (def trip-resp (trip/create {:params {:id (helper/generate-uuid) :duration "30"}}))
  (json/read-str trip-resp))

(deftest test-add
  (testing "add position"
    (def trip (create-trip))
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22" 
                                       :lon "24" 
                                       :instant "12345"}}))
    (test (contains? (json/read-str resp) :success))))

