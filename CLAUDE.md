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

A Dynatrace demo project scaffolded with **Spring Boot 4.0.6** and **Java 21**. The name `spring-kafka-batch-sample-2` indicates the intent to demonstrate Spring Kafka with batch processing, but as of the initial scaffold only `spring-boot-starter` is included — Kafka and Spring Batch dependencies have not yet been added to `pom.xml`.

- Package root: `com.dynatrace.demo.batchsample2`
- Entry point: `src/main/java/com/dynatrace/demo/batchsample2/Batchsample2Application.java`
- Config: `src/main/resources/application.properties`

## Architecture Intent

When Kafka/Batch dependencies are added, the expected pattern for this type of demo is:
- A Kafka consumer that receives messages in batch mode (`BatchListener`)
- Spring Batch jobs triggered by incoming Kafka messages or processing batched records
- Dynatrace observability instrumentation (traces, metrics) over the batch processing pipeline
