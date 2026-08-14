---
title: "0B — Meet Presto"
---

!!! info "Guided lesson — no automatic grader"

    There is a clear goal and working code to model yourself on, but no
    rubric. Your check is the simulator and AdvantageScope.

--8<-- "curriculum/lessons/0b-meet-presto/README.md"

---

## Play with the shooter

Before you read the code, get a feel for the machine it drives. The two things that
decide where a Note lands are the pivot angle and the flywheel speed — drag them.

<div class="robot3d" data-robot="presto"></div>

The limits and presets are the same numbers as `Constants.Flywheels` in your project,
so this is the mechanism you will be commanding in lesson 14. Notice that what throws
the ring is the *surface speed* of the wheel, not the RPM — which is why the code
holds a speed rather than a motor output.

There is a photo of the real robot on the
[Presto tour page](../../../robots/presto-crescendo-tour.md).

---

## Watch it actually play

Before the code, see what it produces. Three real matches from 6328's 2024 season:

- **[Hopper Division final — 2024 World Championship](https://www.youtube.com/watch?v=xvO35NYpKGk)** — their alliance won this division. Watch the shooter spin up *before* the robot arrives,
  and the pivot angle change between shots from different distances.
- **[The same final from a driver station](https://www.youtube.com/watch?v=d6r9zXimfj0)** — filmed from an opposing team's driver station. Notice how little the drivers can see.
- **[Einstein Field playoff](https://www.youtube.com/watch?v=QubuOGQ3xGo)** — watch the first fifteen seconds, where nobody may touch the controls. They won an
  Autonomous Award that season.

More on the [Presto tour page](../../../robots/presto-crescendo-tour.md), and every
match on [The Blue Alliance](https://www.thebluealliance.com/team/6328/2024).

---

## Hints

Four hints, escalating. The answer is only in the last one — open them in order
and stop as soon as you can carry on alone.

??? question "Open the hints"

    --8<-- "curriculum/lessons/0b-meet-presto/hints.md"

---

[:material-arrow-left: 0A · First-run install](../../stage0/0a-first-run-install/index.md){ .md-button } [0C · Meet Kelpie :material-arrow-right:](../../stage0/0c-meet-kelpie/index.md){ .md-button .md-button--primary }
