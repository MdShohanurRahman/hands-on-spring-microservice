# Service Registration and Service Discovery with Eureka

In this branch, we introduce the **Eureka Service Discovery Server**, a crucial component for managing how our microservices find and communicate with each other.

## What is Service Discovery?

In a dynamic microservices environment, the network locations (IP addresses and ports) of service instances change frequently due to scaling, failures, or upgrades. **Service Discovery** is the automatic detection of devices and services on a network.

### How Eureka Works

1.  **Service Registration:** When a microservice (e.g., `user-service`) starts, it registers itself with the Eureka Server, providing its name and network location.
2.  **Heartbeat:** The service continuously sends heartbeats to Eureka to signal it is still alive.
3.  **Service Discovery:** When another service (e.g., `department-service`) needs to call the `user-service`, it asks the Eureka Server for all available instances of `user-service`. Eureka provides the list, and the client uses it to make the request.

This eliminates the need to hardcode the URLs of other services in your application properties.

## Project Structure Update
```
hands-on-spring-microservices/services
├── discovery-server/ # NEW: The Eureka Server
├── user-service/
├── department-service/
└── pom.xml # NEW: Parent POM
```
we'll create a new module called [`discovery-server`](services/discovery-server) for the Eureka Server and update the existing microservices to become Eureka Clients.
## Parent POM Update
We need to create the parent [`pom.xml`](pom.xml) to include the new module and manage Spring Cloud dependencies.
```xml
   <modules>
      <module>services/department-service</module>
      <module>services/user-service</module>
      <module>services/discovery-server</module>
   </modules>
   <properties>
      <java.version>24</java.version>
      <spring-cloud.version>2025.0.0</spring-cloud.version>
   </properties>
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-dependencies</artifactId>
               <version>${spring-cloud.version}</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
       </dependencies>
   </dependencyManagement>
```

## New Component: `discovery-server`

This is a standalone Spring Boot application whose sole purpose is to be the service registry.

1. **Add the** `eureka-server` **dependency.**
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    ```
2. **Configure its** `application.yml` to point to the Eureka server.
    ```yaml
    server:
      port: 8761
    eureka:
      client:
        register-with-eureka: false
        fetch-registry: false
   ```
3. **Annotate the main class** with `@EnableEurekaClient`.
    ```java
    @SpringBootApplication
    @EnableEurekaServer
    public class DiscoveryServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(DiscoveryServerApplication.class, args);
        }
    }
    ```

## **Changes to Microservices (**`user-service`, `department-service`)

To become **Eureka Clients**, each microservice needs to:

1. **Add the** `eureka-client` **dependency.**
    ```xml
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    ```

2. **Configure its** `application.yml` to point to the Eureka server.
   ```yaml
   spring:
     application:
       name: <your_service_name> # Unique service name
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/ # Eureka Server URL
       register-with-eureka: true # Register this service with Eureka
       fetch-registry: true # Fetch registry info from Eureka
     instance:
       prefer-ip-address: true # Use IP address for registration
   ```

3. **Annotate the main class** with `@EnableDiscoveryClient`.
   `@EnableDiscoveryClient` is optional if you are using Spring Cloud dependencies that autoconfigure service discovery (like with Spring Boot 2.x+ and Spring Cloud).
    ```java
    @SpringBootApplication
    @EnableDiscoveryClient
    public class UserServiceApplication {
         public static void main(String[] args) {
              SpringApplication.run(UserServiceApplication.class, args);
         }
    }
    ```
    ```java
    @SpringBootApplication
    @EnableDiscoveryClient
    public class DepartmentServiceApplication {
            public static void main(String[] args) {
                SpringApplication.run(DepartmentServiceApplication.class, args);
            }
    }
   ```


## **How to Run**

1. **Start the Discovery Server first:**
    ```bash
    cd services/discovery-server
    ./mvnw spring-boot:run
    ```
   *The Eureka Dashboard will be available at:* [*http://localhost:8761*](http://localhost:8761/)

2. **Start the microservices:**  
   *In separate terminals, start* `user-service` and `department-service`.

    ```bash
    cd services/user-service
    ./mvnw spring-boot:run
    ```
   
    ```bash
   cd services/department-service
   ./mvnw spring-boot:run
    ```

3. **Observe:** Refresh the Eureka Dashboard ([http://localhost:8761](http://localhost:8761/)). You will see both services registered under "Instances currently registered with Eureka".

## Example DiscoveryClient Usage
In `department-service`, you can use `DiscoveryClient` to programmatically discover instances of `user-service`.

```java
   @Autowired
   private RestClient restClient;
   @Autowired
   private DiscoveryClient discoveryClient;

    public List<User> getUsers() {
         List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
         if (instances.isEmpty()) {
              throw new IllegalStateException("No instances of user-service found");
         }
         String userServiceUrl = instances.getFirst().getUri().toString();
         return restClient.get()
                 .uri(userServiceUrl + "/api/v1/users")
                 .retrieve()
                 .body(String.class);
    }
```

## **What We Achieved**

* **Centralized Registry:** We now have a single source of truth for all running service instances.

* **Dynamic Routing:** Services can now find each other by name instead of hardcoded URL.

* **Resilience:** If a service instance goes down, Eureka will eventually remove it from the registry, preventing requests from being sent to a failed instance.

## Next Step
*   [Spring Could Api Gateway](https://github.com/MdShohanurRahman/hands-on-spring-microservices/tree/api-gateway)
