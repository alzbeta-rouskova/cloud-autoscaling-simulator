# MVP Scope

## Must

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

## Should

- latency percentiles (deferred to Polish)
    - p50
    - p95

## Could

- chaos mode (kill instance randomly)
- additional LB strategy (least-active)
- load test scenarios as preset profiles
- export metrics to CSV
