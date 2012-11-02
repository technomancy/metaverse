(ns metaverse.core-test
  (:require [metaverse.loader :as l]
            [metaverse.requirer :as r]
            [clojure.test :refer :all]))

(deftest loader-test
  (is (= [:original :alternate] (l/get-abcs))))

(deftest requirer-test
  (is (= [:original :alternate] (r/get-abcs))))