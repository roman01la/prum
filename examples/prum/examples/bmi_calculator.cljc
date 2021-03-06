(ns prum.examples.bmi-calculator
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reagent stype BMI calculator


(def *bmi-data (atom {:height 180
                      :weight 80}))


(defn calc-bmi [{:keys [height weight bmi] :as data}]
  (let [h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))


(prum/defc slider [param value min max]
  (let [reset (case param
                :bmi :weight
                :bmi)]
    [:input {:type      "range"
             :value     (int value)
             :min       min
             :max       max
             :style     {:width "100%"}
             :on-change #(swap! *bmi-data assoc
                                param (-> % .-target .-value)
                                reset nil)}]))


(prum/defc bmi-calculator < prum/reactive []
  (let [{:keys [weight height bmi] :as data} (calc-bmi (prum/react *bmi-data))
        [color diagnose] (cond
                           (< bmi 18.5) ["orange" "underweight"]
                           (< bmi 25) ["inherit" "normal"]
                           (< bmi 30) ["orange" "overweight"]
                           :else ["red" "obese"])]
    [:div.bmi {}
     [:div {}
      "Height: " (int height) "cm"
      (slider :height height 100 220)]
     [:div {}
      "Weight: " (int weight) "kg"
      (slider :weight weight 30 150)]
     [:div {}
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      (slider :bmi bmi 10 50)]]))


;; After initial mount, all changes will be re-rendered automatically
#?(:cljs
   (defn mount! [mount-el]
     (prum/mount (bmi-calculator) mount-el)))
