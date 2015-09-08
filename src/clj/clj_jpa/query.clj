(ns clj-jpa.query
  (:require [clojure.walk :as walk])
  (:import [javax.persistence Query]
           [javax.persistence.criteria Predicate]
           [cljjpa JPAEntityMap]))

(defn parameters [^Query query params]
  (cond
    (map? params) (doseq [[k v] params]
                    (.seqParameter k v))
    ;; TODO sequential
    ))

(defn result-list [^Query query em]
  (->> (.getResultList query)
       (map #(JPAEntityMap. em %))
       vec))

(defn single-result [^Query query em]
  (.getSingleResult query))

(defn offset [^Query query start-position]
  (.setFirstResult query start-position))

(defn limit [^Query query max-result]
  (.setMaxResults query max-result))


(def predicates
  {'and 'clj-jpa.query/pred-and
   'or  'clj-jpa.query/pred-or
   '=   'clj-jpa.query/pred-=
   '>   'clj-jpa.query/pred->
   '<   'clj-jpa.query/pred-<
   })

(defn parse-where [builder root where-forms]
  (walk/postwalk (fn [x] (if (contains? predicates x)
                           (list 'partial (predicates x) builder root)
                           x))  where-forms))

(defn decamelize [s]
  (let [sarr (.split s "-")]
    (apply str
           (first sarr)
           (map clojure.string/capitalize (rest sarr)))))

(defn expr [root v]
  (if (keyword? v)
    (.get root (decamelize (name v)))
    v))

(defn pred-and [builder root & args]
  (.and builder (into-array Predicate args)))

(defn pred-or [builder root & args]
  (.or builder (into-array Predicate args)))

(defn pred-= [builder root x y]
  (.equal builder (expr root x) (expr root y)))

(defn pred-> [builder root x y]
  (.gt builder (expr root x) (expr root y)))

(defn pred-< [builder root x y]
  (.lt builder (expr root x) (expr root y)))


