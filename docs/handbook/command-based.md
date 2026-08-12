# The Command-Based framework

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, lessons 04–14 are the canonical reference.

## What this page will cover

The four primitives of WPILib's command-based framework, taught as a reference (not a tutorial):

- **Subsystem** — an abstraction over a collection of hardware that operates as one unit
- **Command** — a unit of work that requires zero or more subsystems
- **Trigger** — a `BooleanSupplier` wrapper that fires commands on edges
- **CommandScheduler** — the runtime, the requirement system, what `Robot.robotPeriodic` actually does

Plus the patterns: factories over subclasses, Suppliers over captured values, triggers over getters, composition operators (`andThen`, `alongWith`, `race`, `deadline`).

## One useful nugget right now

**Write factories, not Command subclasses, for stateless commands.** From the [WPILib docs](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html): *"teams should rarely need to write custom command classes."*

```java
public class Intake extends SubsystemBase {
    public Command intakeNote() {
        return run(() -> motor.setVoltage(12.0))
                .until(this::hasNote)
                .finallyDo(() -> motor.setVoltage(0));
    }
}
```

Subclasses remain appropriate only for genuinely stateful commands (multi-phase motion profiles, etc.).

## Lessons that cover this material today

- [Lesson 04 — Subsystems as state machines](../learn/stage1b/04-subsystems-state-machines/)
- [Lesson 07 — Tank drive wiring](../learn/stage1c/07-tank-drive/) — factories introduced
- [Lesson 08 — Joystick bindings & triggers](../learn/stage1c/08-triggers-bindings/)
- [Lesson 09 — Command composition](../learn/stage1c/09-command-composition/)

## See also

- [Curriculum-Flow.md §3](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md) — Oblarg's three principles
- [BoVLB's distillation](https://bovlb.github.io/frc-tips/commands/best-practices.html) — the single most concentrated summary
