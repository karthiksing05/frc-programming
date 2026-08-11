# Code Examples

The `examples/` directory ships **two kinds of artifact**: the three browser PoCs that power Stages 0–1B (Path A), and the `path-b-demo/` skeleton that the Stage 1C+ lessons graduate to (Path B). This page is the index.

!!! note "How this index works"

    During local development, `serve.sh` symlinks the project's top-level `examples/` directory into `docs/examples/`. Each PoC below is reachable at `/examples/<slug>/index.html` once you run `./serve.sh`. The links here point at those served paths.

## Browser PoCs

Self-contained HTML + JS apps — no install, no JDK, no Gradle. They run on a Chromebook. Each one is the interactive surface for one or more early lessons.

- **[Functions PoC](/examples/functions-poc/index.html)** — *paired with [Lesson 01 — Methods](../learn/stage1a/01-methods/)*. The student fills in a Java method body; a JS sandbox runs it as a joystick deadband filter. First lesson to write the persistent in-browser project filesystem — the file you author here is the same file the Tank Drive PoC imports later.

- **[Elevator PID PoC](/examples/elevator-pid-poc/index.html)** — *paired with [Lesson 05 — PID introduction](../learn/stage1b/05-pid-elevator/)*. The student tunes `kP` / `kI` / `kD` on a real elevator simulation; a step-response plot updates live. The Stage 1B "feel" lesson for control loops.

- **[Tank Drive PoC](/examples/tank-drive-poc/index.html)** — *paired with [Lesson 07 — Tank drive wiring](../learn/stage1c/07-tank-drive/)*. The student wires a subsystem factory that turns joystick Suppliers into drivetrain output. Imports the `MathUtils.applyDeadband` written in the Functions PoC — the lesson where the persistent filesystem pays off.

## Path B Demo

- **[path-b-demo](https://github.com/karthiksing05/FRC-Programming/tree/main/examples/path-b-demo)** — the architectural skeleton for the VS Code half of the curriculum. A real WPILib + AdvantageKit + JUnit project that shows what Stage 1C+ lessons graduate to: IO interfaces, `Constants.java`, `RobotContainer` bindings, the `frcprog` lesson runner, JUnit-tag-based rubrics. **Read it as architecture, not as a runnable widget** — the Gradle wrapper is intentionally not shipped in Phase 0. See [`examples/path-b-demo/README.md`](https://github.com/karthiksing05/FRC-Programming/blob/main/examples/path-b-demo/README.md) for the layout. Phase-0 build-out is tracked in [Implementation-Plan.md §5 Weeks 1-2](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md).

## How these connect to the curriculum

| PoC / artifact | Embedded by | What it teaches |
|---|---|---|
| `functions-poc/` | [Lesson 01](../learn/stage1a/01-methods/) | Methods, parameters, return values |
| `elevator-pid-poc/` | [Lesson 05](../learn/stage1b/05-pid-elevator/) | `PIDController`, tuning by step response |
| `tank-drive-poc/` | [Lesson 07](../learn/stage1c/07-tank-drive/) | Factory pattern, Suppliers, cross-lesson file reuse |
| `path-b-demo/` | All Stage 1C+ lessons (as the project skeleton students clone) | Real WPILib structure, IO Layer, JUnit rubrics |

The browser path has a known ceiling — Java in the browser caps out around "fill in a method body." Real classes, real `Trigger`s, and real WPILib API surface require the local toolchain. The graduation is one button press at the end of Stage 1B; see [Infrastructure-Analysis.md §2.3](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Infrastructure-Analysis.md) for the longer story.
