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
  (let[trip {:id (helper/generate-uuid) :duration "30"}
       trip-resp (trip/create {:params trip})]
       (assoc trip :secret (get (json/read-str trip-resp) "secret"))))

(deftest test-params->position
  (testing "create position from params"
    (def position (position/params->position {:lat "1.1" :lon "2" :instant "1213123"}))
    (is (and 
          (= (:lat position) 1.1M)
          (= (:lon position) 2M)
          (= (:instant position) 1213123M)))))

(deftest test-add
  (testing "add position"
    (def trip (create-trip))
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22" 
                                       :lon "24" 
                                       :instant "12345"}}))
    (test (is (contains? (json/read-str resp) "success")))))


(deftest test-add-validation
  (testing "add position validation"
    (def trip (create-trip))
    (println trip)
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22a" 
                                       :lon "24" 
                                       :instant "12345"}}))
    (test (is (contains? (json/read-str resp) "error")))))
