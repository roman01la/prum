(ns prum.examples.controls
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Control panel


;; generic “atom editor” component
(prum/defc input < prum/reactive [ref]
  [:input {:type      "text"
           :value     (prum/react ref)
           :style     {:width 100}
           :on-change #(reset! ref (.. % -target -value))}])


;; Raw top-level component, everything interesting is happening inside
(prum/defc controls []
  [:dl {}
   [:dt {} "Color: "]
   [:dd {} (input core/*color)]
   ;; Binding another component to the same atom will keep 2 input boxes in sync
   [:dt {} "Clone: "]
   [:dd {} (input core/*color)]
   [:dt {} "Color: "]
   [:dd {} (core/watches-count core/*color) " watches"]

   [:dt {} "Tick: "]
   [:dd {} (input core/*speed) " ms"]
   [:dt {} "Time:"]
   [:dd {} (core/watches-count core/*clock) " watches"]])



#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (controls) mount-el)))

