# Lesson 04 — Subsystems

**Stage 1B · 45 min · Needs: 03**

Take yesterday's code apart. The pieces end up somewhere better.

## Do this

Three files, in this order.

**1. `subsystems/roller/RollerSubsystem.java`** — two TODOs.

`setMode(State desired)` stores the state. One line. It does not touch the motor.

`periodic()` turns state into motor output:

| State | Output |
|---|---|
| `OFF` | `0.0` |
| `INTAKING` | `INTAKE_SPEED`, or `0.0` if `hasGamePiece()` |
| `EJECTING` | `EJECT_SPEED`, always |

**2. `RobotContainer.java`** — make the roller real:

```java
private final RollerSubsystem roller = new RollerSubsystem();
```

**3. `Robot.java`** — delete the motor and beam-break fields. Replace
`teleopPeriodic()` with three or four lines that only say what the operator wants.

## Check it

```bash
./tools/frcprog check 04-subsystems-state-machines
```

Six checks. The first four are the same four scenarios as lesson 03, because the
robot's behaviour must not change during a refactor. Checks 5 and 6 are new: the
hardware fields must be `private final`, and `Robot.java` must no longer mention
`PWMSparkMax` or `DigitalInput`.

## Why

Why was `roller` `null` until now? WPILib gives a PWM channel to exactly one
object. While `Robot` owned a motor on PWM 5, constructing a `RollerSubsystem`
would crash at startup with "PWM 5 already allocated". That error was the framework
refusing to let two things own one motor.

**Notice what disappears.** The beam-break check leaves `Robot.java` entirely. Not
because it stopped mattering, but because "stop intaking once you have one" was
never the driver's decision. It is a fact about rollers, and it now lives with the
roller.

**Why an enum, not booleans.** `boolean isIntaking` works until you add ejecting.
Then two booleans can both be true, which describes a mechanism that cannot exist.
Enums make illegal states unrepresentable, and a `switch` over one is exhaustive.

## See it

```bash
./tools/frcprog sim
```

Identical behaviour to lesson 03. That is the success condition. What changed is
which file you open to fix a roller bug.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**A subsystem makes two promises.** It owns its hardware, so a roller bug is in one
file. And it exposes intent, not mechanism: callers say `setMode(INTAKING)`, not
"0.6 unless the beam is broken".
