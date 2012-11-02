(ns metaverse.loader
  (:require [meta.verse :as v]))

(remove-ns 'sample.a.b3c57239c7035149a511ff1baa067c52a948e254)
(remove-ns 'sample.a.fd869f8b8cee8667e5a5b51487f87aa5d608f8d4)

(v/load "sample/a.clj")

(defn get-abcs []
  [(sample.a.b3c57239c7035149a511ff1baa067c52a948e254/abc)
   (sample.a.fd869f8b8cee8667e5a5b51487f87aa5d608f8d4/abc)])