(ns cocktail.spit.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :page/active
 (fn [db _]
   (:active-page db)))

;; (reg-sub
;;  :collection/cocktails
;;  (fn [db [_ collection]]
;;    (get-in db [:collections collection :cocktails])))

(reg-sub
 :strainer/keys
 (fn [db [_ ks]]
   (select-keys (:strainer db) ks)))

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
