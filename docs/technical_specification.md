# Cloud Autoscaling Simulator (Java + Maven + JavaFX)

## Goal

This project simulates a simplified "cloud service" handling incoming requests. Requests are generated over time, routed through a load balancer to multiple service instances, processed concurrently, and measured via metrics (latency, throughput, queue lengths). An autoscaler adjusts the number of instances based on system load. A JavaFX GUI visualizes the system behavior in real time.

### What is simulated

- incoming requests arriving according to a traffic profile (constant / bursty)
- load balancer routes each request to one service instance (strategy-based)
- each service instance processes requests using a worker pool and a bounded queue
- autoscaler periodically evaluates metrics and scales instances up/down with cooldown to avoid thrashing
- metrics are collected continuously and shown in GUI charts

### What is intentionally simplified

- no real networking/HTTP stack
- no real container orchestration
- no persistence/database layer
- time is simulated (tick-based), not real-time accurate to nanoseconds

## Architecture Overview

- SimulationEngine
  - simulation loop (tick)
  - orchestrates generator → load balancer → instances
  - triggers metrics + autoscaling
  - publishes immutable snapshots for the UI

- TrafficGenerator
  - generates requests according to the selected profile
  - constant / bursty
  - sets `arrivalTime` from the injected `SimulationClock`
  - sets `serviceTimeMs` from `ConstantServiceTimeModel`

- LoadBalancer
  - selects the target instance
  - e.g. round-robin, least-queue

- InstanceManager
  - holds the active instances
  - safe addition/removal at runtime
  - receives `InstanceConfig` and the shared `LatencyTracker` in the constructor to properly parameterize and measure latency of new instances

- ServiceInstance
  - fixed worker pool
  - bounded queue (backpressure)
  - when full → drop
  - graceful shutdown via `retire()` / `isTerminated()`

- MetricsCollector
  - throughput
  - latency
  - queue length
  - utilization
  - owns `LatencyTracker`; the shared instance is injected into `ServiceInstance`

- AutoScaler
  - scaling policy
  - cooldown
  - scale up/down
  - logs the decision reason via `ScalingDecision.reason`

- JavaFX UI
  - start/pause/resume/stop/reset
  - parameter changes
  - charts (latency, throughput, instances)
  - instance table
  - reads snapshots (thread-safety via `Platform.runLater`)

## Scaling Behavior

Autoscaling evaluates the system every N ticks:

- scale up if average queue length or utilization is above a threshold
- scale down if the system is underutilized and queues are near empty

A cooldown mechanism (tick-based) prevents rapid scale oscillation ("thrashing").

## GUI

### Planned UI

- controls: start/pause/resume/stop/reset, traffic rate slider, LB strategy dropdown, load config
- charts: avg latency, throughput, instance count
- table: instances with queue length, active workers, processed count, dropped count
- event log (scale up/down decisions with reason)

## Class Design (~55 classes/types)

### 1) core (9)

- SimulationEngine — `start/pause/resume/stop/reset`; `setOnSnapshotReady`
- SimulationConfig — simulation configuration; `trafficProfile` is a `TrafficProfileType`; `serviceTimeMs` is the fixed request processing time
- SimulationClock — `advance()` moves the tick forward, `tick()` returns the current value, `simulatedTimeMs()` converts it to ms
- SimulationState (enum) — `IDLE / RUNNING / PAUSED / STOPPED`
- Snapshot (immutable record for UI) — scalar metrics + `latencyHistory`, `throughputHistory`, `instanceCountHistory` for charts; no breakdown per request type
- SimulationEvent (value object for log) — `tick`, `EventType type`, `message`
- EventBus (simple publish-subscribe for UI/log) — `publish()` is called from the engine thread; UI handlers must use `Platform.runLater()`
- TrafficProfileType (enum) — `CONSTANT / BURSTY`; a type-safe alternative to `String`
- EventType (enum) — `SCALE_UP / SCALE_DOWN / CONFIG_LOADED / SIMULATION_STARTED / SIMULATION_STOPPED`

### 2) traffic (5)

- TrafficGenerator — receives `SimulationClock` and `ServiceTimeModel` in the constructor
- TrafficProfile (interface)
- AbstractTrafficProfile (abstract — shares `baseRate`; declares `abstract int requestsForTick(long tick)`)
- ConstantTrafficProfile (extends AbstractTrafficProfile)
- BurstyTrafficProfile (extends AbstractTrafficProfile)

### 3) request model (5)

- Request — `status` is `private volatile`; accessed via `getStatus()`, `markProcessing()`, `markCompleted()`, `markDropped()`; no `type` field
- RequestStatus (enum)
- RequestIdGenerator
- ServiceTimeModel (interface) — `long serviceTimeMs()` with no parameter
- ConstantServiceTimeModel (implements ServiceTimeModel) — returns a fixed value from `SimulationConfig.serviceTimeMs`

### 4) load balancing (6)

