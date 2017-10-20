(ns prum.examples.self-reference
  (:require
    [prum.core :as prum]))


;; Self-referencing component


(prum/defc self-reference < prum/static
  ([form] (self-reference form 0))
  ([form depth]
   (if (sequential? form)
     [:.branch {:style {:margin-left (* 10 depth)}} (map #(self-reference % (inc depth)) form)]
     [:.leaf {:style {:margin-left (* 10 depth)}} (str form)])))


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (self-reference [:a [:b [:c :d [:e] :g]]]) mount-el)))
