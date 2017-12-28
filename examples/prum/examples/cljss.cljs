(ns prum.examples.cljss
  (:require [prum.core :as prum]
            [clojure.string :as cstr]
            [cljss.core]))

(def c [:0 :1 :2 :3 :4 :5 :6 :7 :8 :9 :A :B :C :D :E :F])

(defn rand-color []
  (str "#" (cstr/join (map name (repeatedly 6 #(rand-nth c))))))


(prum/defcs button <
  (prum/local "#fff" ::color)
  [{color ::color}]
  [:button {:on-click #(reset! color (rand-color))
            :css      {:background-color @color}}
   "Press to change color"])

(defn mount! [mount-el]
  (prum/mount (button) mount-el))
