# Hints — Lesson 28

## Hint 1 — Where to start

Add the routine to one subsystem — the elevator is a good first choice because its motion is easy to reason about. Bind the four commands to four buttons.

## Hint 2 — The shape of the answer

Run each test in simulation, save the log, and open it in WPILib's SysId analysis tool. The tool wants position, velocity, and applied voltage; make sure you are logging all three.

## Hint 3 — What usually goes wrong

Running SysId on a mechanism with limited travel and hitting a hard stop mid-test, which corrupts the data. Add explicit limits to the routine.

Forgetting to log the applied voltage, so the tool has nothing to fit against.

Using simulation results as if they were real. In sim the physics model and the feedforward model are the same model, so SysId recovers exactly the constants that were fed in. It proves your wiring, not your robot.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[WPILib SysId documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html). The routine plumbing is boilerplate; copy it from the docs and spend your attention on reading the results.

</details>
