# API Gateway with Spring Cloud Gateway

This branch introduces the **API Gateway**, the single entry point for all client requests, which routes them to the appropriate microservice.

## What is an API Gateway?

An API Gateway is a server that acts as an API front-end, receiving API requests, routing them to the correct back-end service, and then returning the response. It is the **front door** for all clients.

## Why Do We Need It?

Imagine a client (web/mobile app) needs data from 3 different microservices to load one page. Without a gateway:
*   The client must know the network addresses of all 3 services.
*   The client makes **3 separate requests**, which is inefficient and hard to manage.
*   It exposes internal service structures to the outside world.

An API Gateway solves this by providing:
1.  **Single Entry Point:** Clients only talk to the gateway.
2.  **Request Routing:** The gateway forwards requests to the correct service based on the path (e.g., `/users/**` -> `user-service`).
3.  **Cross-Cutting Concerns:** It centralizes logic for:
   *   **Authentication & Authorization:** Verify credentials before routing.
   *   **Rate Limiting:** Prevent abuse.
   *   **Load Balancing:** Distribute traffic between service instances.
   *   **CORS:** Handle cross-origin requests in one place.
   *   **Response Caching:** Cache frequent responses to reduce load.

## Key Terminology

*   **Route:** A set of rules (like a path pattern) defining how to forward a request to a service (e.g., route `/user/**` to `user-service`).
*   **Predicate:** Conditions that must be true for a route to be matched (e.g., "if the path starts with `/api/users`").
*   **Filter:** Logic that can be applied to requests before they are forwarded or to responses before they are sent back (e.g., add an HTTP header, log the request).

## Project Structure Update
```angular2html
   hands-on-spring-microservices/services
   ├── api-gateway/ # NEW: The Spring Cloud Gateway
   ├── discovery-server/
   ├── user-service/
   ├── department-service/
   └── pom.xml
```


## New Component: `api-gateway`

This is a standalone Spring Boot application that acts as the gateway.

**Key Dependencies:**
*   `spring-cloud-starter-gateway`
*   `spring-cloud-starter-netflix-eureka-client` (So it can discover services from Eureka)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
**Configure [`application.yml`](services/api-gateway/src/main/resources/application.yml):**
```yaml
  routes:
    - id: user-service
      uri: lb://user-service
      predicates:
        - Path=/api/v1/users/**

    - id: department-service
      uri: lb://department-service
      predicates:
        - Path=/api/v1/departments/**
```

## How It Works

1.  A client sends a request to `http://localhost:8080/api/v1/users/1`.
2.  The API Gateway, using its defined **routes** and **predicates**, sees that the path matches `/api/v1/users/**`.
3.  It looks up the `user-service` location from the **Eureka Discovery Server**.
4.  It uses **client-side load balancing** (Spring Cloud LoadBalancer) to pick an instance.
5.  It **forwards** the request to that instance (e.g., `http://user-service:8081/api/v1/users/1`).
6.  It receives the response and sends it back to the client.

## How to Run

1.  **Start the supporting services first:**
    ```bash
    cd services/discovery-server
    ./mvnw spring-boot:run
    ```

2.  **Start the microservices:**
    ```bash
    cd services/user-service
    ./mvnw spring-boot:run

    cd services/department-service
    ./mvnw spring-boot:run
    ```

3.  **Start the API Gateway:**
    ```bash
    cd services/api-gateway
    ./mvnw spring-boot:run
    ```

## Testing the Flow

*   **Old Way (Direct Call):** http://localhost:8081/api/v1/users
*   **New Way (Through Gateway):** http://localhost:8080/api/v1/users

The gateway is now running on port **8080**. All client requests should be sent to this port. The gateway will seamlessly route them to the correct service on ports 8081 or 8082.

---

## References
*   [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/reference/index.html)
*   [A Comprehensive Guide](https://medium.com/@gauravraisinghani1998/implementing-spring-cloud-gateway-a-comprehensive-guide-3498aaacfdca)
---
## Next Step
*   [Centralized Configuration with spring cloud config server](https://github.com/MdShohanurRahman/hands-on-spring-microservices/tree/config-server)