- LoadBalancer (interface)
- AbstractLoadBalancer (abstract — validates an empty list)
- RoundRobinLoadBalancer (extends AbstractLoadBalancer)
- LeastQueueLoadBalancer (extends AbstractLoadBalancer) — skips instances in the `DRAINING` state via `getStatus()`
- LoadBalancerType (enum)
- LoadBalancerSelection (factory) — `create()` is a static method

### 5) instances (6)

- ServiceInstance — single retirement entry point `retire()` (atomically marks DRAINING, signals workers, shuts down pool); also `isTerminated()`, `getStatus()`; does NOT have `tick()`; `droppedCount` is delegated from `RequestQueue`
- InstanceManager — receives `InstanceConfig` and the shared `LatencyTracker` in the constructor; `retireInstance(String instanceId)`: calls `instance.retire()` (marks DRAINING + shuts down pool); the instance remains in the list until its pool terminates, at which point `sweepTerminated()` (called by the engine each tick) removes it; `getInstances()` returns all non-swept instances (ACTIVE + DRAINING) — load balancers filter to ACTIVE via `AbstractLoadBalancer.activeOnly()`; `isTerminated()` is also used in `stop()`
- InstanceConfig
- InstanceStatus (enum: ACTIVE, DRAINING)
- InstanceSnapshot (immutable DTO for UI) — contains `workerCount` for utilization calculation in `MetricsCollector`
- RequestQueue (bounded queue with drop metrics)

### 6) autoscaling (6)

- AutoScaler
- ScalingPolicy (interface)
- ThresholdScalingPolicy
- CooldownTracker
- ScalingDecision (record) — `Decision decision`, `String reason`; factory methods `noAction()`, `scaleUp(double)`, `scaleDown(double)`
- Decision (enum: SCALE_UP, SCALE_DOWN, NO_ACTION) — nested inside `ScalingDecision`

### 7) metrics (4)

- MetricsCollector — owns `LatencyTracker`; `buildSnapshot(long tick, List<InstanceSnapshot>)`; receives `tickDurationMs` from `SimulationConfig`
- LatencyTracker — the shared instance is injected into `ServiceInstance`
- ThroughputTracker — receives `tickDurationMs` in the constructor
- TimeSeriesBuffer (ring buffer for chart data)

### 8) config (3)

- SimulationConfigDto (Jackson-serializable POJO) — contains `long serviceTimeMs` for the fixed processing time
- ConfigLoader
- ConfigValidator

### 9) exceptions (3)

- SimulationException (extends RuntimeException — base)
- ConfigValidationException (extends SimulationException)
- InstanceException (extends SimulationException)

### 10) UI (8)

- MainApp
- MainController
- ControlPanelController
- ChartsController
- InstancesTableController
- ChartData (DTO: a single data point for charts — `double latency`, `double throughput`, `int instanceCount`; charts manage their own history from `Snapshot.latencyHistory` etc.)
- InstanceRow (DTO: a row in the instance table)
- UiMapper (Snapshot → UI mapping)

## MVP Scope

### Must

- tick-based engine

- correct start/pause/resume/stop/reset of the simulation
  - stopping the generator
  - finishing/terminating worker pools (graceful shutdown via `ServiceInstance.shutdown()`)
  - reset via `SimulationEngine.reset()` returns the system to the `IDLE` state

- request model
  - id
  - arrivalTime (set from `SimulationClock.simulatedTimeMs()`)
  - serviceTimeMs

- traffic generator
  - constant rate
  - bursty (ON/OFF or "periodic spikes")

- load balancer
  - round-robin
  - least-queue

- service instance
  - bounded queue (capacity N)
  - worker pool (fixed thread pool)
  - when full → drop + metric

- metrics
  - throughput (req/s)
  - avg latency
  - queue length (avg + current)
  - dropped count/rate
  - number of instances

- autoscaler
  - periodically (every N ticks, configurable via `autoscalerEvaluationIntervalTicks`)
  - scale up/down based on `avgQueueLength` (configurable thresholds `scaleUpQueueThreshold` / `scaleDownQueueThreshold`)
  - cooldown (configurable via `cooldownTicks`)
  - logs the decision reason in `ScalingDecision.reason`

- UI (minimal dashboard)
  - start/stop/reset
  - 2–3 charts (request rate, latency, instances)
  - a few live values (dropped, queue, throughput)

- simple event log (scale decisions with reason)

- immutable `Snapshot` (the UI reads only snapshots, never accessing live structures directly)

### Should

- latency percentiles
  - p50   // maybe not necessary
  - p95

- autoscaler hysteresis
  - different thresholds for up vs down (scaleUpQueueThreshold ≠ scaleDownQueueThreshold)

- JSON scenario configuration (loading simulation parameters from a file)

### Could

- chaos mode (kill instance randomly)
- additional LB strategy (least-active)
- load test scenarios as preset profiles
- export metrics to CSV
