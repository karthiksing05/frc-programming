# Lesson 10 — Telemetry & AdvantageScope Basics <small>· Stage 1C</small>

<span class="stage-badge">Stage 1C · Lesson 10</span>

*"Why isn't my flywheel reaching target?" Without telemetry, you're debugging blind. With it, the answer is on a graph in five seconds.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1C |
    | **Time** | ~30 min |
    | **Prereqs** | [Lesson 09 — Command composition](../09-command-composition/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/flywheels/Flywheels.java` |
    | **Tests** | `frc.robot.subsystems.flywheels.FlywheelsTelemetryTest` (`@Tag("lesson-10")`) |
    | **Reference robot** | Presto · [`flywheels/Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels) — `Logger.recordOutput` calls scattered throughout |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Add `Logger.recordOutput("Flywheels/TargetRPM", targetRpm)` to any subsystem.
2. Launch AdvantageScope, connect to NetworkTables 4 at `localhost`, and plot a value.
3. Read a step-response trace and identify "overshoot" vs "settle time."
4. Pick a logging key convention (`SubsystemName/Field`) and stick to it.

---

## The real-world problem

You built a flywheel subsystem in Lesson 08. It worked. Or at least, it *looked* like it worked — the LED lit, the simulated motor spun, the score sequence in Lesson 09 ran.

Now your teammate runs the score sequence and says *"the flywheel isn't reaching speed."* You ask what speed it's reaching. They shrug. You ask what speed they expected. They shrug again. You ask what the error is over time. They've already left to grab snacks.

Without telemetry, you are guessing. With telemetry, you have a graph. The graph tells you whether the issue is your PID gains, your supplier wiring, your `waitSeconds` timing, or — most often — that you and your teammate disagreed on what "spun up" meant.

There's a saying among FRC veterans: *if you didn't plot it, it didn't happen.* This lesson is the first rung of that ladder.

---

## What you'll do

Add three `Logger.recordOutput` calls to `Flywheels.periodic()` — target RPM, actual RPM, and error. Open AdvantageScope. Connect to NetworkTables 4 at `localhost`. Drag all three keys into the line-chart tab. Trigger the score sequence and read the trace.

```java linenums="1"
@Override
public void periodic() {
  io.updateInputs(inputs);
  Logger.processInputs("Flywheels", inputs);

  // LESSON 10: add these three lines.
  Logger.recordOutput("Flywheels/TargetRPM", targetRpm);
  Logger.recordOutput("Flywheels/ActualRPM", inputs.velocityRPM);
  Logger.recordOutput("Flywheels/ErrorRPM", targetRpm - inputs.velocityRPM);
}
```

That's the whole edit. The hard part isn't the code — it's learning to *read* the chart it produces.

---

## `processInputs` vs `recordOutput`

You'll see both calls in the snippet above. They sound similar; they do different things.

| Call | Purpose | Where the data comes from |
|---|---|---|
| `Logger.processInputs("Flywheels", inputs)` | Logs the *inputs* struct (sensor reads, encoder values) and replays them on log-replay runs. | The IO layer (`io.updateInputs(inputs)` filled it in). |
| `Logger.recordOutput("Flywheels/TargetRPM", value)` | Logs a single *output* value — something your subsystem computed or decided. | Your code, this tick. |

Inputs are "what the world told us." Outputs are "what we computed in response." On a real robot or in sim, they're both NetworkTables entries you can plot. On a replay run (Lesson 19), inputs are re-fed from the log and outputs are recomputed — which is exactly how AdvantageKit's log replay reproduces a bug after the fact.

For Lesson 10, you only need `recordOutput`. The rest is foundation for Stage 2A.

---

## Key naming convention

Pick a convention now or pay for it later. The one Presto uses, and the one we'll standardize on:

```
SubsystemName/FieldName            ← outputs from this subsystem
SubsystemName/Inputs/SensorName    ← inputs from the IO layer
SubsystemName/Subfield/FieldName   ← nested groups for complex subsystems
```

Always **PascalCase** for the subsystem segment. AdvantageScope sorts the key tree alphabetically; consistent casing makes "all Flywheels stuff together" trivially achievable.

!!! warning "Don't mix conventions"

    `drive_speed`, `Drive/speed`, `DriveSpeed`, and `drive.speed` are four different keys, and you'll absolutely create all four if you don't pick one now. Refactoring keys later breaks every saved AdvantageScope layout that referenced them.

---

## Connecting AdvantageScope to NetworkTables

In a sim run, your robot publishes to NetworkTables 4 on `localhost`. AdvantageScope reads from there live.

