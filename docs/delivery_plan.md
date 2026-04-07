# Delivery Plan — Cloud Autoscaling Simulator

**Technologie:** Java 21 · JavaFX 21 · Maven
**Přístup:** TDD pro doménové třídy, integration-first pro engine a větší celky

---

## Milestony

| Milestone | Obsah |
|-----------|-------|-------|
| M0 | Project setup — Maven multi-module skeleton, GitLab repo |
| M1 | Request model + traffic generator |
| M2 | Load balancer + service instances |
| M3 | Metrics + simulation engine (první živá simulace, CLI výstup) |
| M4 | Autoscaler + event log |
| M5 | JSON konfigurace + JavaFX dashboard |
| M6 | Uživatelská a technická dokumentace (GitLab Wiki + JavaDoc) |

---

## M0 — Project setup ✅

Funkční Maven multi-module projekt (`core` + `ui`), prázdné JavaFX okno, GitLab repozitář s tagem `v0.1`.

---

## M1 — Request model + traffic generator

Čisté doménové třídy bez závislosti na engine nebo UI.

**Třídy:** `Request`, `RequestStatus`, `RequestIdGenerator`, `ServiceTimeModel`, `ConstantServiceTimeModel`, `TrafficProfile`, `AbstractTrafficProfile`, `ConstantTrafficProfile`, `BurstyTrafficProfile`, `TrafficGenerator`, `SimulationClock`

**Poznámky:**
- `Request` nemá pole `type` — všechny requesty jsou jednoho druhu s fixní dobou zpracování
- `Request.status` je privátní `volatile` pole; přístup přes `getStatus()`, `markDropped()`, `markCompleted()`
- `ServiceTimeModel.serviceTimeMs()` nemá parametr — typ requestu neexistuje
- `ConstantServiceTimeModel` dostane hodnotu v konstruktoru z `SimulationConfig.serviceTimeMs`
- `TrafficGenerator` dostane `SimulationClock` a `ServiceTimeModel` v konstruktoru; nastavuje `arrivalTime = clock.simulatedTimeMs()` a `serviceTimeMs = serviceTimeModel.serviceTimeMs()`
- `SimulationClock` zařazen sem (místo M3), protože ho `TrafficGenerator` potřebuje od začátku

---

## M2 — Load balancer + service instances

Requesty tečou přes load balancer do instancí, kde jsou zpracovány nebo odmítnuty (drop).

**Třídy:** `LoadBalancer`, `AbstractLoadBalancer`, `RoundRobinLoadBalancer`, `LeastQueueLoadBalancer`, `LoadBalancerType`, `LoadBalancerSelection`, `ServiceInstance`, `InstanceManager`, `InstanceConfig`, `InstanceSnapshot`, `InstanceStatus`, `RequestQueue`

**Poznámky:**
- `InstanceManager` dostane `InstanceConfig` v konstruktoru (používá ho pro `addInstance()`)
- `ServiceInstance` dostane `LatencyTracker` v konstruktoru (injected z `MetricsCollector`); `InstanceManager` na `LatencyTracker` nezávisí
- `ServiceInstance` má `shutdown()`, `isTerminated()`, `getStatus()`; NEMÁ `tick()`
- `InstanceManager.removeInstance(String instanceId)` — nastaví DRAINING, zavolá `shutdown()`, okamžitě odstraní z aktivního listu; workeři doběhnou v pozadí; `isTerminated()` se používá jen v `stop()` pro graceful shutdown; `getInstances()` vrací jen aktivní instance
- `LoadBalancerSelection.create()` je statická factory metoda

---

## M3 — Metrics + simulation engine

Kompletní simulační smyčka. Metriky se logují přes SLF4J do konzole.

**Třídy:** `SimulationEngine`, `SimulationConfig`, `SimulationState`, `Snapshot`, `MetricsCollector`, `LatencyTracker`, `ThroughputTracker`, `TimeSeriesBuffer`

**Poznámky:**
- `MetricsCollector` vlastní `LatencyTracker` a předává ho do `ServiceInstance` při konstrukci
- `ThroughputTracker` dostane `tickDurationMs` z `SimulationConfig` přes `MetricsCollector`
- `MetricsCollector.buildSnapshot(long tick, List<InstanceSnapshot> instanceSnapshots)` — 2 parametry, bez type counts
- `Snapshot` obsahuje historické řady `latencyHistory`, `throughputHistory`, `instanceCountHistory` (plněné z `TimeSeriesBuffer` v `MetricsCollector`)

---

## M4 — Autoscaler + event log

Systém dynamicky přidává a odebírá instance podle aktuální zátěže.

**Třídy:** `AutoScaler`, `ScalingPolicy`, `ThresholdScalingPolicy`, `CooldownTracker`, `ScalingDecision`, `Decision`, `SimulationEvent`, `EventType`, `EventBus`, `SimulationException`, `ConfigValidationException`, `InstanceException`

**Poznámky:**
- `ScalingDecision` je **record** (nebo třída) s polem `Decision decision` a `String reason`; obsahuje statické factory metody `noAction()`, `scaleUp(double metric)`, `scaleDown(double metric)`
- `Decision` je enum `{SCALE_UP, SCALE_DOWN, NO_ACTION}` vnořený do `ScalingDecision`
- `SimulationEvent.type` je `EventType` (enum) místo `String` — eliminuje magické řetězce
- `EventBus.publish()` je voláno z engine threadu; UI subscribery musí wrapovat handler do `Platform.runLater()`
- `SimulationEngine` přidá `reset()` — přechod do stavu `IDLE`, reinicializace komponent, vyčištění metrik

---

## M5 — JSON konfigurace + JavaFX dashboard

Uživatel načte parametry z JSON souboru a ovládá simulaci přes grafické rozhraní s grafy a tabulkou instancí.

**Třídy:** `SimulationConfigDto`, `ConfigLoader`, `ConfigValidator`, `TrafficProfileType`, `MainApp`, `MainController`, `ControlPanelController`, `ChartsController`, `InstancesTableController`, `UiMapper`, `ChartData`, `InstanceRow`

**Poznámky:**
- `SimulationConfig` a `SimulationConfigDto` obsahují pole `long serviceTimeMs` — fixní doba zpracování všech requestů
- `SimulationConfig.trafficProfile` je `TrafficProfileType` enum (CONSTANT, BURSTY) místo `String`
- `ChartData` je holder pro jeden datový bod (`double latency`, `double throughput`, `int instanceCount`); grafy si historii spravují samy z `Snapshot.latencyHistory` apod.
- UI dashboard: start / pause / resume / stop / reset, slider pro traffic rate, dropdown pro LB strategii, tlačítko pro načtení konfigurace, event log

---

## M6 — Dokumentace

Uživatelský manuál a technická dokumentace na GitLab Wiki. JavaDoc ve zdrojovém kódu.
