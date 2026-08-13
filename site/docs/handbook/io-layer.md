# The IO Layer pattern (AdvantageKit)

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, Stage 2A lessons 16–20 are the canonical reference.

## What this page will cover

[Team 6328](https://github.com/Mechanical-Advantage)'s IO Layer pattern — the architectural backbone of any serious AdvantageKit project:

- The four-file split: `XxxSubsystem`, `XxxIO` (interface), `XxxIOReal`, `XxxIOSim`
- The `@AutoLog`-annotated inputs struct and what code-gen produces
- `io.updateInputs(inputs)` + `Logger.processInputs(...)` as the *only* sensor read path
- Why log replay falls out of this pattern for free
- When **not** to use the IO Layer (rookie subsystems, prototypes)

## One useful nugget right now

**The IO Layer is a Stage 2 concept, not a Stage 1 prereq.** Without an existing mental model for subsystems + commands, the IO interface looks like ceremony. With them, it looks like exactly the right separation. From [Curriculum-Flow.md §3](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md): the pattern is what unlocks log replay, which is what makes "test it on the robot" the most expensive option instead of the cheapest.

```java
public interface ElevatorIO {
    @AutoLog
    class ElevatorIOInputs {
        public double positionMeters;
        public double velocityMetersPerSec;
        public double appliedVolts;
    }
    default void updateInputs(ElevatorIOInputs inputs) {}
    default void setVoltage(double volts) {}
}
```

## Lessons that cover this material today

- [Lesson 16 — The IO Layer pattern](../learn/stage2a/16-io-layer/index.md)
- [Lesson 18 — AdvantageKit logging discipline](../learn/stage2a/18-logging-discipline/index.md)
- [Lesson 19 — Log replay for debugging](../learn/stage2a/19-log-replay/index.md)

## See also

- [AdvantageKit docs — Recording Inputs](https://docs.advantagekit.org/recording-inputs/io-interfaces/)
- Presto's [`flywheels/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) — the textbook example
- Kelpie's [`elevator/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/elevator) — same pattern, different mechanism
