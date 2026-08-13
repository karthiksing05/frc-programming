# Lesson 0B — Meet Presto

**Stage 0 · 15 min · Needs: 0A**

The shooter robot. You will see its code referenced all the way through.

## Do this

1. Read the mechanism list below.
2. Open <https://github.com/Mechanical-Advantage/RobotCode2024Public>
3. Find `src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/`
4. Open `FlywheelsIO.java`. Read it. It is about 40 lines.

## The robot

Team 6328, Crescendo 2024. They wrote AdvantageKit and AdvantageScope, so their
code is the reference for how those are meant to be used. MIT licensed.

| Mechanism | Job |
|---|---|
| Swerve drive | Moves any direction without turning first |
| Flywheels | Two wheels at ~3000 RPM. A ring fed between them leaves fast. |
| Rollers | Move a ring from intake to flywheels |
| Arm | Pivots the shooter to aim |
| Climber | Pulls the robot up at the end |

## What to notice

The flywheels folder has five files:

```
FlywheelsIO.java            what the hardware can sense and be told
FlywheelsIOSim.java         a physics model, for your laptop
FlywheelsIOKrakenFOC.java   real motors, one vendor
FlywheelsIOSparkFlex.java   real motors, another vendor
Flywheels.java              the logic. The only file that decides anything.
```

One file for "what the hardware is", one per "which hardware", one for "what we do
about it". You build this yourself in lesson 16.

Do not try to understand all of Presto. It is a full season by a very good team.

## Done

You can name the mechanisms and you found `FlywheelsIO.java`.

```bash
./tools/frcprog next
```
