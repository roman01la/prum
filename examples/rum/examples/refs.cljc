(ns rum.examples.refs
  (:require
    [rum.core :as rum]
    #?(:cljs [goog.object :as gobj])
    [rum.examples.core :as core]))


(rum/defcc ta < {:after-render
                 (fn [state]
                   #?(:cljs
                      (let [ta (-> state :rum/react-component (gobj/get "ta"))]
                        (set! (.-height (.-style ta)) "0")
                        (set! (.-height (.-style ta)) (str (+ 2 (.-scrollHeight ta)) "px"))))
                   state)}
  [comp]
  [:textarea
   {:ref           #?(:cljs #(gobj/set comp "ta" %)
                      :clj  nil)
    :style         {:width   "100%"
                    :padding "10px"
                    :font    "inherit"
                    :outline "none"
                    :resize  "none"}
    :default-value "Auto-resizing\ntextarea"
    :placeholder   "Auto-resizing textarea"
    :on-input      (fn [_] (rum/request-render comp))}])


(rum/defc refs []
  [:div {}
   (ta)])


#?(:cljs
   (defn mount! [mount-el]
     (rum/mount (refs) mount-el)))
