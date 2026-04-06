# Cloud Autoscaling Simulator (Java + Maven + JavaFX)

## Goal

This project simulates a simplified "cloud service" handling incoming requests. Requests are generated over time, routed through a load balancer to multiple service instances, processed concurrently, and measured via metrics (latency, throughput, queue lengths). An autoscaler adjusts the number of instances based on system load. A JavaFX GUI visualizes the system behavior in real time.

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
  - nastavuje `arrivalTime` z injected `SimulationClock`
  - nastavuje `serviceTimeMs` z `ConstantServiceTimeModel`

- LoadBalancer
  - vybírá cílovou instanci
  - např. round-robin, least-queue

- InstanceManager
  - drží aktivní instance
  - bezpečné přidávání/odebírání za běhu
  - dostane `InstanceConfig` v konstruktoru pro správné parametrizování nových instancí

- ServiceInstance
  - fixed worker pool
  - bounded queue (backpressure)
  - při full → drop
  - graceful shutdown přes `shutdown()` / `isTerminated()`

- MetricsCollector
  - throughput
  - latency
  - queue length
  - utilization
  - vlastní `LatencyTracker`; sdílená instance je injected do `ServiceInstance`

- AutoScaler
  - scaling policy
  - cooldown
  - scale up/down
  - loguje důvod rozhodnutí přes `ScalingDecision.reason`

- JavaFX UI
  - start/pause/resume/stop/reset
  - změna parametrů
  - grafy (latency, throughput, instances)
  - tabulka instancí
  - čte snapshoty (thread-safety přes `Platform.runLater`)

---

## Concurrency Model

- simulation runs in a single engine thread (tick loop)
- each service instance has its own worker pool (`ExecutorService`) to process requests concurrently
- communication uses thread-safe structures (`BlockingQueue`, `AtomicInteger`, `CopyOnWriteArrayList`)
- UI is updated from snapshots via `Platform.runLater(...)` (UI never reads mutable live state directly)
- `EventBus.publish()` is called from the engine thread; UI subscribers must wrap their handler in `Platform.runLater()` to avoid `IllegalStateException`

---

## Scaling Behavior (architecture-focused)

Autoscaling evaluates the system every N ticks:

- scale up if average queue length or utilization is above a threshold
- scale down if the system is underutilized and queues are near empty

A cooldown mechanism (tick-based) prevents rapid scale oscillation ("thrashing").

`ScalingDecision` je record obsahující `Decision decision` a `String reason`, takže každé rozhodnutí nese i důvod (např. `"avgQueue=9.2"`). Factory metody `noAction()`, `scaleUp(double metric)`, `scaleDown(double metric)` zajišťují konzistentní formát zpráv.

---

## GUI

### Planned UI

- controls: start/pause/resume/stop/reset, traffic rate slider, LB strategy dropdown, load config
- charts: avg latency, throughput, instance count
- table: instances with queue length, active workers, processed count, dropped count
- event log (scale up/down decisions with reason)

---

## Roadmap

- MVP 1: core simulation (generator + LB + instances) + basic metrics (CLI)
- MVP 2: autoscaling with cooldown
- MVP 3: JavaFX GUI + charts + instance table
- Polish: export CSV, config load/save, additional strategies

---

## Class Design (~57 tříd/typů)

### 1) core (9)

- SimulationEngine — `start/pause/resume/stop/reset`; `setOnSnapshotReady`
- SimulationConfig — konfigurace simulace; `trafficProfile` je `TrafficProfileType`; obsahuje `requestDistribution`
- SimulationClock — tick čítač a simulovaný čas v ms
- SimulationState (enum) — `IDLE / RUNNING / PAUSED / STOPPED`
- Snapshot (immutable record for UI) — skalární metriky + `latencyHistory`, `throughputHistory`, `instanceCountHistory` pro grafy; bez breakdown per typ requestu
- SimulationEvent (value object for log) — `tick`, `EventType type`, `message`
- EventBus (simple publish-subscribe for UI/log) — `publish()` volá se z engine threadu; UI handlers musí použít `Platform.runLater()`
- TrafficProfileType (enum) — `CONSTANT / BURSTY`; type-safe alternativa k `String`
- EventType (enum) — `SCALE_UP / SCALE_DOWN / CONFIG_LOADED / SIMULATION_STARTED / SIMULATION_STOPPED`

### 2) traffic (5)

