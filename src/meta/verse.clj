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
    `(clojure.core/ns ~transformed-ns ~@(drop 2 ns-form))))

(defn read-all [resource]
  (let [reader (LineNumberingPushbackReader. (io/reader resource))
        forms (repeatedly #(read reader false ::eof))]
    ;; TODO: add :file metadata
    (take-while #(not= ::eof %) forms)))

(defn transformed-sources [lib]
  (let [matches (enumeration-seq (.findResources ccl lib))]
    (for [match matches
          :let [checksum (sha1 (.getBytes (slurp match)))
                ;; TODO: the body must be *read* in the ns of the
                ;; first element
                [ns-form & body] (binding [*ns* (the-ns 'user)]
                                   (read-all match))]]
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

(defn require [[lib & {:as opts} :as orig-args]]
  (if-let [rev (:rev opts)]
    (let [qualified-lib (qualify lib rev)]
      ;; TODO: check *loaded-libs*, don't reload unless necessary
      (load (str (subs (#'clojure.core/root-resource lib) 1) ".clj"))
      (when-let [as (:as opts)]
        (ns-unalias *ns* as)
        (alias as qualified-lib))
      ;; TODO: we might need two concepts of "loaded" for this
      (dosync
       (commute @#'clojure.core/*loaded-libs* conj qualified-lib))
      qualified-lib)
    (clojure.core/require orig-args)))

(defn- ns-clause [[clause-type & args]]
  (if (= :require clause-type)
    
    (cons clause-type args)))

(defn- require? [[clause-type & _]]
  (= clause-type :require))

(defn- versioned-require [[_ & subclauses]]
  (for [subclause subclauses]
    `(require '~subclause)))

(defmacro ns- [name & clauses]
  (let [[requires others] ((juxt filter remove) require? clauses)]
    `(do (clojure.core/ns ~name ~@others)
         ~@(mapcat versioned-require requires))))
