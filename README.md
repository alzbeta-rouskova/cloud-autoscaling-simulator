# Cloud Autoscaling Simulator

A simulator of a cloud environment for processing incoming requests. Requests are generated according to a selected traffic profile, distributed by a load balancer to service instances, and processed by worker pools. An autoscaler dynamically adds or removes instances based on current load. Metrics are visualized in a JavaFX dashboard.

## Documentation

Full project documentation is on the **[GitLab Wiki](https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz/-/wikis/home)**:

- **[User Manual](https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz/-/wikis/User-Manual)** - installation, controls, JSON configuration, reading the dashboard
- **[Technical Documentation](https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz/-/wikis/Technical-Documentation)** - architecture, modules, autoscaler design, state machine, technologies

The Markdown sources for the wiki pages live in `docs/`.

## Quick start

Requirements: **Java 21** + **Maven 3.8+**.

```
git clone https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz.git
cd rouskalz
mvn -pl ui javafx:run
```

For details and troubleshooting see the [User Manual](https://gitlab.fel.cvut.cz/B252_B0B36PJV/rouskalz/-/wikis/User-Manual).

## Technologies

- Java 21
- JavaFX 21
- Maven (multi-module)
- JUnit 5
- JSON library 2.17
- SLF4J 2.0 + Logback 1.5

## Project Structure

```
core/        simulation logic (pure Java, no JavaFX)
  model         request and its states
  traffic       request generator (constant, bursty)
  lb            load balancer (round-robin, least-queue)
  instance      service instance, queue, instance manager
  metrics       latency, throughput and time series
  autoscaler    scaling policy and cooldown
  core          engine, clock, snapshot, event bus
  config        JSON configuration loading and validation
  exception     exceptions

ui/          JavaFX dashboard

scenarios/   ready-made JSON simulation configs
docs/        diagrams + Markdown sources for the wiki pages
```
