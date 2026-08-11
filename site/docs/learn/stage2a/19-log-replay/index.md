# Lesson 19 — Log replay for debugging <small>· Stage 2A</small>

<span class="stage-badge">Stage 2A · Lesson 19</span>

*"I'll reproduce it on the robot" is the most expensive sentence in FRC. Replay deletes that sentence.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2A |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 18 — AdvantageKit logging discipline](../18-logging-discipline/) |
    | **Edits** | `src/main/java/frc/robot/Robot.java` — wire `Constants.Mode.REPLAY` branch with `LogFileUtil.findReplayLog()` + `setUseTiming(false)` |
    | **Tests** | `frc.robot.ReplayTest` (`@Tag("lesson-19")`) — verifies replay outputs match the original within tolerance |
    | **Reference robot** | Presto · [`Robot.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/Robot.java) (the canonical mode-switch + replay setup) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Capture a WPILOG from a sim or real-match run.
2. Re-launch the robot binary in `REPLAY` mode against that log.
3. Use `LogFileUtil.findReplayLog()` to pick up the log file automatically.
4. Call `setUseTiming(false)` so replay runs as fast as the CPU will go — minutes, not match-time.
5. **Add a new `Logger.recordOutput` line, replay the same log, and see the new value as if it had always been there.**

---

## The real-world problem

You're at champs. During the quals-3 match, the elevator slammed into the shoulder at 0:42. The driver swears she didn't hit X. The shoulder code looks fine. The elevator code looks fine. You can't reproduce it on the practice field. You have two minutes between matches.

Without replay: you re-deploy with extra `println`s, drive in laps, hope it happens again. It won't. You go home with a broken robot and no diagnosis.

With replay: you copy the `.wpilog` off the RoboRIO via USB. You launch the binary in REPLAY mode pointed at that log. The bug recurs *deterministically*, in slow motion, with a debugger attached. You add `Logger.recordOutput("Superstructure/InternalState", state)` to your code, replay the same log, and the new signal shows up retroactively — because every input the code ever read came through the IO layer and got logged in Lesson 16.

This is the killer feature. The IO refactor in Lesson 16 was the price of admission. Today you collect the payoff.

---

## What you'll do

Run a sim. Save its log. Launch the same binary in REPLAY mode. Open the resulting `_sim.wpilog` in AdvantageScope and prove that outputs match. Then add a new logged value to the elevator. Re-run replay. Watch the new value appear in the new log file — populated from the old inputs.

---

## Step 1 — Capture a log

```bash
./gradlew simulateJava
```

Drive around for 30 seconds. Trigger an elevator command. Stop the sim with Ctrl-C. Look in `logs/` (configured in `Robot.java`'s `dataReceivers` — Presto's setup is the canonical example). You'll see a file like `akit_25-06-01_14-22-31.wpilog`. That's your replay seed.

---

## Step 2 — Add the REPLAY branch in `Robot.java`

Presto's [`Robot.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/Robot.java) shows the exact shape. The key lines:

```java linenums="1"
switch (Constants.getMode()) {
  case REAL -> {
    Logger.addDataReceiver(new WPILOGWriter("/U/logs"));
    Logger.addDataReceiver(new NT4Publisher());
  }
  case SIM -> {
    Logger.addDataReceiver(new WPILOGWriter("logs"));
    Logger.addDataReceiver(new NT4Publisher());
  }
  case REPLAY -> {
    setUseTiming(false);                                      // (1)
    String logPath = LogFileUtil.findReplayLog();             // (2)
    Logger.setReplaySource(new WPILOGReader(logPath));        // (3)
    Logger.addDataReceiver(
        new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
  }
}
Logger.start();
```

1. `setUseTiming(false)` — *the* line that makes replay fast. Without it, AdvantageKit waits 20 ms between cycles to match real-time. With it, the JVM rips through 5 minutes of match in a few seconds.
2. `LogFileUtil.findReplayLog()` — picks the log via a CLI argument, an env var, or the most-recent file in `logs/`. The convenience function exists so you don't hardcode paths into the binary.
3. `setReplaySource(...)` is the magic. AdvantageKit's `Logger.processInputs("Drive", inputs)` call — the one you've been writing in every IO Layer subsystem — now *overwrites* `inputs` from the log file instead of reading from the IO impl. Your code can't tell the difference.

!!! warning "Don't forget `Constants.Mode.REPLAY` selection logic"

    The cleanest pattern (per Presto): in `Constants.java`, expose `Mode getMode()` that returns `REAL` when on a real RIO, `REPLAY` if a system property `-Dreplay=true` is set, and `SIM` otherwise. Then `./gradlew simulateJava -Dreplay=true` flips the switch.

---

## Step 3 — Replay the log

```bash
./gradlew simulateJava -Dreplay=true
```

