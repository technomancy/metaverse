(ns meta.verse
  (:refer-clojure :exclude [load require ns])
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp])
  (:import (java.security MessageDigest)
           (clojure.lang LineNumberingPushbackReader)))

(defn sha1 [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA1") bytes)]
    (format "%x" (BigInteger. 1 digest))))

(defonce ccl (.getContextClassLoader (Thread/currentThread)))

(defn qualify [lib checksum]
  (symbol (str lib "." checksum)))

(defn transform [ns-form checksum]
  (let [transformed-ns (qualify (second ns-form) checksum)]
    `(ns ~transformed-ns ~@(drop 2 ns-form))))

(defn read-all [resource]
  (let [reader (LineNumberingPushbackReader. (io/reader resource))
        forms (repeatedly #(read reader false ::eof))]
    ;; TODO: add :file metadata
    (take-while #(not= ::eof %) forms)))

(defn transformed-sources [lib]
  (let [matches (enumeration-seq (.findResources ccl lib))]
    (for [match matches
            :let [checksum (sha1 (.getBytes (slurp match)))
                  [ns-form & body] (read-all match) ]]
      (cons (transform ns-form checksum) body))))

(defn load [lib]
  ;; (clojure.core/load lib)
  (doseq [transformed (transformed-sources lib)]
    (binding [*ns* (find-ns 'user)]
      (doseq [form transformed]
        (eval form)))))

;; for debugging
(defn pprint [lib]
  (doseq [transformed (transformed-sources lib)]
    (pp/pprint transformed)))

(defn require [[lib & {:as opts}]]
  (let [qualified-lib (qualify lib (:rev opts))]
    (load (str (#'clojure.core/root-resource lib) ".clj"))
    (when-let [as (:as opts)]
      (alias as qualified-lib))
    (dosync
     (commute @#'clojure.core/*loaded-libs* conj qualified-lib))
    qualified-lib))

;; (defn- ns-clause [[clause-type & args]]
;;   (if (= :require clause-type)
    
;;     (cons clause-type args)))

;; (defmacro ns [name & clauses]
;;   `(do (ns ~name
;;          ~@(map ns-clause clauses))
;;        ~@(versioned-requires )))
