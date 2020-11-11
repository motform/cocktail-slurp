(ns cocktail.spit.events
  (:require [ajax.core :as ajax]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [cocktail.spit.db :as db]
            [cocktail.stuff.util :as util]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx path after debug]]))

;; TODO namespace keys

;;;; Helpers

(defn ->uri
  "Used for running shadow-cljs in stand alone dev mode."
  [route]
  (if goog.DEBUG
    (str "http://localhost:3000" route)
    route))

;;;; Interceptors

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`.
  SOURCE: re-frame docs."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :cocktail.spit.db/db)))
(def spec-interceptor [check-spec-interceptor])

(def ->local-storage (after db/collections->local-storage))
(def local-storage-interceptor [->local-storage])

;;;; Default-db

(reg-event-fx
 :db/initialize
 [(inject-cofx :local-store-collections)]
 (fn [{:keys [local-store-collections]} _]
   {:db (util/?assoc db/default-db :collections local-store-collections)}))

;;;; Page Events

(reg-fx
 :title
 (fn [page-name]
   (let [separator (when page-name " | ")
         title (str "cocktail slurp" separator page-name)]
     (set! (.-title js/document) title))))

(reg-event-fx
 :page/active
 [spec-interceptor]
 (fn [{:keys [db]} [_ page]]
   (cond-> {:db (assoc db :active-page page)
            :title nil}
     (= :cocktail page) (assoc :scroll [0 0])))) ;; NOTE

;; (reg-event-fx
;;  :cocktail-title
;;  (fn [_ [_ cocktail-title]]
;;    {:title cocktail-title}))

(reg-fx
 :scroll
 (fn [[x y]]
   (. js/window scrollTo x y)))

;; (reg-event-fx
;;  :scroll-to
;;  (fn _ [_ points]
;;    {:scroll points}))

;;; Collection

(reg-event-db
 :collection/conj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection cocktail]]
   (update-in db [:collections collection :cocktails] conj cocktail)))

(reg-event-db
 :collection/disj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection cocktail]]
   (update-in db [:collections collection :cocktails] disj cocktail)))

(reg-event-db
 :collection/clear
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection]]
   (assoc-in db [:collections collection :cocktails] #{})))

;;; Strainer

(reg-event-db
 :strainer/conj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] conj (str/lower-case v))))

(reg-event-db
 :strainer/disj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] disj v)))

(reg-event-db
 :strainer/toggle
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] util/toggle v)))

(reg-event-db
 :strainer/search
 [spec-interceptor]
 (fn [db [_ search]]
   (assoc-in db [:strainer :search] search)))

(reg-event-db
 :strainer/clear
 [spec-interceptor]
 (fn [db [_]]
   (assoc db :strainer {:kind #{} :cocktails #{} :ingredients #{} :collection #{} :search ""})))

(reg-event-fx
 :strainer/request-cocktails
 (fn [{:keys [db]} [_ data]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :post
                 :uri (->uri "/bartender/strain")
                 :params data
                 :format (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success/strained-cocktails]
                 :on-failure [:http/failure]}}))

(reg-event-db
 :success/strained-cocktails
 (fn [db [_ result]]
   (assoc-in db [:strainer :cocktails] result)))

;;; Cocktail

(reg-event-fx
 :cocktail/by-id
 (fn [_ [_ id]]
   {:http-xhrio {:method :get
                 :uri (->uri (str "/bartender/cocktail/" id))
                 :format (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success/cocktail-by-id]
                 :on-failure [:http/failure]}}))

(reg-event-fx
 :success/cocktail-by-id
 (fn [{:keys [db]} [_ result]]
   {:db (assoc db :active-cocktail result)
    :title (:title result)}))

;;; Meta

(reg-event-fx
 :meta/all
 (fn [{:keys [db]} [_ attribute]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :get
                 :uri (->uri (str "/bartender/all/" attribute))
                 :format (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success/all (keyword attribute)]
                 :on-failure [:http/failure]}}))

(reg-event-db
 :success/all
 (fn [db [_ attribute result]]
   (assoc-in db [:meta attribute] result)))

;;; Ajax helpers

(reg-event-db
 :http/failure
 (fn [db [_ result]]
   (assoc db :http/failure result)))

