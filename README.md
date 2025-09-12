# Observability with Grafana Stack (Prometheus, Loki, Tempo)

This branch completes our observability setup by introducing the **Grafana Stack** for monitoring, logging, tracing, and alerting. We move from passive observation to active monitoring and alerting.

## What is Observability?

Observability is the ability to understand the internal state of a system from its external outputs, primarily through logs, metrics, and traces. **Monitoring** is the act of collecting and analyzing this data. **Alerting** is the process of notifying you when something needs your attention.

## Why Do We Need It? A Real-Life Scenario

**Scenario:** It's 3 AM. The payment service begins failing silently due to a downstream API rate limit.

**Without Monitoring/Alerting:**
*   You wake up to a flood of angry customer emails and tweets hours later.
*   You start a frantic investigation, manually checking logs and dashboards.
*   Revenue is lost, and customer trust is damaged.

**With Monitoring/Alerting:**
*   **Prometheus** scrapes metrics and detects a spike in HTTP 5xx errors from the payment service.
*   An **Alert Rule** in Prometheus fires because the error rate crosses a threshold for more than 5 minutes.
*   **Alert Manager** sends a notification to your team's Slack channel and PagerDuty.
*   You wake up, click the alert link in Slack, which takes you to a **Grafana dashboard**.
*   The dashboard shows the error rate, latency, and resource usage. You click a **Tempo** link to see traces of the failing requests and a **Loki** link to see the related logs.
*   You identify and fix the issue before most customers are even awake.

## The Grafana Stack (PLG Stack)

![Grafana Stack](/resources/grafana-stack.png)

We integrate a modern observability stack:

*   **Prometheus:** The metrics database. It **pulls** metrics from your services at regular intervals and stores them as time-series data.
*   **Grafana:** The visualization platform. It creates unified dashboards that can query data from Prometheus (metrics), Loki (logs), and Tempo (traces).
*   **Loki:** The log aggregation system. Unlike ELK(Elastic, Logstash, Kibana), it is designed to be cost-effective by not indexing log content, only labels. It integrates seamlessly with Grafana.
*   **Tempo:** The distributed tracing backend. It stores traces and allows you to jump from metrics in Prometheus or logs in Loki directly to the related trace.
*   **Alert Manager:** Handles alerts sent by Prometheus, deduplicates them, groups them, and routes them to the correct receiver (email, Slack, etc.).

## How It Works

1.  **Metrics:** Services expose a `/actuator/prometheus` endpoint. Prometheus scrapes this endpoint every 15s to collect metrics.
2.  **Logs:** Services output logs to stdout. The Promtail agent (like Filebeat) ships these logs to Loki, tagging them with labels (service name, pod name, etc.).
3.  **Traces:** Services are configured to send trace data to Tempo instead of (or in addition to) Zipkin.
4.  **Grafana** is configured with data sources for Prometheus, Loki, and Tempo, allowing you to create correlated dashboards.
5.  **Prometheus** evaluates alerting rules and fires alerts to Alert Manager.

## How to Integrate

### 1. Add Dependencies

All Spring Boot services need the Micrometer Prometheus registry to expose metrics.

**Add to each service's `pom.xml`:**
```xml
<dependencies>
    <!-- Spring Boot Actuator for exposing metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!-- Micrometer Tracing for distributed tracing -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    <!-- Zipkin Reporter for sending traces to Tempo -->
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>
    <!-- Micrometer for Prometheus metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    <!-- Loki Logback Appender for sending logs to Loki -->
    <dependency>
        <groupId>com.github.loki4j</groupId>
        <artifactId>loki-logback-appender</artifactId>
        <version>1.5.2</version>
    </dependency>
</dependencies>
```

### 2. Configure Application Properties
**Add to each service's `application.yml`:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,prometheus"
  endpoint:
    health:
      show:
        details: "always"
  metrics:
    distribution:
      percentiles-histogram:
        http:
          server:
            requests: true
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
  tracing:
    sampling:
      probability: 1.0 # 100% sampling for demonstration; adjust as needed

# Correlation IDs for tracing in logs
logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

### 3. Docker Compose for Grafana Stack
**Update [`docker-compose.yml`](/docker-compose.yml):**

