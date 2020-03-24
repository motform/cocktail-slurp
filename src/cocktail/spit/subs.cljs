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
 :strainer-ingredients
 (fn [db _]
   (get-in db [:strainer :ingredients])))

(reg-sub
 :strained-cocktails
 (fn [db _]
   (get-in db [:strainer :cocktails])))

(reg-sub
 :active-cocktail
 (fn [db _]
   (:active-cocktail db)))
