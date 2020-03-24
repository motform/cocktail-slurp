(ns cocktail.spit.components.header
  (:require [re-frame.core :as rf]
            [cocktail.spit.routes :as routes]))

(declare title menu library cocktails about)

(defn main []
  [:header
   [title] [cocktails] [menu] [library] #_[about]])

(defn title []
  [:a#title.header-btn
   {:href (routes/url-for :cocktails)} "cocktail slurp"])

(defn cocktails []
  [:a.header-btn.header-cocktails
   {:style {:margin-left "auto"}
    :href (routes/url-for :cocktails)} "cocktails"])

(defn menu []
  [:a.header-btn.header-menu
   {:href (routes/url-for :menu)} "menu"])

(defn library []
  [:a.header-btn.header-library
   {:href (routes/url-for :library)} "library"])

(defn about []
  [:a.header-btn
   {:href (routes/url-for :about)} "?"])