(ns cocktail.spit.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx path after debug]]
            [ajax.core :as ajax]
            [clojure.spec.alpha :as s]
            [cocktail.spit.db :as db]
            [cocktail.spit.routes :as routes]
            [cocktail.stuff.util :as util]
            [clojure.string :as str]))

;;;; Helpers

(defn ->uri [route]
  (let [host (.. js/window -location -host)]
    (str "http://" host "/" route)))

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
 :initialize-db
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
 :active-page
 [spec-interceptor]
 (fn [{:keys [db]} [_ page]]
   (let [page-name (routes/titles page)]
     (cond-> {:db (assoc db :active-page page)
              :title page-name}
       (= :cocktail page) (assoc :scroll [0 0])))))

(reg-event-fx
 :cocktail-title
 (fn [_ [_ cocktail-title]]
   {:title cocktail-title}))

(reg-fx
 :scroll
 (fn [[x y]]
   (. js/window scrollTo x y)))

(reg-event-fx
 :scroll-to
 (fn _ [_ points]
   {:scroll points}))

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
 :strainer-conj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] conj (str/lower-case v))))

(reg-event-db
 :strainer-search
 [spec-interceptor]
 (fn [db [_ search]]
   (assoc-in db [:strainer :search] search)))

(reg-event-db
 :strainer-disj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] disj v)))

(reg-event-db
 :strainer-clear
 [spec-interceptor]
 (fn [db [_]]
   (assoc db :strainer {:cocktails #{} :ingredients #{} :search #{}})))

(reg-event-fx
 :strain-cocktails
 (fn [{:keys [db]} [_ data]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :post
                 :uri (->uri "bartender/strain")
                 :timeout 8000
                 :body data
                 :format (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-strained-cocktails]
                 :on-failure [:failure-http]}}))

(reg-event-db
 :success-strained-cocktails
 (fn [db [_ result]]
   (assoc-in db [:strainer :cocktails] result)))

;;; Cocktail

(reg-event-fx
 :cocktail-by-id
 (fn [_ [_ id]]
   {:http-xhrio {:method :get
                 :uri (->uri "bartender/cocktail")
                 :params {:id id}
                 :body ""
                 :timeout 8000
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-cocktail-by-id]
                 :on-failure [:failure-http]}}))

(reg-event-fx
 :success-cocktail-by-id
 (fn [{:keys [db]} [_ result]]
   {:db (assoc db :active-cocktail result)
    :title (:title result)}))

;;; Meta

(reg-event-fx
 :meta-all
 (fn [{:keys [db]} [_ attribute]]
   {:db (assoc db :ajax-test true) ;; NOTE
    :http-xhrio {:method :get
                 :uri (->uri "bartender/all")
                 :params {:attribute attribute}
                 :body ""
                 :timeout 8000
                 :response-format (ajax/transit-response-format {:keywords? true})
                 :on-success [:success-meta-all (keyword attribute)]
                 :on-failure [:failure-http]}}))

(reg-event-db
 :success-meta-all
 (fn [db [_ attribute result]]
   (assoc-in db [:meta attribute] result)))

;;; Ajax helpers

(reg-event-db
 :success-http
 (fn [db [_ result]]
   (assoc db :success-http result)))

(reg-event-db
 :failure-http
 (fn [db [_ result]]
   (assoc db :failure-http result)))