1. Run `./gradlew simulateJava`. Wait for the "robot code initialized" message.
2. Open AdvantageScope (the desktop app you installed in Lesson 0A).
3. Menu → **File → Connect to Robot** (or use the connection icon in the top right).
4. In the **Server** field, type `localhost` (or `127.0.0.1`). Hit Connect.
5. The status indicator turns green. The left pane fills with the NT key tree.

In the key tree, expand `NT/RealOutputs/Flywheels`. You should see `TargetRPM`, `ActualRPM`, and `ErrorRPM`. Drag each into the line-chart tab.

!!! info "Why `RealOutputs`?"

    AdvantageKit publishes outputs under `RealOutputs/...` when the robot is running normally, and `ReplayOutputs/...` when running a log replay. The Lesson 19 arc makes this distinction matter; for now, just look under `RealOutputs`.

---

## Reading a step response

Run the score sequence from Lesson 09 once and study the plot. There are three features worth naming:

- **Rise time** — how long it takes the actual RPM to go from ~10% to ~90% of target. Faster is better, up to a point.
- **Overshoot** — does the actual RPM exceed target before settling? Some overshoot is normal; >20% means your gains are too aggressive.
- **Settle time** — how long until the trace stays within ±5% of target. Long settle times mean a `waitSeconds(0.5)` might not be enough.

This vocabulary unlocks productive debugging conversations. "The flywheel's settling time is 0.8 s but our `waitSeconds` is 0.5" tells you *exactly* what to change — without it, you'd guess.

!!! quote "From the AdvantageKit docs"

    *"`Logger.recordOutput` is for values that the robot code chooses or computes — anything where the question 'what would we have done differently?' is meaningful."*

    — [AdvantageKit · Recording Outputs](https://docs.advantagekit.org/data-flow/recording-outputs/)

---

## Rubric

`FlywheelsTelemetryTest` (with `@Tag("lesson-10")`) asserts:

1. `NT/RealOutputs/Flywheels/TargetRPM` exists and updates each cycle.
2. `NT/RealOutputs/Flywheels/ActualRPM` exists and matches `inputs.velocityRPM` within tolerance.
3. `NT/RealOutputs/Flywheels/ErrorRPM` equals `TargetRPM - ActualRPM` each cycle (verified by reading the log).
4. All three keys follow the `SubsystemName/Field` PascalCase convention.

Run it locally:

```bash
./gradlew test --tests '*FlywheelsTelemetryTest' -DincludeTags='lesson-10'
```

---

## See it run

```bash
./gradlew simulateJava
```

With AdvantageScope connected, schedule the score command (Lesson 09's button). You should see three traces:

- `TargetRPM` — a step from 0 to your target value the moment the command starts, then back to 0 at the end.
- `ActualRPM` — a curve that ramps up toward target, possibly overshoots, settles, then decays.
- `ErrorRPM` — the gap between them. It starts large, shrinks, and approaches zero. If it *doesn't* approach zero, you've found a bug.

Spend five minutes just watching the chart while you trigger the command repeatedly. The shape of the curve is the diagnostic; once you know what "good" looks like, "bad" jumps out.

---

## Going further

- Add `Logger.recordOutput("Flywheels/AtSpeed", Math.abs(error) < 50.0)` and plot it as a boolean strip. Now your composition can use `.until(flywheels::atSpeed)` in place of `waitSeconds(0.5)` — Lesson 09's exercise.
- Read Presto's [`Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels). Count the `Logger.recordOutput` calls. Their density is the standard — every quantity that matters is logged.
- In AdvantageScope, save your current layout (File → Save Layout). Commit the JSON into the project's `dashboards/` folder so teammates open the same view.
- Lesson 18 introduces `@AutoLogOutput`, which auto-logs values from annotated getters. Until then, prefer explicit `recordOutput` calls — they're easier to reason about.

??? tip "Full reveal — only open if you're really stuck"

    ```java
    @Override
    public void periodic() {
      io.updateInputs(inputs);
      Logger.processInputs("Flywheels", inputs);

      Logger.recordOutput("Flywheels/TargetRPM", targetRpm);
      Logger.recordOutput("Flywheels/ActualRPM", inputs.velocityRPM);
      Logger.recordOutput("Flywheels/ErrorRPM", targetRpm - inputs.velocityRPM);
    }
    ```

    Three lines. The rest of the lesson is learning to *read* the chart they produce.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 09**
    Command composition

    [:octicons-arrow-left-24: Back to lesson 09](../09-command-composition/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 11**
    Default commands done right

    [:octicons-arrow-right-24: Continue to lesson 11](../../stage1d/11-default-commands/)

</div>
