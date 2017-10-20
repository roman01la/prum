(ns prum.examples.custom-props
  (:require
    [prum.core :as prum]
    [sablono.core :refer-macros [html]]
    [goog.object :as gobj]
    [prum.examples.core :as core]))


;; Custom methods and data on the underlying React components.

(defn rand-color []
  (str "#" (-> (rand)
               (* 0xffffff)
               (js/Math.floor)
               (.toString 16))))

(def props
  {:msgData   "Components can store custom data on the underlying React component."
   :msgMethod #(this-as this
                 (html
                   [:div {:style {:cursor "pointer"}
                          :on-mouse-move
                          (fn [_]
                            (reset! core/*color (rand-color))
                            (gobj/set this "msgData"
                                      (html
                                        [:div {:style {:color @core/*color}}
                                         (:msgData props)]))
                            (prum/request-render this))}
                    "Custom methods too. Hover me!"]))})


(prum/defcc custom-props < {:class-properties props} [this]
  [:div {}
   [:div {} (gobj/get this "msgData")]
   [:div {} (.call (gobj/get this "msgMethod") this)]])

(defn mount! [mount-el]
  (prum/mount (custom-props) mount-el))
