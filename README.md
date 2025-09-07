# Centralized Logging with the ELK Stack

This branch introduces **Centralized Logging** using the **ELK Stack** (Elasticsearch, Logstash, Kibana) deployed via Docker Compose. 
This solves the critical problem of managing logs scattered across dozens of microservices.

## What is Centralized Logging?

Centralized logging is the practice of aggregating all log data from every component of a distributed system into a single, unified platform. 
Instead of SSH-ing into individual servers to read log files, you can search, analyze, and visualize all logs from one place.

## Why Do We Need It? A Real-Life Scenario

**Scenario:** You get an alert that the `/api/v1/users/{id}/with-department` API is throwing 500 errors.

**Without Centralized Logging:**
1.  You guess which service might be failing.
2.  You SSH into the server running the `user-service`.
3.  You `grep` through its log files but find nothing relevant.
4.  You repeat steps 1-3 for the `api-gateway` and `department-service`.
5.  You finally find an error in the `department-service` logs, but it's missing the context of the original user request. This process is slow and painful.

**With Centralized Logging (and Tracing):**
1.  You open Kibana.
2.  You filter logs for the `error` severity level.
3.  You instantly see the error from `department-service`. The log message automatically includes the **Trace ID** from zipkin.
4.  You click the Trace ID to jump directly to the related trace in Zipkin, seeing the full request flow.
5.  You use the same Trace ID to filter all logs across *every service* involved in that specific user request. You see the complete story in seconds.

## The ELK Stack

*   **Elasticsearch:** A distributed, RESTful search and analytics engine. It stores all the log data and enables powerful searching.
*   **Logstash:** A data processing pipeline. It ingests log data from various sources, transforms it (e.g., parses JSON, adds fields), and sends it to Elasticsearch.
    This is usually done by:
    * Writing logs to a file or console and having a **Filebeat** or **Logstash** agent pick them up.**Filebeat** is a lightweight shipper for forwarding and centralizing log data.
    * Sending logs directly to Logstash over a network (e.g., using TCP or HTTP).
*   **Kibana:** A visualization layer. It provides a web UI for searching, viewing, and creating dashboards based on the log data in Elasticsearch.

## How It Works
![ELK Stack Architecture](/resources/elk-stack-flow.jpg)
1. Our Spring Boot applications write structured JSON logs to a file. Sleuth adds `traceId` and `spanId`.
2. **Logstash** parses the JSON log, adds metadata (e.g., service name), and sends it to **Elasticsearch**.
3. We use **Kibana** to explore the logs stored in Elasticsearch, creating powerful queries and dashboards.

## How to Integrate
### 1. Modify the [`docker-compose.yml`](docker-compose.yml) to include the ELK stack services:
```yaml
  elasticsearch:
    container_name: elasticsearch
    image: elasticsearch:8.17.1
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
      - xpack.security.enabled=false # Disable security for local development
      - xpack.monitoring.collection.enabled=true
    volumes:
      - elastic_data:/usr/share/elasticsearch/data/
    ports:
      - "9200:9200"
    restart: unless-stopped
    networks:
      - microservices-net
  logstash:
    container_name: logstash
    image: logstash:8.17.1
    volumes:
      - ./logstash/:/logstash_dir
    environment:
      - LS_JAVA_OPTS=-Xmx256m -Xms256m
    ports:
      - "5044:5044"
    depends_on:
      - elasticsearch
    command: logstash -f /logstash_dir/pipeline/logstash.conf
    networks:
      - microservices-net
    restart: unless-stopped
  kibana:
    container_name: kibana
    image: kibana:8.17.1
    ports:
      - '5601:5601'
    environment:
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - microservices-net
    restart: unless-stopped
```
### 2. Configure Logstash to parse incoming logs
Create a [`logstash.conf`](/logstash/pipeline/logstash.conf) file with the following content:
```plaintext
input {
  tcp {
    port => 5044
    codec => json
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "logs-%{+YYYY.MM.dd}"
  }
}
```
### 3. Configure Application Logging
Configure Spring Boot's Logback to output logs as JSON.

**Add dependency for Logstash Logback encoder:**
```xml
<!-- In each microservice's pom.xml (user, department, api-gateway) -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```
### 4. Update `logback-spring.xml` in each microservice to use JSON format and include Sleuth trace info:
```xml
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5044</destination> <!-- Send logs to Logstash -->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="LOGSTASH" />
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```
### What is Logback Appender?
A Logback appender is a component in Logback (Spring Boot’s default logging framework) responsible for directing log messages 
to a specific destination, such as a file, console, database, or remote logging system like Logstash.

Common Logback Appenders:
* **ConsoleAppender:** Outputs log messages to the console (standard output).
* **FileAppender:** Writes log messages to a specified file.
* **RollingFileAppender:** Similar to FileAppender but supports log file rotation based on size or date.
* **SocketAppender:** Sends log messages over a network socket to a remote server.
* **LogstashTcpSocketAppender:** Specifically designed to send log messages in JSON format to a Logstash instance over TCP.

# Testing Integration
1. Start the ELK stack and microservices:
   ```bash
   docker-compose up -d
   ```
2. Access Kibana at `http://localhost:5601` and configure the index pattern to `logs-*`.
3. Make some API calls to generate logs:
   ```bash
   curl http://localhost:8080/api/v1/users/1/with-department
   ```
4. In Kibana, navigate to the "Discover" tab to see the aggregated logs from all services.
5. Use the search bar to filter logs by `traceId`, `service name`, or `error` level.


## Resources
* [ELK Stack With Docker Guide](https://codingstreams.in/java/spring-boot/logging-and-monitoring/2025/01/26/setup-elk-stack-using-docker-compose.html)
* [ELK Stack Guide](https://www.codingshuttle.com/spring-boot-handbook/microservice-advance-centralized-logging-with-the-elk-stack/)

## Next Step
Now that we have centralized logging set up, the next logical step is to implement monitoring and alerting to proactively manage our microservices.

* [Monitoring And Alerting](https://github.com/MdShohanurRahman/hands-on-spring-microservice/tree/monitor)