# API Documentation with SpringDoc OpenAPI

This branch introduces **SpringDoc OpenAPI** (Swagger) to automatically generate interactive API documentation for our microservices. 
This provides a single source of truth for our API contracts and allows for easy testing.

## What is SpringDoc OpenAPI?

SpringDoc OpenAPI is a library that automatically generates API documentation based on the OpenAPI 3 specification by inspecting
your Spring application at runtime. It scans your `@RestController`, `@RequestMapping`, and other annotations to create a JSON API definition, which is then presented by the **Swagger UI**.

## Why Do We Need It? Best Practices

1.  **Live Documentation:** Your documentation is always in sync with your code. When you update an endpoint, the docs update automatically.
2.  **API Testing:** Developers and testers can use the Swagger UI to execute all API endpoints directly from the browser, eliminating the need for tools like Postman for basic testing.
3.  **Client SDK Generation:** The OpenAPI spec can be used to automatically generate client code for frontends or other services in multiple languages.
4.  **Improves Collaboration:** Provides a clear contract between frontend and backend teams on what the API expects and returns.

## Best Practices for Integration

1.  **Centralized API Definition (Optional):** For microservices, you can use a dedicated service to aggregate all service API docs. However, we will document each service individually for simplicity.
2.  **Protect Swagger in Production:** The Swagger UI should not be exposed publicly in production. We will configure it to be available only on specific profiles (e.g., `dev`, `local`).
3.  **Use Annotations for Clarity:** While SpringDoc works out-of-the-box, using `@Operation`, `@ApiResponse`, and other annotations greatly improves the generated documentation.
4.  **Secure the Documentation:** If your API is secured, configure Swagger UI to include the JWT token in its requests so you can test secured endpoints

## How to Integrate

### 1. Add Dependency to Each Service

Add the `springdoc-openapi-starter-webmvc-ui` dependency to every service that has REST controllers (`user-service`, `department-service`). For the API Gateway (which uses WebFlux), we use the `webflux-ui` dependency.

**Add to `user-service` and `department-service` `pom.xml`:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version> <!-- Check for the latest version -->
</dependency>
```
**Add to `api-gateway` `pom.xml`:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.5.0</version> <!-- Check for the latest version -->
</dependency>
```

### 2. Add Configuration Class:
* [ApiGateway Config](services/api-gateway/src/main/java/com/example/api_gateway/config/OpenAPIConfig.java)
* [User Service Config](services/department-service/src/main/java/com/example/department_service/config/OpenApiConfig.java)

### 3. Add Configuration Properties in api gateway
```yml
springdoc:
  swagger-ui:
    path: /
    urls:
      - name: User Service
        url: /user-service/api-docs # configured route
      - name: Department Service
        url: /department-service/api-docs
    enabled: true
```

## Resources
* [Documentation](https://springdoc.org/)
* [Comprehensive Guide](https://www.baeldung.com/spring-cloud-gateway-integrate-openapi)

## Next Step
Containerizing microservices with Docker and setting up a CI/CD pipeline with GitHub Actions.
* [Api Documentation](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/containerization)