# batchsample2 — Spring Cloud Stream Functional Batch Consumer

Demonstrates the **Spring Cloud Stream functional batch consumer** pattern (`Consumer<Message<List<String>>>`), which is the approach used in production at Dynatrace.

The sibling project [`spring-kafka-batch-sample`](../spring-kafka-batch-sample) demonstrates the lower-level `@KafkaListener(batch=true)` approach for comparison.

## How it works

| Component | Class | Role |
|---|---|---|
| Batch consumer | `DemoBatchConsumerConfig` | `@Bean Consumer<Message<List<String>>>` bound to `demo-batch-events` via `spring.cloud.function.definition` |
| Load generator | `DemoLoadGenerator` | Publishes one event every 2 s via `StreamBridge` |

With `max.poll.records: 10` and a 2 s publish interval, the consumer receives batches of 1–10 messages — observable in the logs without overwhelming output.

**Key naming constraint:** `spring.cloud.function.definition: demoBatchConsumer` must exactly match the `@Bean` name. This drives the binding name `demoBatchConsumer-in-0` used throughout `application.yaml`.

## Prerequisites

- Java 21+
- Podman (or Docker) for Kafka and Kafka-UI

## Run

**1. Start infrastructure**

```bash
podman compose up -d
```

**2. Start the application** (port 8082)

```bash
./mvnw spring-boot:run
```

Or with explicit Java 21 (if `JAVA_HOME` is not set to 21):

```bash
JAVA_HOME=$JAVA_HOME_21 ./mvnw spring-boot:run
```

## Load generator API

The generator starts **stopped**. Use the REST API to control it:

```bash
# Start (default 100 ms interval)
curl -X POST "http://localhost:8082/load/start?intervalMs=2000"

# Stop
curl -X POST http://localhost:8082/load/stop

# Status
curl http://localhost:8082/load/status
# {"running":true,"intervalMs":2000}
```

| Parameter    | Default | Description                           |
|--------------|---------|---------------------------------------|
| `intervalMs` | `100`   | Interval between sends (milliseconds) |

## Observe

**Application logs** show the producer and consumer interleaving after `/load/start`:

```
INFO  DemoLoadGenerator      : Load generator started (intervalMs=2000)
INFO  DemoLoadGenerator      : Sent #1: msg-1 at 2026-05-11T... (accepted=true)
INFO  DemoLoadGenerator      : Sent #2: msg-2 at 2026-05-11T... (accepted=true)
INFO  DemoBatchConsumerConfig: Batch received: 2 messages
```

**Kafka-UI** at http://localhost:8080 → cluster `local` → topic `demo-batch-events` shows the published messages and consumer group `demo-batch-events.consumer.batchsample2`.

| Service   | URL                   |
|-----------|-----------------------|
| Kafka     | `localhost:9092`      |
| Kafka-UI  | http://localhost:8080 |
| App       | http://localhost:8082 |

## Test

```bash
./mvnw test
```

The `contextLoads()` test runs against the in-memory test binder — no Kafka needed.
