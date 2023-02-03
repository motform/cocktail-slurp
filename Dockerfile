# Based on: https://github.com/borkdude/fly_io_clojure

FROM clojure:openjdk-11-tools-deps-1.11.1.1113 AS builder

WORKDIR /opt
COPY . .

RUN clj -Sdeps '{:mvn/local-repo "./.m2/repository"}' -T:build uber

FROM openjdk:21-slim-buster AS runtime
COPY --from=builder /opt/target/core-1.0-standalone.jar /core.jar

EXPOSE 8090

ENTRYPOINT ["java", "-cp", "core.jar", "clojure.main", "-m", "org.motform.cocktail.slurp.core"]
