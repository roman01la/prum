(ns prum.test.server
  (:require
    [prum.core :as prum]
    [clojure.test :refer [deftest is are testing]]))


(prum/defcs comp-mixins < (prum/local 7)
            {:will-mount (fn [s] (assoc s ::key 1))}
            [state]
            [:div
             [:.local @(:prum/local state)]
             [:.key (::key state)]])


(deftest test-lifecycle
  (is (= (comp-mixins)
         [:div
          [:.local 7]
          [:.key 1]])))


(prum/defc comp-arglists
           ([a])
           ([a b])
           ([a b c]))


(prum/defcc comp-arglists-1
            ([comp a])
            ([comp a b])
            ([comp a b c]))


(deftest test-arglists
  (is (= (:arglists (meta #'comp-mixins))
         '([])))
  (is (= (:arglists (meta #'comp-arglists))
         '([a] [a b] [a b c])))
  (is (= (:arglists (meta #'comp-arglists-1))
         '([a] [a b] [a b c]))))
