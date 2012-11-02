(remove-ns 'sample.a.b3c57239c7035149a511ff1baa067c52a948e254)
(remove-ns 'sample.a.fd869f8b8cee8667e5a5b51487f87aa5d608f8d4)

(meta.verse/ns-
 metaverse.nser
 (:require [clojure.set :as s]
           [sample.a :as original
            :rev "b3c57239c7035149a511ff1baa067c52a948e254"]
           [sample.a :as alternate
            :rev "fd869f8b8cee8667e5a5b51487f87aa5d608f8d4"]))

(defn get-abcs []
  [(original/abc) (alternate/abc)])