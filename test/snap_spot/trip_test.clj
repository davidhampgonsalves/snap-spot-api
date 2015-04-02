(ns snap-spot.trip-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [ring.mock.request :as mock]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [clojure.data.json :as json]))

(deftest test-params->trip
  (testing "params->trip"
    (is (= 1M (:duration (trip/params->trip {:id "1" :duration "1"}))))))

(deftest test-create
  (testing "trip/create test"
    (def trip (json/read-str (trip/create {:params {:id 5 :duration 30}})))
    (is (contains? trip "secret"))))

(deftest test-create-bad-duration
  (testing "trip/create error test"
    (def error (json/read-str (trip/create {:params {:id 5 :duration -30}})))
    (is (contains? error "error"))))

(deftest test-create-no-duration
  (testing "trip/create no duration error test"
    (def error (json/read-str (trip/create {:params {:id 5}})))
    (is (contains? error "error"))))

(deftest test-update
  (testing "trip/update duration error test"
    (def error (json/read-str (trip/create {:params {:id 5}})))
    (is (contains? error "error"))))
