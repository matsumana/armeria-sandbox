# armeria-sandbox

## How to deploy this app on Kubernetes

This application can be started easily on Kubernetes.  
Only `Docker for Mac with Kubernetes` is tested for now.

ref: [Get started with Docker for Mac - Kubernetes](https://docs.docker.com/docker-for-mac/#kubernetes)

### preparation

#### launch local Docker registry

```
$ docker run -d -p 5000:5000 --name registry registry:2.6
```

#### launch dependent Docker containers

```
$ make kubectl-create-depends
```

#### generate initial project, repository data in Central Dogma

```
$ make kubectl-create-depends-data
```

<br>
<br>

### deploy to Kubernetes

#### build the app

```
$ ./gradlew --no-daemon clean build
```

or

```
$ make build-with-docker
```

#### build Docker images

```
$ make docker-build-kubernetes-dev
```

or

```
$ make docker-build-kubernetes-production
```

#### push Docker images to local Docker registry

```
$ make docker-push
```

#### deploy the app

```
$ make kubectl-create-apps
```

<br>
<br>

## How to dispose

```
$ make kubectl-delete-apps
$ make kubectl-delete-depends
$ docker stop registry
$ docker rm registry
```

<br>
<br>

---

## URLs

- [Prometheus' scraping targets](http://localhost:30000/targets)
- [Armeria's CircuitBreaker metrics](http://localhost:30000/graph?g0.range_input=1h&g0.expr=armeria_client_circuitBreaker_requests&g0.tab=0&g1.range_input=1h&g1.expr=irate(armeria_client_circuitBreaker_transitions_total%5B1m%5D)&g1.tab=0&g2.range_input=1h&g2.expr=irate(armeria_client_circuitBreaker_rejectedRequests_total%5B1m%5D)&g2.tab=0)
- [Armeria's Client EndpointGroup metrics](http://localhost:30000/graph?g0.range_input=1h&g0.expr=armeria_client_endpointGroup_count&g0.tab=0&g1.range_input=1h&g1.expr=armeria_client_endpointGroup_healthy&g1.tab=0)
- [Zipkin](http://localhost:30001/zipkin/)
- [Central Dogma](http://localhost:30002/#/armeriaSandbox/apiServers)
- [App frontend](http://localhost:31000/hello/foo)
