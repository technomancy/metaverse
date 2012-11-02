(ns metaverse.core-test
  (:require [metaverse.loader :as l]
            [metaverse.requirer :as r]
            [metaverse.nser :as n]
            [clojure.test :refer :all]))

(deftest loader-test
  (is (= [:original :alternate] (l/get-abcs))))

(deftest requirer-test
  (is (= [:original :alternate] (r/get-abcs))))

(deftest ns-test
  (is (= [:original :alternate] (n/get-abcs))))