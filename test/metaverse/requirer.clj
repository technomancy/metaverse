(ns metaverse.requirer
  (:require [meta.verse :as v]))

(v/require '[sample.a :as original
             :rev "b3c57239c7035149a511ff1baa067c52a948e254"])

(v/require '[sample.a :as alternate
             :rev "fd869f8b8cee8667e5a5b51487f87aa5d608f8d4"])

(defn get-abcs []
  [(original/abc) (alternate/abc)])