# Distributed Tracing with Zipkin and Micrometer

This branch introduces **Distributed Tracing**, a method used to profile and monitor applications, especially those built using a **microservices** architecture. We will use **Micrometer** for metrics collection and **Zipkin** for visualizing them.

## What is Distributed Tracing?

Distributed tracing is a technique for tracking a request as it propagates through a distributed system, like your microservices. It gives you a complete view of the journey of a single user request, showing you which services it touched, how long each step took, and where errors occurred.

## Why Do We Need It? A Real-Life Scenario

**Scenario:** A user reports that loading their profile page is very slow.

**Without Tracing:** You have to manually check the logs of the API Gateway, `user-service`, `department-service`, and any other involved service. Correlating logs for a single request across all these services is like finding a needle in a haystack.

**With Tracing:** You have a unique **trace ID** assigned to the user's request. You can use this ID in Zipkin's UI to instantly see a visual timeline (a "trace") of the entire request:
*   You see the request entered through the API Gateway.
*   You see it took 150ms in the `user-service`.
*   You see the `user-service` then called the `department-service`, which took 2000ms to respond.
*   **Instantly, you identify the bottleneck:** The `department-service` is the cause of the slowdown.

## Key Terminology

*   **Trace:** The entire journey of a single request, from its starting point (e.g., the API Gateway) through all the services it touches.
*   **Span:** A single operation within a trace. It represents a unit of work (e.g., an HTTP call, a database query). A trace is a tree of spans.
*   **Trace ID:** A unique identifier that is assigned to a single request and remains constant as it flows through the system. This is what links all the logs and spans together.
*   **Span ID:** A unique identifier for a specific operation within a trace.
*   **Zipkin:** A distributed tracing system that gathers timing data and provides a UI for visualizing traces.
*   **Micrometer:** A metrics collection library that integrates with various monitoring systems, including Zipkin.

## How It Works

1.  **Request In:** A request enters the system (e.g., at the API Gateway). Sleuth generates a **Trace ID** and a **Span ID** for it.
2.  **Propagation:** When one service (e.g., `api-gateway`) calls another (e.g., `user-service`), Sleuth automatically adds the Trace ID and Span ID to the HTTP headers of the outgoing request.
3.  **Logging:** Each service's logs now contain the `[app-name,trace-id,span-id]` information, making it easy to filter logs for a specific request.
4.  **Exporting:** Each service sends its timing data (spans) to a **Zipkin server**.
5.  **Visualization:** You use the Zipkin UI to search for traces by service, time, or even error, and see a detailed timeline of the request's flow.

![zipkin-trace-example](/resources/zipkin-tracing.png)

## Project Changes

We will add Zipkin server and configure all our services to send trace data to it.

## How to Integrate
### 1. Set Up Zipkin Server
You can run Zipkin server using Docker. If you have Docker installed, you can start a Zipkin server with the following command:
```bash
  docker run -d -p 9411:9411 openzipkin/zipkin
```
or use Docker Compose by adding the following service to your [`docker-compose.yml`](./docker-compose.yml):
```yaml
   zipkin:
     container_name: zipkin
     image: openzipkin/zipkin
     ports:
       - "9411:9411"
```
Then run:
```bash 
  docker-compose up -d zipkin
```
Or alternatively, you can download and run the Zipkin server jar directly:
```bash
    wget -O zipkin.jar https://search.maven.org/remotecontent?filepath=io/zipkin/java/zipkin-server/2.23.2/zipkin-server-2.23.2-exec.jar
    java -jar zipkin.jar
```
### 2. Add Dependencies to ALL Services

Every service that should be traced (API Gateway, `user-service`, `department-service`) needs the Sleuth and Zipkin dependencies.

**Add to each service's `pom.xml`:**
```xml
    <!-- add actuator if not added  -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer and Zipkin dependencies -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-observation</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>

    <!-- for tracing openfeign outgoing request-->
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-micrometer</artifactId>
    </dependency>
```
### 3. Configure Application Properties
In each service's `application.yml`, add the following configuration to point to the Zipkin server:
```yml
#Zipkin config
management:
  tracing:
    sampling:
      probability: 1.0 # 100% sampling for demonstration; adjust as needed
  #Actuator config
  endpoints:
    web:
      exposure:
        include: "*"
```
### 4. Add OpenFeign Micrometer Capability
If you are using OpenFeign for inter-service communication, you need to add Micrometer capability to it.
**Create a configuration class in each service that uses OpenFeign (e.g., `user-service`, `department-service`):**
```java
import feign.Capability;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public Capability capability(final MeterRegistry registry) {
        return new MicrometerCapability(registry);
    }
}
```

## Testing the Setup
1. Start the Zipkin server.
2. Start all your microservices (`discovery-server`, `config-server`, `api-gateway`, `user-service`, `department-service`).
3. Make a request to the API Gateway that will trigger calls to the other services. For example:
    ```bash
       curl http://localhost:8080/api/v1/users/1/with-department
    ```
4. Open your browser and navigate to `http://localhost:9411` to access the Zipkin UI.
5. Use the search functionality to find traces by service name or time range.
6. Click on a trace to see the detailed timeline of the request, including spans for each service call.

## Resources
* [Zipkin Documentation](https://zipkin.io/pages/quickstart.html)
* [Distributed Tracing Blog](https://www.codingshuttle.com/spring-boot-handbook/microservice-advance-distributed-tracing-using-zipkin-and-micrometer/)

## Next Step
With tracing, we can see where a problem is. The next step is to aggregate all logs from all services into one place to easily see what happened. 
The next branch will introduce **Centralized Logging with the ELK Stack (Elasticsearch, Logstash, Kibana)**
* [Centralized Logging](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/centralized-logging)