# Hints — Lesson 26

## Hint 1 — Where to start

Get the drivetrain registered with the arena and driving before adding any game pieces. Collisions with walls are the simplest thing to verify.

## Hint 2 — The shape of the answer

Then add one game piece and drive into it. One piece, one intake, one confirmation.

## Hint 3 — What usually goes wrong

Registering the drivetrain twice, or leaving the old `ModuleIOSim` also running, so two models fight over the same state.

A robot mass or moment of inertia that is far from reality, producing collisions that look wrong in a way that is hard to name.

Expecting your existing autos to still work. They will need re-tuning, and that is the lesson.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

[maple-sim documentation](https://shenzhen-robotics-alliance.github.io/maple-sim/) and Kelpie's `ModuleIOMapleSim.java`, which is a complete working integration alongside a plain one you can diff against.

</details>
