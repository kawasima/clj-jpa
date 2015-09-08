(ns clj-jpa.core
  (:require [clj-jpa.entity-manager :as em]
            [clj-jpa.query :as query]))

(defmacro with-entity-manager [& body]
  `(binding [~'clj-jpa.entity-manager/*em* (em/create-entity-manager)]
     ~@body))

(defmacro with-transaction [& body]
  `(let [tx# (.getTransaction ~'clj-jpa.entity-manager/*em*)]
     (.begin tx#)
     (try
       (let [ret# (do ~@body)]
         (.commit tx#)
         ret#) 
       (catch Throwable t#
         (when (.isActive tx#)
           (.rollback tx#))
         (throw t#)))))

