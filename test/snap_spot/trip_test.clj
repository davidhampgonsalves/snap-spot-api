(ns snap-spot.trip-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [snap-spot.test-helper :as test-helper]
            [ring.mock.request :as mock]
            [snap-spot.controllers 
             (position :as position)
             (trip :as trip)]
            [clojure.data.json :as json]))

(use-fixtures :each test-helper/stateless-redis-fixture)

(deftest test-params->trip
  (testing "params->trip"
    (is (= 1M (:duration (trip/params->trip {:id "1" :duration "1"}))))))

(deftest test-create
  (testing "trip/create valid"
    (def trip (json/read-str (trip/create {:params {:id 2 :duration 30}})))
    (is (contains? trip "secret"))))

(deftest test-create-bad-duration
  (testing "trip/create bad duration"
    (def error (json/read-str (trip/create {:params {:id 3 :duration -30}})))
    (is (contains? error "error"))))

(deftest test-create-no-duration
  (testing "trip/create no duration error test"
    (def error (json/read-str (trip/create {:params {:id 4}})))
    (is (contains? error "error"))))

(deftest test-update
  (testing "trip/update invalid secret test"
    (def trip (json/read-str (trip/create {:params {:id 5 :duration 15}}) :key-fn keyword))
    (let [error (json/read-str (trip/update {:params {:id 5 :secret "abc" :duration -1}}))]
      (is (contains? error "errors")))))

(deftest test-update
  (testing "trip/update invalid duration test"
    (def trip (json/read-str (trip/create {:params {:id 5 :duration 15}}) :key-fn keyword))

    (testing "- success"
      (let [res (json/read-str (trip/update {:params {:id 5 :secret (:secret trip) :duration 120}}))]
        (is (contains? res "success"))))

    (testing "- too long"
      (let [error (json/read-str (trip/update {:params {:id 5 :secret (:secret trip) :duration 130}}))]
        (is (contains? error "error"))))

    (testing "- negative"
      (let [error (json/read-str (trip/update {:params {:id 5 :secret (:secret trip) :duration -1}}))]
        (is (contains? error "error"))))))
