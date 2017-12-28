(ns prum.compiler
  (:require [hicada.compiler :as h]
            [prum.react.inline :as inline]
            [prum.cljss.compiler :as css]))

(defn normalize-attrs [tag {:keys [type on-change onChange] :as attrs}]
  (let [on-change (or on-change onChange)]
    (if (and on-change
             (or (= tag :textarea)
                 (and (= tag :input)
                      (or (nil? type)
                          (->> type (re-matches #"^(fil|che|rad).*") nil?)))))
      (-> attrs
          (dissoc :on-change :onChange)
          (assoc :on-input on-change))
      attrs)))

(defn transform-fn [[tag attrs children {:keys [inline? css-attr?]}]]
  (let [attrs (if css-attr? (css/compile-css-attr attrs) attrs)
        ret [tag (normalize-attrs tag attrs) children]]
    (if inline?
      (inline/inline-element ret)
      ret)))

(defn compile-html [hiccup]
  (h/compile
    hiccup
    {:create-element 'prum-preact/createElement
     :transform-fn   transform-fn}
    nil
    {:css-attr? true}))

(defmacro html [hiccup]
  (compile-html hiccup))
