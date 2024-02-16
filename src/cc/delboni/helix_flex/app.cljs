(ns cc.delboni.helix-flex.app
  (:require ["react-dom/client" :as rdom]
            [cc.delboni.helix-flex.infra.flex.hook :refer [use-flex]]
            [cc.delboni.helix-flex.infra.helix :refer [defnc]]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [town.lilac.flex :as flex]
            [town.lilac.flex.promise :as flex.promise]))

(defn sleep [ms]
  (js/Promise.
   (fn [res _rej]
     (js/setTimeout
      (fn [] (res))
      ms))))

(def counter (flex/source 0))
(def counter-signal (flex/signal @counter))
(def counter-map (flex/source {:counter 0}))

(def counter-default-async
  (flex/source {:counter 0}))

(def counter-async-inc-map
  (flex.promise/resource
   (fn []
     (-> (sleep 1000)
         (.then #(counter-default-async update :counter inc))
         (.catch #(do (js/console.error %)))))))

(def counter-async-inc-map-status
  (flex/signal {:state @(:state counter-async-inc-map)
                :value @(:value counter-async-inc-map)
                :error @(:error counter-async-inc-map)
                :loading? @(:loading? counter-async-inc-map)}))

;; app
(defnc app []
  (let [counter-flex (use-flex counter)
        counter-signal-flex (use-flex counter-signal)
        counter-map-flex (use-flex counter-map)
        counter-async-flex (use-flex counter-default-async)
        {:keys [loading?] :as counter-async-flex-status} (use-flex counter-async-inc-map-status)]
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
        (d/h3 (str "Counter Async : " counter-async-flex))
        (d/h4 (str "Counter Async Status: " counter-async-flex-status))
        (d/button {:disabled loading?
                   :on-click #(counter-async-inc-map)} "Count")))))

;; start your app with your React renderer
(defn ^:export init []
  (doto (rdom/createRoot (js/document.getElementById "app"))
    (.render ($ app))))
