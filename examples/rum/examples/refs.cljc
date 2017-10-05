(ns rum.examples.refs
  (:require
    [rum.core :as rum]
    [rum.examples.core :as core]))

(rum/defc textarea [{:keys [on-change]}]
  [:textarea
   {:style         {:width   "100%"
                    :padding "10px"
                    :font    "inherit"
                    :outline "none"
                    :resize  "none"}
    :default-value "Auto-resizing\ntextarea"
    :placeholder   "Auto-resizing textarea"
    :on-change     on-change}])

(rum/defcc tac < {:after-render
                  (fn [state]
                    #?(:cljs
                       (let [tac (rum/ref-node state :tac)]
                         (set! (.-height (.-style tac)) "0")
                         (set! (.-height (.-style tac)) (str (+ 2 (.-scrollHeight tac)) "px"))))
                    state)}
  [comp]
  (rum/with-ref
    (textarea {:on-change #(rum/request-render comp)})
    (rum/use-ref comp :tac)))


(rum/defcc ta < {:after-render
                 (fn [state]
                   #?(:cljs
                      (let [ta (rum/ref state :ta)]
                        (set! (.-height (.-style ta)) "0")
                        (set! (.-height (.-style ta)) (str (+ 2 (.-scrollHeight ta)) "px"))))
                   state)}
  [comp]
  [:textarea
   {:ref           (rum/use-ref comp :ta)
    :style         {:width   "100%"
                    :padding "10px"
                    :font    "inherit"
                    :outline "none"
                    :resize  "none"}
    :default-value "Auto-resizing\ntextarea"
    :placeholder   "Auto-resizing textarea"
    :on-change     (fn [_] (rum/request-render comp))}])


(rum/defc refs []
  [:div {}
   (ta)
   (tac)])


#?(:cljs
   (defn mount! [mount-el]
     (rum/mount (refs) mount-el)))
