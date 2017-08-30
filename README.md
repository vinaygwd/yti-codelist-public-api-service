# CLS (Code List Service) - Public API Service microservice

This application is part of the [Joint metadata and information management programme](https://wiki.julkict.fi/julkict/yti).

## Description

This is the implementation of the Public API Service microservice for the Code List Service (CLS) with:

* [Spring boot] For getting things up and running
* Embedded [Jetty] to serve
* [Jersey 2] for JAX-RS

### Example queries:

Do a HTTP GET to public resource:
`http://localhost:9600/cls-api/api/v1/hello`

## Interface Documentation

When the microservice is running, you can get the Swagger REST API documentation from:
- [http://localhost:9600/cls-api/api/swagger.json](http://localhost:9600/cls-api/api/swagger.json)
- [http://localhost:9600/cls-api/swagger/index.html](http://localhost:9600/cls-api/swagger/index.html)

## Prerequisities

### Building
- Java 8+
- Maven 3.3+
- Docker

## Running

- [cls-config](https://github.com/vrk-yti/cls-config) - Default configuration for development use

## Starting service on local development environment

### Running inside IDE

Add the following Run configurations options:

- Program arguments: `--spring.profiles.active=default --spring.config.location=../cls-config/application.yml,../cls-config/cls-public-api-service.yml`
- Workdir: `$MODULE_DIR$`

Add folder for cls-project, application writes modified files there:

```bash
$ mkdir /data/cls
```


### Building the Docker Image

```bash
$ mvn clean package docker:build
```

### Running the Docker Image

```bash
$ docker run --rm -p 9600:9600 -p 19600:19600 -v /path/to/cls-config:/config --name=cls-public-api-service cls-public-api-service -a --spring.config.location=/config/application.yml,/config/cls-public-api-service.ym
```

.. or in [cls-compose](https://github.com/vrk-yti/cls-compose/) run

```bash
$ docker-compose up cls-public-api-service
```

[Spring boot]:http://projects.spring.io/spring-boot/
[Jetty]:http://www.eclipse.org/jetty/
[Jersey 2]:https://jersey.java.net
