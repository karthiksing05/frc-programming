# Lesson 02 — Tank Drive Wiring

> **Stage 1C · ~35 minutes · Requires [Lesson 01](../01-methods/README.md)**

A **tank drive** has a left side and a right side. To turn, you spin
the two sides at different speeds. The classic mapping from a single
joystick is called **arcade drive**:

```
leftDemand  = forward + rotation
rightDemand = forward - rotation
```

Push forward → both wheels spin forward. Push sideways → one side
speeds up, the other slows down → robot rotates. Push diagonally →
robot curves while driving.

## What you'll do

Wire up `periodic()` inside [`Drive.java`](../../src/main/java/frc/robot/subsystems/drive/Drive.java).
The file you wrote in lesson 01 — `MathUtils.java` — is already
imported. You're using your own code.

```java
@Override
public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive", inputs);

    double forward  = -controller.getLeftY();   // up on stick = +forward
    double rotation =  controller.getRightX();

    // TODO 1: pass each through MathUtils.applyDeadband
    double forwardClean  = forward;
    double rotationClean = rotation;

    // TODO 2: arcade-drive mixing
    double leftDemand  = 0.0;
    double rightDemand = 0.0;

    // TODO 3: send commands to the IO layer
    // io.setVoltage(leftDemand * 12.0, rightDemand * 12.0);
}
```

## What the IO layer is doing for you

You won't write motor controller code in this lesson. The file
[`DriveIO.java`](../../src/main/java/frc/robot/subsystems/drive/DriveIO.java)
defines an interface with two implementations:

- [`DriveIOSim`](../../src/main/java/frc/robot/subsystems/drive/DriveIOSim.java)
  uses WPILib's [`DifferentialDrivetrainSim`](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/drivesim-tutorial/drivetrain-model.html)
  to simulate physics — wheel positions, robot pose, current draw, all the realistic things.
- [`DriveIOReal`](../../src/main/java/frc/robot/subsystems/drive/DriveIOReal.java)
  is a stub for real CTRE TalonFX motors — what you'd flesh out at
  the start of a real season.

Your `Drive` class never directly talks to a motor controller. It talks
to `io` and reads from `inputs`. This is the **AdvantageKit IO Layer
pattern** — the same one used by [FRC 6328](https://github.com/Mechanical-Advantage/RobotCode2025Public)
and [FRC 254](https://github.com/Team254/FRC-2025-Public). The reason it
matters: deterministic replay. The robot can record every input from a
real match, then re-run your `periodic()` against those inputs in a
later sim and reproduce the behavior exactly — invaluable for debugging
and for grading lessons.

## Run it

```bash
./tools/frcprog.sh check 02-tank-drive
# or directly: ./gradlew lesson02
```

The rubric ([`DriveTest`](../../src/test/java/frc/robot/subsystems/drive/DriveTest.java))
runs three scenarios in the sim:
1. Hold forward → robot drives in a straight line for 2 seconds.
2. Hold rotation only → robot turns roughly in place.
3. Hold both at tiny (sub-deadband) values → robot doesn't move.

Each emits a boolean output (`Lesson02/StraightLineOK`,
`Lesson02/TurningOK`, `Lesson02/DeadbandRespected`). All three must
stay `true`.

## See it move

```bash
./gradlew simulateJava
```

Open AdvantageScope → connect to NT4 at `localhost` → open the 3D field
view. Drop your robot model into the layout and you'll see it actually
drive around as `periodic()` writes voltages to the simulated motors.

The mechanism viewer (`Mechanism2d`) is wired too — wheels visibly spin
proportional to their voltage. Watch the `Drive/Inputs/leftCurrentAmps`
plot when you slam forward from rest: you'll see the inrush spike just
like a real motor.

## Why this lesson compounds on the previous one

The first thing you'll notice on opening the file: at the top,

```java
import frc.robot.util.MathUtils;
```

That's the file **you wrote in lesson 01**. If you skipped lesson 01,
that file doesn't exist and this lesson won't compile — exactly like a
real codebase. (Run `./tools/frcprog.sh next` to see what's missing.)

When you finish lesson 02, two real files are saved in your project,
and lesson 03 (Subsystems as State Machines) will import both.

> **Stuck?** [`hints.md`](hints.md) has progressive hints.
