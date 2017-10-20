(ns prum.examples.timer-static
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Static component (quiescent-style)


(prum/defc timer-static < prum/static [label ts]
  [:div {} label ": "
    [:span {:style {:color @core/*color}} (core/format-time ts)]])


#?(:cljs
   (defn mount! [mount-el]
     (let [root (atom nil)]
       (reset! root (prum/mount (timer-static "Static" @core/*clock) mount-el))
    ;; Setting up watch manually,
    ;; force top-down re-render via mount
       (add-watch core/*clock :timer-static
                  (fn [_ _ _ new-val]
                    (reset! root (prum/mount (timer-static "Static" new-val) mount-el @root)))))))
