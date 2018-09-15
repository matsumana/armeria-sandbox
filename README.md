# armeria-sandbox

## How to build

```
$ ./gradlew clean build
```

## How to run dependent containers

The following the apps depend on Zipkin.

```
$ docker-compose up -d
```

## How to stop dependent containers

### stop

```
$ docker-compose stop
```

### dispose

```
$ docker-compose down
```

## How to run the apps

This repo includes 5 apps.

### Backend 1

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend1-*.jar
```

### Backend 2

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend2-*.jar
```

### Backend 3

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend3-*.jar
```

### Backend 4

```
$ java -jar ./armeria-sandbox-backend1/build/libs/armeria-sandbox-backend4-*.jar
```

### Frontend

```
$ java -jar ./armeria-sandbox-frontend/build/libs/armeria-sandbox-frontend-*.jar
```
