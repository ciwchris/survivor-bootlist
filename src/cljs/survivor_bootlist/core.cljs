(ns survivor-bootlist.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.react :as react]))

;; -------------------------
;; State

(def contestants (atom [
                        {:id 1 :name "Carolyn" :out 0 :tribe "masaya"}
                        {:id 2 :name "Dan" :out 0 :tribe "escameca"}
                        {:id 3 :name "Hali" :out 0 :tribe "nagarote"}
                        {:id 4 :name "Jenn" :out 0 :tribe "nagarote"}
                        {:id 5 :name "Joaquin" :out 0 :tribe "masaya"}
                        {:id 6 :name "Joe" :out 0 :tribe "nagarote"}
                        {:id 7 :name "Kelly" :out 0 :tribe "escameca"}
                        {:id 8 :name "Lindsey" :out 0 :tribe "escameca"}
                        {:id 9 :name "Max" :out 0 :tribe "masaya"}
                        {:id 10 :name "Mike" :out 0 :tribe "escameca"}
                        {:id 11 :name "Nina" :out 0 :tribe "nagarote"}
                        {:id 12 :name "Rodney" :out 0 :tribe "escameca"}
                        {:id 13 :name "Shirin" :out 0 :tribe "masaya"}
                        {:id 14 :name "Sierra" :out 0 :tribe "escameca"}
                        {:id 15 :name "So" :out 0 :tribe "masaya"}
                        {:id 16 :name "Tyler" :out 0 :tribe "masaya"}
                        {:id 17 :name "Vince" :out 0 :tribe "nagarote"}
                        {:id 18 :name "Will" :out 0 :tribe "nagarote"}
                        ]))
(def voted-out-list (atom [
                           ]))

(def entries (atom [
                    {:id 1 :name "phil" :points 0 :order [15 8 5 11 3 2 12 16 18 13 1 6 7 10 9 4 17 14]}
                    {:id 2 :name "will" :points 0 :order [8 5 11 2 15 18 3 1 12 16 7 14 17 6 13 10 9 4]}
                    ]))

(def selected-entry (atom 1))

;; -------------------------
;; To read the players when sorted
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

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

(defn entries-display [entries selected-entry]
  [:div.column
   [:h3 "Entries"]
   [:select {:on-change #(swap! selected-entry (fn [] (int (-> % .-target .-value))))}
    (for [entry entries]
      ^{:key (:id entry)} [:option {:value (:id entry)}
                           (:name entry)])]
   [lister (create-contestant-list entries @selected-entry @contestants) "selected-entry"]])

(defn home []
  (let [entries (calculate-points-for-entries @entries @voted-out-list)]
    [:div [:h1 "Phil & Will's Survivor World's Apart  Boot List"]
     [leader-board entries @contestants]
     [entries-display entries selected-entry]
     [:div.column
      [:h3 "Booted"]
      [lister (create-placed-list  (filter #(true? (:locked %)) @contestants)) "placed"]]
     [:div.column
      [:h3 "Players"]
      [lister (filter #(= 0 (:out %)) @contestants) "contestants"]]]))

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


(defn home-component []
  (reagent/create-class {:render home
                         :component-did-mount home-did-mount}))
;; -------------------------
;; Initialize app
(defn init! []
  (reagent/render-component [home-component] (.getElementById js/document "app")))
