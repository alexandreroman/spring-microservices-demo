# Spring Microservices Demo

This project shows how to write
[Spring Boot](http://spring.io/projects/spring-boot) microservices,
and how to manage these API endpoints with
[Spring Cloud Gateway](http://spring.io/projects/spring-cloud-gateway).

## How does it work?

Spring Cloud Gateway enables you to manage your microservices API
endpoints from a single location. When you are building a big application,
made of several microservices, you could use Spring Cloud Gateway
to control how your API endpoints are seen by your clients. Using this
configuration, you could easily refactor your microservices
(divide a big microservice in two, merge smaller ones, etc.), at your
own pace. If you start from a monolith app, and you want to incrementally
build microservices by extracting API endpoints, Spring Cloud Gateway
is your best friend.

You define routes by code using the
[Spring Cloud Gateway API](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.0.2.RELEASE/single/spring-cloud-gateway.html),
or by using a configuration file. This demo project is using the latter method.

Routes are defined in the application configuration file:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: echo
          uri: ${microservices.echo}
          predicates:
            - Path=/api/echo/{segment}

        - id: time
          uri: ${microservices.time}
          predicates:
            - Path=/api/time/{segment}
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  # How many requests per second do you want a user to be allowed to do?
                  replenish-rate: 1
                  # Maximum number of requests a user is allowed to do in a single second.
                  burst-capacity: 1

        - id: whoami
          uri: ${microservices.whoami}
          predicates:
            - Path=/api/whoami/{segment}

        - id: greeting
          uri: ${microservices.greeting}
          predicates:
            - Path=/api/greeting/{segment}
          filters:
            - name: Hystrix
              args:
                name: greetingFallback
                fallbackUri: forward:/api/fallback/greeting
```

## How to use it?

Compile this project using a JDK 8:
```shell
$ ./mvnw clean package
```

In order to use request rate limiter, you also need to start
a local Redis instance:
```shell
$ docker run --rm --name redis -p "6379:6379/tcp" redis:5
```

You can now start each microservice on your host:
```shell
$ java -jar gateway/target/spring-microservices-gateway.jar
$ java -jar echo/target/spring-microservices-echo.jar
$ java -jar time/target/spring-microservices-time.jar
$ java -jar whoami/target/spring-microservices-whoami.jar
$ java -jar greeting/target/spring-microservices-greeting.jar
```

Although each microservice is listening on its own network port,
you should use the microservice gateway to reach API endpoints.

Hit http://localhost:8080 to discover how to reach API endpoints.
<img src="https://imgur.com/download/oE26wdY"/>

### Deploy to Cloud Foundry

Make sure to create a Redis instance on your space prior
to pushing apps:
```shell
$ cf create-service p-redis shared-vm redis
```

You can easily deploy these microservices to Cloud Foundry:
```shell
$ cf push
```

This project relies on
[container-to-container networking](https://docs.cloudfoundry.org/concepts/understand-cf-networking.html):
you do not need to install
[Spring Cloud Services](https://docs.pivotal.io/spring-cloud-services/common/index.html)
or any external service registry when using Cloud Foundry.

Prior to accessing these microservices, you need to open a network route
between the gateway and each microservice:
```shell
$ cf add-network-policy gateway --destination-app time
$ cf add-network-policy gateway --destination-app echo
$ cf add-network-policy gateway --destination-app greeting
$ cf add-network-policy gateway --destination-app whoami
```

### Deploy to Kubernetes

Deploy all microservices using this manifest:
```shell
$ kubectl apply -f k8s.yml
```

A namespace named `spring-microservices` will be created, which will
contain all resources.

A `LoadBalancer` resource named `gateway-lb` is created, pointing to the
API gateway.
You need to get the allocated IP address in order to reach the API gateway:
```shell
$ kubectl -n spring-microservices get svc gateway-lb
NAME         TYPE           CLUSTER-IP       EXTERNAL-IP    PORT(S)        AGE
gateway-lb   LoadBalancer   10.100.200.159   35.187.2.214   80:31304/TCP   43m
```

Use the external IP address to reach the API gateway:
```shell
$ curl 35.187.2.214/api/time/current
2018-12-19T16:14:25.143Z%
```

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2019 Pivotal Software, Inc.

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
