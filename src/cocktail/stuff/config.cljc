(ns cocktail.stuff.config)

(def dev
  {:datomic {:uri "datomic:mem://cocktail.slurp/prod"
             :schema "resources/edn/cocktail-schema.edn"
             :posts "posts.edn"}
   :http {:port 3232}})

(def env
  dev)

