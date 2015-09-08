# clj-jpa

Tasty JPA for Clojure

## Get started

Add clj-jpa as a dependency to your project:

```clojure
[clj-jpa "0.1.0-SNAPSHOT"]
```

And add also a JPA implementation.

```clojure
[org.eclipse.persistence/org.eclipse.persistence.jpa "2.5.2"]
[org.jboss.weld.se/weld-se "2.2.14.Final"]
```


## Examples

### Select

```clojure
(require '[clj-jpa.entity-manager :as em])
(require '[clj-jpa.core :refer :all])

(with-entity-manager
  (let [users (em/search User
                         :where (and (= :family-name "Kawashima")
                                     (= :last-name   "Yoshitaka")))]
    (println users)))

;;; -> [{:last-name Yoshitaka, :id 1, :memberships #object[clojure.lang.Delay 0x289fdb08 {:status :pending, :val nil}],
;;;      :groups #object[clojure.lang.Delay 0x30814f43 {:status :pending, :val nil}], :family-name Kawashima}]
```

Associations will be returned as a Delay object.

```clojure
(get-in users [0 :groups 0 :name])

;; -> group1
```

### Merge

```clojure
(require '[clj-jpa.entity-manager :as em])
(require '[clj-jpa.core :refer :all])

(with-entity-manager
  (with-transaction
    (let [user (em/merge User {:family-name "Kawashima"
                               :last-name "Yoshitaka"})]
      (em/merge Group {:name "group1"
                       :users [user]}))))
```


