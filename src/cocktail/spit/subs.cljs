(ns cocktail.spit.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :active-page
 (fn [db _]
   (:active-page db)))

(reg-sub
 :menu-cocktails
 (fn [db _]
   (get-in db [:collections :menu :cocktails])))

(reg-sub
 :library-cocktails
 (fn [db _]
   (get-in db [:collections :library :cocktails])))

(reg-sub
 :strainer
 (fn [db _]
   (select-keys (:strainer db) [:ingredients :search])))

;; TODO remove?
(reg-sub
 :strainer-ingredients
 (fn [db _]
   (get-in db [:strainer :ingredients])))

(reg-sub
 :strainer-search
 (fn [db _]
   (get-in db [:strainer :search])))

(reg-sub
 :meta-all
 (fn [db _ attribute]
   (get-in db [:meta attribute])))

(reg-sub
 :strained-cocktails
 (fn [db _]
   (get-in db [:strainer :cocktails])))

(reg-sub
 :active-cocktail
 (fn [db _]
   (:active-cocktail db)))
