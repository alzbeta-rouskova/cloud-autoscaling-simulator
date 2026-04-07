# Cloud Autoscaling Simulator

A simulator of a cloud environment for processing incoming requests. Requests are generated according to a selected traffic profile, distributed by a load balancer to service instances, and processed by worker pools. An autoscaler dynamically adds or removes instances based on current load. Metrics are visualized in a JavaFX dashboard.

## Technologies

- Java 21
- JavaFX 21
- Maven (multi-module)
- JUnit 5
- Jackson 2.17
- SLF4J 2.0 + Logback 1.5

## Project Structure

```
core/   simulation logic
  model       request and its states
  traffic     request generator (constant, bursty)
  lb          load balancer (round-robin, least-queue)
  instance    service instance, queue, instance manager
  metrics     latency, throughput and time series tracking
  autoscaler  scaling policy and cooldown
  core        engine, clock, snapshot, event bus
  config      JSON configuration loading and validation
  exception   exceptions

ui/     JavaFX interface
  charts, instance table, control panel
```

## Documentation

All documentation is located in the `docs/` directory:

- `class_diagram.md` / `class_diagram.svg` — class diagram with relationships
- `state_diagram.md` — application state diagram
- `technical_specification.md` — architecture overview
- `product_description.md` — product description and goals
- `delivery_plan.md` — milestone plan