- TrafficGenerator — dostane `SimulationClock` a `ServiceTimeModel` v konstruktoru
- TrafficProfile (interface)
- AbstractTrafficProfile (abstract — sdílí `baseRate`; deklaruje `abstract int requestsForTick(int tick)`)
- ConstantTrafficProfile (extends AbstractTrafficProfile)
- BurstyTrafficProfile (extends AbstractTrafficProfile)

### 3) request model (4)

- Request — `status` je `private volatile`; přístup přes `getStatus()`, `markDropped()`, `markCompleted()`; bez pole `type`
- RequestStatus (enum)
- RequestIdGenerator
- ServiceTimeModel (interface) — `long serviceTimeMs()` bez parametru
- ConstantServiceTimeModel (implements ServiceTimeModel) — vrací fixní hodnotu z `SimulationConfig.serviceTimeMs`

### 4) load balancing (6)

- LoadBalancer (interface)
- AbstractLoadBalancer (abstract — validace prázdného listu)
- RoundRobinLoadBalancer (extends AbstractLoadBalancer)
- LeastQueueLoadBalancer (extends AbstractLoadBalancer) — přeskakuje instance ve stavu `DRAINING` přes `getStatus()`
- LoadBalancerType (enum)
- LoadBalancerSelection (factory) — `create()` je statická metoda

### 5) instances (6)

- ServiceInstance — přidá `shutdown()`, `isTerminated()`, `getStatus()`; `droppedCount` deleguje z `RequestQueue`
- InstanceManager — dostane `InstanceConfig` v konstruktoru; `removeInstance(String instanceId)` pro explicitní výběr
- InstanceConfig
- InstanceStatus (enum: ACTIVE, DRAINING)
- InstanceSnapshot (immutable DTO for UI)
- RequestQueue (bounded queue with drop metrics)

### 6) autoscaling (6)

- AutoScaler
- ScalingPolicy (interface)
- ThresholdScalingPolicy
- CooldownTracker
- ScalingDecision (record) — `Decision decision`, `String reason`; factory metody `noAction()`, `scaleUp(double)`, `scaleDown(double)`
- Decision (enum: SCALE_UP, SCALE_DOWN, NO_ACTION) — vnořený v `ScalingDecision`

### 7) metrics (4)

- MetricsCollector — vlastní `LatencyTracker`; `buildSnapshot(int tick, List<InstanceSnapshot>)`; dostane `tickDurationMs` ze `SimulationConfig`
- LatencyTracker — sdílená instance injected do `ServiceInstance`
- ThroughputTracker — dostane `tickDurationMs` v konstruktoru
- TimeSeriesBuffer (ring buffer for chart data)

### 8) config (3)

- SimulationConfigDto (Jackson-serializable POJO) — obsahuje `long serviceTimeMs` pro fixní dobu zpracování
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
- ChartData (DTO: jeden datový bod pro grafy — `double latency`, `double throughput`, `int instanceCount`; grafy si historii plní samy z `Snapshot.latencyHistory` apod.)
- InstanceRow (DTO: řádek tabulky instancí)
- UiMapper (Snapshot → UI mapping)

Total: ~57 tříd/typů (včetně rozhraní, abstraktních tříd a enumů).

---

## MVP Scope

### Must

- tick-based engine

- korektní start/pause/resume/stop/reset simulace
  - zastavení generátoru
  - dokončení/ukončení worker poolů (graceful shutdown přes `ServiceInstance.shutdown()`)
  - reset přes `SimulationEngine.reset()` vrátí systém do stavu `IDLE`

- request model
  - id
  - arrivalTime (nastaveno z `SimulationClock.simulatedTimeMs()`)
  - serviceTimeMs
  - type

- traffic generator
  - constant rate
  - bursty (ON/OFF nebo „periodické špičky")

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
  - loguje důvod rozhodnutí v `ScalingDecision.reason`

- UI (minimal dashboard)
  - start/stop/reset
  - 2–3 grafy (request rate, latency, instances)
  - pár live hodnot (dropped, queue, throughput)

- simple event log (scale decisions with reason)

- immutable `Snapshot` (UI čte jen snapshoty přes `Platform.runLater`, nikdy ne leze do live struktur)

### Should

- percentily latence
  - p50   // maybe not necessary
  - p95

- hysteresis pro autoscaler
  - jiné prahy pro up vs down (scaleUpQueueThreshold ≠ scaleDownQueueThreshold)

- JSON konfigurace scénáře (načtení parametrů simulace ze souboru včetně `requestDistribution`)

### Could

- chaos mode (kill instance randomly)
- additional LB strategy (least-active)
- load test scenarios as preset profiles
- export metrik do CSV
