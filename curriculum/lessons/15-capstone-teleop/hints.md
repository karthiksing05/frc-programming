# Hints — Lesson 15

## Hint 1 — Where to start

If `frcprog check --all` shows failures, work in lesson order. Earlier failures
often cause later ones, and fixing the first one frequently clears three.

If everything before 15 is green and only the capstone fails, the problem is an
interaction between subsystems rather than a bug in any of them. Read the scenario
name in the failure — it tells you which combination broke.

## Hint 2 — The shape of the answer

There is no new code in this lesson. Every scenario is testing something an earlier
lesson built.

| Scenario fails | Look at |
|---|---|
| 1. Drive | Lesson 07 suppliers, lesson 14's `DriverBindings` |
| 2. Intake | Lesson 04's `periodic` — is the beam-break check inside the subsystem? |
| 3. Score | Lesson 09's composition — timeout and `finallyDo` |
| 4. Autonomous | Lesson 12 — is a routine in the chooser? |
| 5. Undefined state | Default commands (11) and `startEnd` cleanup (08) |

## Hint 3 — Scenario 5 specifically

"No subsystem is ever left in an undefined state" fails when something keeps
commanding a mechanism after the human stopped asking. Three usual causes:

- A subsystem with no default command, so "nothing scheduled" means "whatever it was
  last told".
- A command built with `run(...)` where `startEnd(...)` was wanted — no cleanup
  lambda, so releasing the button stops the command without undoing what it did.
- A composition with no timeout, so it is still running long after it should have
  given up.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

There is no single answer to reveal — the capstone has no new TODO. What it grades is
the accumulated state of everything you have written.

If you want to compare your project against a complete reference:

```bash
./tools/frcprog solution 15-capstone-teleop
```

That overwrites your work with the reference version of every file. **Read the diff
rather than the result** — the useful thing is seeing where your choices differed
from the reference's, and most differences will be legitimate style rather than
bugs.

Afterwards:

```bash
./tools/frcprog reset 15-capstone-teleop
```

restores the starter code so you can re-solve it.

A more useful exercise, if the capstone is passing: extend it. Add a second scoring
sequence for the low target. Add a trigger that stows the elevator automatically when
the drivetrain moves fast. Add telemetry for a value you found yourself wanting.
Nothing grades any of that, which is exactly why it is worth doing.

</details>
