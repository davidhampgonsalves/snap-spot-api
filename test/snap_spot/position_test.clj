(ns snap-spot.position-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [ring.mock.request :as mock]
            [snap-spot.helper :as helper]
            [snap-spot.helpers.test-helper :as test-helper]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [snap-spot.models.position :as position-model]
            [clojure.data.json :as json]))

(deftest test-params->position
  (testing "create position from params"
    (def position (position/params->position {:lat "1.1" :lon "2"}))
    (is (and 
          (= (:lat position) 1.1M)
          (= (:lon position) 2M)))))

(deftest test-add
  (testing "add position"
    (def trip (test-helper/create-trip))
    (def resp (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22" 
                                       :lon "24"}}))
    (test (is (contains? (test-helper/response-json resp) "success"))))
  
  (testing "add positions"
    (def trip (test-helper/create-trip))
    (def resp1 (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22.1231243234234" 
                                       :lon "24.1239348398459348"}}))
    (test (is (contains? (test-helper/response-json resp1) "success"))))
    (Thread/sleep 2000)
    (def resp2 (position/add {:params {:id (:id trip) 
                                       :secret (:secret trip) 
                                       :lat "22.1231231231231" 
                                       :lon "24.12313123123123"}}))
    (test (is (contains? (test-helper/response-json resp2) "success")))

    (def positions (position-model/fetch-all trip))
    (test (is (= (count positions) 2))))

(deftest test-add-validation
  (testing "add position validation"
    (def trip (test-helper/create-trip))
    (testing "- lat is a number"
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret (:secret trip) 
                                         :lat "22a" 
                                         :lon "24"}})]
        (is (contains? (test-helper/response-json resp) "errors"))))
    
    (testing "- secret validation"
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret "abc" 
                                         :lat "22" 
                                         :lon "24"}})]
        (is (contains? (test-helper/response-json resp) "errors"))))

    (testing "- success"
      (let [resp (position/add {:params {:id (:id trip) 
                                         :secret (:secret trip) 
                                         :lat "22" 
                                         :lon "24"}})]
        (is (contains? (test-helper/response-json resp) "success"))))))
