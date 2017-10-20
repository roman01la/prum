(ns prum.examples.context
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))

;; Components with context that all descendants have access to implicitly.

;; This is useful when you are using child components you cannot modify.
;; For example, a JS library that gives you components which rely on a context
;; value being set by an ancestor component.


(prum/defcc prum-context-comp [comp]
  [:span
   {:style {:color (prum/context comp :color)}}
   "Child component uses context to set font color."])


;; Assume the following component is from our source code.
(def color-theme
  {:child-context    (fn [state] {:color @core/*color})})

(prum/defc context < color-theme []
  [:div {}
   [:div {} "Root component implicitly passes data to descendants."]
   (prum-context-comp)])


(defn mount! [mount-el]
  (prum/mount (context) mount-el))
