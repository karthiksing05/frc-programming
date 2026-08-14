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

## Hints

Four hints, escalating. The answer is only in the last one — open them in order
and stop as soon as you can carry on alone.

??? question "Open the hints"

    --8<-- "curriculum/lessons/0b-meet-presto/hints.md"

---

[:material-arrow-left: 0A · First-run install](../../stage0/0a-first-run-install/index.md){ .md-button } [0C · Meet Kelpie :material-arrow-right:](../../stage0/0c-meet-kelpie/index.md){ .md-button .md-button--primary }
