(ns prum.cljss.compiler
  (:require [cljss.builder :as builder]))

(defn- compile-class [class styles]
  (let [cls (str "css-" (hash styles))
        gen-class `(cljss.core/css ~@(builder/build-styles cls styles))]
    (if (seq class)
      `(str ~gen-class " " ~@(interpose " " class))
      gen-class)))

(defn compile-css-attr [attrs]
  (if-let [styles (:css attrs)]
    (let [{:keys [class className class-name]} attrs
          class (->> [class className class-name]
                     (mapcat identity)
                     (filter identity))]
      (-> attrs
          (dissoc :css)
          (assoc :class (compile-class class styles))))
    attrs))
