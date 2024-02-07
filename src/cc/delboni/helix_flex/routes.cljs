(ns cc.delboni.helix-flex.routes
  (:require ["react-dom/client" :as rdom]
            [cc.delboni.helix-flex.infra.flex.hook :refer [use-flex]]
            [cc.delboni.helix-flex.infra.helix :refer [defnc]]
            [helix.core :refer [$]]
            [helix.dom :as d]
            [town.lilac.flex :as flex]
            [reitit.core :as r]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

;;; state ;;;

(def routes-db (flex/source {:current-route nil}))

(defn navigated [new-match]
  (let [old-match (:current-route @routes-db)
        controllers (rfc/apply-controllers (:controllers old-match) new-match)]
    (routes-db assoc :current-route (assoc new-match :controllers controllers))))

;;; Views ;;;

(defnc home-page []
  (d/div
    (d/h1 "This is home page")
    (d/button
      {:on-click #(rfe/push-state ::sub-page2)}
      "Go to sub-page 2")))

(defnc sub-page1 []
  (d/div
    (d/h1 "This is sub-page 1")))

(defnc sub-page2 []
  (d/div
    (d/h1 "This is sub-page 2")))

;;; Routes ;;;

(def routes
  ["/"
   [""
    {:name      ::home
     :view      home-page
     :link-text "Home"
     :controllers
     [{;; Do whatever initialization needed for home page
       :start (fn [& _params] (js/console.log "Entering home page"))
       ;; Teardown can be done here.
       :stop  (fn [& _params] (js/console.log "Leaving home page"))}]}]
   ["sub-page1"
    {:name      ::sub-page1
     :view      sub-page1
     :link-text "Sub page 1"
     :controllers
     [{:start (fn [& _params] (js/console.log "Entering sub-page 1"))
       :stop  (fn [& _params] (js/console.log "Leaving sub-page 1"))}]}]
   ["sub-page2"
    {:name      ::sub-page2
     :view      sub-page2
     :link-text "Sub-page 2"
     :controllers
     [{:start (fn [& _params] (js/console.log "Entering sub-page 2"))
       :stop  (fn [& _params] (js/console.log "Leaving sub-page 2"))}]}]])

(defn on-navigate [new-match]
  (when new-match
    (navigated new-match)))

(def router
  (rf/router routes {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   ; use # fragment on route
   ; to not use this on servers you need special rules
   ; to redirect 404 to index.html configuration
   {:use-fragment true}))

(defnc nav [{:keys [router current-route]}]
  (d/ul
    (for [route-name (r/route-names router)
          :let       [route (r/match-by-name router route-name)
                      text (-> route :data :link-text)]]
      (d/li {:key route-name}
        (when (= route-name (-> current-route :data :name))
          "> ")
        ;; Create a normal links that user can click
        (d/a {:href (rfe/href route-name)} text)))))

(defnc router-component [{:keys [router]}]
  (let [{:keys [current-route]} (use-flex routes-db)]
    (d/div
      ($ nav {:router router :current-route current-route})
      (when current-route
        (-> current-route :data :view $)))))

;; start your app with your React renderer
(defn ^:export init []
  (init-routes!)
  (doto (rdom/createRoot (js/document.getElementById "app"))
    (.render ($ router-component {:router router}))))
