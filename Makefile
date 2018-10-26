gradlew-clean-build:
	./gradlew --no-daemon clean build

docker-compose-up:
	docker-compose up -d

docker-compose-build: gradlew-clean-build
	docker-compose up -d --build

docker-compose-stop:
	docker-compose stop

docker-compose-stop-immediately:
	docker-compose stop -t 0

docker-compose-down:
	docker-compose down

docker-compose-down-immediately:
	docker-compose down -t 0

docker-ps:
	docker ps -a

docker-stats:
	docker stats

docker-image-prune:
	docker image prune

docker-build-k8s: gradlew-clean-build
	docker build -t localhost:5000/frontend -f ./armeria-sandbox-frontend/Dockerfile.production ./armeria-sandbox-frontend
	docker build -t localhost:5000/backend1 -f ./armeria-sandbox-backend1/Dockerfile.production ./armeria-sandbox-backend1
	docker build -t localhost:5000/backend2 -f ./armeria-sandbox-backend2/Dockerfile.production ./armeria-sandbox-backend2
	docker build -t localhost:5000/backend3 -f ./armeria-sandbox-backend3/Dockerfile.production ./armeria-sandbox-backend3
	docker build -t localhost:5000/backend4 -f ./armeria-sandbox-backend4/Dockerfile.production ./armeria-sandbox-backend4

docker-push: docker-build-k8s
	docker push localhost:5000/frontend
	docker push localhost:5000/backend1
	docker push localhost:5000/backend2
	docker push localhost:5000/backend3
	docker push localhost:5000/backend4

kubectl-rollout: docker-push
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

kubectl-create-depends:
	kubectl create -f ./k8s/centraldogma.yml
	kubectl create -f ./k8s/zipkin.yml
	kubectl create -f ./k8s/prometheus.yml

kubectl-delete-depends:
	kubectl delete -f ./k8s/centraldogma.yml
	kubectl delete -f ./k8s/zipkin.yml
	kubectl delete -f ./k8s/prometheus.yml

kubectl-create-apps:
	kubectl create -f ./armeria-sandbox-backend1/k8s.yml
	kubectl create -f ./armeria-sandbox-backend2/k8s.yml
	kubectl create -f ./armeria-sandbox-backend3/k8s.yml
	kubectl create -f ./armeria-sandbox-backend4/k8s.yml
	kubectl create -f ./armeria-sandbox-frontend/k8s.yml

kubectl-delete-apps:
	kubectl delete -f ./armeria-sandbox-backend1/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend2/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend3/k8s.yml
	kubectl delete -f ./armeria-sandbox-backend4/k8s.yml
	kubectl delete -f ./armeria-sandbox-frontend/k8s.yml

kubectl-get:
	kubectl get deployment -o wide
	kubectl get svc -o wide
	kubectl get pod -o wide
