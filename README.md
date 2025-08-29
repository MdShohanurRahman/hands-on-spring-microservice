# Hands-On Spring Microservices

A practical, step-by-step guide to building microservices with Spring Boot. This repository is structured as a progressive tutorial, where each branch represents a new concept or integration in the microservices architecture.

## 📋 Learning Path

Follow the branches in order to build the system incrementally:

1. **[`Introduction`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/introduction)**: Overview of microservices architecture and project setup. 
2. **[`Service Discovery`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/discovery-server)**: Implementing service discovery using Netflix Eureka.
3. **[`API Gateway`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/api-gateway)**: Setting up an API Gateway to route requests to microservices.
4. **[`Centralized Configs`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/config-server)**: Managing configurations centrally using Spring Cloud Config Server.
5. **[`Open Feign`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/open-feign)**: Enabling inter-service communication using Open Feign clients.
6. **[`Fault Tolerance`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/fault-tolerance)**: Adding fault tolerance with Resilience4J (circuit breakers, retries, rate limiter).
7. **[`Distributed Tracing`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/distributed-tracing)**: Integrating distributed tracing with Zipkin and Micrometer.
8. **[`Centralized Logging`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/centralized-logging)**: Setting up centralized logging with ELK Stack (Elasticsearch, Logstash, Kibana).
9. **[`Monitoring & Alerting`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/monitor)**: Setting up monitoring and alerting with Prometheus and Grafana.
10. **[`Security`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/security)**: Implementing security with Spring Security and OAuth2.
11. **[`Containerization & CI/CD Pipeline`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/containerization)**: Containerizing microservices with Docker and setting up a CI/CD pipeline with GitHub Actions.
12. **[`Asynchronous Communication`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/async-communication)**: Implementing asynchronous communication with RabbitMQ, and Kafka. (Event-driven architecture)
13. **[`Distributed Transaction Management`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/distributed-transaction)**: Handling distributed transactions with Saga pattern.
14. **[`Testing Microservices`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/testing)**: Writing unit and integration tests for microservices.
15. **[`Performance Optimization`](https://github.com/MdShohanurRahman/hands-on-spring-microservice.git/tree/performance-optimization)**: Techniques for optimizing microservice performance.

## 🛠️ Technology Stack

*   **Java:** 24
*   **Spring Boot:** 3.5.6
*   **Database:** PostgreSQL
*   **Containerization:** Docker & Docker Compose

## 🚀 How to Use This Repo

1.  Clone the repository:
    ```bash
    git clone https://github.com/MdShohanurRahman/hands-on-spring-microservice.git
    ```
2.  Check out the branch you want to explore:
    ```bash
    git checkout 1.Introduction
    ```
3.  Follow the instructions in the branch's `README.md` to run the code.

---

*This project is for educational purposes. Stay tuned for more updates!*