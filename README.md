# Building Resilient Microservices with Resilience4j

This branch introduces **Resilience4j**, a lightweight fault tolerance library designed for Java 8 and functional programming. We will integrate it to make our service-to-service communication resilient to failures.

## What is Fault Tolerance?

Fault tolerance is the ability of a system to continue operating properly in the event of the failure of some of its components. In microservices, the failure of one service should not cascade and bring down the entire system.

## Why Do We Need It? A Real-Life Use Case

**Scenario:** Our `user-service` calls the `department-service` using OpenFeign to get department details for a user.

**What happens if `department-service` is slow or down?**
*   The thread in `user-service` waiting for a response gets blocked.
*   As more requests for users come in, more threads get blocked.
*   Eventually, `user-service` runs out of threads and becomes unresponsive itself. This is a **cascading failure**.

**Solution:** Implement fault tolerance patterns to handle the failure gracefully, for example:
*   **Retry:** Maybe the failure is temporary.
*   **Circuit Breaker:** Stop making requests if the failure is persistent.
*   **Fallback:** Return a default response instead of failing completely (e.g., show the user without department details).

## Core Resilience4j Patterns

### 1. Retry
*   **What it does:** Automatically retries a failed operation a specified number of times.
*   **When to use:** For transient, temporary failures (e.g., network glitch, service temporarily unavailable). Do not use for permanent errors (e.g., `404 Not Found`, `400 Bad Request`).

### 2. Rate Limiter
*   **What it does:** Limits the number of calls to a service in a specific time period.
*   **When to use:** To protect a service from being overwhelmed by too many requests (e.g., to prevent denial-of-service, accidental or intentional).

### 3. Bulkhead
*   **What it does:** Limits the number of concurrent calls to a service. This isolates failures to a part of the system, preventing a single slow service from consuming all resources (e.g., all threads).
*   **When to use:** To ensure one misbehaving service doesn't use all the threads in your application, starving other healthy operations.

### 4. Circuit Breaker
*   **What it does:** Prevents a service from repeatedly trying to execute an operation that's likely to fail. It transitions between `CLOSED`, `OPEN`, and `HALF-OPEN` states.
    *   `CLOSED`: Everything is fine, requests pass through.
    *   `OPEN`: Requests fail immediately for a duration. Gives the failing service time to recover.
    *   `HALF-OPEN`: After the duration, allow a few test requests to check if the service is back.
*   **When to use:** To prevent cascading failures and avoid overwhelming a failing service. Essential for protecting your system.
* 
![circuit-breaker-states](/resources/circuit-breaker.jpg)

## How to Integrate Resilience4j with OpenFeign

Resilience4j integrates seamlessly with Spring Cloud OpenFeign. The configuration is primarily done in the `application.properties` file.

### 1. Add Dependencies

Add the Resilience4j starter to the service making the calls (e.g., `user-service`).

**`user-service/pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
### 2. Configure Resilience4j in [`application.yml`](services/user-service/src/main/resources/application.yml)

```yaml
resilience4j:
  retry:
    configs:
      default:
        max-attempts: 3 # The operation will be retried up to 3 times before failing.
        wait-duration: 10s # There will be a 10-second wait between each retry attempt.
    instances:
      userServiceRetry: # Replace with the name of your Feign client or service or default.
        base-config: default # Inherit settings from the 'default' config above.
        wait-duration: 200ms # Override: for this instance, wait 200ms between retries.
    ratelimiter:
      instances:
        default:
          limit-for-period: 1 # Only 1 call is allowed per refresh period.
          limit-refresh-period: 5s # The rate limit resets every 5 seconds.
          timeout-duration: 1ms # If a call cannot be made immediately, it will wait up to 1 millisecond before failing.
    bulkhead:
      instances:
        default:
          max-concurrent-calls: 20 # Up to 20 concurrent calls are allowed.
    circuitbreaker:
      instances:
        default:
          registerHealthIndicator: true  # Expose circuit breaker health via the actuator
          slidingWindowSize: 10          # Size of the sliding window for call recording
          slidingWindowType: COUNT_BASED # Use count-based sliding window
          minimumNumberOfCalls: 10       # Minimum number of calls before calculating failure rate
          failureRateThreshold: 50       # If 50% (5 out of 10) calls fail, the circuit goes to OPEN state
          waitDurationInOpenState: 1s   # Time the circuit stays OPEN before moving to HALF_OPEN
          permittedNumberOfCallsInHalfOpenState: 3  # Number of test calls in HALF_OPEN state
          eventConsumerBufferSize: 10    # Buffer size for event logs
```
### 3. Implement Fallback Methods
 * Available annotations
    * `@Retry`
    * `@RateLimiter`
    * `@Bulkhead`
    * `@CircuitBreaker`
    
  All these annotations have a `fallbackMethod` attribute to specify the fallback method to be called when the main method fails.

 * Fallback methods in same class
     ```java
    @FeignClient(name = "department-service", path = "/api/v1/departments")
    public interface DepartmentClient {
    
        @GetMapping("/{id}")
        @Retry(name = "department-service", fallbackMethod = "getUserWithDepartmentFallback")
        DepartmentDto getDepartmentById(@PathVariable("id") Long id);
   
        
        default DepartmentDto getUserWithDepartmentFallback(Long id, Throwable throwable) {
            // Fallback logic: return a default DepartmentDto or handle the error as needed
            return new DepartmentDto(); // Return an empty or default department
        }
    }
      ```
 * Fallback methods in different class
    Before using fallback with Feign clients, you must enable Feign's Resilience4j integration in your application.yml or application.properties.
    `spring.cloud.openfeign.circuitbreaker.enabled=true`

    ```java
    @FeignClient(name = "department-service", path = "/api/v1/departments", fallback = DepartmentClientFallback.class)
    public interface DepartmentClient {
        @GetMapping("/{id}")
        DepartmentDto getDepartmentById(@PathVariable("id") Long id);
    }
    
    @Component
    public class DepartmentClientFallback implements DepartmentClient {
        @Override
        public DepartmentDto getDepartmentById(Long id) {
            // Fallback logic: return a default DepartmentDto or handle the error as needed
            return new DepartmentDto(); // Return an empty or default department
        }
    }
    ```
   
### 4. Monitor Circuit Breaker Status
Resilience4j provides integration with Spring Boot Actuator, allowing you to monitor the status of your circuit breakers.
Add the Actuator dependency if not already present:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
Make sure to enable the necessary actuator endpoints in your [`application.yml`](services/user-service/src/main/resources/application.yml) :

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,circuitbreakers,metrics
  endpoint:
    health:
      show-details: always
```
You can access the circuit breaker status at the following endpoint:
`http://localhost:8080/actuator/circuitbreakers`
This endpoint provides information about the state of each circuit breaker, including metrics like the number of successful and failed calls.
You can also access detailed metrics at:
`http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls`

## Testing Integration
1. Start `discovery-service`, `config-server`, `api-gatway`, `user-service`, and `department-service`.
2. Make a request to `user-service` to fetch a user along with their department details.
   `http://localhost:8080/api/v1/users/1/with-department`
3. Simulate failures in `department-service` (e.g., stop the service or introduce delays).
4. Observe how `user-service` handles the failures using retries, circuit breakers, and fallbacks.

## Resources
* [Resilience4j Documentation](https://resilience4j.readme.io/docs/getting-started)

## Next Step
For the next part of the series, we will implement distributed tracing using micrometer and Zipkin to trace requests across multiple microservices.
* [Distributed Tracing](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/distributed-tracing)