Btw, For simplicity we remove ELK stack from this compose file. You can keep it if you want.
```yaml
services:
  # Prometheus Service
  prometheus:
    image: prom/prometheus
    container_name: prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - microservices-net

  ## Tempo Service
  tempo:
    image: grafana/tempo
    container_name: tempo
    command: [ "-config.file=/etc/tempo.yaml" ]
    volumes:
      - ./docker/tempo/tempo.yml:/etc/tempo.yaml:ro
      - ./docker/tempo/tempo-data:/tmp/tempo
    ports:
      - "3200:3200" # Tempo
      - "9411:9411" # zipkin
    networks:
      - microservices-net

  #loki Service
  loki:
    image: grafana/loki:main
    container_name: loki
    command: [ "-config.file=/etc/loki/local-config.yaml" ]
    ports:
      - "3100:3100"
    networks:
      - microservices-net

  # Grafana Service
  grafana:
    image: grafana/grafana
    container_name: grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    volumes:
      - ./docker/grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./docker/grafana/provisioning/datasources:/etc/grafana/provisioning/datasources:ro
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=password
    networks:
      - microservices-net

networks:
  microservices-net: # Custom network for container communication
    driver: bridge
```
### Configuration Files
#### Prometheus Configuration: [`docker/prometheus/prometheus.yml`](./docker/prometheus/prometheus.yml)
#### Tempo Configuration: [`docker/tempo/tempo.yml`](./docker/tempo/tempo.yml)
#### Grafana Configuration: [`docker/grafana/provisioning/datasources/datasource.yml`](./docker/grafana/provisioning/datasources/datasource.yml)
#### Grafana Dashboards: [`docker/grafana/provisioning/dashboards/dashboard.yml`](./docker/grafana/provisioning/dashboards/dashboard.yml)
#### Grafana Predefined Dashboards: [`docker/grafana/provisioning/dashboards/dashboard.json`](./docker/grafana/provisioning/dashboards/dashboard.json)

### 4. Configure Logback for Loki
**Update to each service's `logback-spring.xml`:**

Btw, For simplicity we remove ELK appender from this logback file. You can keep it if you want.
```xml
<configuration>
      <include resource="org/springframework/boot/logging/logback/base.xml"/>
      <springProperty scope="context" name="appName" source="spring.application.name"/>

      <appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
            <http>
                  <url>http://localhost:3100/loki/api/v1/push</url>
            </http>
            <format>
                  <label>
                        <pattern>application=${appName},host=${HOSTNAME},level=%level</pattern>
                  </label>
                  <message>
                        <pattern>${FILE_LOG_PATTERN}</pattern>
                  </message>
                  <sortByTime>true</sortByTime>
            </format>
      </appender>

      <root level="INFO">
            <appender-ref ref="LOKI"/>
      </root>
</configuration>
```

### Testing Integration
   1. Run docker compose 
       ```bash
       docker-compose up -d
       ```
   2. Start all services: `discovery-service`, `config-service`, `api-gateway`, `user-service`, `department-service`
   3. Call some endpoints to generate traffic and logs. For example:
       ```bash
       curl -X GET http://localhost:8080/api/v1/users/1/with-department
      ```
   4. Access Grafana:
      Open your browser and navigate to `http://localhost:3000`. Log in with: **username:** `admin` & **password:** `password`.
      * Explore pre-configured dashboard: Navigate at `http://localhost:3000/d/sOae4vCnk/spring-boot-statistics`
      * Explore Drilldowns: Navigate at `http://localhost:3000/drilldown`
   5. Explore Prometheus:
      * Navigate to `http://localhost:9090` to access the Prometheus UI.
      * Use the "Graph" tab to run queries and visualize metrics.
  
 
## Resources
* [Grafana Documentation](https://grafana.com/docs/)
* [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
* [Logging With Loki Guide](https://www.baeldung.com/spring-boot-loki-grafana-logging)
* [Integrating Grafana Stack Guide #1](https://medium.com/@narasimha4789/integrating-grafana-observability-stack-into-a-spring-boot-application-a-comprehensive-guide-eb9d21f29fe6)
* [Integrating Grafana Stack Guide #2](https://programmingtechie.com/articles/spring-boot3-observability-grafana-stack)

## Next Step
We have successfully integrated the Grafana Stack for observability. The next step is to enhance our microservices with robust security measures.

* [Security](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/security)