(ns prum.examples
  (:require
    [clojure.string :as str]
    [prum.core :as prum]
    [prum.examples.core :as core]

    [prum.examples.timer-static :as timer-static]
    [prum.examples.timer-reactive :as timer-reactive]
    [prum.examples.controls :as controls]
    [prum.examples.binary-clock :as binary-clock]
    [prum.examples.board-reactive :as board-reactive]
    [prum.examples.bmi-calculator :as bmi-calculator]
    [prum.examples.form-validation :as form-validation]
    [prum.examples.inputs :as inputs]
    [prum.examples.refs :as refs]
    [prum.examples.local-state :as local-state]
    [prum.examples.keys :as keys]
    [prum.examples.self-reference :as self-reference]
    [prum.examples.context :as context]
    [prum.examples.custom-props :as custom-props]
    [prum.examples.swap-recognizer :as swap-recognizer]
    [prum.examples.cljss :as cljss]))


(enable-console-print!)


;; Mount everything

(timer-static/mount!    (core/el "timer-static"))
(timer-reactive/mount!  (core/el "timer-reactive"))
(controls/mount!        (core/el "controls"))
(binary-clock/mount!    (core/el "binary-clock"))
(board-reactive/mount!  (core/el "board-reactive"))
(bmi-calculator/mount!  (core/el "bmi-calculator"))
(form-validation/mount! (core/el "form-validation"))
(inputs/mount!          (core/el "inputs"))
(refs/mount!            (core/el "refs"))
(local-state/mount!     (core/el "local-state"))
(keys/mount!            (core/el "keys"))
(self-reference/mount!  (core/el "self-reference"))
(context/mount!         (core/el "context"))
(custom-props/mount!    (core/el "custom-props"))
(swap-recognizer/mount! (core/el "swap-recognizer"))
(cljss/mount!           (core/el "cljss"))


;; Start clock ticking

(defn tick []
  (reset! core/*clock (.getTime (js/Date.)))
  (js/setTimeout tick @core/*speed))


(tick)
