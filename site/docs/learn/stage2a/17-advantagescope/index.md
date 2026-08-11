# Lesson 17 — AdvantageScope first-class <small>· Stage 2A</small>

<span class="stage-badge">Stage 2A · Lesson 17</span>

*Plots told you the elevator reached 1.5 m. A picture would tell you whether it crashed into the shoulder on the way up.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2A |
    | **Time** | ~40 min |
    | **Prereqs** | [Lesson 16 — The IO Layer pattern](../16-io-layer/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java` (publish `Mechanism2d`); commit `lessons/17-advantagescope/AdvantageScope.json` |
    | **Tests** | `frc.robot.subsystems.elevator.MechanismVizTest` (`@Tag("lesson-17")`) |
    | **Reference robot** | Presto · [`ascope_assets/Robot_Presto/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/ascope_assets/Robot_Presto) (the `.glb` model) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Build a `Mechanism2d` for the elevator + shoulder + wrist and publish it via NT.
2. Load Presto's `Robot_Presto.glb` into AdvantageScope's 3D field view.
3. Save a custom AdvantageScope layout and commit the JSON for teammates.
4. Drop a `Pose2d` value onto the 3D field and watch the robot follow your drive.
5. Choose between line chart, swerve states, mechanism viewer, and 3D field per debugging task.

---

## The real-world problem

Lesson 10 taught `Logger.recordOutput` and a line chart. That's enough for *numerical* problems — overshoot, settle time, current draw. It's not enough for *geometric* problems.

If your elevator hits 1.5 m at t=3.2 s but the shoulder is still pivoted forward, the wrist crashes into the reef. The numbers all look fine. The picture would have screamed.

AdvantageScope ships four views that beat plotting at four different jobs:

