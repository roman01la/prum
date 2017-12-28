(ns prum.react.inline
  (:require [clojure.spec.alpha :as s]
            [hicada.compiler :as h]))

(s/def ::element
  (s/and
    (s/conformer #(if (keyword? %) (name %) %))))

(s/def ::static-element
  (s/cat
    :tag ::element
    :props map?
    :children (s/? (s/spec some?))))

(defn parse-static-element [element]
  (s/conform ::static-element element))

(defn inline-element [[tag props children]]
  (let [result (parse-static-element [tag props children])]
    (if (not= ::s/invalid result)
      (let [{:keys [tag props children]} result
            key (or (:key props) 'js/undefined)
            attrs (-> props (dissoc :ref :key) h/compile-config h/to-js)
            children '(cljs.core/array children)]
        (h/to-js
          {:nodeName tag
           :key      key
           :attrs    attrs
           :children children}))
      (throw (Error. (str "Element " tag " can not be inlined"))))))
