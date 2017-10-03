(ns rum.examples.form-validation
  (:require
    [rum.core :as rum]
    [rum.examples.core :as core]
    [sablono.preact :as p]))


(rum/defc validating-input < rum/reactive [ref f]
  [:input {:type     "text"
           :style    {:width            170
                      :background-color (when-not (f (rum/react ref))
                                          (rum/react core/*color))}
           :value    (rum/react ref)
           :on-input #(reset! ref (.. % -target -value))}])


(rum/defcc restricting-input < rum/reactive [comp ref f]
  [:input {:type     "text"
           :style    {:width 170}
           :value    (rum/react ref)
           :on-input #(let [new-val (.. % -target -value)]
                        (if (f new-val)
                          (reset! ref new-val)
                          (rum/request-render comp)))}])


(rum/defcc restricting-input-native < rum/reactive [comp ref f]
  (p/createElement "input"
                   #js {:type    "text"
                        :style   #js {:width 170}
                        :value   (rum/react ref)
                        :onInput #(let [new-val (.. % -target -value)]
                                    (if (f new-val)
                                      (reset! ref new-val)
                                      (rum/request-render comp)))}))


(rum/defc form-validation []
  (let [state (atom {:email "a@b.c"
                     :phone "+7913 000 0000"
                     :age   "22"})]
    [:dl {}
     [:dt {} "E-mail:"]
     [:dd {} (validating-input (rum/cursor state :email) #(re-matches #"[^@]+@[^@.]+\..+" %))]
     [:dt {} "Phone:"]
     [:dd {} (restricting-input (rum/cursor state :phone) #(re-matches #"[0-9\- +()]*" %))]
     [:dt {} "Age:"]
     [:dd {} (restricting-input-native (rum/cursor state :age) #(re-matches #"([1-9][0-9]*)?" %))]]))


(defn mount! [mount-el]
  (rum/mount (form-validation) mount-el))
