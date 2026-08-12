# Hints — Lesson 24

## Hint 1 — Where to start

Get the simulated camera producing targets before writing any fusion code. `VisionSystemSim` plus the field layout will show you what the camera sees; if it sees nothing, no amount of estimator work helps.

## Hint 2 — The shape of the answer

Then add measurements one at a time and watch the pose on AdvantageScope's field view. A correction should look like a gentle pull, not a jump.

## Hint 3 — What usually goes wrong

Getting the camera-to-robot transform wrong. Every pose is then offset by exactly that error, consistently, which reads as 'vision is broken' rather than 'the mounting numbers are wrong'.

Accepting measurements with high ambiguity, which single-tag results routinely have.

Using `Timer.getFPGATimestamp()` instead of the result's own timestamp, discarding the latency compensation.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[PhotonVision documentation](https://docs.photonvision.org/), particularly the simulation and pose-estimation sections. Presto's [`apriltagvision/`](https://github.com/Mechanical-Advantage/RobotCode2024Public/tree/main/src/main/java/org/littletonrobotics/frc2024/subsystems/apriltagvision) is the production reference.

</details>
