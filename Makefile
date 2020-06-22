APP=app
SERVER=cocktail-slurp
NAME=motform/$(SERVER)
JAR=target/$(SERVER).jar

docker: clean cljs-compile uberjar container
jar: clean cljs-compile uberjar runjar

cljs-compile:
	@npx shadow-cljs compile $(APP)

uberjar:
	@clojure -A:depstar -m hf.depstar.uberjar $(JAR) -S

runjar:
	@java -cp $(JAR) clojure.main -m cocktail.slurp.server

container:
	@docker build --tag $(NAME) .

drun:
	@docker run --rm --publish 3000:3000 --detach --name cs $(NAME)

dstop:
	@docker stop cs

clean:
	@rm -rf target/ -q

# HACK there is most likely a better way of doing this
dev:
	npx shadow-cljs watch $(APP) & clojure -A:server

.PHONY: dev
