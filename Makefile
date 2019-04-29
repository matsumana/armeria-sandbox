gradlew-clean-build:
	./gradlew --no-daemon clean build

build-with-docker:
	docker run --rm -v "$(HOME)"/.gradle:/root/.gradle -v "$(PWD)":/root/armeria-sandbox -w /root/armeria-sandbox azul/zulu-openjdk-debian:11.0.3 bash -c "./gradlew --no-daemon clean build"

docker-ps:
	docker ps -a

docker-stats:
	docker stats

docker-image-prune:
	docker image prune

docker-build-kubernetes-dev:
	docker build -t localhost:5000/armeria-sandbox-job-kubernetes -f ./armeria-sandbox-job-kubernetes/Dockerfile.dev ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend -f ./armeria-sandbox-frontend/Dockerfile.dev ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1 -f ./armeria-sandbox-backend1/Dockerfile.dev ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2 -f ./armeria-sandbox-backend2/Dockerfile.dev ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3 -f ./armeria-sandbox-backend3/Dockerfile.dev ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4 -f ./armeria-sandbox-backend4/Dockerfile.dev ./armeria-sandbox-backend4

docker-build-kubernetes-production:
	docker build -t localhost:5000/armeria-sandbox-job-kubernetes -f ./armeria-sandbox-job-kubernetes/Dockerfile.production ./armeria-sandbox-job-kubernetes
	docker build -t localhost:5000/armeria-sandbox-frontend -f ./armeria-sandbox-frontend/Dockerfile.production ./armeria-sandbox-frontend
	docker build -t localhost:5000/armeria-sandbox-backend1 -f ./armeria-sandbox-backend1/Dockerfile.production ./armeria-sandbox-backend1
	docker build -t localhost:5000/armeria-sandbox-backend2 -f ./armeria-sandbox-backend2/Dockerfile.production ./armeria-sandbox-backend2
	docker build -t localhost:5000/armeria-sandbox-backend3 -f ./armeria-sandbox-backend3/Dockerfile.production ./armeria-sandbox-backend3
	docker build -t localhost:5000/armeria-sandbox-backend4 -f ./armeria-sandbox-backend4/Dockerfile.production ./armeria-sandbox-backend4

docker-push:
	docker push localhost:5000/armeria-sandbox-job-kubernetes
	docker push localhost:5000/armeria-sandbox-frontend
	docker push localhost:5000/armeria-sandbox-backend1
	docker push localhost:5000/armeria-sandbox-backend2
	docker push localhost:5000/armeria-sandbox-backend3
	docker push localhost:5000/armeria-sandbox-backend4

kubectl-rollout: docker-push
	kubectl set image deployment/armeria-sandbox-job-kubernetes armeria-sandbox-job-kubernetes=localhost:5000/armeria-sandbox-job-kubernetes:latest
	kubectl rollout status deployment/armeria-sandbox-job-kubernetes
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
	kubectl apply -f ./kubernetes/centraldogma.yml
	kubectl apply -f ./kubernetes/zipkin.yml
	kubectl apply -f ./kubernetes/prometheus.yml

kubectl-delete-depends:
	kubectl delete -f ./kubernetes/centraldogma.yml
	kubectl delete -f ./kubernetes/zipkin.yml
	kubectl delete -f ./kubernetes/prometheus.yml

kubectl-create-depends-data:
	$(eval CENTRAL_DOGMA_POD := $(shell kubectl get pod | grep '^centraldogma-' | awk '{print $$1}'))
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "dogma --connect=localhost:36462 new armeriaSandbox"
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "dogma --connect=localhost:36462 new armeriaSandbox/apiServers"
	kubectl exec -it $(CENTRAL_DOGMA_POD) -- /bin/bash -c "echo '{\"backend1\": {\"ratio\": 1}, \"backend2\": {\"ratio\": 1}, \"backend3\": {\"ratio\": 1}, \"backend4\": {\"ratio\": 1}, \"frontend\": {\"ratio\": 1}}' > /tmp/throttling.json && dogma --connect=localhost:36462 put armeriaSandbox/apiServers /tmp/throttling.json -m 'Add initial throttling.json'"

kubectl-create-apps:
	kubectl apply -f ./armeria-sandbox-job-kubernetes/kubernetes.yml
	kubectl apply -f ./armeria-sandbox-backend1/kubernetes.yml
	kubectl apply -f ./armeria-sandbox-backend2/kubernetes.yml
	kubectl apply -f ./armeria-sandbox-backend3/kubernetes.yml
	kubectl apply -f ./armeria-sandbox-backend4/kubernetes.yml
	kubectl apply -f ./armeria-sandbox-frontend/kubernetes.yml

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
