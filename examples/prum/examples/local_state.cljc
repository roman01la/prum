(ns prum.examples.local-state
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;; Local component state


(prum/defcs local-state < (prum/local 0)
  [state title]
  (let [*count (:prum/local state)]
    [:div
     {:style {"-webkit-user-select" "none"
              "cursor" "pointer"}
      :on-click (fn [_] (swap! *count inc))}
     title ": " @*count]))


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (local-state "Clicks count") mount-el)))
