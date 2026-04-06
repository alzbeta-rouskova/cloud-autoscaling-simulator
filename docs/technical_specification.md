# Cloud Autoscaling Simulator (Java + Maven + JavaFX)

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
- communication uses thread-safe structures (`BlockingQueue`, `AtomicInteger`, `CopyOnWriteArrayList`)
- UI is updated from snapshots via `Platform.runLater(...)` (UI never reads mutable live state directly)

---

## Scaling Behavior (architecture-focused)

Autoscaling evaluates the system every N ticks:

- scale up if average queue length or utilization is above a threshold
- scale down if the system is underutilized and queues are near empty

A cooldown mechanism (tick-based) prevents rapid scale oscillation (“thrashing”).

---

## GUI

### Planned UI

- controls: start/pause/resume/stop/reset, traffic rate slider, LB strategy dropdown, load config
- charts: avg latency, throughput, instance count
- table: instances with queue length, active workers, processed count, dropped count
- event log (scale up/down decisions)

---

## Roadmap

- MVP 1: core simulation (generator + LB + instances) + basic metrics (CLI)
- MVP 2: autoscaling with cooldown
- MVP 3: JavaFX GUI + charts + instance table
- Polish: export CSV, config load/save, additional strategies

---

## Class Design (~52 tříd/typů)

### 1) core (7)

- SimulationEngine
- SimulationConfig
- SimulationClock
- SimulationState (enum)
- Snapshot (immutable record for UI)
- SimulationEvent (value object for log)
- EventBus (simple publish-subscribe for UI/log)

### 2) traffic (5)

- TrafficGenerator
- TrafficProfile (interface)
- AbstractTrafficProfile (abstract — sdílí baseRate)
- ConstantTrafficProfile (extends AbstractTrafficProfile)
- BurstyTrafficProfile (extends AbstractTrafficProfile)

### 3) request model (8)

- Request
- RequestStatus (enum)
- RequestType (enum: LIGHT, MEDIUM, HEAVY)
- RequestIdGenerator
- ServiceTimeModel (interface)
- ConstantServiceTimeModel (implements ServiceTimeModel)
- DistributedServiceTimeModel (implements ServiceTimeModel)
- RequestTypeSelector (váhový výběr typu + serviceTimeMs per typ)

### 4) load balancing (6)

- LoadBalancer (interface)
- AbstractLoadBalancer (abstract — validace prázdného listu)
- RoundRobinLoadBalancer (extends AbstractLoadBalancer)
- LeastQueueLoadBalancer (extends AbstractLoadBalancer)
- LoadBalancerType (enum)
- LoadBalancerSelection (factory)

### 5) instances (6)

- ServiceInstance
- InstanceManager
- InstanceConfig
- InstanceStatus (enum: ACTIVE, DRAINING)
- InstanceSnapshot (immutable DTO for UI)
- RequestQueue (bounded queue with drop metrics)

### 6) autoscaling (5)

- AutoScaler
- ScalingPolicy (interface)
- ThresholdScalingPolicy
- CooldownTracker
- ScalingDecision (enum)

### 7) metrics (4)

- MetricsCollector (aggregates trackers, computes utilization)
- LatencyTracker
- ThroughputTracker
- TimeSeriesBuffer (ring buffer for chart data)

### 8) config (3)

- SimulationConfigDto (Jackson-serializable POJO)
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
- ChartData (DTO: data pro grafy)
- InstanceRow (DTO: řádek tabulky instancí)
- UiMapper (Snapshot → UI mapping)

Total: ~57 tříd/typů (včetně rozhraní, abstraktních tříd a enumů).

---

## MVP Scope

### Must

- tick-based engine

- korektní start/pause/resume/stop simulace
  - zastavení generátoru
  - dokončení/ukončení worker poolů (graceful shutdown)
  - zastavení autoscaler evaluace (žádný separátní timer — evaluuje se v engine tick loop)

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
  - least-queue

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
  - periodicky (každých N ticků, konfigurovatelné přes `autoscalerEvaluationIntervalTicks`)
  - scale up/down podle `avgQueueLength` (konfigurovatelné prahy `scaleUpQueueThreshold` / `scaleDownQueueThreshold`)
  - cooldown (konfigurovatelné přes `cooldownTicks`)

- UI (minimal dashboard)  
  - start/stop  
  - 2–3 grafy (request rate, latency, instances)  
  - pár live hodnot (dropped, queue, throughput)  

- simple event log (scale decisions)

- immutable `Snapshot` (UI čte jen snapshoty přes `Platform.runLater`, nikdy ne leze do live struktur)

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
  - jiné prahy pro up vs down (scaleUpQueueThreshold ≠ scaleDownQueueThreshold)

- JSON konfigurace scénáře (načtení parametrů simulace ze souboru)

### Could

- chaos mode (kill instance randomly)
- additional LB strategy (least-active)
- load test scenarios as preset profiles
- export metrik do CSV
