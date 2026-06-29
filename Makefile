IMAGE_NAME=feature-flag-service
CONTAINER_NAME=feature-flag-service

build:
	docker build -t $(IMAGE_NAME) .

up:
	docker-compose up -d
	down:
	docker-compose down

logs:
	docker logs -f $(CONTAINER_NAME)

ps:
	docker-compose ps

restart: down up

clean:
	docker-compose down --rmi all -v --remove-orphans

rebuild: clean build up