(ns cc.delboni.helix-flex.app
  (:require ["react" :as react]
            ["react-dom/client" :as rdom]
            [cc.delboni.helix-flex.infra.helix :refer [defnc]]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [town.lilac.flex :as flex]))

(def counter (flex/source 0))

(defn use-flex
  "React hook to subscribe to flex sources."
  [container]
  (let [subscribe (fn [callback]
                    (let [signal (flex/signal @container)
                          listener (flex/listen signal callback)]
                      #(flex/dispose! listener)))
        snapshot (fn [] @container)]
    (react/useDebugValue @container str)
    (react/useSyncExternalStore subscribe snapshot)))

;; app
(defnc app []
  (let [counter-flex (use-flex counter)]
    (d/div
      (d/h1 "helix-flex")
      (d/h3 (str "Counter: " counter-flex))
      (d/button {:on-click #(counter inc)} "Count"))))

;; start your app with your React renderer
(defn ^:export init []
  (doto (rdom/createRoot (js/document.getElementById "app"))
    (.render ($ app))))
