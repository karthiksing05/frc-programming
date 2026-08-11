# Lesson 0B — Meet Presto <small>· Stage 0</small>

<span class="stage-badge">Stage 0 · Lesson 0B</span>

*Before you write a line of code, look at where you're going. Today: a real, championship-level robot you can open in a browser.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 0 |
    | **Time** | ~15 min |
    | **Prereqs** | [Lesson 0A — First-run install](../0a-install/) |
    | **Edits** | None — this is an observation lesson. |
    | **Tests** | None — checkbox lesson. |
    | **Reference robot** | [Presto (Team 6328, Crescendo 2024)](https://github.com/Mechanical-Advantage/RobotCode2024Public) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Name **Presto's** main mechanisms and describe what each one does.
2. Find a real subsystem (`Flywheels.java`) on GitHub and skim its shape without panicking.
3. Open AdvantageScope, load Presto's 3D model, and watch the robot rendered in space.
4. Recognize the **IO Layer** file-naming pattern (`FlywheelsIO`, `FlywheelsIOSim`, `FlywheelsIOKrakenFOC`) when you see it later.

---

## The real-world problem

When you start learning FRC programming, the code looks like alphabet soup: `@AutoLog`, `LoggedRobot`, `SubsystemBase`, `CommandXboxController`. It's tempting to assume the people who wrote this stuff are wizards and your code will never look like theirs.

**They're not wizards.** Their code is built out of small, named pieces — exactly the pieces you'll build over the next thirty lessons. The fastest way to lose the imposter feeling is to see the destination *before* you start walking. So today, before you've written one line, you're going to look at a real championship robot's source code and see that it's just... files. Java files. With names that mostly describe what they do.

---

## Meet Presto

!!! note "Why Presto?"

    Presto is **Team 6328's "Mechanical Advantage"** robot from the 2024 Crescendo season. 6328 are the people who wrote AdvantageKit and AdvantageScope — the logging and telemetry tools the whole community now uses. Their public competition code is, definitionally, the canonical reference for "how to use these tools well."

**Repo:** [github.com/Mechanical-Advantage/RobotCode2024Public](https://github.com/Mechanical-Advantage/RobotCode2024Public) · License: MIT (open and reusable).

The game: Crescendo 2024. Robots picked up foam donuts called "notes" off the floor, aimed a pivoting shooter at a hub, and fired the notes through the hub from variable distances across the field.

### Mechanism inventory

This is the table you'll come back to whenever a future lesson cites "Presto's flywheels" or "Presto's drive." Each subsystem maps to a folder in [`src/main/java/org/littletonrobotics/frc2024/subsystems/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems).

| Subsystem | Folder | What it does | Lessons that use it |
|---|---|---|---|
| **Swerve drive** | `drive/` | Four-module swerve drivetrain | Stage 1C drive intro · Stage 2B swerve deep-dive |
| **Flywheels** | `flywheels/` | Two independent shooter wheels — different speeds spin the note like a curveball | **The canonical AdvantageKit example** · Lessons 02, 18 |
| **Rollers** | `rollers/` | Conveys notes from the floor intake to the shooter | Lesson 04 (subsystem basics) |
| **Superstructure: Arm** | `superstructure/arm/` | Pivots the shooter up and down | Lesson 06 (gravity feedforward) |
| **Superstructure: Climber** | `superstructure/climber/` | End-of-match hang on the stage trusses | Stage 2A composition |
| **Superstructure: Backpack actuator** | `superstructure/backpackactuator/` | A flip-up mechanism for scoring in the "amp" | Stage 2A composition |
| **AprilTag vision** | `apriltagvision/` | Multi-camera AprilTag fusion for field-relative pose | Stage 2C vision |
| **LEDs** | `leds/` | Driver feedback patterns | Stage 2A |
| **Generic Slam Elevator** | `GenericSlamElevator.java` | Reusable pattern for "slam to limit, then zero" mechanisms | Stage 2A pattern lesson |

!!! info "What's that 'IO' suffix?"

    You'll notice file names like `FlywheelsIO.java`, `FlywheelsIOSim.java`, and `FlywheelsIOKrakenFOC.java`. This is the **IO Layer pattern** — one interface plus implementations for sim and real hardware. We don't need to understand it yet; just notice the shape and recognize that you'll learn the why in Stage 2A. For now: *"`Sim` runs on your laptop, `KrakenFOC` runs on the real robot."*

---

## What you'll do

Three quick observations. Don't take notes — just look. The point is to *see*, not to memorize.

### 1. Watch Presto play (5 min)

Spend five minutes watching this Crescendo highlight reel of Team 6328 at their 2024 championship:

[Watch on YouTube :material-youtube:](https://www.youtube.com/watch?v=ueGyYWi8wBM){ .md-button }

Notice the rhythm: drive to a note, pick it up, drive to a shooting spot, the arm pivots, the flywheels spin, the rollers push the note through. Every motion you see is one of the subsystems in the table above doing its job.

### 2. Open `Flywheels.java` on GitHub (3 min)

Click through to the real source file:

[`flywheels/Flywheels.java` :material-github:](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java){ .md-button }

You don't need to read all of it. Just **scroll through it** for 90 seconds and notice:

- It's about 150 lines. A real championship subsystem is not enormous.
- The class extends `SubsystemBase`. You'll learn what that means in Lesson 04.
- There are methods like `runVelocity(...)`, `runCharacterization(...)`, and the inputs struct is logged with `Logger.processInputs("Flywheels", inputs)`.
- There's nothing magic happening. It's Java, with WPILib calls, in a class.

!!! quote "From the top of Flywheels.java"

    ```java
    public Flywheels(FlywheelsIO io) {
      this.io = io;
      setDefaultCommand(runOnce(() -> setGoal(Goal.IDLE))
          .andThen(run(() -> {})).withName("Flywheels Idle"));
    }
    ```

    By Stage 1D you'll write code that looks exactly like this.

### 3. Render Presto in 3D (5 min)

The Presto repo ships a 3D model of the robot you can load into AdvantageScope. This is the same model the team uses on their drive-station laptop during matches.

1. Open the Presto repo's [`ascope_assets/Robot_Presto/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/ascope_assets/Robot_Presto) folder.
2. Download `config.json`, `model.glb`, and `model_0.glb` (the three files in that folder).
3. Open **AdvantageScope** (installed last lesson).
4. From the menu, choose **Help → Show Assets Folder**. Drag the three files you downloaded into the folder that opens. Restart AdvantageScope.
5. Open a **3D Field** tab. In the robot dropdown, select **Robot_Presto**.

You should see Presto rendered on the Crescendo field. Spin the view around. That's the actual robot, the actual geometry, that scored at champs.

!!! tip "Photo placeholder"

    *(A side-by-side image of the rendered Presto in AdvantageScope next to a photo of the real Presto on the field will live here once the assets are added to the site.)*

---

## Why this matters

You just looked at a real championship robot's code, watched it play, and rendered it in 3D — and the world didn't end. You also have new vocabulary: *flywheels*, *swerve*, *AprilTag*, *IO layer*, *subsystem*. Don't memorize the definitions. Just let your brain index them so they feel familiar the second time you see them.

By Stage 2A, you'll have written a robot of your own that looks structurally similar to Presto (smaller, less polish, but the same shape). And you'll cite this lesson when somebody on your team asks you, *"What's the difference between `FlywheelsIOSim` and `FlywheelsIOKrakenFOC`?"*

---

## Going further (optional)

- Read [Team 6328's build blog](https://www.chiefdelphi.com/c/general/build-blogs/189) for Crescendo. It's one of the best programming-team writeups in FRC.
- Skim the [AdvantageKit IO interfaces doc](https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/) — don't try to absorb it, just notice the file names match what you saw in `flywheels/`.
- Browse [`RobotContainer.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/RobotContainer.java) and look at the section where buttons are bound to commands. You'll write this exact pattern in Lesson 08.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 0A**
    First-run install

    [:octicons-arrow-left-24: Back to lesson 0A](../0a-install/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 0C**
    Meet Kelpie — the pick-and-place robot

    [:octicons-arrow-right-24: Continue to lesson 0C](../0c-meet-kelpie/)

</div>
