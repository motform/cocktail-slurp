(ns build
  (:require [clojure.tools.build.api :as build]))

(def lib       'org.motform.cocktail.slurp/core)
(def version    "1.0")
(def class-dir  "target/classes")
(def basis      (build/create-basis {:project "deps.edn"}))
(def uber-file  (format "target/%s-%s-standalone.jar" (name lib) version))
(def jar-file   (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (println "Cleaning /target")
  (build/delete {:path "target"}))

(defn jar [_]
  (println "Compiling jar.")
  (build/write-pom {:class-dir class-dir
                    :lib       lib
                    :version   version
                    :basis     basis
                    :src-dirs  ["src"]})
  (build/copy-dir {:src-dirs   ["src" "resources"]
                   :target-dir class-dir})
  (build/jar {:class-dir class-dir
              :jar-file  jar-file}))

(defn uber [_]
  (clean nil)
  (println "Compiling uberjar.")
  (build/copy-dir {:src-dirs   ["src" "resources"]
                   :target-dir class-dir})
  (build/compile-clj {:basis       basis
                      :src-dirs    ["src"]
                      :class-dir   class-dir
                      :ns-compile '[org.motform.cocktail.slurp.core]})
  (build/uber {:class-dir class-dir
               :uber-file uber-file
               :basis     basis})
  (println "Uberjar written to" uber-file))
