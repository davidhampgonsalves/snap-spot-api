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
       (assoc trip :secret (:secret (json/read-str trip-resp :key-fn keyword)))))

(deftest test-params->position
  (testing "create position from params"
    (def position (position/params->position {:lat "1.1" :lon "2" :order "1"}))
    (is (and 
          (= (:lat position) 1.1M)
          (= (:lon position) 2M)
          (= (:order position) 1M)))))

(deftest test-add
  (testing "add position"
    (def trip (create-trip))
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22" 
                                       :lon "24" 
                                       :order "1"}}))
    (test (is (contains? (json/read-str resp) "success")))))

(deftest test-add-validation
  (testing "add position validation"
    (def trip (create-trip))
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
      (let [resp (position/add {:params {:id (:id trip) 
                               :secret (:secret trip) 
                               :lat "22" 
                               :lon "24" 
                               :order "1"}})]
        (is (contains? (json/read-str resp) "success"))))))
