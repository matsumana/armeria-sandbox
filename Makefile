rebuild-jar:
	./gradlew clean build

up:
	docker-compose up -d

reload: rebuild-jar
	docker-compose up -d --build

stop:
	docker-compose stop

stop-immediately:
	docker-compose stop -t 0

down:
	docker-compose down

down-immediately:
	docker-compose down -t 0

ps:
	docker ps -a

stats:
	docker stats

prune-images:
	docker image prune
