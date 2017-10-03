(ns rum.core
  (:refer-clojure :exclude [ref])
  (:require-macros rum.core)
  (:require
    [sablono.preact :as p]
    [goog.object :as gobj]
    [rum.cursor :as cursor]
    [rum.util :as util :refer [collect collect* call-all]]
    [rum.derived-atom :as derived-atom]))

(defn state
  "Given React component, returns Rum state associated with it"
  [comp]
  (gobj/get (gobj/get comp "state") ":rum/state"))


(defn- build-class [render mixins display-name]
  (let [init           (collect   :init mixins)             ;; state props -> state
        will-mount     (collect* [:will-mount               ;; state -> state
                                  :before-render] mixins)   ;; state -> state
        render         render                               ;; state -> [dom state]
        wrap-render    (collect   :wrap-render mixins)      ;; render-fn -> render-fn
        wrapped-render (reduce #(%2 %1) render wrap-render)
        did-mount      (collect* [:did-mount                ;; state -> state
                                  :after-render] mixins)    ;; state -> state
        did-remount    (collect   :did-remount mixins)      ;; old-state state -> state
        should-update  (collect   :should-update mixins)    ;; old-state state -> boolean
        will-update    (collect* [:will-update              ;; state -> state
                                  :before-render] mixins)   ;; state -> state
        did-update     (collect* [:did-update               ;; state -> state
                                  :after-render] mixins)    ;; state -> state
        will-unmount   (collect   :will-unmount mixins)     ;; state -> state
        child-context  (collect   :child-context mixins)    ;; state -> child-context
        class-props    (reduce merge (collect :class-properties mixins)) ;; custom properties and methods

        ctor (fn [props]
               (this-as this
                 (gobj/set this "state"
                           #js {":rum/state"
                                (-> (gobj/get props ":rum/initial-state")
                                    (assoc :rum/react-component this)
                                    (call-all init props)
                                    volatile!)})
                 (.call p/Component this props)))]

    (gobj/set ctor "displayName" display-name)
    (goog/inherits ctor p/Component)

    (-> {:componentWillMount
         (when-not (empty? will-mount)
           (fn []
             (this-as this
               (vswap! (state this) call-all will-mount))))
         :componentDidMount
         (fn []
           (this-as this
             (gobj/set this ":rum/mounted?" true)
             (when-not (empty? did-mount)
               (vswap! (state this) call-all did-mount))))
         :componentWillReceiveProps
         (fn [next-props]
           (this-as this
             (let [old-state  @(state this)
                   state      (merge old-state
                                     (gobj/get next-props ":rum/initial-state"))
                   next-state (reduce #(%2 old-state %1) state did-remount)]
               ;; allocate new volatile so that we can access both old and new states in shouldComponentUpdate
               (.setState this #js {":rum/state" (volatile! next-state)}))))
         :shouldComponentUpdate
         (when-not (empty? should-update)
           (fn [next-props next-state]
             (this-as this
               (let [old-state @(state this)
                     new-state @(gobj/get next-state ":rum/state")]
                 (or (some #(% old-state new-state) should-update) false)))))
         :componentWillUpdate
         (when-not (empty? will-update)
           (fn [_ next-state]
             (this-as this
               (let [new-state (gobj/get next-state ":rum/state")]
                 (vswap! new-state call-all will-update)))))
         :render
         (fn []
           (this-as this
             (let [state (state this)
                   [dom next-state] (wrapped-render @state)]
               (vreset! state next-state)
               dom)))
         :componentDidUpdate
         (when-not (empty? did-update)
           (fn [_ _]
             (this-as this
               (vswap! (state this) call-all did-update))))
         :componentWillUnmount
         (fn []
           (this-as this
             (when-not (empty? will-unmount)
               (vswap! (state this) call-all will-unmount))
             (gobj/set this ":rum/mounted?" false)))
         :getChildContext
         (when-not (empty? child-context)
           (fn []
             (this-as this
               (let [state @(state this)]
                 (clj->js (transduce (map #(% state)) merge {} child-context))))))}
      (merge class-props)
      (->> (util/filter-vals some?))
      (clj->js)
      (->> (gobj/extend (gobj/get ctor "prototype"))))
    ctor))


(defn- build-ctor [render mixins display-name]
  (let [class  (build-class render mixins display-name)
        key-fn (first (collect :key-fn mixins))
        ctor   (if (some? key-fn)
                 (fn [& args]
                   (let [props #js { ":rum/initial-state" { :rum/args args}
                                     "key" (apply key-fn args)}]
                     (p/createElement class props)))
                 (fn [& args]
                   (let [props #js { ":rum/initial-state" { :rum/args args}}]
                     (p/createElement class props))))]
    (with-meta ctor { :rum/class class})))


(defn build-defc [render-body mixins display-name]
  (if (empty? mixins)
    (let [class (fn [props]
                  (apply render-body (gobj/get props ":rum/args")))
          _     (gobj/set class "displayName" display-name)
          ctor  (fn [& args]
                  (p/createElement class #js { ":rum/args" args}))]
      (with-meta ctor { :rum/class class}))
    (let [render (fn [state] [(apply render-body (:rum/args state)) state])]
      (build-ctor render mixins display-name))))


(defn build-defcs [render-body mixins display-name]
  (let [render (fn [state] [(apply render-body state (:rum/args state)) state])]
    (build-ctor render mixins display-name)))


(defn build-defcc [render-body mixins display-name]
  (let [render (fn [state] [(apply render-body (:rum/react-component state) (:rum/args state)) state])]
    (build-ctor render mixins display-name)))


;; render queue

(def ^:private schedule
  js/window.requestAnimationFrame)


(def ^:private batch
  (fn [f a] (f a)))


(def ^:private empty-queue [])
(def ^:private render-queue (volatile! empty-queue))


(defn- render-all [queue]
  (doseq [comp queue
          :when (gobj/get comp ":rum/mounted?")]
    (.forceUpdate comp)))


(defn- render []
  (let [queue @render-queue]
    (vreset! render-queue empty-queue)
    (batch render-all queue)))


(defn request-render
  "Schedules react component to be rendered on next animation frame"
  [component]
  (when (empty? @render-queue)
    (schedule render))
  (vswap! render-queue conj component))


(defn mount
  "Add component to the DOM tree. Idempotent. Subsequent mounts will just update component"
  ([component node]
   (mount component node nil))
  ([component node root]
   (p/render component node root)))

;; initialization

(defn with-key
  "Adds React key to component"
  [component key]
  (p/cloneElement component #js { "key" key } nil))


(defn with-ref
  "Adds React ref (string or callback) to component"
  [component ref]
  (p/cloneElement component #js { "ref" ref } nil))


(defn dom-node
  "Given state, returns top-level DOM node. Can’t be called during render"
  [state]
  (gobj/get (:rum/react-component state) "base"))


;; static mixin

(def static
  "Mixin. Will avoid re-render if none of component’s arguments have changed.
   Does equality check (=) on all arguments"
  { :should-update
    (fn [old-state new-state]
      (not= (:rum/args old-state) (:rum/args new-state)))})


;; local mixin

(defn local
  "Mixin constructor. Adds an atom to component’s state that can be used to keep stuff
   during component’s lifecycle. Component will be re-rendered if atom’s value changes.
   Atom is stored under user-provided key or under `:rum/local` by default"
  ([initial] (local initial :rum/local))
  ([initial key]
   { :will-mount
     (fn [state]
       (let [local-state (atom initial)
             component   (:rum/react-component state)]
         (add-watch local-state key
           (fn [_ _ _ _]
             (request-render component)))
         (assoc state key local-state)))}))


;; reactive mixin

(def ^:private ^:dynamic *reactions*)


(def reactive
  "Mixin. Works in conjunction with `rum.core/react`"
  { :init
    (fn [state props]
      (assoc state :rum.reactive/key (random-uuid)))
    :wrap-render
    (fn [render-fn]
      (fn [state]
        (binding [*reactions* (volatile! #{})]
          (let [comp             (:rum/react-component state)
                old-reactions    (:rum.reactive/refs state #{})
                [dom next-state] (render-fn state)
                new-reactions    @*reactions*
                key              (:rum.reactive/key state)]
            (doseq [ref old-reactions]
              (when-not (contains? new-reactions ref)
                (remove-watch ref key)))
            (doseq [ref new-reactions]
              (when-not (contains? old-reactions ref)
                (add-watch ref key
                  (fn [_ _ _ _]
                    (request-render comp)))))
            [dom (assoc next-state :rum.reactive/refs new-reactions)]))))
    :will-unmount
    (fn [state]
      (let [key (:rum.reactive/key state)]
        (doseq [ref (:rum.reactive/refs state)]
          (remove-watch ref key)))
      (dissoc state :rum.reactive/refs :rum.reactive/key))})


(defn react
  "Works in conjunction with `rum.core/reactive` mixin. Use this function instead of
   `deref` inside render, and your component will subscribe to changes happening
   to the derefed atom."
  [ref]
  (assert *reactions* "rum.core/react is only supported in conjunction with rum.core/reactive")
  (vswap! *reactions* conj ref)
  @ref)


;; derived-atom

(def ^{:style/indent 2} derived-atom
  "Use this to create “chains” and acyclic graphs of dependent atoms.
   `derived-atom` will:
    - Take N “source” refs
    - Set up a watch on each of them
    - Create “sink” atom
    - When any of source refs changes:
       - re-run function `f`, passing N dereferenced values of source refs
       - `reset!` result of `f` to the sink atom
    - return sink atom

    (def *a (atom 0))
    (def *b (atom 1))
    (def *x (derived-atom [*a *b] ::key
              (fn [a b]
                (str a \":\" b))))
    (type *x) ;; => clojure.lang.Atom
    \\@*x     ;; => 0:1
    (swap! *a inc)
    \\@*x     ;; => 1:1
    (reset! *b 7)
    \\@*x     ;; => 1:7

   Arguments:
     refs - sequence of source refs
     key  - unique key to register watcher, see `clojure.core/add-watch`
     f    - function that must accept N arguments (same as number of source refs)
            and return a value to be written to the sink ref.
            Note: `f` will be called with already dereferenced values
     opts - optional. Map of:
       :ref           - Use this as sink ref. By default creates new atom
       :check-equals? - Do an equality check on each update: `(= @sink (f new-vals))`.
                        If result of `f` is equal to the old one, do not call `reset!`.
                        Defaults to `true`. Set to false if calling `=` would be expensive"
  derived-atom/derived-atom)


;; cursors

(defn cursor-in
  "Given atom with deep nested value and path inside it, creates an atom-like structure
   that can be used separately from main atom, but will sync changes both ways:

     (def db (atom { :users { \"Ivan\" { :age 30 }}}))
     (def ivan (rum/cursor db [:users \"Ivan\"]))
     \\@ivan ;; => { :age 30 }
     (swap! ivan update :age inc) ;; => { :age 31 }
     \\@db ;; => { :users { \"Ivan\" { :age 31 }}}
     (swap! db update-in [:users \"Ivan\" :age] inc) ;; => { :users { \"Ivan\" { :age 32 }}}
     \\@ivan ;; => { :age 32 }

  Returned value supports deref, swap!, reset!, watches and metadata.
  The only supported option is `:meta`"
  [ref path & {:as options}]
  (if (instance? cursor/Cursor ref)
    (cursor/Cursor. (gobj/get ref "ref") (into (gobj/get ref "path") path) (:meta options))
    (cursor/Cursor. ref path (:meta options))))


(defn cursor
  "Same as `rum.core/cursor-in` but accepts single key instead of path vector"
  [ref key & options]
  (apply cursor-in ref [key] options))
