# Cloud Autoscaling Simulator (Java + Maven + JavaFX)

## Table of Contents

- [1. Goal](#1-goal)
  - [1.1 What is simulated](#11-what-is-simulated)
  - [1.2 What is intentionally simplified](#12-what-is-intentionally-simplified)
- [2. Module Structure](#2-module-structure)
- [3. Architecture Overview](#3-architecture-overview)
- [4. Autoscaler Design](#4-autoscaler-design)
- [5. State Machine](#5-state-machine)
- [6. Testing Approach](#6-testing-approach)
- [7. Technologies](#7-technologies)

## 1. Goal

This project simulates a simplified "cloud service" handling incoming requests. Requests are generated over time, routed through a load balancer to multiple service instances, processed concurrently, and measured via metrics (latency, throughput, queue lengths). An autoscaler adjusts the number of instances based on system load. A JavaFX GUI visualizes the system behavior in real time.

### 1.1 What is simulated

- incoming requests arriving according to a traffic profile (constant / bursty)
- load balancer routes each request to one service instance (strategy-based)
- each service instance processes requests using a worker pool and a bounded queue
- autoscaler periodically evaluates metrics and scales instances up/down with cooldown to avoid thrashing
- metrics are collected continuously and shown in GUI charts

### 1.2 What is intentionally simplified

- no real networking/HTTP stack
- no real container orchestration
- no persistence/database layer
- time is simulated (tick-based), not real-time accurate to nanoseconds

## 2. Module Structure

- `core` = pure Java, no JavaFX dependency
  - all simulation logic and configuration loading
  - unit-testable without starting the JavaFX toolkit
- `ui` = JavaFX dashboard
  - depends on `core`

## 3. Architecture Overview

- SimulationEngine
  - simulation loop (tick)
  - orchestrates generator → load balancer → instances
  - triggers metrics + autoscaling
  - publishes immutable snapshots for the UI
  - publishes `SIMULATION_STARTED` / `SIMULATION_STOPPED` to the `EventBus`

- SimulationClock
  - tick counter, converts ticks to simulated milliseconds
  - shared by engine and `TrafficGenerator` so both agree on time

- TrafficGenerator
  - generates requests according to the selected profile
  - constant / bursty
  - sets `arrivalTime` from the injected `SimulationClock`
  - sets `serviceTimeMs` from `ConstantServiceTimeModel`

- LoadBalancer
  - selects the target instance
  - e.g. round-robin, least-queue
  - filters to ACTIVE via `AbstractLoadBalancer.activeOnly()` (DRAINING instances are skipped)

- InstanceManager
  - holds all non-swept instances (ACTIVE + DRAINING)
  - safe addition/removal at runtime
  - receives `InstanceConfig` and the shared `LatencyTracker` in the constructor to properly parameterize and measure latency of new instances

- ServiceInstance
  - fixed worker pool
  - bounded queue (backpressure)
  - when full → drop
  - graceful shutdown via `retire()` (drains the queue before terminating)

- MetricsCollector
  - throughput
  - latency
  - queue length
  - utilization
  - owns `LatencyTracker`, the shared instance is injected into `ServiceInstance`
  - owns three `TimeSeriesBuffer`s for chart histories
  - produces the immutable `Snapshot` via `buildSnapshot(tick, instanceSnapshots)`

- AutoScaler
  - scaling policy
  - cooldown
  - scale up/down
  - cadence configurable via `evaluationIntervalTicks`
  - publishes `SCALE_UP` / `SCALE_DOWN` to the `EventBus`
  - logs the decision reason via `ScalingDecision.reason`

- Snapshot
  - immutable record produced once per tick
  - the only data structure crossing the engine → UI
  - carries scalar metrics, history buffers, and per-instance views (ACTIVE + DRAINING)

- EventBus
  - publish/subscribe channel for `SimulationEvent`
  - possibility to subscribe / unsubscribe / publish
  - UI subscribers wrap handlers in `Platform.runLater`

- ConfigLoader / ConfigValidator
  - read JSON files into `SimulationConfigDto`
  - validate ranges and cross-field invariants
  - map DTO → `SimulationConfig` for the engine
  - violations throw `ConfigValidationException`

- JavaFX UI
  - start/stop/reset
  - Load config (JSON file picker) / Save log (export event log)
  - parameter changes via slider, dropdown, spinner
  - charts (latency, throughput, instances)
  - instance table
  - event log (scaling and lifecycle events)
  - reads snapshots (thread-safety via `Platform.runLater`)

## 4. Autoscaler Design

- the `AutoScaler` checks the system every few ticks (`evaluationIntervalTicks` in `SimulationConfig`)
- it looks at the average queue length and decides via `ThresholdScalingPolicy`
  - queues too long (above `scaleUpQueueThreshold`) → `SCALE_UP` → `InstanceManager.addInstance()`
  - queues mostly empty (below `scaleDownQueueThreshold`) → `SCALE_DOWN` → `InstanceManager.retireInstance(id)` on the newest ACTIVE instance
  - otherwise → `NO_ACTION`
- after each scale change a `CooldownTracker` blocks further scaling for `cooldownTicks` (prevents constant up-and-down)
- it never goes below `minInstanceCount` or above `maxInstanceCount` set by the user
  - as a safety net, `InstanceManager` itself always keeps at least one ACTIVE instance
- when `autoscalerEnabled == false` the engine skips the autoscaler entirely
- every decision is recorded with the value that caused


## 5. State Machine

- `IDLE` = no engine thread alive, configuration may be changed
- `RUNNING` = tick loop active, live parameter widgets disabled
- `STOPPED` = engine thread terminated, worker pools drained

Transitions:

- `start()` = `IDLE` → `RUNNING`
- `stop()` = `RUNNING` / `PAUSED` → `STOPPED`
- `reset()` = `STOPPED` → `IDLE`

## 6. Testing Approach

- unit TDD for isolated domain classes (M1–M4)
- integration-first for the engine and autoscaler (M3–M4)
  - integration tests written before the orchestration code
  - engine driven through its public API, not internal state
- UI is not auto-tested — JavaFX components exercised manually

## 7. Technologies

- Java 21 — records, pattern matching, sealed types used throughout
- JavaFX 21 — built-in `LineChart` / `TableView` cover the dashboard requirements without extra UI libraries
- Maven (multi-module) — `javafx-maven-plugin` simplifies running JavaFX from the command line
- JUnit 5.10 — project test framework
- JSON library 2.17 — simple databinding from JSON into `SimulationConfigDto`
- SLF4J 2.0 + Logback 1.5 — facade + production-grade backend
  - logging convention by level (ERROR / WARN / INFO / DEBUG) documented in `.claude/docs/DELIVERY_PLAN.md`
