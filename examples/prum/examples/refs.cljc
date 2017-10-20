(ns prum.examples.refs
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))

(prum/defc textarea [{:keys [on-change]}]
  [:textarea
   {:style         {:width   "100%"
                    :padding "10px"
                    :font    "inherit"
                    :outline "none"
                    :resize  "none"}
    :default-value "Auto-resizing\ntextarea"
    :placeholder   "Auto-resizing textarea"
    :on-change     on-change}])

(prum/defcc tac < {:after-render
                   (fn [state]
                     #?(:cljs
                        (let [tac (prum/ref-node state :tac)]
                          (set! (.-height (.-style tac)) "0")
                          (set! (.-height (.-style tac)) (str (+ 2 (.-scrollHeight tac)) "px"))))
                     state)}
  [comp]
  (prum/with-ref
    (textarea {:on-change #(prum/request-render comp)})
    (prum/use-ref comp :tac)))


(prum/defcc ta < {:after-render
                  (fn [state]
                    #?(:cljs
                       (let [ta (prum/ref state :ta)]
                         (set! (.-height (.-style ta)) "0")
                         (set! (.-height (.-style ta)) (str (+ 2 (.-scrollHeight ta)) "px"))))
                    state)}
  [comp]
  [:textarea
   {:ref           (prum/use-ref comp :ta)
    :style         {:width   "100%"
                    :padding "10px"
                    :font    "inherit"
                    :outline "none"
                    :resize  "none"}
    :default-value "Auto-resizing\ntextarea"
    :placeholder   "Auto-resizing textarea"
    :on-change     (fn [_] (prum/request-render comp))}])


(prum/defc refs []
  [:div {}
   (ta)
   (tac)])


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (refs) mount-el)))
