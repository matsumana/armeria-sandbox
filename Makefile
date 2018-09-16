rebuild-jar:
	./gradlew clean build

up:
	docker-compose up -d

reload: rebuild-jar
	docker-compose up -d --build

down:
	docker-compose down

force-down:
	docker-compose down -t 0

ps:
	docker ps -a

stats:
	docker stats
