# Plan: Spring Cloud Stream Kafka Batch Consumer Demo

## Context

This project (`batchsample2`) is a blank Spring Boot 4.0.6 scaffold intended to demo Kafka batch consumption. The sibling project `spring-kafka-batch-sample` already shows the `@KafkaListener` + `setBatchListener(true)` approach. This sample will instead demo the **Spring Cloud Stream functional batch consumer** pattern — `Consumer<Message<List<String>>>` — which is the pattern used in production at Dynatrace.

The compose.yaml (single-node KRaft Kafka + Kafka UI) lives in the sibling project and will be copied into this project so it is self-contained.

---

## Implementation Steps

### 1. Copy `compose.yaml`
Copy `C:\workspaces\projects\demos-for-vi-and-epic\spring-kafka-batch-sample\compose.yaml` → `C:\workspaces\projects\demos-for-vi-and-epic\spring-kafka-batch-sample-2\compose.yaml`

### 2. Update `pom.xml`
File: `pom.xml`

Add Spring Cloud BOM in `<dependencyManagement>`:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>2025.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
> If `2025.0.0` is not in Maven Central yet (Boot 4.0.x is recent), add the Spring Milestone repo and try `2025.0.0-M2` or latest available milestone.

Add dependencies:
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-stream-kafka</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-test-binder</artifactId>
  <scope>test</scope>
</dependency>
```

After editing, verify resolution: `./mvnw dependency:tree -Dincludes=org.springframework.cloud`

### 3. Replace `application.properties` → `application.yaml`
File: `src/main/resources/application.yaml` (delete `application.properties`)

```yaml
spring:
  application:
    name: batchsample2

  cloud:
    function:
      definition: demoBatchConsumer
    stream:
      bindings:
        demoBatchConsumer-in-0:
          destination: demo-batch-events
          group: demo-batch-events.consumer.batchsample2
          consumer:
            batch-mode: true
            use-native-decoding: false
            maxAttempts: 3
            concurrency: 1
      kafka:
        binder:
          brokers: localhost:9092
          auto-create-topics: true
        bindings:
          demoBatchConsumer-in-0:
            consumer:
              configuration:
                max.poll.records: 10
                max.poll.interval.ms: 10000
                fetch.max.bytes: 1048576

server:
  port: 8082
```

Key decisions:
- `use-native-decoding: false` — Spring handles String deserialization; no custom deserializer config needed
- `max.poll.records: 10` + load generator at 2s interval → batches of 1–10 messages, ideal for demo
- Port 8082 avoids conflicts with kafka-ui (8080) and sibling project (8081)

### 4. Add `@EnableScheduling` to main class
File: `src/main/java/com/dynatrace/demo/batchsample2/Batchsample2Application.java`

```java
@SpringBootApplication
@EnableScheduling
public class Batchsample2Application { ... }
```

### 5. Create consumer configuration
File: `src/main/java/com/dynatrace/demo/batchsample2/DemoBatchConsumerConfig.java`

```java
@Configuration
public class DemoBatchConsumerConfig {
    private static final Logger log = LoggerFactory.getLogger(DemoBatchConsumerConfig.class);

    @Bean
    public Consumer<Message<List<String>>> demoBatchConsumer() {
        return message -> {
            List<String> payloads = message.getPayload();
            log.info("Batch received: {} messages", payloads.size());
            payloads.forEach(payload -> log.debug("  -> {}", payload));
        };
    }
}
```

> The bean name `demoBatchConsumer` **must** exactly match `spring.cloud.function.definition` — this drives the binding name `demoBatchConsumer-in-0`.

### 6. Create load generator
File: `src/main/java/com/dynatrace/demo/batchsample2/DemoLoadGenerator.java`

Uses `StreamBridge` (Spring Cloud Stream's imperative send API) with `@Scheduled`:

```java
@Component
public class DemoLoadGenerator {
    private static final Logger log = LoggerFactory.getLogger(DemoLoadGenerator.class);
    private static final String DESTINATION = "demo-batch-events";
    private final StreamBridge streamBridge;
    private final AtomicLong counter = new AtomicLong(0);

    public DemoLoadGenerator(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Scheduled(fixedDelay = 2000)
    public void sendMessage() {
        long n = counter.incrementAndGet();
        String payload = "demo-event-" + n + " at " + Instant.now();
        boolean sent = streamBridge.send(DESTINATION, payload);
        log.info("Sent #{}: {} (accepted={})", n, payload, sent);
    }
}
```

### 7. Update `CLAUDE.md`
Update the existing `CLAUDE.md` to reflect the actual implementation:
- Document the Spring Cloud Stream + Kafka batch consumer architecture
- Add infrastructure start command: `docker compose up -d`
- Note kafka-ui at http://localhost:8080, app at http://localhost:8082
- Remove the stale "Architecture Intent" placeholder section
- Note the `spring.cloud.function.definition` ↔ bean name ↔ binding name constraint

---

## Verification

1. `./mvnw test` — `contextLoads()` passes via in-memory test binder (no Kafka needed)
2. `docker compose up -d` — starts Kafka + kafka-ui
3. `./mvnw spring-boot:run` — app starts on port 8082
4. Watch logs for: `Sent #N:` lines and `Batch received: N messages` lines
5. Open http://localhost:8080 → kafka-ui → verify topic `demo-batch-events` exists with messages