- **Mechanism viewer** — articulated 2D linkage of your elevator/shoulder/wrist.
- **3D field** — the *whole robot* on the actual game field, in the right pose.
- **Swerve states** — four-arrow wheel direction view (you'll need this in 2B).
- **Joystick view** — which buttons did the driver actually press during that replay?

Today: the first two.

---

## What you'll do

Publish a `Mechanism2d` from your elevator subsystem. Open AdvantageScope, drop the mechanism onto a new tab, and watch it animate as your elevator commands fire. Then load Presto's `.glb` model, drop `Drive/Pose` onto the 3D field view, and drive around in sim. Save the resulting layout to `lessons/17-advantagescope/AdvantageScope.json` and commit it.

---

## Step 1 — Build the `Mechanism2d`

`Mechanism2d` is a tree: a root, then ligaments (line segments with length + angle) chained off it. Each ligament is a child of the previous. The elevator's vertical post is the root's first ligament; the shoulder pivot is a child of the elevator's tip; the wrist is a child of the shoulder.

```java linenums="1"
private final Mechanism2d viz = new Mechanism2d(2.0, 2.5);
private final MechanismRoot2d root = viz.getRoot("elevatorBase", 1.0, 0.0);
private final MechanismLigament2d post =
    root.append(new MechanismLigament2d("post", 0.5, 90.0));   // height, vertical
private final MechanismLigament2d shoulder =
    post.append(new MechanismLigament2d("shoulder", 0.4, 0.0));
private final MechanismLigament2d wrist =
    shoulder.append(new MechanismLigament2d("wrist", 0.2, 0.0));
```

In `periodic()`, push live state into the ligaments:

```java linenums="1"
post.setLength(inputs.positionMeters);                 // elevator height
shoulder.setAngle(Rotation2d.fromRadians(shoulderRad));
wrist.setAngle(Rotation2d.fromRadians(wristRad));
Logger.recordOutput("Elevator/Mechanism", viz);
```

That last line is the whole point: `Logger.recordOutput` knows how to serialize a `Mechanism2d`. AdvantageScope reads it back as a structured value, not a wad of numbers.

!!! note "Why `Mechanism2d` and not the 3D field for this?"

    The 3D field shows the *chassis pose* — where the whole robot is on the carpet. The mechanism viewer shows the *internal geometry* — how the arm is folded right now. You usually want both; they answer different questions.

---

## Step 2 — Open AdvantageScope and add the tab

```bash
./gradlew simulateJava
```

In AdvantageScope: **File → Connect to Robot** → `localhost` (NT4 is on by default in sim). The signal tree on the left now shows `Elevator/Mechanism` as a structured value with a wrench icon.

- Click the **+** at the top to add a tab → choose **Mechanism**.
- Drag `Elevator/Mechanism` onto the empty tab.

Trigger an elevator command. The 2D linkage articulates in real time. You're seeing what the line chart can't show you.

---

## Step 3 — The 3D field view

Add another tab → **3D Field**. You'll see an empty game field. Two things turn on the robot:

1. **Drop `Drive/Pose` (or whatever you named your `@AutoLogOutput` pose — Lesson 18 standardizes this)** onto the **Poses** section in the right sidebar.
2. **Load the robot model**: click the cube icon, point at `ascope_assets/Robot_Presto/config.json` from the [Presto repo](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/ascope_assets/Robot_Presto). AdvantageScope ships several built-in models too — pick "Generic" if you don't have the Presto assets locally.

Drive in sim. The Presto-shaped robot translates and rotates on the actual Crescendo field. The first time you see it, the abstraction "the robot has a `Pose2d`" becomes concrete in a way no plot can match.

!!! quote "From the AdvantageScope docs"

    "The 3D Field view renders the robot in three dimensions on the field, with support for component animation, AprilTags, game pieces, and trajectory visualization."

    — <https://docs.advantagescope.org/tab-reference/3d-field/>

---

## Step 4 — Save the layout

AdvantageScope lets you export a layout JSON. **File → Export Layout** → save to `lessons/17-advantagescope/AdvantageScope.json` in your repo. Commit it. Now a teammate can `git pull`, **File → Import Layout**, and see the same tabs you do.

This is small but matters. "Open AdvantageScope and recreate my view" is a 10-minute task someone won't bother to do. "Open AdvantageScope and load the layout" is 10 seconds.

---

## Rubric

`MechanismVizTest` asserts:

1. `Elevator/Mechanism` publishes as a structured value (verified by reading the NT type).
2. The published `Mechanism2d`'s post ligament length tracks `inputs.positionMeters` within 1 mm.
3. `lessons/17-advantagescope/AdvantageScope.json` exists and parses as valid JSON.

```bash
./gradlew test --tests '*MechanismVizTest' -DincludeTags='lesson-17'
```

---

## See it run

```bash
./gradlew simulateJava
```

Send the elevator through its four lesson-05 setpoints. The mechanism viewer animates the climb. Drive the chassis around with WASD on the keyboard joystick — the 3D field shows Presto pirouetting on the Crescendo carpet. Save a screenshot for your README.

---

## Going further

- Add a **second** robot pose called `Drive/PoseGoal` and publish your auto's target. Drop both onto the 3D field and watch the gap close as path-following converges.
- Color the wrist ligament green when the gamepiece-detected trigger is true. (`MechanismLigament2d` has a `setColor` method.)
- Read Presto's `ascope_assets/Robot_Presto/config.json` — note how it declares articulated components (e.g., the shooter pivot) that AdvantageScope animates from logged poses.

!!! tip "Kelpie has no `.glb` — that's not a blocker"

    [Kelpie](https://github.com/HighlanderRobotics/Reefscape) ships no 3D model file. Their lessons rely on `Mechanism2d` + photos. If your team builds a Reefscape robot, modeling a quick `.glb` from CAD is a one-afternoon contribution and a permanent upgrade for everyone reading your code.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 16**
    The IO Layer pattern

    [:octicons-arrow-left-24: Back to lesson 16](../16-io-layer/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 18**
    AdvantageKit logging discipline

    [:octicons-arrow-right-24: Continue to lesson 18](../18-logging-discipline/)

</div>
