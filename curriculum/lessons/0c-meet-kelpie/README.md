# Lesson 0C — Meet Kelpie

**Stage 0 · 15 min · Needs: 0A**

Kelpie solves a completely different problem from Presto, and the difference shows up
everywhere in its code. Where Presto throws things, Kelpie places them — carefully, at
a specific height, without dropping them.

## Do this

1. Read about the game and the robot below.
2. Open <https://github.com/HighlanderRobotics/Reefscape>
3. Find `src/main/java/frc/robot/subsystems/elevator/`
4. Open `ElevatorIOSim.java`. You control this exact mechanism in lesson 05.

## The game it was built for

The 2025 game was called **Reefscape**, and it had two game pieces. **Coral** were
lengths of white PVC pipe; **Algae** were large rubber balls. The main scoring
structure was the **Reef**, a set of branches at four different heights, and robots
scored by hanging Coral onto those branches. Higher branches were worth more points and
were much harder to reach accurately.

Algae had to be pulled off the Reef and delivered elsewhere — into a **Processor** at
floor level, or thrown into a **Barge** high above the field. In the endgame robots
climbed a **Cage** hanging over the field.

The important thing about this game, for a programmer, is that it rewarded *precision*
rather than power. Missing a shot in Crescendo costs you one Note. Missing a placement
in Reefscape means the Coral falls on the floor and you have to go and get another one.

## The robot

Kelpie was built by Team 8033 for that game. Its mechanisms are a chain of joints that
all have to agree with each other.

The **swerve drive** works on the same principle as Presto's. The **elevator** lifts the
whole end of the robot up and down, which is how it reaches the four different Reef
heights. The **shoulder** pivots that assembly forward and back, and the **wrist**
rotates the gripper to match the angle of the branch. The **roller** grips a Coral and
releases it. The **climber** takes the robot up the Cage at the end.

You will build simplified versions of the elevator in lesson 05 and the shoulder in
lesson 06.

## What to notice

A shooter essentially needs one number to be right — the flywheel speed — and its aim
can be fairly rough, because a ring in flight is forgiving. A pick-and-place robot
needs three joints to agree at the same instant: the elevator height, the shoulder
angle and the wrist angle all have to be correct together, or the game piece ends up
somewhere you did not want it.

That difference drives almost everything about how the two codebases are organised, and
it is the reason Stage 1B spends a whole lesson each on the elevator and the arm rather
than covering "position control" once.

The naming is worth noticing too. Team 8033 writes `ElevatorIOReal` while team 6328
writes `FlywheelsIOKrakenFOC` — one names the file after the fact that it is real
hardware, the other after which motor it is for. Both conventions work, and two
strong teams simply disagree. When you read a new codebase, working out which
convention it follows is usually the fastest way in.

## Done

You can name the mechanisms, you know what game they were built for, and you have found
`ElevatorIO.java`.

```bash
./tools/frcprog next
```
