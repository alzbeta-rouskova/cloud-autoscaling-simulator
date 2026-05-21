# Cloud Autoscaling Simulator — User Manual

## 1. Introduction

Cloud Autoscaling Simulator is a desktop application that simulates a simplified cloud service handling incoming requests. The user picks a traffic profile and other parameters, starts the simulation, and watches in real time how requests flow through a load balancer to a pool of service instances. An autoscaler dynamically adds or removes instances based on the current load. All key metrics — throughput, average latency, queue lengths, dropped requests, instance count — are visualized live in charts and a per-instance table.
The application is a single-window JavaFX desktop app. No network connection, no server, no external services.

## 2. Installation

### 2.1 Prerequisites

| Tool             | Version                 | Notes                                                                      |
|------------------|-------------------------|----------------------------------------------------------------------------|
| JDK              | 21                      | any distribution (Temurin, OpenJDK, Oracle). `java --version` must show 21 |
| Maven            | 3.8 or newer            | `mvn -v` must run                                                          |
| Operating system | Windows / macOS / Linux | JavaFX 21 supports all three                                               |

### 2.2 Getting the source

```
git clone https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz.git
cd rouskalz
```

### 2.3 Building

```
mvn package
```

This compiles both modules (`core` and `ui`), runs all tests, and produces the artifact `ui/target/ui-1.0-SNAPSHOT.jar`. Build success is reported as `BUILD SUCCESS` on the last line.

## 3. Running the application

### 3.1 Recommended (via Maven)

From the project root:

```
mvn -pl ui javafx:run
```

The `javafx-maven-plugin` resolves the JavaFX runtime, sets up the module path, and launches `cz.cvut.fel.pjv2026.MainApp`. After a few seconds the dashboard window appears.

To stop the application, simply close the window. The application performs a graceful shutdown: a running simulation is stopped, worker pools drain, and the process exits.

### 3.2 From an IDE

Open the project as a Maven project in IntelliJ IDEA or Eclipse, then run the class `cz.cvut.fel.pjv2026.Launcher` (in the `ui` module). The IDE handles the JavaFX module path automatically when the project is recognised as a Maven project.

> `Launcher` is a plain (non-`Application`) entry point. It exists so the `Main-Class` of a packaged jar does not directly extend `javafx.application.Application`, which the JVM rejects unless the JavaFX modules are on the module path.

## 4. Dashboard layout

The window is divided into four areas:

```
┌──────────────────────────────────────────────────────────────────┐
│  CONTROL PANEL  (top)                                            │
│  ▶ Start  ■ Stop  ↻ Reset  [Load config] [Save log]              │
│  Traffic rate | LB strategy | Max instances                      │
├─────────────────────────────────────────────┬────────────────────┤
│  STATUS BAR  Tick | Generated | Throughput  │                    │
│              Latency | Dropped | Active     │                    │
│                                             │                    │
│  ┌─ Throughput chart ─────────────────────┐ │  INSTANCES TABLE   │
│  └────────────────────────────────────────┘ │  (right)           │
│  ┌─ Average latency chart ────────────────┐ │  ID | Queue |      │
│  └────────────────────────────────────────┘ │  Workers | Pro-    │
│  ┌─ Active instances chart ───────────────┐ │  cessed | Dropped  │
│  └────────────────────────────────────────┘ │  | Status          │
│                                             │                    │
│  EVENT LOG                                  │                    │
│  [tick 42]  SCALE_UP   added instance-3 …   │                    │
└─────────────────────────────────────────────┴────────────────────┘
```

## 5. Controls

### 5.1 Lifecycle buttons

| Button      | Effect                                                                                                   |
|-------------|----------------------------------------------------------------------------------------------------------|
| **▶ Start** | Starts a new simulation using the current configuration plus any live UI overrides (5.2).                |
| **■ Stop**  | Stops the running simulation gracefully — all worker pools drain and the engine state becomes `STOPPED`. |
| **↻ Reset** | Returns to `IDLE`, clears charts, table, status bar and event log. The next Start builds a fresh engine. |

### 5.2 Live parameter widgets

Set **before** pressing Start. While the simulation is running they are disabled.

| Widget                  | Range                         | Meaning                                                                                                                                                    |
|-------------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Traffic rate (req/tick) | 1 – 200                       | Number of requests the generator emits each tick. For the **CONSTANT** profile this is the rate. For **BURSTY**, this is the baseline rate between spikes. |
| LB strategy             | `ROUND_ROBIN` / `LEAST_QUEUE` | Algorithm the load balancer uses to pick a target service instance.                                                                                        |
| Max instances           | 1 – 32                        | Upper limit the autoscaler may not exceed.                                                                                                                 |

> Other parameters (worker count, queue capacity, scaling thresholds, traffic profile, etc.) are configured via JSON (see section 6).

### 5.3 Load config

Opens a file picker (default folder: `scenarios/`) and loads a JSON file. On success the live widgets are updated to reflect the loaded values; on failure an error dialog explains why. Load config is disabled while a simulation is running.

