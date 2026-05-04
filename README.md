# batchsample

A Spring Boot demo showcasing Spring Kafka single-message and batch receive patterns, with an HTTP-controlled load generator.

## Prerequisites

- Java 21+
- Docker (for Kafka and Kafka-UI)

## Infrastructure

Start Kafka and Kafka-UI using Docker Compose:

```bash
docker compose up -d
```

| Service   | URL                    |
|-----------|------------------------|
| Kafka     | `localhost:9092`       |
| Kafka-UI  | http://localhost:8080  |

## Running the Application

### Using Maven (development)

```bash
./mvnw spring-boot:run
```

### Building and running an uber-jar

Package the application into a single executable jar:

```bash
./mvnw clean package -DskipTests
```

The jar is written to `target/batchsample-0.0.1-SNAPSHOT.jar`. Run it with:

```bash
java -jar target/batchsample-0.0.1-SNAPSHOT.jar
```

Override any property at runtime with `--` arguments or `-D` system properties:

```bash
java -jar target/batchsample-0.0.1-SNAPSHOT.jar \
  --spring.kafka.bootstrap-servers=kafka-host:9092 \
  --server.port=9000
```

The application starts on **http://localhost:8081**.

On startup, two topics are automatically created:

| Topic             | Partitions | Consumer group  | Receive mode  |
|-------------------|------------|-----------------|---------------|
| `single-messages` | 3          | `single-group`  | Single record |
| `batch-messages`  | 3          | `batch-group`   | Batch (up to 10 records per poll) |

## Load Generator API

The load generator sends one message to each topic on every tick.  
Message format: `msg-{counter} at {ISO-8601 timestamp}`

### Start

```bash
curl -X POST "http://localhost:8081/load/start?intervalMs=2000"
```

| Parameter    | Default | Description                        |
|--------------|---------|------------------------------------|
| `intervalMs` | `100`   | Interval between sends (milliseconds) |

### Stop

```bash
curl -X POST "http://localhost:8081/load/stop"
```

### Status

```bash
curl http://localhost:8081/load/status
```

Example response:

```json
{"running": true, "intervalMs": 100}
```

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property                              | Default       | Description                         |
|---------------------------------------|---------------|-------------------------------------|
| `server.port`                         | `8081`        | HTTP server port                    |
| `spring.kafka.bootstrap-servers`      | `localhost:9092` | Kafka broker address             |
| `spring.kafka.consumer.auto-offset-reset` | `earliest` | Offset reset strategy             |
| `spring.kafka.consumer.max-poll-records`  | `10`       | Max records per batch poll          |

## Observing Output

Single-message consumer logs each record individually (INFO level):

```
Single received [partition=1, offset=42]: msg-42 at 2026-04-24T10:00:00.123Z
```

Batch consumer logs the batch size on INFO, and individual records on DEBUG:

```
Batch received 10 messages (partitions 0-2)
```

Enable DEBUG logging for detailed per-record output:

```properties
logging.level.com.dynatrace.demo.batchsample=DEBUG
```

## Running Tests

```bash
./mvnw test
```
