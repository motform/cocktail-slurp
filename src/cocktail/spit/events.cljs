(ns cocktail.spit.events
  (:require [ajax.core :as ajax]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [cocktail.spit.db :as db]
            [cocktail.stuff.util :as util]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx path after debug]]))

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
   (cond-> {:db (assoc db :active-page page)
            :title nil}
     (= :cocktail page) (assoc :scroll [0 0])))) ;; NOTE

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

;;; Collection

(reg-event-db
 :collection-conj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection cocktail]]
   (update-in db [:collections collection :cocktails] conj cocktail)))

(reg-event-db
 :collection-disj
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection cocktail]]
   (update-in db [:collections collection :cocktails] disj cocktail)))

(reg-event-db
 :collection-clear
 [spec-interceptor local-storage-interceptor]
 (fn [db [_ collection]]
   (assoc-in db [:collections collection :cocktails] #{})))

;;; Strainer

(reg-event-db
 :strainer-conj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] conj (str/lower-case v))))

(reg-event-db
 :strainer-disj
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] disj v)))

(reg-event-db
 :strainer-toggle
 [spec-interceptor]
 (fn [db [_ k v]]
   (update-in db [:strainer k] util/toggle v)))

(reg-event-db
 :strainer-search
 [spec-interceptor]
 (fn [db [_ search]]
   (assoc-in db [:strainer :search] search)))

(reg-event-db
 :strainer-clear
 [spec-interceptor]
 (fn [db [_]]
   (assoc db :strainer {:kind #{} :cocktails #{} :ingredients #{} :collection #{} :search ""})))

(reg-event-fx
 :strain-cocktails
 (fn [{:keys [db]} [_ data]]
   (let [_ (println data)]
     {:db (assoc db :ajax-test true) ;; NOTE
      :http-xhrio {:method :post
                   :uri (->uri "bartender/strain")
                   :timeout 8000
                   :body data
                   :format (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format {:keywords? true})
                   :on-success [:success-strained-cocktails]
                   :on-failure [:failure-http]}})))

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

