# Lesson 0C — Meet Kelpie

**Stage 0 · 15 min · Needs: 0A**

The pick-and-place robot. A different problem from Presto, and different code.

## Do this

1. Read the mechanism list below.
2. Open <https://github.com/HighlanderRobotics/Reefscape>
3. Find `src/main/java/frc/robot/subsystems/elevator/`
4. Open `ElevatorIOSim.java`. You control this exact mechanism in lesson 05.

## The robot

Team 8033, Reefscape 2025. Picks up PVC tubes and places them on a structure at
four heights.

| Mechanism | Job |
|---|---|
| Swerve drive | Same idea as Presto's |
| Elevator | Lifts the whole end effector |
| Shoulder | Pivots it forward and back |
| Wrist | Rotates the gripper |
| Roller | Grabs and releases |
| Climber | End of match |

## What to notice

**A shooter needs one number right.** Flywheel speed. Aim is rough.

**A pick-and-place needs three joints to agree.** Elevator height, shoulder angle
and wrist angle all have to be correct at the same moment, or the piece goes
somewhere you did not want.

That difference drives everything about how the code is organised. It is why
Stage 1B spends a lesson each on the elevator and the arm.

Also notice the naming. 8033 writes `ElevatorIOReal`. 6328 writes
`FlywheelsIOKrakenFOC`. Both are fine. Two good teams disagree about this.

## Done

You can name the mechanisms and you found `ElevatorIO.java`.

```bash
./tools/frcprog next
```
