# Reference Robots

Throughout the curriculum, two reference robots anchor the lessons — one shooter, one pick-and-place. Every lesson under [Learn](../learn/) cites code from one of them. By Stage 1B you'll be familiar with both; by Stage 2A you'll be cross-referencing their source on a regular basis.

Click in for the full tour of each.

<div class="grid cards" markdown>

-   :material-bullseye-arrow:{ .lg .middle } __Presto — the shooter__

    ---

    **Team 6328 Mechanical Advantage · Crescendo 2024**

    The canonical AdvantageKit reference. Written by AdvantageKit's authors. MIT-licensed. Ships a 3D `.glb` model for AdvantageScope. Two flywheels, a pivoting shooter arm, and a trap climber.

    [:octicons-arrow-right-24: Tour Presto](presto-crescendo-tour.md)

-   :material-elevator-up:{ .lg .middle } __Kelpie — the pick-and-place__

    ---

    **Team 8033 Highlander Robotics · Reefscape 2025**

    The cleanest IO-Layer pedagogy in the wild. Only candidate with `maple-sim` (physics-accurate swerve simulation). Separate `elevator` / `shoulder` / `wrist` / `roller` subsystems map perfectly to single-lesson scope.

    [:octicons-arrow-right-24: Tour Kelpie](kelpie-reefscape-tour.md)

</div>

---

## Why two robots?

One shooter, one pick-and-place. Between them they cover every Stage 1–2 mechanism a student needs to see — swerve drivetrain, elevator, pivoting arm/shoulder, wrist, intake rollers, flywheels, climber. Recurring examples build familiarity; familiarity reduces cognitive load.

But it's not just about mechanism coverage. **Two robots from two different teams means two different code styles applied to the same architectural pattern.** Presto names its hardware files after the motor vendor (`FlywheelsIOKrakenFOC`, `FlywheelsIOSparkFlex`); Kelpie names them by their role (`ElevatorIOReal`, `ElevatorIOSim`). Presto bundles arm + climber + backpack into one `superstructure/`; Kelpie keeps shoulder, wrist, and roller as separate sibling packages. Both use AdvantageKit. Both use the IO Layer pattern. Both run in simulation. They just *style* it differently.

That variety, layered on top of consistency, is the point. You graduate from the curriculum knowing not just "the FRC way" but **two real teams' actual ways** — enough to recognize that your own future style is just one more variation, not a deviation from gospel.

The full rationale, alternates considered, and license caveats are in [`process/Reference-Robots.md`](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Reference-Robots.md).