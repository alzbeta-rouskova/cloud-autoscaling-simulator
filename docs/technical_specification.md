# Mini Cloud Autoscaling Simulator (Java + Maven + JavaFX)

## Goal

This project simulates a simplified “cloud service” handling incoming requests. Requests are generated over time, routed through a load balancer to multiple service instances, processed concurrently, and measured via metrics (latency, throughput, queue lengths). An autoscaler adjusts the number of instances based on system load. A JavaFX GUI visualizes the system behavior in real time.

---

### Why this project

- learn core cloud concepts by building them: load balancing, scaling, queueing, backpressure
- practice clean architecture: separation of simulation core and UI
- practice concurrency in Java using `ExecutorService`, concurrent queues, and snapshot-based UI updates
- produce a portfolio-friendly demo app (interactive GUI + metrics + strategies)

---

### What is simulated

- incoming requests arriving according to a traffic profile (constant / bursty)
- load balancer routes each request to one service instance (strategy-based)
- each service instance processes requests using a worker pool and a bounded queue
- autoscaler periodically evaluates metrics and scales instances up/down with cooldown to avoid thrashing
- metrics are collected continuously and shown in GUI charts

---

### What is intentionally simplified

- no real networking/HTTP stack
- no real container orchestration
- no persistence/database layer
- time is simulated (tick-based), not real-time accurate to nanoseconds

---

## Architecture Overview

- SimulationEngine  
  - simulation loop (tick)  
  - orchestruje generator → load balancer → instances  
  - spouští metrics + autoscaling  
  - publikuje immutable snapshoty pro UI  

- TrafficGenerator  
  - generuje requesty podle profilu  
  - constant / bursty  

- LoadBalancer  
  - vybírá cílovou instanci  
  - např. round-robin, least-queue  

- InstanceManager  
  - drží aktivní instance  
  - bezpečné přidávání/odebírání za běhu  

- ServiceInstance  
  - fixed worker pool  
  - bounded queue (backpressure)  
  - při full → drop  

- MetricsCollector  
  - throughput  
  - latency  
  - queue length  
  - utilization  

- AutoScaler  
  - scaling policy  
  - cooldown  
  - scale up/down  

- JavaFX UI  
  - start/stop  
  - změna parametrů  
  - grafy (latency, throughput, instances)  
  - tabulka instancí  
  - čte snapshoty (thread-safety)  

---

## Concurrency Model

- simulation runs in a single engine thread (tick loop)
- each service instance has its own worker pool (`ExecutorService`) to process requests concurrently
- communication uses thread-safe structures (`BlockingQueue`, `ConcurrentHashMap`, atomic counters)
- UI is updated from snapshots via `Platform.runLater(...)` (UI never reads mutable live state directly)

---

## Scaling Behavior (architecture-focused)

Autoscaling evaluates the system every N ticks:

- scale up if average queue length or utilization is above a threshold
- scale down if the system is underutilized and queues are near empty

A cooldown timer prevents rapid scale oscillation (“thrashing”).

---

## GUI

### Planned UI

- controls: start/pause/reset, traffic rate slider, service time slider, LB strategy dropdown
- charts: avg latency, throughput, instance count
- table: instances with queue length, active workers, processed count
- optional: event log (scale up/down decisions)

---

## Roadmap

- MVP 1: core simulation (generator + LB + instances) + basic metrics (CLI)
- MVP 2: autoscaling with cooldown
- MVP 3: JavaFX GUI + charts + instance table
- Polish: export CSV, config load/save, additional strategies

---

## Class Design (25–30 classes, realistic scope)

### 1) core (7–8)

- SimulationEngine  
- SimulationConfig  
- SimulationClock  
- SimulationState (enum)  
- Snapshot (immutable for UI)  
- SnapshotBuilder  
- SimulationEvent (value object for log)  
- EventBus (simple publish-subscribe for UI/log)

### 2) traffic (4)

- TrafficGenerator  
- TrafficProfile (interface)  
- ConstantTrafficProfile  
- BurstyTrafficProfile  

### 3) request model (3–4)

- Request  
- RequestStatus (enum)  
- RequestIdGenerator  
- ServiceTimeModel (e.g., constant or simple random-based)

### 4) load balancing (5)

- LoadBalancer (interface)  
- RoundRobinLoadBalancer  
- LeastQueueLoadBalancer  
- LoadBalancerType (enum)  
- LoadBalancerFactory  

### 5) instances (5–6)

- ServiceInstance  
- InstanceManager  
- InstanceConfig  
- InstanceSnapshot (DTO for UI)  
- RequestQueue (wrapper over `BlockingQueue` for metrics/limits)  
- InstanceLifecycle (start/stop hooks)

### 6) autoscaling (4–5)

- AutoScaler  
- ScalingPolicy (interface)  
- ThresholdScalingPolicy  
- CooldownTracker  
- ScalingDecision (enum / value object)

### 7) metrics (4–5)

- MetricsCollector  
- LatencyTracker  
- ThroughputTracker  
- UtilizationTracker  
- TimeSeriesBuffer (stores data for charts)

### 8) UI (4–6)

- MainApp  
- MainController  
- ControlPanelController  
- ChartsController  
- InstancesTableController  
- UiMapper (Snapshot → UI mapping)

Total: ~28 classes (realistic, not artificially inflated).

---

## MVP Scope (20–30 hours, architecture-focused)

### Must

- tick-based engine

- korektní start/stop simulace
  - zastavení generátoru
  - dokončení/ukončení worker poolů (graceful shutdown)
  - vypnutí autoscaler timeru

- request model  
  - id  
  - arrivalTime  
  - serviceTimeMs  
  - type  

- traffic generator  
  - constant rate  
  - bursty (ON/OFF nebo „periodické špičky“)  

- load balancer  
  - round-robin  

- service instance  
  - bounded queue (kapacita N)  
  - worker pool (fixed thread pool)  
  - když full → drop + metrika  

- metrics  
  - throughput (req/s)  
  - avg latency  
  - queue length (avg + current)  
  - dropped count/rate  
  - number of instances  

- autoscaler  
  - periodicky (např. 2 s)  
  - scale up/down podle 1 metriky (např. avg queue nebo p95 latency)  
  - cooldown  

- UI (minimal dashboard)  
  - start/stop  
  - 2–3 grafy (request rate, latency, instances)  
  - pár live hodnot (dropped, queue, throughput)  

- simple event log (scale decisions)

- immutable MetricsSnapshot (UI čte jen snapshoty, nikdy ne leze do live struktur)

### Should

- service time distribuce  

- mix typů requestů  
  - např. 80% light  
  - 15% medium  
  - 5% heavy  

- percentily latence  
  - p50   // maybe not necessary
  - p95  

- hysteresis pro autoscaler  
  - jiné prahy pro up vs down

- load balancer
  - add another methods

### Could

- chaos mode (kill instance randomly)
- additional LB strategy (least-active)
- load test scenarios as preset profiles
- least-queue strategie LB  
- export metrik do CSV  
- konfigurace scénáře (JSON)  
