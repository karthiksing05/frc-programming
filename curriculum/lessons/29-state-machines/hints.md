# Hints — Lesson 29

## Hint 1 — Where to start

Write the enum and `isValidTransition` first, with no hardware at all. It is a pure function and you can unit-test it in a couple of minutes.

## Hint 2 — The shape of the answer

Then connect it: a command that requests transitions based on sensors and operator input, and a `periodic()` that drives motors according to the current state — exactly the shape of lesson 04's roller, with more states and guards.

## Hint 3 — What usually goes wrong

A state machine with no guards, which is an enum with extra steps.

Requesting transitions from several places, so nothing knows the current state's provenance. Funnel every request through one method.

Using a state machine for something that is not modal — most subsystems are not, and the ceremony costs more than it returns.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

254's public code is the canonical example of state-machine-first robot programming, and Presto's `superstructure/climber/` is a more moderate one. The [Chief Delphi discussion on standardised state-based control](https://www.chiefdelphi.com/t/standardized-state-based-robot-control-vendor-dep/415582) is worth reading for the arguments on both sides.

</details>
