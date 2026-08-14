# Lesson 0B — Meet Presto

**Stage 0 · 15 min · Needs: 0A**

Presto is the shooter robot, and its code is referenced all the way through this
curriculum. Before you look at any of it, it helps to know what game it was built to
play, because every mechanism on a robot exists to answer something the game asked for.

## Do this

1. Read about the game and the robot below.
2. Open <https://github.com/Mechanical-Advantage/RobotCode2024Public>
3. Find `src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/`
4. Open `FlywheelsIO.java` and read it. It is about 40 lines.

## The game it was built for

The 2024 game was called **Crescendo**. The game piece was an orange foam ring, about
the size of a dinner plate, called a **Note**. Robots scored by getting Notes into two
places: the **Speaker**, a tall opening you had to shoot up into, and the **Amp**, a
low slot you dropped a Note into from close range. Filling the Amp temporarily doubled
the value of Speaker shots, so there was a real strategic reason to do both.

In the endgame, robots climbed a **Chain** hanging from a raised structure called the
**Stage**, and could score one more Note into a **Trap** on the side of it.

## The robot

Presto was built by Team 6328 for that game. They also wrote AdvantageKit and
AdvantageScope, which is why their code is the reference for how those tools are meant
to be used. It is MIT licensed, so you can read all of it.

Every mechanism on it maps to something in the paragraph above.

The **swerve drive** lets the robot move in any direction without turning to face that
direction first, which matters when you are shooting at a fixed target while dodging
defenders. The **flywheels** are two wheels spun to roughly 3000 RPM; feeding a Note
between them throws it, and the speed determines how far. The **rollers** move a Note
from the floor intake up to the flywheels. The **arm** pivots the whole shooter to
change the launch angle, which is what lets the robot score from different distances.
The **climber** pulls the robot up the Chain at the end of the match.

The flywheels and the arm are the two you will effectively rebuild yourself — lesson 06
is an arm with gravity compensation, and lesson 14 is a flywheel held at a target
speed.

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
