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

docker-build-kubernetes: gradlew-clean-build
	docker build -t localhost:5000/armeria-sandbox-job -f ./armeria-sandbox-job-kubernetes/Dockerfile.production ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend -f ./armeria-sandbox-frontend/Dockerfile.production ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1 -f ./armeria-sandbox-backend1/Dockerfile.production ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2 -f ./armeria-sandbox-backend2/Dockerfile.production ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3 -f ./armeria-sandbox-backend3/Dockerfile.production ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4 -f ./armeria-sandbox-backend4/Dockerfile.production ./armeria-sandbox-backend4

docker-push: docker-build-kubernetes
	docker push localhost:5000/armeria-sandbox-job
	docker push localhost:5000/armeria-sandbox-frontend
	docker push localhost:5000/armeria-sandbox-backend1
	docker push localhost:5000/armeria-sandbox-backend2
	docker push localhost:5000/armeria-sandbox-backend3
	docker push localhost:5000/armeria-sandbox-backend4

kubectl-rollout: docker-push
	kubectl set image deployment/armeria-sandbox-job armeria-sandbox-job=localhost:5000/armeria-sandbox-job:latest
	kubectl rollout status deployment/armeria-sandbox-job
	kubectl set image deployment/armeria-sandbox-backend1 armeria-sandbox-backend1=localhost:5000/armeria-sandbox-backend1:latest
	kubectl rollout status deployment/armeria-sandbox-backend1
	kubectl set image deployment/armeria-sandbox-backend2 armeria-sandbox-backend2=localhost:5000/armeria-sandbox-backend2:latest
	kubectl rollout status deployment/armeria-sandbox-backend2
	kubectl set image deployment/armeria-sandbox-backend3 armeria-sandbox-backend3=localhost:5000/armeria-sandbox-backend3:latest
	kubectl rollout status deployment/armeria-sandbox-backend3
	kubectl set image deployment/armeria-sandbox-backend4 armeria-sandbox-backend4=localhost:5000/armeria-sandbox-backend4:latest
	kubectl rollout status deployment/armeria-sandbox-backend4
	kubectl set image deployment/armeria-sandbox-frontend armeria-sandbox-frontend=localhost:5000/armeria-sandbox-frontend:latest
	kubectl rollout status deployment/armeria-sandbox-frontend

kubectl-create-depends:
	kubectl create -f ./kubernetes/centraldogma.yml
	kubectl create -f ./kubernetes/zipkin.yml
	kubectl create -f ./kubernetes/prometheus.yml

kubectl-delete-depends:
	kubectl delete -f ./kubernetes/centraldogma.yml
	kubectl delete -f ./kubernetes/zipkin.yml
	kubectl delete -f ./kubernetes/prometheus.yml

kubectl-create-apps:
	kubectl create -f ./armeria-sandbox-job-kubernetes/kubernetes.yml
	kubectl create -f ./armeria-sandbox-backend1/kubernetes.yml
	kubectl create -f ./armeria-sandbox-backend2/kubernetes.yml
	kubectl create -f ./armeria-sandbox-backend3/kubernetes.yml
	kubectl create -f ./armeria-sandbox-backend4/kubernetes.yml
	kubectl create -f ./armeria-sandbox-frontend/kubernetes.yml

kubectl-delete-apps:
	kubectl delete -f ./armeria-sandbox-job-kubernetes/kubernetes.yml
	kubectl delete -f ./armeria-sandbox-backend1/kubernetes.yml
	kubectl delete -f ./armeria-sandbox-backend2/kubernetes.yml
	kubectl delete -f ./armeria-sandbox-backend3/kubernetes.yml
	kubectl delete -f ./armeria-sandbox-backend4/kubernetes.yml
	kubectl delete -f ./armeria-sandbox-frontend/kubernetes.yml

kubectl-get:
	kubectl get deployment -o wide
	kubectl get svc -o wide
	kubectl get pod -o wide
