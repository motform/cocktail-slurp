(ns cocktail.stuff.config)

(def dev
  {:datomic {:uri "datomic:mem://cocktail.slurp/dev-server"
             :schema "resources/edn/cocktail-schema.edn"
             :posts "posts.edn"}
   :http {:port 3232}})

(def prod
  {:datomic {:uri "datomic:mem://cocktail.slurp/prod"
             :schema "resources/edn/cocktail-schema.edn"
             :posts "resources/edn/posts.edn"}
   :http {:port 3000}})

(def env
  (case (System/getenv "ENV")
    "PROD" prod
    dev))
