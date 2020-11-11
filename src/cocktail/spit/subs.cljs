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
 :strainer/cocktails
 (fn [{{:keys [collection cocktails]} :strainer
       {:keys [menu library]} :collections} _]
   (cond->> cocktails
     (:menu collection)    (filter menu)
     (:library collection) (filter library))))

(reg-sub
 :meta/keys
 (fn [db [_ ks]]
   (select-keys (:meta db) ks)))

;; (reg-sub
;;  :strainer/cocktails
;;  (fn [db _]
;;    (get-in db [:strainer :cocktails])))

(reg-sub
 :cocktail/active
 (fn [db _]
   (:active-cocktail db)))
