(ns rum.examples.self-reference
  (:require
    [rum.core :as rum]))


;; Self-referencing component


(rum/defc self-reference < rum/static
  ([form] (self-reference form 0))
  ([form depth]
   (if (sequential? form)
     [:.branch {:style {:margin-left (* 10 depth)}} (map #(self-reference % (inc depth)) form)]
     [:.leaf {:style {:margin-left (* 10 depth)}} (str form)])))


#?(:cljs
   (defn mount! [mount-el]
     (rum/mount (self-reference [:a [:b [:c :d [:e] :g]]]) mount-el)))
