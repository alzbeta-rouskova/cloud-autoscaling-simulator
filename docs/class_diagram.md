```mermaid
classDiagram
    namespace model {
        class Request {
            +long id
            +long arrivalTime
            +long serviceTimeMs
            -RequestStatus status
            +RequestStatus getStatus()
            +void markProcessing()
            +void markCompleted()
            +void markDropped()
        }
        class RequestStatus {
            <<enumeration>>
            PENDING
            PROCESSING
            COMPLETED
            DROPPED
        }
        class RequestIdGenerator {
            +long nextId()
        }
        class ServiceTimeModel {
            <<interface>>
            +long serviceTimeMs()
        }
        class ConstantServiceTimeModel {
            +long serviceTimeMs()
        }
    }

    namespace traffic {
        class TrafficProfile {
            <<interface>>
            +int requestsForTick(long tick)
        }
        class AbstractTrafficProfile {
            <<abstract>>
            #int baseRate
            +abstract int requestsForTick(long tick)
        }
        class ConstantTrafficProfile {
            +int requestsForTick(long tick)
        }
        class BurstyTrafficProfile {
            -double burstMultiplier
            -int burstIntervalTicks
            +int requestsForTick(long tick)
        }
        class TrafficGenerator {
            +List~Request~ generate(long tick)
        }
    }

    namespace lb {
        class LoadBalancer {
            <<interface>>
            +ServiceInstance select(List~ServiceInstance~ instances)
        }
        class AbstractLoadBalancer {
            <<abstract>>
            #void validateNotEmpty(List~ServiceInstance~ instances)
        }
        class RoundRobinLoadBalancer {
            +ServiceInstance select(List~ServiceInstance~ instances)
        }
        class LeastQueueLoadBalancer {
            +ServiceInstance select(List~ServiceInstance~ instances)
        }
        class LoadBalancerType {
            <<enumeration>>
            ROUND_ROBIN
            LEAST_QUEUE
        }
        class LoadBalancerSelection {
            +static LoadBalancer create(LoadBalancerType type)
        }
    }

    namespace instance {
        class RequestQueue {
            +boolean offer(Request r)
            +int size()
            +int droppedCount()
        }
        class InstanceConfig {
            +int queueCapacity
            +int workerCount
        }
        class InstanceStatus {
            <<enumeration>>
            ACTIVE
            DRAINING
        }
        class InstanceSnapshot {
            +String id
            +int queueLength
            +int activeWorkers
            +int workerCount
            +int processedCount
            +int droppedCount
            +InstanceStatus status
        }
        class ServiceInstance {
            -String id
            +boolean submit(Request r)
            +int currentQueueSize()
            +InstanceSnapshot snapshot()
            +void shutdown()
            +boolean isTerminated()
            +InstanceStatus getStatus()
        }
        class InstanceManager {
            +void addInstance()
            +void removeInstance(String instanceId)
            +List~ServiceInstance~ getInstances()
        }
    }

    namespace metrics {
        class LatencyTracker {
            +void record(long latencyMs)
            +double average()
            +void reset()
        }
        class ThroughputTracker {
            -int tickDurationMs
            +void record(int count, long currentTick)
            +double requestsPerSecond()
        }
        class TimeSeriesBuffer {
            +void add(double value)
            +List~Double~ values()
        }
        class MetricsCollector {
            +Snapshot buildSnapshot(long tick, List~InstanceSnapshot~ instanceSnapshots)
        }
    }

    namespace core {
        class SimulationState {
            <<enumeration>>
            IDLE
            RUNNING
            PAUSED
            STOPPED
        }
        class SimulationClock {
            -long currentTick
            -int tickDurationMs
            +void advance()
            +long tick()
            +long simulatedTimeMs()
        }
        class TrafficProfileType {
            <<enumeration>>
            CONSTANT
            BURSTY
        }
        class EventType {
            <<enumeration>>
            SCALE_UP
            SCALE_DOWN
            CONFIG_LOADED
            SIMULATION_STARTED
            SIMULATION_STOPPED
        }
        class SimulationConfig {
            +int trafficRate
            +TrafficProfileType trafficProfile
            +double burstMultiplier
            +int burstIntervalTicks
            +int initialInstanceCount
            +int minInstanceCount
            +int maxInstanceCount
            +int queueCapacity
            +int workerCount
            +long serviceTimeMs
            +LoadBalancerType lbStrategy
            +boolean autoscalerEnabled
            +int scaleUpQueueThreshold
            +int scaleDownQueueThreshold
            +int cooldownTicks
            +int autoscalerEvaluationIntervalTicks
            +int tickDurationMs
        }
        class SimulationEvent {
            +long tick
            +EventType type
            +String message
        }
        class EventBus {
            +void publish(SimulationEvent event)
            +void subscribe(Consumer~SimulationEvent~ listener)
            +void unsubscribe(Consumer~SimulationEvent~ listener)
        }
        class Snapshot {
            +long tick
            +double throughput
            +double avgLatency
            +double avgQueueLength
            +int droppedCount
            +double dropRate
            +double utilization
            +int instanceCount
            +List~Double~ latencyHistory
            +List~Double~ throughputHistory
            +List~Double~ instanceCountHistory
            +List~InstanceSnapshot~ instances
        }
        class SimulationEngine {
            +void start()
            +void pause()
            +void resume()
            +void stop()
            +void reset()
            +void setOnSnapshotReady(Consumer~Snapshot~ listener)
        }
    }

    namespace autoscaler {
        class Decision {
            <<enumeration>>
            SCALE_UP
            SCALE_DOWN
            NO_ACTION
        }
        class ScalingDecision {
            +Decision decision
            +String reason
            +static ScalingDecision noAction()
            +static ScalingDecision scaleUp(double metric)
            +static ScalingDecision scaleDown(double metric)
        }
        class ScalingPolicy {
            <<interface>>
            +ScalingDecision evaluate(Snapshot snapshot)
        }
        class ThresholdScalingPolicy {
            -int scaleUpThreshold
            -int scaleDownThreshold
            -int minInstances
            -int maxInstances
            +ScalingDecision evaluate(Snapshot snapshot)
        }
        class CooldownTracker {
            -int cooldownTicks
            +boolean canScale(long currentTick)
            +void recordScale(long currentTick)
            +void reset()
        }
        class AutoScaler {
            -int evaluationIntervalTicks
            +void evaluate(Snapshot snapshot, long currentTick)
        }
    }

    namespace config {
        class SimulationConfigDto {
            +int trafficRate
            +String trafficProfile
            +double burstMultiplier
            +int burstIntervalTicks
            +int initialInstanceCount
            +int minInstanceCount
            +int maxInstanceCount
            +int queueCapacity
            +int workerCount
            +long serviceTimeMs
            +String loadBalancerStrategy
            +boolean autoscalerEnabled
            +int scaleUpQueueThreshold
            +int scaleDownQueueThreshold
            +int cooldownTicks
            +int autoscalerEvaluationIntervalTicks
            +int tickDurationMs
        }
        class ConfigLoader {
            +SimulationConfig load(Path path)
        }
        class ConfigValidator {
            +void validate(SimulationConfigDto dto)
        }
    }

    namespace exception {
        class SimulationException {
            +SimulationException(String message)
        }
        class ConfigValidationException {
            +ConfigValidationException(String message)
        }
        class InstanceException {
            +InstanceException(String message)
        }
    }

    namespace ui {
        class MainApp
        class MainController
        class ControlPanelController
        class ChartsController
        class InstancesTableController
        class ChartData {
            +double latency
            +double throughput
            +int instanceCount
        }
        class InstanceRow {
            +String id
            +int queueLength
            +int activeWorkers
            +int processedCount
            +int droppedCount
            +InstanceStatus status
        }
        class UiMapper {
            +ChartData toChartData(Snapshot s)
            +List~InstanceRow~ toInstanceRows(Snapshot s)
        }
    }

    %% ─── VZTAHY ──────────────────────────────────────────

    ServiceTimeModel <|.. ConstantServiceTimeModel
    Request --> RequestStatus

    TrafficProfile <|.. AbstractTrafficProfile
    AbstractTrafficProfile <|-- ConstantTrafficProfile
    AbstractTrafficProfile <|-- BurstyTrafficProfile
    TrafficGenerator --> TrafficProfile
    TrafficGenerator --> RequestIdGenerator
    TrafficGenerator --> ServiceTimeModel
    TrafficGenerator --> SimulationClock

    LoadBalancer <|.. AbstractLoadBalancer
    AbstractLoadBalancer <|-- RoundRobinLoadBalancer
    AbstractLoadBalancer <|-- LeastQueueLoadBalancer
    LoadBalancerSelection --> LoadBalancerType
    LoadBalancerSelection --> LoadBalancer

    RequestQueue --> Request
    ServiceInstance --> RequestQueue
    ServiceInstance --> InstanceConfig
    ServiceInstance --> InstanceSnapshot
    ServiceInstance --> LatencyTracker
    InstanceManager --> ServiceInstance
    InstanceManager --> InstanceConfig
    InstanceManager --> InstanceException

    MetricsCollector --> LatencyTracker
    MetricsCollector --> ThroughputTracker
    MetricsCollector --> TimeSeriesBuffer
    MetricsCollector --> SimulationConfig
    MetricsCollector --> Snapshot

    SimulationEngine --> SimulationState
    SimulationEngine --> SimulationClock
    SimulationEngine --> SimulationConfig
    SimulationEngine --> TrafficGenerator
    SimulationEngine --> LoadBalancer
    SimulationEngine --> InstanceManager
    SimulationEngine --> MetricsCollector
    SimulationEngine --> AutoScaler
    SimulationEngine --> EventBus

    ScalingPolicy <|.. ThresholdScalingPolicy
    AutoScaler --> ScalingPolicy
    AutoScaler --> CooldownTracker
    AutoScaler --> InstanceManager
    AutoScaler --> SimulationEvent
    AutoScaler --> EventBus
    ThresholdScalingPolicy --> ScalingDecision
    ScalingDecision --> Decision
    EventBus --> SimulationEvent
    SimulationEvent --> EventType
    InstanceSnapshot --> InstanceStatus
    SimulationConfig --> TrafficProfileType

    ConfigLoader --> SimulationConfigDto
    ConfigLoader --> ConfigValidator
    ConfigLoader --> SimulationConfig
    SimulationException <|-- ConfigValidationException
    SimulationException <|-- InstanceException
    ConfigValidator --> ConfigValidationException

    MainApp --> MainController
    MainController --> ControlPanelController
    MainController --> ChartsController
    MainController --> InstancesTableController
    MainController --> UiMapper
    UiMapper --> Snapshot
    MainController --> SimulationEngine
    MainController --> EventBus
```
