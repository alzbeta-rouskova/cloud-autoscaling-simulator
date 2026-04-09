# Delivery Plan — Cloud Autoscaling Simulator

**Technologies:** Java 21 · JavaFX 21 · Maven
**Approach:** TDD for domain classes, integration-first for the engine and larger components

---

## Milestones

| Milestone | Content |
|-----------|---------|
| M0 ✅ | Project setup — Maven multi-module skeleton, GitLab repository |
| M1        | Request model + traffic generator |
| M2        | Load balancer + service instances |
| M3        | Metrics + simulation engine (first live simulation, CLI output) |
| M4        | Autoscaler + event log |
| M5        | JSON configuration + JavaFX dashboard |
| M6        | User and technical documentation (GitLab Wiki + JavaDoc) |

---

## M0 — Project setup 

A working Maven multi-module project (`core` + `ui`), an empty JavaFX window, a GitLab repository with the `v0.1` tag.

---

## M1 — Request model + traffic generator

Clean domain classes with no dependency on the engine or UI.

**Classes:** `Request`, `RequestStatus`, `RequestIdGenerator`, `ServiceTimeModel`, `ConstantServiceTimeModel`, `TrafficProfile`, `AbstractTrafficProfile`, `ConstantTrafficProfile`, `BurstyTrafficProfile`, `TrafficGenerator`, `SimulationClock`

**Notes:**
- `Request` does not have a `type` field, all requests are of a single kind with a fixed service time
- `Request.status` is a private `volatile` field; accessed via `getStatus()`, `markDropped()`, `markCompleted()`
- `ServiceTimeModel.serviceTimeMs()` has no parameter, request types do not exist
- `ConstantServiceTimeModel` receives its value via the constructor from `SimulationConfig.serviceTimeMs`
- `TrafficGenerator` receives `SimulationClock` and `ServiceTimeModel` in its constructor; it sets `arrivalTime = clock.simulatedTimeMs()` and `serviceTimeMs = serviceTimeModel.serviceTimeMs()`
- `SimulationClock` is placed here (instead of M3) because `TrafficGenerator` needs it from the beginning

---

## M2 — Load balancer + service instances

Requests flow through the load balancer into the instances, where they are either processed or rejected (dropped).

**Classes:** `LoadBalancer`, `AbstractLoadBalancer`, `RoundRobinLoadBalancer`, `LeastQueueLoadBalancer`, `LoadBalancerType`, `LoadBalancerSelection`, `ServiceInstance`, `InstanceManager`, `InstanceConfig`, `InstanceSnapshot`, `InstanceStatus`, `RequestQueue`

**Notes:**
- `InstanceManager` receives `InstanceConfig` and the shared `LatencyTracker` in its constructor (used when creating new instances via `addInstance()`)
- `ServiceInstance` receives `LatencyTracker` in its constructor (injected by `InstanceManager`, which gets it from `MetricsCollector`)
- `ServiceInstance` has `shutdown()`, `isTerminated()`, `getStatus()`; it does NOT have `tick()`
- `InstanceManager.removeInstance(String instanceId)` - sets the instance to DRAINING, calls `shutdown()`, and immediately removes it from the active list; workers finish in the background; `isTerminated()` is used only in `stop()` for a graceful shutdown; `getInstances()` returns only active instances
- `LoadBalancerSelection.create()` is a static factory method

---

## M3 — Metrics + simulation engine

The full simulation loop. Metrics are logged via SLF4J to the console.

**Classes:** `SimulationEngine`, `SimulationConfig`, `SimulationState`, `Snapshot`, `MetricsCollector`, `LatencyTracker`, `ThroughputTracker`, `TimeSeriesBuffer`

**Notes:**
- `MetricsCollector` owns the shared `LatencyTracker` and exposes it so that `InstanceManager` can inject it into each `ServiceInstance` upon creation
- `ThroughputTracker` receives `tickDurationMs` from `SimulationConfig` via `MetricsCollector`
- `MetricsCollector.buildSnapshot(long tick, List<InstanceSnapshot> instanceSnapshots)` - 2 parameters, no per-type counts
- `Snapshot` contains the historical series `latencyHistory`, `throughputHistory`, `instanceCountHistory` (populated from `TimeSeriesBuffer` inside `MetricsCollector`)

---

## M4 — Autoscaler + event log

The system dynamically adds and removes instances based on the current load.

**Classes:** `AutoScaler`, `ScalingPolicy`, `ThresholdScalingPolicy`, `CooldownTracker`, `ScalingDecision`, `Decision`, `SimulationEvent`, `EventType`, `EventBus`, `SimulationException`, `ConfigValidationException`, `InstanceException`

**Notes:**
- `ScalingDecision` is a record (or class) with a `Decision decision` field and a `String reason`; it provides static factory methods `noAction()`, `scaleUp(double metric)`, `scaleDown(double metric)`
- `Decision` is an enum `{SCALE_UP, SCALE_DOWN, NO_ACTION}` nested inside `ScalingDecision`
- `SimulationEvent.type` is an `EventType` enum instead of a `String`, this eliminates magic strings
- `EventBus.publish()` is called from the engine thread; UI subscribers must wrap their handler in `Platform.runLater()`
- `SimulationEngine` adds `reset()` - a transition back to the `IDLE` state, reinitialization of components, and clearing of metrics

---

## M5 — JSON configuration + JavaFX dashboard

The user loads parameters from a JSON file and controls the simulation through a graphical interface with charts and an instance table.

**Classes:** `SimulationConfigDto`, `ConfigLoader`, `ConfigValidator`, `TrafficProfileType`, `MainApp`, `MainController`, `ControlPanelController`, `ChartsController`, `InstancesTableController`, `UiMapper`, `ChartData`, `InstanceRow`

**Notes:**
- `SimulationConfig` and `SimulationConfigDto` contain a `long serviceTimeMs` field - a fixed service time for all requests
- `SimulationConfig.trafficProfile` is a `TrafficProfileType` enum (CONSTANT, BURSTY) instead of a `String`
- `ChartData` is a holder for a single data point (`double latency`, `double throughput`, `int instanceCount`); the charts manage their own history from `Snapshot.latencyHistory` and so on
- UI dashboard: start / pause / resume / stop / reset, a slider for the traffic rate, a dropdown for the LB strategy, a button for loading the configuration, and an event log

---

## M6 — Documentation

User manual and technical documentation on the GitLab Wiki. JavaDoc in the source code.
