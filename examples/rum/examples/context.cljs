(ns rum.examples.context
  (:require
    [rum.core :as rum]
    [rum.examples.core :as core]))

;; Components with context that all descendants have access to implicitly.

;; This is useful when you are using child components you cannot modify.
;; For example, a JS library that gives you components which rely on a context
;; value being set by an ancestor component.


(rum/defcc rum-context-comp [comp]
  [:span
   {:style {:color (rum/context comp :color)}}
   "Child component uses context to set font color."])


;; Assume the following component is from our source code.
(def color-theme
  {:child-context    (fn [state] {:color @core/*color})})

(rum/defc context < color-theme []
  [:div {}
   [:div {} "Root component implicitly passes data to descendants."]
   (rum-context-comp)])


(defn mount! [mount-el]
  (rum/mount (context) mount-el))
