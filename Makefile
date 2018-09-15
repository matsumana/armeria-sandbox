rebuild-jar:
	./gradlew clean build

up:
	docker-compose up -d

reload: rebuild-jar
	docker-compose up -d --build

down:
	docker-compose down

ps:
	docker ps -a
