gradlew-clean-build:
	./gradlew --no-daemon clean build

build-with-docker:
	docker run --rm -v "$(HOME)"/.gradle:/root/.gradle -v "$(PWD)":/root/armeria-sandbox -w /root/armeria-sandbox bellsoft/liberica-openjdk-centos:11.0.3 bash -c "./gradlew --no-daemon clean build"

docker-ps:
	docker ps -a

docker-stats:
	docker stats

docker-image-prune:
	docker image prune

docker-build-kubernetes-dev:
	docker build -t localhost:5000/armeria-sandbox-job-kubernetes:latest -f ./armeria-sandbox-job-kubernetes/Dockerfile.dev ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend:latest -f ./armeria-sandbox-frontend/Dockerfile.dev ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1:latest -f ./armeria-sandbox-backend1/Dockerfile.dev ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2:latest -f ./armeria-sandbox-backend2/Dockerfile.dev ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3:latest -f ./armeria-sandbox-backend3/Dockerfile.dev ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4:latest -f ./armeria-sandbox-backend4/Dockerfile.dev ./armeria-sandbox-backend4

docker-build-kubernetes-production:
	docker build -t localhost:5000/armeria-sandbox-job-kubernetes -f ./armeria-sandbox-job-kubernetes/Dockerfile.production ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend:latest -f ./armeria-sandbox-frontend/Dockerfile.production ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1:latest -f ./armeria-sandbox-backend1/Dockerfile.production ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2:latest -f ./armeria-sandbox-backend2/Dockerfile.production ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3:latest -f ./armeria-sandbox-backend3/Dockerfile.production ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4:latest -f ./armeria-sandbox-backend4/Dockerfile.production ./armeria-sandbox-backend4

docker-push:
	docker push localhost:5000/armeria-sandbox-job-kubernetes:latest
	docker push localhost:5000/armeria-sandbox-frontend:latest
	docker push localhost:5000/armeria-sandbox-backend1:latest
	docker push localhost:5000/armeria-sandbox-backend2:latest
	docker push localhost:5000/armeria-sandbox-backend3:latest
	docker push localhost:5000/armeria-sandbox-backend4:latest

kubectl-create-infra:
	kubectl apply -f ./manifests/infra -R

kubectl-delete-infra:
	kubectl delete -f ./manifests/infra -R

kubectl-create-infra-data:
	$(eval CENTRAL_DOGMA_POD := $(shell kubectl get pod | grep '^centraldogma-' | awk '{print $$1}'))
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "curl -X POST -H 'authorization: bearer anonymous' -H 'Content-Type: application/json' -d '{\"name\": \"armeriaSandbox\"}' http://localhost:36462/api/v1/projects"
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "curl -X POST -H 'authorization: bearer anonymous' -H 'Content-Type: application/json' -d '{\"name\": \"apiServers\"}' http://localhost:36462/api/v1/projects/armeriaSandbox/repos"
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "curl -X POST -H 'authorization: bearer anonymous' -H 'Content-Type: application/json' -d '{\"commitMessage\": {\"summary\": \"Add initial throttling.json\", \"detail\": {\"content\": \"\", \"markup\": \"PLAINTEXT\"}}, \"file\": {\"name\": \"throttling.json\", \"type\": \"TEXT\", \"content\": \"{\\\"backend1\\\": {\\\"ratio\\\": 1}, \\\"backend2\\\": {\\\"ratio\\\": 1}, \\\"backend3\\\": {\\\"ratio\\\": 1}, \\\"backend4\\\": {\\\"ratio\\\": 1}}\", \"path\": \"/throttling.json\"}}' http://localhost:36462/api/v0/projects/armeriaSandbox/repositories/apiServers/files/revisions/head"

kubectl-create-apps:
	kubectl apply -f ./manifests/app -R

kubectl-delete-apps:
	kubectl delete -f ./manifests/app -R

kubectl-get:
	kubectl get deployment -o wide
	kubectl get svc -o wide
	kubectl get pod -o wide
