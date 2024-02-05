(ns cc.delboni.helix-flex.infra.flex.promise
  (:require
   [town.lilac.flex :as flex]))

(defrecord Resource [signal state error value loading? fetcher ^:volatile-mutable p]
  IDeref
  (-deref [_]
    @signal)

  IFn
  (-invoke [this]
    (flex/untrack
     (case @state
       (:unresolved :ready :error)
       (set! p (-> (fetcher this)
                   (.then (fn [x]
                            (flex/batch
                             (state :ready)
                             (value x))))
                   (.catch (fn [e]
                             (flex/batch
                              (state :error)
                              (error e))))))
       nil)
     (case @state
       :unresolved (state :pending)
       (:ready :error) (state :refreshing)
       nil))
    this))

(defn resource
  "Returns a flex source that updates its state based on a promise-returning
  function. Calling the source like a function will execute the `fetcher`,
  a function that returns a promise, updating the state of the resource as it
  proceeds. Derefing will return the last value retrieved by `fetcher`.

  The return value is also an associative containing the following keys:
  `:state` - a source containing one of :unresolved, :pending, :ready,
             :refreshing, :error
  `:error` - a source containing last error from `fetcher`
  `:value` - a source containing last value retrieved by `fetcher`
  `:loading?` - a signal containing true/false whether currently waiting for a
                promise returned by `fetcher` to fulfill
  `:fetcher` - the original `fetcher` function
  `:p` - the last promise returned by `fetcher`"
  ([fetcher]
   (resource fetcher nil))
  ([fetcher initial-value]
   (let [state (flex/source :unresolved)
         error (flex/source nil)
         value (flex/source initial-value)
         loading? (flex/signal
                   (case @state
                     (:pending :refreshing) true
                     false))
         signal (flex/signal {:loading? @loading?
                              :value @value
                              :error @error
                              :state @state})]
     (->Resource signal state error value loading? fetcher nil))))