The sim window briefly opens and immediately closes — replay runs as fast as it can and exits when the source log ends. Look in `logs/` for `akit_25-06-01_14-22-31_sim.wpilog`. That's the replay output.

Open both logs in AdvantageScope (you can open multiple). Drop `Drive/Inputs/leftPositionMeters` from each. The traces overlap perfectly. **That's determinism — the same inputs produced the same outputs.** Replay is correct only if this holds.

---

## Step 4 — The "add a logged output, replay, see it" trick

In `ElevatorSubsystem.java`, add a new line:

```java
@AutoLogOutput(key = "Elevator/HeightAboveStow")
private double heightAboveStow() {
  return inputs.positionMeters - Constants.Elevator.STOW_HEIGHT;
}
```

Build. Replay:

```bash
./gradlew simulateJava -Dreplay=true
```

Open the new `_sim.wpilog`. `Elevator/HeightAboveStow` is there — populated correctly for the entire 30-second log. The line never existed when you originally drove the robot. **You added an output to historical data.**

This is the moment most students audibly say "oh." It's why the IO Layer pattern wasn't optional.

---

## Why this works (the mental model)

```
              IO impl
              ──────
  REAL  ──→  sensors  ──→  inputs  ──→  subsystem  ──→  outputs  ──→  log
                              ↑                            │
                              │                            └──→  (also to NT)
                              │
  REPLAY ────────────────────┘            (subsystem runs unchanged;
            inputs come                    outputs go to a new log)
            from the log
```

The IO Layer is the *seam*. Every sensor read goes through it. Every motor write goes through it. In REAL mode the seam is the hardware. In REPLAY mode the seam is the log. The subsystem code can't tell — and that's the whole point.

If you read a sensor *outside* the IO layer (a stray `gyro.getYaw()` call somewhere in `Drive.java`), replay diverges. That's why Lesson 18 was strict about pushing all reads through `inputs`.

---

## Rubric

`ReplayTest` asserts:

1. `./gradlew simulateJava -Dreplay=true` produces an `_sim.wpilog`.
2. For every `Drive/Inputs/*` key, the values in the replay log match the original within `1e-9`.
3. A new `@AutoLogOutput` field added between captures appears in the replay log.

```bash
./gradlew test --tests '*ReplayTest' -DincludeTags='lesson-19'
```

---

## See it run

```bash
./gradlew simulateJava                  # capture
./gradlew simulateJava -Dreplay=true    # replay
```

Two `.wpilog`s in `logs/`. Open both in AdvantageScope; the inputs traces lie on top of each other; the outputs trace shows your newly-added signal. The replay finished in seconds, not minutes.

---

## Going further

- Pin a bug. Add a `Logger.recordOutput("Elevator/IntegralAccumulator", pid.getAccumulatedError())` line. Replay. The integral wind-up is now visible across the whole capture. You've debugged a PID without re-running the robot.
- Set up a CI job that replays a checked-in "golden log" on every PR and asserts the outputs match a snapshot. (Mentioned in [Infrastructure-Analysis.md §3.8](/process/Infrastructure-Analysis.md) as research-direction; not in scope here, but worth knowing exists.)
- Read [AdvantageKit's log-replay theory page](https://docs.advantagekit.org/theory/log-replay-comparison/). It's short and frames replay against the alternatives (sim-only testing, hardware re-runs).

!!! success "What you can now say at a build meeting"

    "Bring me the match log; I'll replay it before the next match." This is a sentence top teams say. You can say it now.

---

??? tip "Full reveal — the complete `Robot.java` mode-switch block"

    Most of this is verbatim from Presto's [`Robot.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/Robot.java). Don't peek until you've written your own.

    ```java
    @Override
    public void robotInit() {
      Logger.recordMetadata("ProjectName", "MyRobot");
      Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);

      switch (Constants.getMode()) {
        case REAL -> {
          Logger.addDataReceiver(new WPILOGWriter("/U/logs"));
          Logger.addDataReceiver(new NT4Publisher());
        }
        case SIM -> {
          Logger.addDataReceiver(new WPILOGWriter("logs"));
          Logger.addDataReceiver(new NT4Publisher());
        }
        case REPLAY -> {
          setUseTiming(false);
          String logPath = LogFileUtil.findReplayLog();
          Logger.setReplaySource(new WPILOGReader(logPath));
          Logger.addDataReceiver(
              new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        }
      }
      Logger.start();
      robotContainer = new RobotContainer();
    }
    ```

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 18**
    AdvantageKit logging discipline

    [:octicons-arrow-left-24: Back to lesson 18](../18-logging-discipline/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 20**
    Subsystem composition at scale

    [:octicons-arrow-right-24: Continue to lesson 20](../20-superstructure/)

</div>
