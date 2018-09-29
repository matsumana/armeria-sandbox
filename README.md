# armeria-sandbox

<br>

## How to run in local

### How to run dependent containers

The following the apps depend on Zipkin.

```
$ docker-compose up -d zipkin
```

### How to stop dependent containers

#### stop

```
$ docker-compose stop zipkin
```

#### dispose

```
$ docker-compose down
```

### How to build the apps

```
$ ./gradlew clean build
```

### How to run the apps

This repo includes 5 apps.

#### Backend 1

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend1-*.jar
```

#### Backend 2

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend2-*.jar
```

#### Backend 3

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend3-*.jar
```

#### Backend 4

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend4-*.jar
```

#### Frontend

```
$ java -jar ./armeria-sandbox-frontend/build/libs/armeria-sandbox-frontend-*.jar
```

<br>

---

## How to run with Docker Compose

### How to build the apps

```
$ ./gradlew clean build
```

### How to run the apps

```
$ make up
```

### How to reload the apps

This command recreates the apps after that recreate containers.

```
$ make reload
```

### How to stop containers

#### stop

```
$ make stop
```

#### dispose

```
$ make down
```

<br>

---

## URLs

- [Zipkin](http://localhost:9411/zipkin/)
- [Prometheus' scraping targets](http://localhost:9090/targets)
- [Armeria's CircuitBreaker metrics](http://localhost:9090/graph?g0.range_input=1h&g0.expr=armeria_client_circuitBreaker_requests&g0.tab=0&g1.range_input=1h&g1.expr=irate(armeria_client_circuitBreaker_transitions_total%5B1m%5D)&g1.tab=0&g2.range_input=1h&g2.expr=irate(armeria_client_circuitBreaker_rejectedRequests_total%5B1m%5D)&g2.tab=0)
- [Armeria's Client EndpointGroup metrics](http://localhost:9090/graph?g0.range_input=1h&g0.expr=armeria_client_endpointGroup_count&g0.tab=0&g1.range_input=1h&g1.expr=armeria_client_endpointGroup_healthy&g1.tab=0)
