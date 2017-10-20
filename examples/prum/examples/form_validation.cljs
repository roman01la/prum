(ns prum.examples.form-validation
  (:require
    [prum.core :as prum]
    [prum.examples.core :as core]
    [sablono.preact :as p]))


(prum/defc validating-input < prum/reactive [ref f]
  [:input {:type      "text"
           :style     {:width            170
                       :background-color (when-not (f (prum/react ref))
                                           (prum/react core/*color))}
           :value     (prum/react ref)
           :on-change #(reset! ref (.. % -target -value))}])


(prum/defcc restricting-input < prum/reactive [comp ref f]
  [:input {:type      "text"
           :style     {:width 170}
           :value     (prum/react ref)
           :on-change #(let [new-val (.. % -target -value)]
                         (if (f new-val)
                           (reset! ref new-val)
                           (prum/request-render comp)))}])


(prum/defcc restricting-input-native < prum/reactive [comp ref f]
  (p/createElement "input"
                   #js {:type    "text"
                        :style   #js {:width 170}
                        :value   (prum/react ref)
                        :onInput #(let [new-val (.. % -target -value)]
                                    (if (f new-val)
                                      (reset! ref new-val)
                                      (prum/request-render comp)))}))


(prum/defc form-validation []
  (let [state (atom {:email "a@b.c"
                     :phone "+7913 000 0000"
                     :age   "22"})]
    [:dl {}
     [:dt {} "E-mail:"]
     [:dd {} (validating-input (prum/cursor state :email) #(re-matches #"[^@]+@[^@.]+\..+" %))]
     [:dt {} "Phone:"]
     [:dd {} (restricting-input (prum/cursor state :phone) #(re-matches #"[0-9\- +()]*" %))]
     [:dt {} "Age:"]
     [:dd {} (restricting-input-native (prum/cursor state :age) #(re-matches #"([1-9][0-9]*)?" %))]]))


(defn mount! [mount-el]
  (prum/mount (form-validation) mount-el))
