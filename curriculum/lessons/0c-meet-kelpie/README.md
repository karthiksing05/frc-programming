# Lesson 0C — Meet Kelpie

> **Stage 0 · ~15 minutes · Prerequisite: 0A**

The other reference robot, and deliberately a different kind of machine.

Kelpie is Team 8033 Highlander Robotics's 2025 Reefscape robot. Where Presto
shoots, Kelpie picks things up and puts them down precisely — which turns out to
need a completely different set of skills from the code.

## What you'll learn

1. Kelpie's mechanisms, and why a pick-and-place robot is organised differently
   from a shooter.
2. That there is more than one reasonable way to name and structure things.
3. Where the elevator and arm code lives — the mechanisms you will build in
   lessons 05 and 06.

## What you'll do

### The robot

Reefscape asked robots to pick up lengths of PVC pipe ("coral") and place them on
a branching structure at four different heights, plus handle a ball ("algae").

| Mechanism | What it does |
|---|---|
| **Swerve drive** | Same idea as Presto's. |
| **Elevator** | Lifts the whole end effector up and down. Four heights, and it has to hit them exactly. |
| **Shoulder** | Pivots the end effector forward and back. |
| **Wrist** | Rotates the gripper at the end of the shoulder. |
| **Roller** | Grabs and releases game pieces. |
| **Funnel** | Guides pieces in from the intake station. |
| **Climber** | End-of-match climb. |

Notice how many separately-controlled joints there are. A shooter needs one number
right (wheel speed) and a rough aim. A pick-and-place robot needs an elevator, a
shoulder and a wrist to all be in the right place *at the same time*, or the game
piece does not go where you wanted. That difference drives everything about how the
code is organised.

### The code

Open <https://github.com/HighlanderRobotics/Reefscape> and find
`src/main/java/frc/robot/subsystems/elevator/`.

```
ElevatorIO.java            what an elevator can sense and be told
ElevatorIOReal.java        real hardware
ElevatorIOSim.java         a physics model
ElevatorSubsystem.java     the logic
```

Same four-way split as Presto's flywheels. But look at the names:

- 8033 calls their hardware file `ElevatorIOReal`.
- 6328 calls theirs `FlywheelsIOKrakenFOC`.

Both are right, and the difference is a real judgement call. `IOReal` says "real
versus simulated", which is the distinction that matters when you are learning.
`IOKrakenFOC` says "this specific motor", which is the distinction that matters
when you support two of them at once. The interface is what makes either choice
survivable.

That two excellent teams disagree about this — and that neither is wrong — is
worth internalising early. A great deal of programming argument is like this.

### One thing worth doing

Open `ElevatorIOSim.java`. It is a physics model of an elevator: mass, gearing,
drum radius, gravity. In lesson 05 you will control a simulated elevator built from
exactly these ingredients, using exactly this class from WPILib.

Kelpie also maintains a
[public training repository](https://github.com/HighlanderRobotics/Highlanders-Training)
— a whole second curriculum, from the same team, structured differently. If
something here does not click, a second explanation is sometimes all it takes.

## Done?

You can name Kelpie's mechanisms and you have found `ElevatorIO.java`.

```bash
./tools/frcprog next
```

Next is your first line of code.

## Why two robots

Because a curriculum that invents a new imaginary robot for every lesson makes you
learn a new machine and a new concept simultaneously, every time. Coming back to
the same two means that by Stage 1C, "Kelpie's elevator" and "Presto's flywheels"
are things you already know, and the only new thing in a lesson is the idea it is
actually about.
