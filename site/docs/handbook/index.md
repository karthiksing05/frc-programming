# Programming Handbook

*The non-sequential reference wiki for FRC programming. Look things up; jump in anywhere.*

The Programming Handbook is the topic-organized companion to the [Learning Course](../learn/). Where the course walks you through 30 lessons in a fixed order — each lesson solving a pain the previous one created — the handbook is **alphabetical by concept**, no prerequisites assumed, no narrative arc. You're meant to land here from a search bar, a Google result, or an internal link from a lesson.

The model is borrowed directly from [FRCDesign.org's Design Handbook](https://frcdesign.org): a wiki of self-contained concept pages, each grounded in real production code from our two reference robots ([Kelpie](../robots/kelpie-reefscape-tour.md) and [Presto](../robots/presto-crescendo-tour.md)).

!!! warning "Status — Phase 0"

    These pages are **stubs**. Real authoring is Phase 2 territory per the project's [Implementation Plan §9](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md). Each page below currently carries a "Coming in Phase 2" marker and a pointer to the lesson that already covers the same ground.

    **Feel free to contribute!** Handbook pages are the easiest entry point for new authors — they don't have the prereq-graph constraints the lessons do. See [Contributing](../contributing.md).

## Topic index

<div class="grid cards" markdown>

-   :material-language-java:{ .lg .middle } __Java basics for FRC__

    ---

    The subset of Java the curriculum assumes — methods, classes, lambdas, Suppliers — explained in robot terms.

    [:octicons-arrow-right-24: Java basics](java-basics.md)

-   :material-puzzle:{ .lg .middle } __The Command-Based framework__

    ---

    Subsystems, commands, triggers, the scheduler, the requirement system. Why factories beat subclasses.

    [:octicons-arrow-right-24: Command-Based](command-based.md)

-   :material-layers-triple:{ .lg .middle } __The IO Layer pattern (AdvantageKit)__

    ---

    `XxxIO` interfaces, `@AutoLog` input structs, `XxxIOSim` vs `XxxIOReal`, log replay.

    [:octicons-arrow-right-24: IO Layer](io-layer.md)

-   :material-chart-bell-curve-cumulative:{ .lg .middle } __PID & feedforward__

    ---

    Reaching a setpoint, holding against gravity, why feedforward is half of every good loop.

    [:octicons-arrow-right-24: PID & feedforward](pid-and-feedforward.md)

-   :material-map-marker-path:{ .lg .middle } __Trajectories & path-following__

    ---

    PathPlanner, Choreo, holonomic vs differential, when to hand-author vs generate.

    [:octicons-arrow-right-24: Trajectories](trajectories.md)

-   :material-camera-iris:{ .lg .middle } __Vision (PhotonVision, AprilTags)__

    ---

    Camera calibration, single-tag pose, multi-tag fusion, integrating with odometry.

    [:octicons-arrow-right-24: Vision](vision.md)

-   :material-chart-line:{ .lg .middle } __Logging & telemetry (AdvantageScope)__

    ---

    `Logger.recordOutput`, NT4, the WPILOG format, layouts, mechanism2d, 3D field views.

    [:octicons-arrow-right-24: Logging](logging.md)

</div>

## How the handbook relates to the course

| If you want to… | Go here |
|---|---|
| Learn from scratch, in order | [Learning Course](../learn/) |
| Look up *one* concept while building something | This handbook |
| Read a real robot's code with annotations | [Robots](../robots/) |
| Run an interactive widget | [Examples](../examples/) |

The course teaches *why* and *when*; the handbook teaches *what* and *how*. Both cite the same reference robots, so you can cross-link freely.
