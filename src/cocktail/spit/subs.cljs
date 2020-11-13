(ns cocktail.spit.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 ::route
 (fn [db _]
   (:route db)))

(reg-sub
 :collection/cocktails
 (fn [db [_ collection]]
   (get-in db [:collections collection])))

(reg-sub
 :strainer/keys
 (fn [db [_ ks]]
   (select-keys (:strainer db) ks)))

(reg-sub
 :strainer/all
 (fn [db _]
   (:strainer db)))

(reg-sub
 :cocktails/strained
 (fn [{:keys  [cocktails]
       {:keys [collection]}   :strainer
       {:keys [menu library]} :collections} _]
   (cond->> cocktails
     (:menu    collection) (filter menu)
     (:library collection) (filter library))))

(reg-sub
 :cocktails/cursor
 (fn [db _]
   (:cursor db)))

(reg-sub
 :meta/ingredent
 (fn [db _]
   (get-in db [:meta :ingredient])))

(reg-sub
 :cocktail/active
 (fn [db _]
   (:active-cocktail db)))
