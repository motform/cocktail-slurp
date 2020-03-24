(ns cocktail.spit.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after debug]]
            [ajax.core :as ajax]
            [cognitect.transit :as t]
            [cocktail.spit.db :as db]
            [cocktail.spit.helpers :as helpers]))

;;;; Interceptors

(def check-spec-interceptor (after (partial helpers/check-and-throw :cocktail.spit.db/db)))
(def spec-interceptor [check-spec-interceptor])

(def ->local-storage (after db/collections->local-storage))
(def local-storage-interceptor [->local-storage])

;;;; Default-db

(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-collections)]
 (fn [{:keys [db local-store-collections] :as fx} _]
   {:db (helpers/?assoc db/default-db :collections local-store-collections)}))

;;;; Page Events

(reg-event-db
 :active-page
 spec-interceptor
 (fn [db [_ page]]
   (assoc db :active-page page)))

;;; Menu

(reg-event-db
 :menu-cocktails-conj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ cocktail]]
   (update-in db [:collections :menu :cocktails] conj cocktail)))

(reg-event-db
 :menu-cocktails-disj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ cocktail]]
   (update-in db [:collections :menu :cocktails] disj cocktail)))

;;; Library

(reg-event-db
 :library-cocktails-conj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ cocktail]]
   (update-in db [:collections :library :cocktails] conj cocktail)))

(reg-event-db
 :library-cocktails-disj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ cocktail]]
   (update-in db [:collections :library :cocktails] disj cocktail)))

(reg-event-db
 :library-clear
 [spec-interceptor local-storage-interceptor]
 (fn [db [_]]
   (assoc-in db [:collections :library :cocktails] #{})))

;;; Strainer

;; TODO add an interceptor/spec that checks for valid ingredients
(reg-event-db
 :strainer-ingredient-conj
 [spec-interceptor]
 (fn [db [_ ingredient]]
   (update-in db [:strainer :ingredients] conj ingredient)))

(reg-event-db
 :strainer-ingredient-disj
 [spec-interceptor]
 (fn [db [_ ingredient]]
   (update-in db [:strainer :ingredients] disj ingredient)))

(reg-event-db
 :strainer-clear
 [spec-interceptor]
 (fn [db [_]]
   (assoc-in db [:strainer :ingredients] #{})))

(reg-event-fx
 :cocktail-feed
 (fn [{:keys [db]} [_ cocktails]]
   {:db (assoc db :ajax-test true) ;; NOTE leaving this here as a reminder (for now)
    :http-xhrio {:method :get
                 :uri "http://localhost:3232/bartender/cocktails/feed"
                 :params {:cocktails cocktails}
                 :body ""
                 :timeout 8000
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-strained-cocktails]
                 :on-failure [:failure-http]}}))

(reg-event-db
 :success-strained-cocktails
 (fn [db [_ result]]
   (assoc-in db [:strainer :cocktails] result)))

(reg-event-fx
 :strain-cocktails
 (fn [{:keys [db]} [_ data]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :post
                 :uri "http://localhost:3232/bartender/strain"
                 :timeout 8000
                 :body data
                 :format (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-http]
                 :on-failure [:failure-http]}}))

;;; Cocktail

(reg-event-fx
 :cocktail-by-id
 (fn [{:keys [db]} [_ id]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :get
                 :uri "http://localhost:3232/bartender/cocktail"
                 :params {:id id}
                 :body ""
                 :timeout 8000
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-cocktail-by-id]
                 :on-failure [:failure-http]}}))

(reg-event-db
 :success-cocktail-by-id
 (fn [db [_ result]]
   (assoc db :active-cocktail result)))

;;; Ajax helpers

(reg-event-db
 :success-http
 (fn [db [_ result]]
   (assoc db :success-http result)))

(reg-event-db
 :failure-http
 (fn [db [_ result]]
   (assoc db :failure-http result)))

