```mermaid
stateDiagram-v2
    [*] --> IDLE : aplikace spuštěna

    IDLE --> RUNNING : start()

    RUNNING --> PAUSED : pause()
    RUNNING --> STOPPED : stop()

    PAUSED --> RUNNING : resume()
    PAUSED --> STOPPED : stop()

    STOPPED --> IDLE : reset()
    STOPPED --> [*] : aplikace ukončena
```