### 5.4 Save log

Exports the textual contents of the event log into a `.txt` file chosen by the user. Useful for handing over a run's scaling timeline together with screenshots.

## 6. Loading a JSON configuration

### 6.1 Workflow

1. Make sure the engine is in `IDLE` (use Reset first if necessary).
2. Click **Load config**.
3. Choose a `.json` file. The picker opens in the `scenarios/` folder by default.
4. If validation passes, the live widgets update and the event log shows `[CONFIG] loaded <filename>`.
5. If validation fails, an error dialog appears; the previous configuration remains active.

### 6.2 Field reference

All fields are optional — any missing key falls back to a default.

| Field                               | Type                              |       Default | Meaning                                                                               |
|-------------------------------------|-----------------------------------|--------------:|---------------------------------------------------------------------------------------|
| `trafficRate`                       | int (>0)                          |            50 | Requests per tick (baseline for `BURSTY`).                                            |
| `trafficProfile`                    | `"CONSTANT"` / `"BURSTY"`         |    `CONSTANT` | Traffic generator profile.                                                            |
| `burstMultiplier`                   | double (≥1.0)                     |           3.0 | Multiplier applied to `trafficRate` during a burst tick (BURSTY only).                |
| `burstIntervalTicks`                | int (>0)                          |            20 | Period of bursts in ticks (BURSTY only).                                              |
| `initialInstanceCount`              | int (≥1)                          |             2 | Number of service instances created at start.                                         |
| `minInstanceCount`                  | int (≥1)                          |             1 | Lower bound the autoscaler must not cross.                                            |
| `maxInstanceCount`                  | int (≥`minInstanceCount`)         |             8 | Upper bound the autoscaler must not exceed.                                           |
| `queueCapacity`                     | int (>0)                          |           100 | Maximum number of waiting requests per instance. When full, new requests are dropped. |
| `workerCount`                       | int (>0)                          |             4 | Worker threads per instance (max concurrent requests in service).                     |
| `serviceTimeMs`                     | long (>0)                         |            20 | Time each request spends being processed.                                             |
| `loadBalancerStrategy`              | `"ROUND_ROBIN"` / `"LEAST_QUEUE"` | `ROUND_ROBIN` | LB algorithm.                                                                         |
| `autoscalerEnabled`                 | boolean                           |       `false` | When `false`, the autoscaler is not invoked and `initialInstanceCount` stays fixed.   |
| `scaleUpQueueThreshold`             | int (>0)                          |             8 | Average queue length above which the policy scales up.                                |
| `scaleDownQueueThreshold`           | int (≥0)                          |             2 | Average queue length below which the policy scales down.                              |
| `cooldownTicks`                     | int (≥0)                          |            10 | Ticks after a scale event during which no further scale is allowed.                   |
| `autoscalerEvaluationIntervalTicks` | int (>0)                          |             5 | How often the autoscaler is evaluated (every N ticks).                                |
| `tickDurationMs`                    | int (>0)                          |           100 | Simulated time advanced per tick. Affects throughput-per-second conversion.           |


### 6.3 Bundled scenarios

The `scenarios/` directory ships ready-made JSON files demonstrating different system behaviours.

| File                   | What to observe                                                                          |
|------------------------|------------------------------------------------------------------------------------------|
| `default.json`         | Balanced baseline = moderate load, autoscaler enabled.                                   |
| `morning-spike.json`   | Bursty traffic = periodic spikes trigger SCALE_UP, then SCALE_DOWN during quieter ticks. |
| `flash-crash.json`     | Sudden overload = queue saturates, dropped count climbs, autoscaler reacts.              |
| `idle-scale-down.json` | Very low traffic = autoscaler scales the cluster down to `minInstanceCount`.             |
| `no-autoscaler.json`   | Autoscaler disabled = fixed cluster, latency degrades under bursts.                      |
| `round-robin.json`     | Round-robin LB demo.                                                                     |
| `slow-service.json`    | High `serviceTimeMs` = queues fill even at modest rate.                                  |
| `thrashing.json`       | Aggressive scaling thresholds and short cooldown = illustrates why cooldown matters.     |
| `huge-burst.json`      | Extreme bursts = drops + scaling up to `maxInstanceCount`.                               |

## 7. Glossary

| Term              | Meaning                                                                                                                                                                  |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Tick              | Smallest unit of simulated time. The engine advances one tick per loop iteration. `tickDurationMs` controls how many milliseconds of simulated time one tick represents. |
| Snapshot          | An immutable read-only view of the simulation state at the end of a tick. The UI only reads snapshots; it never touches live engine structures.                          |
| ACTIVE / DRAINING | Instance lifecycle states. ACTIVE accepts new requests; DRAINING is being retired by the autoscaler and finishes its existing queue before disappearing.                 |
| Cooldown          | Number of ticks after a SCALE_UP / SCALE_DOWN during which no further scaling is allowed. Prevents thrashing.                                                            |
| Backpressure      | The drop-on-full-queue mechanism that prevents unbounded memory growth under overload.                                                                                   |
