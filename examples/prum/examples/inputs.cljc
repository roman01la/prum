(ns prum.examples.inputs
  (:require
    [clojure.string :as str]
    [prum.core :as prum]))


(def values (range 1 5))


(prum/defc reactive-input < prum/reactive
  [*ref]
  (let [value (prum/react *ref)]
    [:input {:type      "text"
             :value     value
             :style     {:width 170}
             :on-change (fn [e] (reset! *ref (long (.. e -currentTarget -value))))}]))


(prum/defc checkboxes < prum/reactive
  [*ref]
  (let [value (prum/react *ref)]
    [:div {}
     (for [v values]
       [:input {:type     "checkbox"
                :checked  (= v value)
                :value    v
                :on-click (fn [_] (reset! *ref v))}])]))


(prum/defc radio < prum/reactive
  [*ref]
  (let [value (prum/react *ref)]
    [:div {}
     (for [v values]
       [:input {:type     "radio"
                :name     "inputs_radio"
                :checked  (= v value)
                :value    v
                :on-click (fn [_] (reset! *ref v))}])]))


(prum/defc select < prum/reactive
  [*ref]
  (let [value (prum/react *ref)]
    [:select
     {:on-change (fn [e] (reset! *ref (long (.. e -target -value))))
      :value     value}
     (for [v values]
       [:option {:value v} v])]))


(defn next-value [v]
  (loop [v' v]
    (if (= v v')
      (recur (rand-nth values))
      v')))


(prum/defc shuffle-button < prum/reactive
  [*ref]
  [:button
   {:on-click (fn [_]
                (swap! *ref next-value))}
   "Next value"])


(prum/defc value < prum/reactive
  [*ref]
  [:code {} (pr-str (prum/react *ref))])


(prum/defc inputs []
  (let [*ref (atom nil)]
    [:dl {}
     [:dt {} "Input"] [:dd {} (reactive-input *ref)]
     [:dt {} "Checks"] [:dd {} (checkboxes *ref)]
     [:dt {} "Radio"] [:dd {} (radio *ref)]
     [:dt {} "Select"] [:dd {} (select *ref)]
     [:dt {} (value *ref)] [:dd {} (shuffle-button *ref)]]))


#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (inputs) mount-el)))


