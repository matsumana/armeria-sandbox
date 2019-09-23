gradlew-clean-build:
	./gradlew --no-daemon clean build

build-with-docker:
	docker run --rm -v "$(HOME)"/.gradle:/root/.gradle -v "$(PWD)":/root/armeria-sandbox -w /root/armeria-sandbox bellsoft/liberica-openjdk-centos:11.0.4 bash -c "./gradlew --no-daemon clean build"

docker-ps:
	docker ps -a

docker-stats:
	docker stats

docker-image-prune:
	docker image prune

docker-build:
	docker build -t localhost:5000/armeria-sandbox-job-kubernetes:latest ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend:latest ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1:latest ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2:latest ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3:latest ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4:latest ./armeria-sandbox-backend4

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
	kustomize build manifests/overlays/dev | kubectl apply -f -

kubectl-delete-apps:
	kustomize build manifests/overlays/dev | kubectl delete -f -

kubectl-get:
	kubectl get deployment -o wide
	kubectl get svc -o wide
	kubectl get configMap -o wide
	kubectl get pod -o wide
