# PID & feedforward

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, lessons 05 and 06 are the canonical reference.

## What this page will cover

The feedback + feedforward pairing that drives almost every FRC mechanism:

- What `kP`, `kI`, `kD` actually do (and why `kI` is almost always zero in practice)
- How to read a step response plot to tune
- Feedforward: `kS`, `kV`, `kA`, `kG` — and when each applies
- Why `kG` for an arm varies with `cos(angle)` but `kG` for an elevator is constant
- WPILib's `PIDController`, `ProfiledPIDController`, `ArmFeedforward`, `ElevatorFeedforward`
- The convention: send **voltage** to motors, not normalized throttle

## One useful nugget right now

**Feedback alone is fighting the mechanism. Feedforward is helping it.** A PID-only loop holding an elevator at height has to *constantly correct* for gravity. Adding a gravity feedforward (`kG`) lets the loop *barely work at all* in steady state — and that's the point. From [Curriculum-Flow.md §4 Stage 1B](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md): the first time a constant becomes a function (arm gravity) is the first hint that feedforward is a richer language than PID alone.

```java
double pidOutput = pid.calculate(inputs.positionMeters, setpointMeters);
double ffOutput  = feedforward.calculate(setpointMeters, 0.0);
io.setVoltage(pidOutput + ffOutput);
```

## Lessons that cover this material today

- [Lesson 05 — PID introduction (Elevator)](../learn/stage1b/05-pid-elevator/index.md) — paired with the [elevator PID PoC](../code-examples/index.md)
- [Lesson 06 — Arm with gravity feedforward](../learn/stage1b/06-arm-gravity-ff/index.md)
- [Lesson 28 — System identification (SysId)](../learn/stage2d/28-sysid/index.md) — measuring `kV`/`kA` instead of guessing

## See also

- [WPILib — PID Control](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html)
- [WPILib — Feedforward](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/feedforward.html)
