# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System

This project uses **Maven** (no Gradle wrapper present). Use `mvnw` (not `gradle`).

A Java 21 runtime is available at `$env:JAVA_HOME_21` (Windows) / `$JAVA_HOME_21` (bash). Set `JAVA_HOME` to this value before building if the default JDK is not 21:

```powershell
$env:JAVA_HOME = $env:JAVA_HOME_21
```

```bash
./mvnw clean package          # build and package
./mvnw spring-boot:run        # run the application
./mvnw test                   # run all tests
./mvnw -Dtest=ClassName test  # run a single test class
```

## Project Overview

A Dynatrace demo for the **Spring Cloud Stream functional batch consumer** pattern — `Consumer<Message<List<String>>>` — which is the pattern used in production at Dynatrace. The sibling project `spring-kafka-batch-sample` demos the `@KafkaListener` + `setBatchListener(true)` approach for comparison.

- Package root: `com.dynatrace.demo.batchsample2`
- Entry point: `src/main/java/com/dynatrace/demo/batchsample2/Batchsample2Application.java`
- Config: `src/main/resources/application.yaml`
- App port: **8082**

## Architecture

The functional consumer bean `demoBatchConsumer` (in `DemoBatchConsumerConfig`) is bound to the `demo-batch-events` Kafka topic via Spring Cloud Stream.

**Critical naming constraint:** `spring.cloud.function.definition: demoBatchConsumer` must exactly match the `@Bean` name — this drives the binding name `demoBatchConsumer-in-0` used throughout `application.yaml`.

`DemoLoadGenerator` uses `StreamBridge` with `@Scheduled(fixedDelay=2000)` to publish one event every 2 s. With `max.poll.records: 10`, consumers receive batches of 1–10 messages.

## Infrastructure

Single-node KRaft Kafka + Kafka UI via `compose.yaml`. Start with:

```bash
podman compose up -d
```

- Kafka: `localhost:9092`
- Kafka UI: http://localhost:8080
- App: http://localhost:8082
