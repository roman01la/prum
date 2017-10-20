(ns prum.examples.keys
  (:refer-clojure :exclude [keys])
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


(prum/defc keyed < {:key-fn (fn [label number]
                             (str label "-" number))}
  [label number]
  [:div {} (str label "-" number)])


(prum/defc keys []
  [:div {}
   '((keyed "a" 1)
     (keyed "a" 2)
     (keyed "b" 1)
     (prum/with-key (keyed "a" 1) "x"))])


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (keys) mount-el)))
