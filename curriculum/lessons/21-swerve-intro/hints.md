# Hints — Lesson 21

## Hint 1 — Where to start

Do one module first. Get `Module.java` closing the loop on angle and speed with a `DCMotorSim` behind it, and verify it in isolation before building four.

## Hint 2 — The shape of the answer

Then kinematics. `SwerveDriveKinematics` takes the four module positions relative to the robot centre; get the signs right on paper before you type them.

## Hint 3 — What usually goes wrong

Wrong module order. Kinematics returns states in the same order you gave positions — mixing them up produces a robot that drives diagonally when asked to go straight, which looks like a physics bug and is a bookkeeping bug.

Skipping `optimize`, so modules take the long way round and the robot lurches on every direction change.

Skipping `desaturateWheelSpeeds`, so full-speed diagonal travel silently becomes something else.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

Kelpie's [`swerve/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/swerve) is a complete, readable implementation with a maple-sim variant alongside the plain one. AdvantageKit's `template_projects/` also ships a swerve skeleton designed to be read.

</details>
