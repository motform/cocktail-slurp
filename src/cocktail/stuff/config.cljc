(ns cocktail.stuff.config)

(def dev
  {:datomic {:uri "datomic:mem://cocktail.slurp"
             :posts "posts.edn"}
   :http {:port 3232}})

(def env
  dev)

