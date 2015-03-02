(ns survivor-bootlist.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.firebase]
            [cljsjs.jquery-ui]
            [cljsjs.react :as react]))

(def fb-root (js/Firebase. "https://survivor.firebaseio.com"))

;; -------------------------
;; State

(def contestants (atom []))

(def voted-out-list (atom []))

(def entries (atom [
                    {:id 1 :name "phil" :points 0 :order [15 8 5 11 3 2 12 16 18 13 1 6 7 10 9 4 17 14]}
                    {:id 2 :name "will" :points 0 :order [8 5 11 2 15 18 3 1 12 16 7 14 17 6 13 10 9 4]}
                    {:id 3 :points 0 :name "Yasuhiro Swagakure" :order [16 2 7 14 13 9 8 12 17 1 3 18 10 4 11 15 6 5]}
                    {:id 4 :points 0 :name "Connor Berry" :order [16 5 12 13 8 15 2 18 17 11 4 6 9 1 3 14 7 10]}
                    {:id 5 :points 0 :name "ciwchris" :order [8 2 13 11 5 12 1 3 7 18 15 9 17 16 10 14 4 6]}
                    {:id 6 :points 0 :name "PopsicleIncorporated" :order [11 18 15 17 12 2 8 1 14 3 5 16 9 7 6 10 13 4]}
                    {:id 7 :points 0 :name "Michael McNulty" :order [12 11 18 5 3 1 10 8 4 2 16 17 7 14 15 9 13 6]}
                    {:id 8 :points 0 :name "TribalQueen" :order [2 18 12 17 1 10 13 3 16 11 5 14 8 9 15 4 6 7]}
                    {:id 9 :points 0 :name "OfficialEláfi" :order [1 11 5 16 12 4 6 10 2 7 9 15 14 8 13 18 3 17]}
                    {:id 10 :points 0 :name "UnnecessaryTodd" :order [11 9 18 14 12 16 8 3 2 5 15 13 1 17 10 6 7 4]}
                    {:id 11 :points 0 :name "Adam Klein" :order [15 17 7 14 5 11 18 1 8 6 2 16 10 3 9 12 4 13]}
                    {:id 12 :points 0 :name "MrCryingBanana" :order [16 3 1 13 8 2 14 6 10 11 9 4 5 12 15 7 17 18]}
                    {:id 13 :points 0 :name "Julio Lowe" :order [18 12 7 1 8 2 6 4 10 16 5 11 3 14 17 13 9 15]}
                    {:id 14 :points 0 :name "Ryan H" :order [8 5 11 7 12 10 3 16 2 9 14 18 4 6 13 1 15 17]}
                    {:id 15 :points 0 :name "Stevo Smito" :order [11 16 7 3 18 2 8 14 5 15 12 6 17 4 10 9 1 13]}
                    {:id 16 :points 0 :name "Jimmy O" :order [8 2 15 11 5 18 3 12 16 1 13 9 7 6 14 17 4 10]}
                    {:id 17 :points 0 :name "Patrick Kennedy" :order [18 8 1 11 2 6 13 12 5 10 16 7 9 4 3 15 14 17]}
                    {:id 18 :points 0 :name "Alex Blackson" :order [11 1 15 17 8 3 18 10 12 6 16 14 5 13 9 4 2 7]}
                    {:id 19 :points 0 :name "Corri Bananas" :order [11 18 8 15 1 5 2 3 12 16 14 6 13 17 9 10 4 7]}
                    {:id 20 :points 0 :name "Tarek Emir Chehab" :order [16 2 13 4 3 10 1 17 15 5 7 14 8 11 18 12 6 9]}
                    {:id 21 :points 0 :name "Will Butler" :order [8 5 12 11 17 2 16 10 1 18 13 14 3 4 9 7 15 6]}
                    {:id 22 :points 0 :name "Will Holston" :order [4 3 16 6 13 11 8 2 12 5 10 14 1 9 15 17 18 7]}
                    {:id 23 :points 0 :name "kyle gardiner" :order [11 2 7 1 13 8 6 18 5 15 12 17 14 10 3 16 4 9 ]}
                    {:id 24 :points 0 :name "Robb LoCurto" :order [11 7 18 13 1 8 12 6 3 5 14 4 17 16 2 10 15 9]}
                    {:id 25 :points 0 :name "Connor Young" :order [18 2 5 17 7 10 12 15 16 3 1 14 4 9 8 11 6 13 ]}
                    {:id 26 :points 0 :name "Jake Everts" :order [13 18 11 8 5 2 6 12 17 10 7 14 3 15 9 4 1 16]}
                    {:id 27 :points 0 :name "Rob Kellogg" :order [15 11 1 2 8 18 5 16 7 12 17 3 4 9 6 14 13 10]}
                    {:id 28 :points 0 :name "Sarah Harkins" :order [15 8 11 5 18 10 12 13 2 1 17 3 9 7 6 4 14 16]}
                    {:id 29 :points 0 :name "Ravindra Sharma" :order [7 3 12 1 8 11 13 15 14 10 18 5 4 9 6 2 16 17]}
                    {:id 30 :points 0 :name "Zachary Irons" :order [1 8 2 5 7 3 11 12 10 15 18 13 14 16 17 4 9 6]}
                    {:id 31 :points 0 :name "Bretton Johnson" :order [13 2 1 8 18 12 17 3 10 7 14 5 6 16 9 11 4 15]}
                    {:id 32 :points 0 :name "Punkpunkpunk" :order [6 1 18 8 16 7 12 13 3 11 10 17 4 5 14 9 15 2]}
                    {:id 33 :points 0 :name "RedFalconGames" :order [11 15 14 5 7 18 17 12 8 2 16 10 6 4 13 1 3 9]}
                    {:id 34 :points 0 :name "TheGreatKeaton" :order [8 16 2 15 5 18 7 13 10 1 11 9 17 12 14 3 4 6]}
                    {:id 35 :points 0 :name "peter rubenstein" :order [10 7 18 8 5 11 1 2 13 6 16 3 15 17 4 14 9 12]}
                    {:id 36 :points 0 :name "None of your business" :order [8 18 5 11 16 7 12 6 15 13 10 4 14 2 17 1 3 9]}
                    {:id 37 :points 0 :name "Jakethesnake903" :order [16 11 8 2 18 15 3 7 4 10 17 5 1 12 9 14 6 13]}
                    {:id 38 :points 0 :name "Demi Nguyen" :order [2 11 8 12 16 18 14 17 10 7 5 15 3 1 9 4 13 6]}
                    {:id 39 :points 0 :name "J Young" :order [2 18 14 11 7 10 12 15 16 9 1 6 3 8 5 17 4 13]}
                    {:id 40 :points 0 :name "ThePoliphilo" :order [14 8 1 5 3 18 4 12 15 2 6 17 10 13 9 11 7 16]}
                    {:id 41 :points 0 :name "Hannah Klare" :order [8 15 11 5 18 2 16 3 1 12 10 17 4 13 14 9 7 6]}
                    {:id 42 :points 0 :name "dotmyiis" :order [5 14 10 8 3 4 18 7 1 16 6 11 2 12 9 15 13 17]}
                    {:id 43 :points 0 :name "Jayden Osborne" :order [2 11 4 5 8 10 12 16 17 18 7 3 14 6 15 1 13 9]}
                    {:id 44 :points 0 :name "Proud Nurse" :order [8 11 18 2 15 1 12 3 16 5 7 14 10 6 17 13 4 9]}
                    {:id 45 :points 0 :name "Glenn Haffner" :order [1 8 11 2 12 16 18 5 7 10 14 13 9 6 15 17 3 4]}
                    {:id 46 :points 0 :name "Róbert Szalai" :order [14 1 11 18 8 4 12 2 9 5 13 3 16 6 7 10 15 17]}
                    {:id 47 :points 0 :name "Noah Trader" :order [11 7 18 15 8 3 5 12 10 6 17 2 14 16 9 1 4 13]}
                    {:id 48 :points 0 :name "Robert Tablefloor" :order [16 11 2 8 13 10 1 6 4 3 12 5 17 15 18 14 7 9]}
                    {:id 49 :points 0 :name "Jimmy Cannata" :order [18 2 11 15 16 13 14 17 10 1 7 8 4 3 5 9 6 12]}
                    {:id 50 :points 0 :name "Pegboards" :order [18 16 5 12 11 1 8 3 17 14 6 10 4 7 2 9 13 15]}
                    {:id 51 :points 0 :name "callum McDonald" :order [16 11 1 8 2 3 10 12 7 13 4 9 17 18 15 14 5 6 ]}
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
   [:ul#leaderboard
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
