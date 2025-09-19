# Securing Microservices with Spring Security & Auth0

This branch introduces **Spring Security** to secure our microservices. We will use the OAuth 2.0 **Client Credentials** flow, 
ideal for service-to-service communication, with **Auth0** as our authorization server.

## What is Application Security?

Application security involves measures taken to protect applications from threats and unauthorized access. In microservices, it ensures that only authenticated and authorized services or users can access specific endpoints.

## Why Do We Need It? A Real-Life Scenario

**Scenario:** Your `user-service` API that returns personal user information (email, address) is currently exposed to the internet without any protection.

**Without Security:**
*   Anyone on the internet can access sensitive user data.
*   A malicious actor could spam your APIs, leading to a Denial-of-Service (DoS) attack.
*   Your monitoring endpoints (like `/actuator`) could be accessed, exposing sensitive internal state and metrics.

**With Security:**
*   Every request must present a valid **access token**.
*   Auth0 validates the token and defines what permissions (scopes) it has.
*   Your APIs can check these permissions to allow or deny access.
*   Internal endpoints like `/actuator` are protected from public access.

## How It Works: The Client Credentials Flow

The Client Credentials flow is used for machine-to-machine (M2M) communication where a service itself is the client needing to access resources. It does not involve a user.

1.  **Request Token:** The `api-gateway` (client) requests an access token from Auth0 using its `client_id` and `client_secret`.
2.  **Validate Credentials:** Auth0 validates the credentials and issues a signed **JWT (JSON Web Token)** access token.
3.  **Call API:** The `api-gateway` includes this token in the `Authorization: Bearer <token>` header when making requests to other services (e.g., `user-service`).
4.  **Verify Token:** The `user-service` receives the request, validates the JWT's signature against Auth0's public keys, and checks if the token has the required permissions.
5.  **Grant Access:** If the token is valid, the request is processed.

## Project Changes

We will secure our internal microservices (`user-service`, `department-service`) and the `api-gateway`. We will also protect the sensitive Actuator endpoints used by Prometheus.

## How to Integrate
### 1. Add Dependencies

Add the Spring Security, OAuth2 Resource Server dependency to any service that needs to be protected.

** 1. Add to service's `pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```
### 2. Configure Auth0
In your `application.yml`, configure the OAuth2 resource server settings.
**Example `application.yml`:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://YOUR_AUTH0_DOMAIN/
          audience:
            - YOUR_API_IDENTIFIER
```
### 3. Configure Security Rules.
Create a security configuration class to define access rules.

for gateway, [`SecurityConfig.java`](/services/api-gateway/src/main/java/com/example/api_gateway/config/SecurityConfig.java)

**For other microservice, Example `SecurityConfig.java`:**
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );
        return httpSecurity.build();
    }
}
```

## Testing the Integration
1.  **Obtain Access Token:** Use a tool like Postman or curl to obtain an access token from Auth0 using the Client Credentials flow.
    ```bash
        curl --request POST \
        --url https://dev-1spgkyzchsjzz7md.us.auth0.com/oauth/token \
        --header 'content-type: application/json' \
        --data '{
        "client_id":"8An8z2oi8WJgtyPR5vQXEeRDPWOnapDk",
        "client_secret":"baV77LIAjnmm8e5hqBenU2afLK6qqW8Ak4sxZtfeoHFDzls2mApbVy_V-m7oEFfe",
        "audience":"https://dev-1spgkyzchsjzz7md.us.auth0.com/api/v2/",
        "grant_type":"client_credentials"
        }'
    ```
2. **Access Protected Endpoint:** Use the obtained token to access a protected endpoint.
    ```bash
    curl --request GET \
    --url http://localhost:8080/users \
    --header 'Authorization: Bearer YOUR_ACCESS_TOKEN'
    ``` 

## Resources
* [Spring Security Docs](https://docs.spring.io/spring-security/reference/index.html)
* [Comprehensive Guide](https://medium.com/@ihor.polataiko/spring-security-guide-part-1-introduction-c2709ff1bd98)

## Next Step
Api Documentation with Swagger & OpenAPI
* [Api Documentation](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/open-api)