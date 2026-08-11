# Lesson 26 — maple-sim & game-piece physics <small>· Stage 2C</small>

<span class="stage-badge">Stage 2C · Lesson 26</span>

*Your sim robot has been teleporting through walls and pretending coral exists by faith. Today it picks up a piece because it physically ran into one.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 2C |
    | **Time** | ~55 min |
    | **Prereqs** | [Lesson 25 — Multi-tag pose estimation](../25-multitag/) |
    | **Edits** | `vendordeps/maple-sim.json` (new); `src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java` (new); `RobotContainer` mode-switch |
    | **Tests** | `frc.robot.subsystems.swerve.MapleSimTest` (`@Tag("lesson-26")`) |
    | **Reference robot** | Kelpie · [`swerve/ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Install the [maple-sim](https://github.com/Shenzhen-Robotics-Alliance/maple-sim) vendordep.
2. Stand up a `SwerveDriveSimulation` and connect it to a new `ModuleIOMapleSim`.
3. Switch your sim mode between kinematic (`ModuleIOSim`) and physics-accurate (`ModuleIOMapleSim`) by changing one line in `RobotContainer`.
4. Spawn simulated coral game pieces and detect intake via collision.
5. State plainly what maple-sim does and doesn't model, and decide when the upgrade is worth it.

---

## An honest preamble

This lesson is the **one place where Kelpie's sim stack diverges from Presto's** (see [`process/Reference-Robots.md §2.3`](/process/Reference-Robots.md)). Presto ships with WPILib's stock `FlywheelSim` / `SwerveModuleSim` and stops there. Kelpie ships *both* a `ModuleIOSim` (kinematic) and a `ModuleIOMapleSim` (physics-accurate) — it's the only candidate in the curriculum with a maple-sim integration.

That divergence is the lesson. Up through lesson 25, your robot has been driving on a kinematic sim: the swerve modules' commanded velocities are integrated into a pose, walls don't exist, and game pieces are imaginary booleans you toggled by hand. That's been fine — every lesson worked. But it has hidden two classes of bug:

- **Wall-contact autos.** Autos that score by ramming the reef and using wall contact for alignment look great in kinematic sim because the wall isn't there. On the real field they bounce.
- **Game-piece interactions.** A "did the intake see a piece?" check that's wired to a manual `SmartDashboard` boolean does not exercise the intake's actual geometry. Maple-sim spawns the piece in 3D, and your intake either runs into it or it doesn't.

This lesson is **opt-in**. You can skip it and proceed to Stage 2D with everything still working. The cost is that your auto bugs will surface on the practice field instead of in sim. The benefit is that they surface in sim.

!!! note "Opt-in, not required"

    If your project's sim is already meeting your debugging needs, keep `ModuleIOSim` and revisit maple-sim before next season. The reason this is in the curriculum is so that *when you need it, you know what door to open* — not because every robot needs physics-accurate sim from day one.

---

## What you'll do

Install the maple-sim vendordep. Inside `swerve/`, create `ModuleIOMapleSim.java` next to `ModuleIOSim.java` (both implement `ModuleIO`). In `RobotContainer`, swap the sim-mode branch to construct `ModuleIOMapleSim` instead. Spawn three coral game pieces at known field locations on simulation init. In the intake subsystem, replace the manual beam-break toggle with a maple-sim collision query. Verify in AdvantageScope's 3D view: the robot bumps walls, pieces roll when struck, and the intake's beam-break trips when the robot drives onto one.

---

## Installing maple-sim

```bash
# Add the vendordep URL via WPILib's Manage Vendor Libraries
# https://github.com/Shenzhen-Robotics-Alliance/maple-sim/releases
# Commit the resulting vendordeps/maple-sim.json
./gradlew build
```

Confirm `org.ironmaple.simulation` resolves in your IDE. If it doesn't, you grabbed the wrong release URL — re-check the version pin in `process/Reference-Robots.md`.

---

## `ModuleIOMapleSim` — copy the structure, not the lines

Kelpie's [`ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java) is the canonical reference. It does three things:

1. Holds a reference to a `SwerveModuleSimulation` (a per-module physics object owned by the parent `SwerveDriveSimulation`).
2. Forwards drive and steer voltage commands into that simulation each cycle.
3. Reads position / velocity / current back out and packs them into the same `ModuleIOInputs` your real-hardware impl populates.

```java linenums="1"
public class ModuleIOMapleSim implements ModuleIO {
  private final SwerveModuleSimulation moduleSim;
  private final SimulatedMotorController.GenericMotorController driveMotor;
  private final SimulatedMotorController.GenericMotorController steerMotor;

  public ModuleIOMapleSim(SwerveModuleSimulation sim) {
    this.moduleSim = sim;
    this.driveMotor = sim.useGenericMotorControllerForDrive()
        .withCurrentLimit(Amps.of(60));
    this.steerMotor = sim.useGenericControllerForSteer()
        .withCurrentLimit(Amps.of(20));
  }

  @Override
  public void updateInputs(ModuleIOInputs inputs) {
    inputs.drivePositionRad = moduleSim.getDriveWheelFinalPosition().in(Radians);
    inputs.driveVelocityRadPerSec = moduleSim.getDriveWheelFinalSpeed().in(RadiansPerSecond);
    inputs.steerPositionRad = moduleSim.getSteerAbsoluteFacing().getRadians();
    // ... currents, applied voltages, etc.
  }
}
```

