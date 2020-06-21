FROM adoptopenjdk/openjdk11:alpine

ENV ENV PROD

COPY target/cocktail-slurp.jar cocktail-slurp.jar
COPY resources resources

EXPOSE 3000
CMD java -cp cocktail-slurp.jar clojure.main -m cocktail.slurp.server

