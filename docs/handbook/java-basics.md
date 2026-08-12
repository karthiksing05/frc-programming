# Java basics for FRC

!!! warning "Coming in Phase 2"

    This page is a stub. The full handbook entry is authored in Phase 2 — see [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Until then, the lessons themselves are the canonical reference.

## What this page will cover

The subset of Java the curriculum actually uses, taught in robot terms — not as a generic Java course:

- Methods, parameters, return values (the smallest unit of reuse)
- Classes, fields, `private final`, constructors
- `static` vs instance; when each appears in WPILib
- Lambdas and method references (`() -> x` and `controller::getLeftY`)
- `Supplier<T>` — and why every joystick read has to be one
- Generics enough to read `List<Pose2d>` and stop there
- The handful of Java idioms WPILib leans on (records, `var`, switch expressions)

## One useful nugget right now

**Joystick values must be passed as Suppliers, not as captured doubles.** This is the single most common subtle bug in beginner command-based code:

```java
// WRONG — joystick value is read ONCE at construction time
drive.driveCommand(controller.getLeftY());

// RIGHT — joystick value is read every tick
drive.driveCommand(() -> controller.getLeftY());
```

The wrong version compiles, deploys, and "works" the first frame — then never updates again.

## Lessons that cover this material today

- [Lesson 01 — Methods](../learn/stage1a/01-methods/) — your first method, deadband filter
- [Lesson 02 — Variables & types](../learn/stage1a/02-variables-and-types/) — `Constants.java`, typed PID gains
- [Lesson 07 — Tank drive wiring](../learn/stage1c/07-tank-drive/) — Suppliers in action

## See also

- [Curriculum-Flow.md](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md) — why Java syntax is deliberately *not* front-loaded
- [Oracle Java tutorials](https://docs.oracle.com/javase/tutorial/) — for the language proper