The whole point of the IO Layer pattern (lesson 16) is paying off here: `ModuleIOMapleSim` plugs into the same `Module` and `Swerve` classes as `ModuleIOReal` and `ModuleIOSim`. No subsystem code changes.

---

## Switching sim modes

In `RobotContainer`, the mode switch is one line:

```java linenums="1"
swerve = switch (Constants.currentMode) {
  case REAL    -> new Swerve(/* ModuleIOReal × 4 + GyroIOPigeon2 */);
  case SIM     -> new Swerve(/* ModuleIOSim × 4 + GyroIOSim */);
  case MAPLE   -> new Swerve(/* ModuleIOMapleSim × 4 + GyroIOMapleSim */);
  case REPLAY  -> new Swerve(/* no-op IOs */);
};
```

Adding `MAPLE` as a sibling of `SIM` (rather than replacing it) lets you A/B the two during the same dev session. Boot once in kinematic sim, hit a bug, boot in maple-sim, see if it reproduces.

!!! warning "Don't ship maple-sim to the RIO"

    `ModuleIOMapleSim` runs a physics engine. It is for desktop sim only. The mode switch above is what keeps it from being constructed in `REAL` mode — break that and your RIO will refuse to deploy when the maple-sim dependencies can't be found.

---

## Spawning game pieces

`SimulatedArena` owns the field. Coral pieces are spawned and tracked there:

```java linenums="1"
SimulatedArena.getInstance().addGamePiece(
    new ReefscapeCoralOnField(
        new Pose2d(3.0, 4.0, Rotation2d.kZero)));
```

Each tick, the arena steps physics — pieces fall, slide, bounce off the robot's bumper polygon. The intake subsystem can ask the arena "is a coral inside my intake region?" and use that as the beam-break input:

```java
boolean beamBreakTripped = !SimulatedArena.getInstance()
    .getGamePiecesByType("Coral")
    .stream()
    .filter(p -> intakeRegion.contains(p.getPoseOnField().getTranslation()))
    .toList()
    .isEmpty();
```

That `intakeRegion` is whatever 2D polygon represents the inside of your intake. The real beam-break in `ModuleIOReal` reports a boolean too — same shape, same IO contract.

---

## Rubric

The test class `MapleSimTest` asserts:

1. Driving full-throttle into the field wall **does not** advance the pose past the wall (`Drive/Pose.X` clamps to `FIELD_LENGTH - HALF_BUMPER`).
2. A coral spawned 1.5 m in front of the robot, intake region aligned, trips the beam-break input within 3 s of full-throttle drive into it.
3. A simulated 200 N lateral impulse on the robot displaces it ≥ 5 cm (the robot can be pushed).
4. With `Constants.currentMode = SIM` (kinematic mode), assertion 1 fails. (Proves maple-sim is doing real work, not theater.)

Run locally:

```bash
./gradlew test --tests '*MapleSimTest' -DincludeTags='lesson-26'
```

---

## See it run

```bash
./gradlew simulateJava
```

In AdvantageScope's 3D field view:

- `Drive/Pose` — should refuse to cross walls.
- `Sim/CoralPoses` — array of `Pose3d`s for the spawned pieces; they should roll and tumble.
- Robot model — drive into a coral and watch it kick away under realistic friction.

Run an existing auto from lesson 12 or 13. If it depended on wall contact, it now fails in sim *before* embarrassing you on the practice field. That failure is the win.

---

## Going further

- Read [maple-sim's docs](https://shenzhen-robotics-alliance.github.io/maple-sim/) on tuning bumper friction and wheel slip. Defaults are calibrated to MK4i swerve; if your robot's different, override them.
- Add a *second* simulated robot (defender) and route it through `IntakeSimulation` to interact with your robot. Multi-robot collision is the next maple-sim power-up.
- Compare your `ModuleIOMapleSim` line-by-line to Kelpie's. What current-limit numbers did they pick, and why?

---

??? tip "Full reveal — only open if you're really stuck"

    The most common stumble is forgetting to *register* the swerve drive simulation with the arena. Kelpie does it once at construction time:

    ```java
    SwerveDriveSimulation driveSim = new SwerveDriveSimulation(
        DriveTrainSimulationConfig.Default(),
        new Pose2d(2.0, 2.0, Rotation2d.kZero));
    SimulatedArena.getInstance().addDriveTrainSimulation(driveSim);
    ```

    If you skip the `addDriveTrainSimulation` call, the modules will integrate but nothing will collide. Walls will still teleport.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 25**
    Multi-tag pose estimation

    [:octicons-arrow-left-24: Back to lesson 25](../25-multitag/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 27**
    Motion profiling

    [:octicons-arrow-right-24: Continue to lesson 27](../../stage2d/27-motion-profiling/)

</div>
