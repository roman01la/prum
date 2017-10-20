(ns prum.examples.board-reactive
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;; Reactive drawing board


(def *board (atom (core/initial-board)))
(def *board-renders (atom 0))


(prum/defc cell < prum/reactive [x y]
  (swap! *board-renders inc)
  (let [*cursor (prum/cursor-in *board [y x])]
    ;; each cell subscribes to its own cursor inside a board
    ;; note that subscription to color is conditional:
    ;; only if cell is on (@cursor == true),
    ;; this component will be notified on color changes
    [:div.art-cell {:style {:background-color (when (prum/react *cursor) (prum/react core/*color))}
                    :on-mouse-over (fn [_] (swap! *cursor not) nil)}]))


(prum/defc board-reactive []
  [:div.artboard {}
    (for [y (range 0 core/board-height)]
      [:div.art-row {:key y}
        (for [x (range 0 core/board-width)]
          ;; this is how one can specify React key for component
          (-> (cell x y)
              (prum/with-key [x y])))])
   (core/board-stats *board *board-renders)])


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (board-reactive) mount-el)))
