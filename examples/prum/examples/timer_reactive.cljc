(ns prum.examples.timer-reactive
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reactive components (reagent-style)


;; regular static top-down component with immutable args
(prum/defc colored-clock < prum/static [time color]
  [:span {:style {:color color}} (core/format-time time)])


(prum/defc timer-reactive < prum/reactive []
  [:div {} "Reactive: "
    ;; Subscribing to atom changes with prum/react
    ;; Then pass _dereferenced values_ to static component
    (colored-clock (prum/react core/*clock) (prum/react core/*color))])


;; After initial mount, all changes will be re-rendered automatically
#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (timer-reactive) mount-el)))
