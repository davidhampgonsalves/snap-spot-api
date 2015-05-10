(ns snap-spot.task-test
  (:require [clojure.test :refer :all]
            [snap-spot.core :refer :all]
            [snap-spot.helpers.test-helper :as test-helper]
            [ring.mock.request :as mock]
            [snap-spot.controllers.trip :as trip-controller]
            [snap-spot.models
             (position :as position)
             (trip :as trip)]
            [snap-spot.tasks.retire-trips :as task]
            [clojure.data.json :as json]))


(use-fixtures :each test-helper/stateless-redis-fixture)

(deftest retire-trips
  (testing "retire-trips"
    (trip-controller/create {:params {:id 5 :duration 15}})
    (trip-controller/create {:params {:id 4 :duration 0}})
    (task/delete-expired-trips) 

    (testing "was active trip retained"
      (is (not (nil? (trip/fetch 5)))))

    (testing "was expired trip deleted"
      (is (nil? (trip/fetch 4))))))
