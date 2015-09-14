(ns clj-jpa.query
  (:require [clojure.walk :as walk])
  (:import [javax.persistence Query]
           [javax.persistence.criteria Predicate]
           [cljjpa JPAEntityMap]))

(defn- decamelize [s]
  (let [sarr (.split s "-")]
    (apply str
           (first sarr)
           (map clojure.string/capitalize (rest sarr)))))

(defn parameters [^Query query params]
  (cond
    (map? params)
    (doseq [[k v] params]
      (.setParameter query (decamelize (name k)) v))
    
    (sequential? params)
    (loop [i 0]
      (when (< i (count params))
        (.setParameter query (inc i) (nth params i))
        (recur (inc i))))))

(defn result-list [^Query query em]
  (->> (.getResultList query)
       (map (fn [rec]
              (if (.isArray (class rec))
                (vec rec)
                (JPAEntityMap. em rec))))
       vec))

(defn single-result [^Query query em]
  (.getSingleResult query))

(defn execute-update [^Query query]
  (.executeUpdate query))

(defn offset [^Query query start-position]
  (.setFirstResult query start-position))

(defn limit [^Query query max-result]
  (.setMaxResults query max-result))


(def predicates
  {'like 'clj-jpa.query/pred-like
   'and 'clj-jpa.query/pred-and
   'or  'clj-jpa.query/pred-or
   'not 'clj-jpa.query/pred-not
   'in  'clj-jpa.query/pred-in
   'not-in 'clj-jpa.query/pred-not-in
   'between 'clj-jpa.query/pred-between
   '>   'clj-jpa.query/pred->
   '<   'clj-jpa.query/pred-<
   '>=   'clj-jpa.query/pred->=
   '<=   'clj-jpa.query/pred-<=
   'not= 'clj-jpa.query/pred-not=
   '=   'clj-jpa.query/pred-=
   })

(defn parse-where [builder root where-forms]
  (walk/postwalk (fn [x] (if (contains? predicates x)
                           (list 'partial (predicates x) builder root)
                           x))  where-forms))



(defn expr [root v]
  (if (keyword? v)
    (.get root (decamelize (name v)))
    v))

(defn pred-and [builder root & args]
  (.and builder (into-array Predicate args)))

(defn pred-or [builder root & args]
  (.or builder (into-array Predicate args)))

(defn pred-like [builder root x y]
  (.like builder (expr root x) (expr root y)))

(defn pred-not [builder root x]
  (.not builder x))

(defn pred-in [builder root x values]
  (.in (expr root x) values))

(defn pred-between [builder root v x y]
  (.between builder
            (expr root v)
            (expr root x)
            (expr root y)))

(defn pred-= [builder root x y]
  (.equal builder (expr root x) (expr root y)))

(defn pred-not= [builder root x y]
  (.notEqual builder (expr root x) (expr root y)))

(defn pred-> [builder root x y]
  (.gt builder (expr root x) (expr root y)))

(defn pred-< [builder root x y]
  (.lt builder (expr root x) (expr root y)))

(defn pred->= [builder root x y]
  (.ge builder (expr root x) (expr root y)))

(defn pred-<= [builder root x y]
  (.le builder (expr root x) (expr root y)))

(defn parse-order [builder root order-list]
  (->> order-list
       (map #(cond
               (keyword? %) (list '.desc builder (expr root %))
               (list? %) (condp = (first %)
                           'asc  (list '.asc builder (list 'clj-jpa.query/expr root (second %)))
                           'desc (list '.desc builder (list 'clj-jpa.query/expr root (second %))))
               :else (throw (Exception. "hhohoho"))))
       vec))

