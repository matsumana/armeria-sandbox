rebuild-jar:
	./gradlew --no-daemon clean build

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

docker-build-k8s: rebuild-jar
	docker build -t localhost:5000/frontend -f ./armeria-sandbox-frontend/Dockerfile.production ./armeria-sandbox-frontend
	docker build -t localhost:5000/backend1 -f ./armeria-sandbox-backend1/Dockerfile.production ./armeria-sandbox-backend1
	docker build -t localhost:5000/backend2 -f ./armeria-sandbox-backend2/Dockerfile.production ./armeria-sandbox-backend2
	docker build -t localhost:5000/backend3 -f ./armeria-sandbox-backend3/Dockerfile.production ./armeria-sandbox-backend3
	docker build -t localhost:5000/backend4 -f ./armeria-sandbox-backend4/Dockerfile.production ./armeria-sandbox-backend4

docker-push:
	docker push localhost:5000/frontend
	docker push localhost:5000/backend1
	docker push localhost:5000/backend2
	docker push localhost:5000/backend3
	docker push localhost:5000/backend4

k8s-rollout:
	kubectl set image deployment/backend1 backend1=localhost:5000/backend1:latest
	kubectl rollout status deployment/backend1
	kubectl set image deployment/backend2 backend2=localhost:5000/backend2:latest
	kubectl rollout status deployment/backend2
	kubectl set image deployment/backend3 backend3=localhost:5000/backend3:latest
	kubectl rollout status deployment/backend3
	kubectl set image deployment/backend4 backend4=localhost:5000/backend4:latest
	kubectl rollout status deployment/backend4
	kubectl set image deployment/frontend frontend=localhost:5000/frontend:latest
	kubectl rollout status deployment/frontend

k8s-create:
	kubectl create -f ./armeria-sandbox-backend1/k8s.yml
	kubectl create -f ./armeria-sandbox-backend2/k8s.yml
	kubectl create -f ./armeria-sandbox-backend3/k8s.yml
	kubectl create -f ./armeria-sandbox-backend4/k8s.yml
	kubectl create -f ./armeria-sandbox-frontend/k8s.yml
	kubectl create -f ./k8s/k8s-zipkin.yml

k8s-delete:
	kubectl delete -f ./armeria-sandbox-backend1/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend2/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend3/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend4/k8s.yml
	kubectl delete -f ./armeria-sandbox-frontend/k8s.yml
	kubectl delete -f ./k8s/k8s-zipkin.yml

k8s-info:
	kubectl get deployment
	echo
	kubectl get svc
	echo
	kubectl get pod
