# Lesson 27 — Motion profiling <small>· Stage 2D</small>

<span class="stage-badge">Stage 2D · Lesson 27</span>

*Your lesson-06 arm slams from rest to full speed the instant you hand it a setpoint. The motor screams, the battery sags, and the whole superstructure shudders. Today you tell the controller to take a breath.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2D |
    | **Time** | ~55 min |
    | **Prereqs** | [Lesson 26 — maple-sim & game-piece physics](../../stage2c/26-maplesim/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.shoulder.ProfiledMotionTest` (`@Tag("lesson-27")`) |
    | **Reference robot** | Presto · [`superstructure/arm/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/superstructure/arm) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Replace `PIDController` with `ProfiledPIDController` and read the new method signatures fluently.
2. Pick max-velocity and max-acceleration constraints that match a real mechanism, not a textbook.
3. Add the **velocity term** (`kV`) to `ArmFeedforward` — the piece you skipped in lesson 06.
4. Decide when motion profiling is worth the configuration cost and when a vanilla PID loop is fine.
5. Recognize the first situation in this curriculum where subclassing `Command` is actually the right call.

---

## The real-world problem

Lesson 06 gave the shoulder a `PIDController`, a `kG cos(theta)` feedforward, and a four-line `periodic()` that worked beautifully — at small step sizes. The moment you commanded a 90° move, the controller saw a huge error, multiplied by `kP`, and asked for 12 V. The motor obliged. Gearboxes complain when they're handed step inputs. Batteries brown out. And once the carriage finally starts moving, integral wind-up means it overshoots the far side before you can blink.

Motion profiling fixes this by handing the controller a **moving target**: instead of "go to 90°," it sees "be at 30° next tick, 50° the tick after, 75° the tick after that." The trapezoid profile bounds velocity and acceleration; the controller only ever sees small per-tick errors and never spikes voltage. The mechanism follows a planned trajectory instead of being yanked toward an endpoint.

Presto's pivot arm does this for exactly the reason you'd expect: a Crescendo shooter that aims by snapping between angles can't reliably hit the speaker. A profiled arm settles, holds, and trusts its feedforward to keep it there.

---

## What you'll do

Open `ShoulderSubsystem.java`. Swap the `PIDController` field for a `ProfiledPIDController` and add a `TrapezoidProfile.Constraints` with explicit max velocity and acceleration. In `periodic()`, the pid call now returns voltage for the *profiled* setpoint, and your `ArmFeedforward` evaluation uses both the angle (for gravity) **and** the profile's instantaneous velocity setpoint (for `kV`). Log the profile setpoint alongside the measured position so AdvantageScope can show the planned trajectory next to the real one.

```java
private final ProfiledPIDController pid = new ProfiledPIDController(
    Shoulder.kP, Shoulder.kI, Shoulder.kD,
    new TrapezoidProfile.Constraints(
        Shoulder.MAX_VEL_RAD_PER_S,
        Shoulder.MAX_ACCEL_RAD_PER_S2));

private final ArmFeedforward ff = new ArmFeedforward(
    Shoulder.kS, Shoulder.kG, Shoulder.kV);
```

The combined-output rule from lesson 06 still holds. The only thing that changes is *what* the feedforward is told to do.

```java
@Override
public void periodic() {
  io.updateInputs(inputs);
  Logger.processInputs("Shoulder", inputs);

  double pidVolts = pid.calculate(inputs.angleRad, goalRad);
  var setpoint = pid.getSetpoint();
  double ffVolts = ff.calculate(setpoint.position, setpoint.velocity);

  io.setVoltage(pidVolts + ffVolts);
  Logger.recordOutput("Shoulder/profileSetpointRad", setpoint.position);
  Logger.recordOutput("Shoulder/profileVelocityRadPerS", setpoint.velocity);
}
```

!!! note "Where do the constraints come from?"

    You can derive max velocity from `(12 V - kS) / kV` and max acceleration from `(12 V - kS - kG) / kA` — but lesson 28 is going to give you a way to find `kV` and `kA` rigorously. For now, set conservative numbers (something like 60% of the physical maximum) and tune up. Profiles that are too slow feel safe; profiles that are too fast clip and behave like step inputs again.

---

## When a Command subclass earns its keep

Stage 1C committed hard to factories: `run`, `runOnce`, `startEnd`, `Commands.sequence`. Almost every command you've written so far is a lambda hanging off a subsystem method. Motion profiling is the **first** place in this curriculum where a `Command` subclass might actually read better than a factory.

Why? A motion-profiled "go to angle X" command genuinely owns per-instance state: the goal, the profile's progress, possibly a tolerance check for when it's "done." That state has to live somewhere across multiple `execute()` calls, and a factory closure can express it — but a small class makes the lifecycle explicit:

```java
public class MoveShoulderTo extends Command {
  private final ShoulderSubsystem shoulder;
  private final double goalRad;

  public MoveShoulderTo(ShoulderSubsystem shoulder, double goalRad) {
    this.shoulder = shoulder;
    this.goalRad = goalRad;
    addRequirements(shoulder);
  }

  @Override public void initialize() { shoulder.setGoal(goalRad); }
  @Override public boolean isFinished() { return shoulder.atGoal(); }
}
```

That's the same shape WPILib's own `TrapezoidProfileCommand` takes. You're not violating the "factories first" rule from Stage 1C — you're recognizing the exact niche the rule always carved out for stateful behavior. Most of your codebase still uses factories. A few profiled commands earn their class definition.

!!! warning "Don't subclass for sequencing"

    The temptation now is to start writing `IntakeNoteCommand`, `ScoreCommand`, `ResetEverythingCommand`. Resist. Those are still composable with `Commands.sequence(...)`. Subclass when a single command owns *its own* multi-tick state, not when you're composing multiple existing commands.

---

## Rubric

`ProfiledMotionTest` asserts:

1. The shoulder reaches each setpoint within ±2° (same tolerance as lesson 06).
2. The measured velocity never exceeds `MAX_VEL_RAD_PER_S` by more than 5%.
3. Settle time is at least 30% better than the unprofiled controller on a 90° step (you'll have both runs logged side-by-side).
4. `Shoulder/profileSetpointRad` and `Shoulder/profileVelocityRadPerS` are present in the log.

```bash
./gradlew test --tests '*ProfiledMotionTest' -DincludeTags='lesson-27'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope, plot `Shoulder/Inputs/angleRad`, `Shoulder/profileSetpointRad`, and `Shoulder/goalRad` on one chart. The measured trace should hug the setpoint trace, which in turn ramps smoothly toward the goal. On a second chart, plot `Shoulder/Inputs/velocityRadPerS` against `Shoulder/profileVelocityRadPerS` — you'll see the classic trapezoid shape.

---

## Going further

- Pass two different `Constraints` instances at runtime ("careful mode" vs "match mode"). When should the driver pick?
- Read [WPILib's `ProfiledPIDSubsystem` source](https://github.com/wpilibsuite/allwpilib/blob/main/wpilibNewCommands/src/main/java/edu/wpi/first/wpilibj2/command/ProfiledPIDSubsystem.java) and compare to your hand-rolled version.
- Compare your code to Presto's [arm controller](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/superstructure/arm). Notice they use a different profile representation for variable-goal aiming — why?

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 26**
    maple-sim & game-piece physics

    [:octicons-arrow-left-24: Back to lesson 26](../../stage2c/26-maplesim/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 28**
    System identification (SysId)

    [:octicons-arrow-right-24: Continue to lesson 28](../28-sysid/)

</div>
