(ns survivor-bootlist.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljsjs.react :as react]))

;; -------------------------
;; State

(def contestants (atom [
                        {:id 1 :name "Carolyn" :out 17 :tribe "masaya"}
                        {:id 2 :name "Dan" :out 18 :tribe "escameca"}
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

(def entries (atom [
                    {:id 1 :name "phil" :points 0 :order [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18]}
                    {:id 2 :name "will" :points 0 :order [2 1 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18]}
                    {:id 3 :name "ciwchris" :points 0 :order [3 1 2 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18]}
                    {:id 11 :name "last" :points 0 :order [3 1 2 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18]}
                    ]))

(def selected-entry (atom 1))

;; -------------------------
;; Views

(defn create-class [contestant]
  (let [default-class ["ui-state-default" (:tribe contestant)]
        class (if (= 0 (:out contestant))
                default-class
                (into default-class ["ui-state-disabled"]))]
    {:class (clojure.string/join " " class)}))

(defn lister [items name]
  [:ul {:id name :class "connectedSortable"}
   (for [item items]
     ^{:key (:id item)} [:li (create-class item)
                         [:img {:src (str "/images/" (:name item) ".png")}]
                         [:div (:name item)]])])

(defn create-placed-list [placed]
  (into [] (sort #(compare (:out %2) (:out %1)) placed)))

(defn create-contestant-list [entries selected-entry contestants]
  (let [contestant-order (:order (first (filter #(= selected-entry (:id %)) entries)))]
    (map
     #(first (filter (fn [contestant] (= (:id contestant) %)) contestants))
     contestant-order)))

(defn entries-display [entries selected-entry]
    [:div
     [:select {:on-change #(swap! selected-entry (fn [] (int (-> % .-target .-value))))}
      (for [entry entries]
        ^{:key (:id entry)} [:option {:value (:id entry)}
                             (:name entry)])]
     [lister (create-contestant-list entries @selected-entry @contestants) "selected-entry"]])

(defn home []
  [:div [:h2 "Phil & Will's Survivor 30 Bootlist"]
   [entries-display @entries selected-entry]
   [lister (create-placed-list  (filter #(not= 0 (:out %)) @contestants)) "placed"]
   [lister (filter #(= 0 (:out %)) @contestants) "contestants"]])

(defn home-did-mount []
  (js/$ (fn []
          (.sortable (js/$ "#placed, #contestants") (clj->js {
                                                              :items "li:not(.ui-state-disabled)"
                                                              :connectWith ".connectedSortable"
                                                              }))
          (.disableSelection (js/$ "#placed")))))

(defn home-component []
  (reagent/create-class {:render home
                         :component-did-mount home-did-mount}))
;; -------------------------
;; Initialize app
(defn init! []
  (reagent/render-component [home-component] (.getElementById js/document "app")))
