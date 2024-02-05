(ns cc.delboni.helix-flex.app
  (:require ["react-dom/client" :as rdom]
            [cc.delboni.helix-flex.infra.flex.hook :refer [use-flex]]
            [cc.delboni.helix-flex.infra.flex.promise :as flex.promise]
            [cc.delboni.helix-flex.infra.helix :refer [defnc]]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [town.lilac.flex :as flex]))

(defn sleep [ms]
  (js/Promise.
   (fn [res _rej]
     (js/setTimeout
      (fn [] (res))
      ms))))

(def counter (flex/source 0))
(def counter-signal (flex/signal @counter))
(def counter-map (flex/source {:counter 0}))
(def counter-async-inc (flex.promise/resource
                        (fn [this]
                          (-> (sleep 1000)
                              (.then #(inc @(:value this)))))
                        0))
(def counter-async-inc-map (flex.promise/resource
                            (fn [this]
                              (-> (sleep 1000)
                                  (.then #(update @(:value this) :counter inc))))
                            {:counter 0}))

;; app
(defnc app []
  (let [counter-flex (use-flex counter)
        counter-signal-flex (use-flex counter-signal)
        counter-map-flex (use-flex counter-map)
        counter-async-flex (use-flex counter-async-inc)
        counter-async-map-flex (use-flex counter-async-inc-map)]
    (d/div
      (d/h1 "helix-flex")
      (d/div
        (d/h3 (str "Counter: " counter-flex))
        (d/h3 (str "Counter Signal: " counter-signal-flex))
        (d/button {:on-click #(counter inc)} "Count"))
      (d/div
        (d/h3 (str "Counter Map: " counter-map-flex))
        (d/button {:on-click #(counter-map update :counter inc)} "Count"))
      (d/div
        (d/h3 (str "Counter Async: " counter-async-flex))
        (d/button {:disabled (:loading? counter-async-flex)
                   :on-click #(counter-async-inc)} "Count"))
      (d/div
        (d/h3 (str "Counter Async: " counter-async-map-flex))
        (d/button {:disabled (:loading? counter-async-map-flex)
                   :on-click #(counter-async-inc-map)} "Count")))))

;; start your app with your React renderer
(defn ^:export init []
  (doto (rdom/createRoot (js/document.getElementById "app"))
    (.render ($ app))))
