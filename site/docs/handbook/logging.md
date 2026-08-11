# Logging & telemetry (AdvantageScope)

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, lessons 10 and 17–19 are the canonical reference.

## What this page will cover

The "what just happened?" toolchain that turns FRC debugging from guessing into reading:

- **NetworkTables 4** — the live pub/sub layer; how `Shuffleboard` and `SmartDashboard` are surface skins on it
- **WPILOG** — WPILib's binary log format; on-robot files vs replay sources
- **AdvantageKit `Logger`** — `recordOutput`, `processInputs`, the `@AutoLogOutput` annotation
- **AdvantageScope** — the viewer: line charts, mechanism2d, 3D field, table view, swerve view
- **Key naming conventions** — `Subsystem/Inputs/...`, `Subsystem/setpoint`, `RealOutputs/...`, `ReplayOutputs/...`
- **Layouts** — saving and sharing `.json` configs so an entire team sees the same plots

## One useful nugget right now

**`Logger.recordOutput` is one-liner debugging.** Anywhere a number, pose, or array exists in your code, one call publishes it to both NT4 (live) and WPILOG (post-match). The naming convention pays off when you replay a failing match log and the keys you'd want are already there.

```java
Logger.recordOutput("Drive/Speed", inputs.velocityMetersPerSec);
Logger.recordOutput("Drive/CurrentPose", odometry.getPoseMeters());
Logger.recordOutput("Drive/ActiveCommand", getCurrentCommand().getName());
```

## Lessons that cover this material today

- [Lesson 10 — Telemetry & SmartDashboard](../learn/stage1c/10-telemetry-and-smartdashboard/) — first plots
- [Lesson 17 — AdvantageScope first-class](../learn/stage2a/17-advantagescope/)
- [Lesson 18 — AdvantageKit logging discipline](../learn/stage2a/18-logging-discipline/)
- [Lesson 19 — Log replay for debugging](../learn/stage2a/19-log-replay/)

## See also

- [AdvantageScope docs](https://docs.advantagescope.org/)
- [AdvantageKit — Recording Outputs](https://docs.advantagekit.org/recording-outputs/)
- [WPILib — NetworkTables 4](https://docs.wpilib.org/en/stable/docs/software/networktables/networktables-intro.html)
