(ns snap-spot.position-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [ring.mock.request :as mock]
            [snap-spot.helper :as helper]
            [snap-spot.helpers.trip-helper :as trip-helper]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [snap-spot.models.position :as position-model]
            [clojure.data.json :as json]))

(deftest test-params->position
  (testing "create position from params"
    (def position (position/params->position {:lat "1.1" :lon "2" :order "1"}))
    (is (and 
          (= (:lat position) 1.1M)
          (= (:lon position) 2M)
          (= (:order position) 1M)))))

(deftest test-add
  (testing "add position"
    (def trip (trip-helper/create))
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22" 
                                       :lon "24" 
                                       :order "1"}}))
    (test (is (contains? (json/read-str resp) "success"))))
  
  (testing "add positions"
    (def trip (trip-helper/create))
    (def resp1 (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22.1231243234234" 
                                       :lon "24.1239348398459348" 
                                       :order "1"}}))
    (test (is (contains? (json/read-str resp1) "success"))))
    (Thread/sleep 2000)
    (def resp2 (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22.1231231231231" 
                                       :lon "24.12313123123123" 
                                       :order "2"}}))
    (test (is (contains? (json/read-str resp2) "success")))

    (def positions (position-model/fetch-all trip))
    (test (is (= (count positions) 2))))

(deftest test-add-validation
  (testing "add position validation"
    (def trip (trip-helper/create))
    (testing "- lat is a number"
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret (:secret trip) 
                                         :lat "22a" 
                                         :lon "24" 
                                         :order "1"}})]
        (is (contains? (json/read-str resp) "error"))))
    
    (testing "- secret validation"
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret "abc" 
                                         :lat "22" 
                                         :lon "24" 
                                         :order "1"}})]
        (is (contains? (json/read-str resp) "error"))))

    (testing "- success"
      (println trip)
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret (:secret trip) 
                                         :lat "22" 
                                         :lon "24" 
                                         :order "1"}})]
        (is (contains? (json/read-str resp) "success"))))))
