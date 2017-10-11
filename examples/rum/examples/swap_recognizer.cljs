(ns rum.examples.swap-recognizer
  (:require [rum.core :as rum]
            [goog.events :as evt]
            [goog.object :as gobj]))

(def tolerance 100)

(def initial-state {:x [] :y [] :match nil})

(defn- capture [state e]
  (let [touch (-> e (gobj/get "event_") (gobj/get "touches") (gobj/get "0"))]
    (.preventDefault e)
    (swap! state #(-> %
                      (update :x conj (.-clientX touch))
                      (update :y conj (.-clientY touch))))))

(defn- compute [on-swipe state e]
  (let [{:keys [x y]} @state
        x0      (first x)
        y0      (first y)
        x1      (last x)
        y1      (last y)
        xdt     (- x1 x0)
        ydt     (- y1 y0)
        gesture (cond
                  (and (> y0 y1)
                       (>= (Math/abs ydt) tolerance)
                       (< xdt tolerance))
                  :up

                  (and (< y0 y1)
                       (>= ydt tolerance)
                       (< xdt tolerance))
                  :down

                  (and (> x0 x1)
                       (>= (Math/abs xdt) tolerance)
                       (< ydt tolerance))
                  :left

                  (and (< x0 x1)
                       (>= xdt tolerance)
                       (< ydt tolerance))
                  :right

                  :else nil)]
    (when gesture
      (on-swipe gesture))
    (reset! state initial-state)))

(defn swap-recognizer-mixin [f]
  {:did-mount
   (fn [st]
     (let [node      (rum/dom-node st)
           state     (atom initial-state)
           set-state #(f (::state st) %)
           capture   #(capture state %)
           compute   #(compute set-state state %)]
       (evt/listen node "touchstart" capture)
       (evt/listen node "touchmove" capture)
       (evt/listen node "touchend" compute)
       (-> st
           (assoc ::capture-state state)
           (assoc ::capture capture)
           (assoc ::compute compute))))
   :will-unmount
   (fn [st]
     (let [node    (rum/dom-node st)
           state   (::capture-state st)
           capture (::capture st)
           compute (::compute st)]
       (evt/unlisten node "touchstart" capture)
       (evt/unlisten node "touchmove" capture)
       (evt/unlisten node "touchend" compute)
       (dissoc st ::capture-state ::capture ::compute)))})

(rum/defcs example <
  (rum/local 0 ::state)
  (swap-recognizer-mixin
    #(cond
       (and (zero? @%1) (= %2 :right)) nil
       (= %2 :left) (swap! %1 inc)
       (= %2 :right) (swap! %1 dec)
       :else nil))
  [{state ::state}]
  (let [idx @state]
    [:div {:style {:height     160
                   :background "#eee"
                   :position   "relative"
                   :overflow   "hidden"}}
     [:div {:style {:transform (str "translate3d(" (* -256 idx) "px, 0, 0)")
                    :display   "flex"}}
      (for [x (range (inc idx))]
        [:div {:key   x
               :style {:min-width       256
                       :min-height      160
                       :display         "flex"
                       :align-items     "center"
                       :justify-content "center"}}
         x])]]))


(defn mount! [el]
  (rum/mount (example) el))
