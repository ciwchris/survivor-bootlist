(ns survivor-bootlist.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.firebase]
            [cljsjs.react :as react]))

(def fb-root (js/Firebase. "https://survivor.firebaseio.com"))

;; -------------------------
;; State

(def contestants (atom []))

(def voted-out-list (atom []))

(def entries (atom [
                    {:id 1 :name "phil" :points 0 :order [15 8 5 11 3 2 12 16 18 13 1 6 7 10 9 4 17 14]}
                    {:id 2 :name "will" :points 0 :order [8 5 11 2 15 18 3 1 12 16 7 14 17 6 13 10 9 4]}
                    ]))

(def selected-entry (atom 1))

;; ------------------------
;; Retrieve Contestants from Firebase
(defn created-voted-out [voted-out players]
  (if (empty? players)
    voted-out
    (let [p (first players)]
      (recur
       (if (= 0 (int (:out p)))
         voted-out
         (conj voted-out {:id (int (:id p)) :out (int (:out p))}))
         (rest players)))))

(defn create-player [player]
  {:id (get player "id")
   :name (get player "name")
   :out (get player "out")
   :tribe (get player "tribe")
   :locked (not= 0 (get player "out"))})

(defn create-players
  ([players] (create-players players []))
  ([players created-players]
   (if (empty? players)
     (do
       (swap! contestants (fn [] (into [] created-players)))
       (swap! voted-out-list (fn [] (into [] (created-voted-out [] created-players))))
       nil)
     (recur
      (rest players)
      (conj created-players (create-player (first players)))))))

(defn retrieve-players []
  (.once fb-root "value" #(-> %
                              .val
                              js->clj
                              (get "players")
                              create-players))
  contestants)

;; -------------------------
;; To read the players when sorted
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(enable-console-print!)

;; -------------------------
;; Views

(defn create-class [contestant]
  (let [default-class ["ui-state-default" (:tribe contestant)]
        class (if (true? (:locked contestant))
                (into default-class ["ui-state-disabled"])
                default-class)]
    {:class (clojure.string/join " " class) :id (:id contestant)}))

(defn lister [items name]
  [:ul {:id name :class "connectedSortable"}
   (for [item items]
     ^{:key (:id item)} [:li (create-class item)
                         [:img {:src (str "/images/" (:name item) ".png")}]
                         [:div (:name item)]])])

(defn create-contestant-list [entries selected-entry voted-out-list]
  (let [entry-contestant-order (:order (first (filter #(= selected-entry (:id %)) entries)))]
            (map
             #(first (filter (fn [voted-out-entry] (= (:id voted-out-entry) %)) voted-out-list))
             entry-contestant-order)))

(defn calculate-points-for-entry
  ([entry] (calculate-points-for-entry 0 18 entry))
  ([points max-points entry]
   (if (empty? entry)
     points
     (let [new-points (if (nil? (first entry))
                        0
                        (Math/abs (- max-points (:out (first entry)))))]
       (recur
        (+ points new-points)
        (dec max-points)
        (rest entry))))))

(defn calculate-points-for-entries [entries-to-modify voted-out-list]
  (map #(assoc
         %
         :points
         (calculate-points-for-entry (create-contestant-list entries-to-modify (:id %) voted-out-list)))
       entries-to-modify))

(defn sorted-entries [entries]
   (into [] (sort #(compare (:points %1) (:points %2)) entries)))

(defn leader-board [entries contestants]
  [:div.column
   [:h3 "Leaderboard"]
   [:ul
    (for [entry (sorted-entries entries)]
      ^{:key (:id entry)} [:li.ui-state-default 
                           [:div {:class "points"} (:name entry)
                            [:div (:points entry)]]])]])

(defn create-placed-list [placed]
  (into [] (sort #(compare (:out %2) (:out %1)) placed)))

(defn entries-display [entries selected-entry contestants]
  [:div.column
   [:h3 "Entries"]
   [:select {:on-change #(swap! selected-entry (fn [] (int (-> % .-target .-value))))}
    (for [entry entries]
      ^{:key (:id entry)} [:option {:value (:id entry)}
                           (:name entry)])]
   (if (not (empty? contestants))
     [lister (create-contestant-list entries @selected-entry contestants) "selected-entry"])])

(defn rand-loader []
  (let [tribes ["escameca" "nagarote" "masaya"]]
    (nth tribes (-> tribes
                    count
                    rand
                    int))))

(defn loader []
  [:div.loader
   [:div {:class (str "uil-facebook-css " (rand-loader)) :style {:-webkit-transform "scale(0.6)"}}
    [:div]
    [:div]
    [:div]]])

(defn home [contestants]
  (if (empty? @contestants)
    [loader]
    (let [entries (calculate-points-for-entries @entries @voted-out-list)]
      [:div [:h1 "Phil & Will's Survivor World's Apart  Boot List"]
       [leader-board entries @contestants]
       [entries-display entries selected-entry @contestants]
       [:div.column
        [:h3 "Booted"]
        [lister (create-placed-list  (filter #(true? (:locked %)) @contestants)) "placed"]]
       [:div.column
        [:h3 "Players"]
        [lister (filter #(= 0 (:out %)) @contestants) "contestants"]]])))

(defn update-voted-out [new-placed-list]
  (let [new-sorted-list (map-indexed (fn [index item] (hash-map :id (int (.-id item)) :out (- 18 index))) new-placed-list)]
    (swap! voted-out-list (fn [] new-sorted-list))))

(defn home-did-mount []
  (js/$ (fn []
          (.sortable (js/$ "#contestants") (clj->js {
                                                     :items "li:not(.ui-state-disabled)"
                                                     :connectWith ".connectedSortable"
                                                     }))
          (.sortable (js/$ "#placed") (clj->js {
                                                :items "li:not(.ui-state-disabled)"
                                                :connectWith ".connectedSortable"
                                                :update (fn [e ui] (update-voted-out (-> e .-target .-childNodes)))
                                                }))
          (.disableSelection (js/$ "#placed")))))

(defn home-component [contestants]
  (reagent/create-class {:reagent-render (fn [contestants] (home contestants))
                         :display-name "home-component"
                         :get-initial-state retrieve-players
                         :component-did-update home-did-mount}))
;; -------------------------
;; Initialize app
(defn init! []
  (reagent/render-component [home-component contestants] (.getElementById js/document "app")))
