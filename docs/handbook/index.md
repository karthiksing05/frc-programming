# Programming Handbook

*The non-sequential reference wiki for FRC programming. Look things up; jump in anywhere.*

The Programming Handbook is the topic-organized companion to the [Learning Course](../learn/index.md). Where the course walks you through thirty-four lessons in a fixed order — each lesson solving a pain the previous one created — the handbook is **alphabetical by concept**, no prerequisites assumed, no narrative arc. You're meant to land here from a search bar, a Google result, or an internal link from a lesson.

The model is borrowed directly from [FRCDesign.org's Design Handbook](https://frcdesign.org): a wiki of self-contained concept pages, each grounded in real production code from our two reference robots ([Kelpie](../robots/kelpie-reefscape-tour.md) and [Presto](../robots/presto-crescendo-tour.md)).

!!! success "Written and complete"

    **[Java basics for FRC](java-basics.md)** — the subset of Java robot code actually
    uses, in robot terms. Types, methods, classes, `private final`, enums, lambdas, and
    the `Supplier` bug that silently stops a robot from moving. Start here if you have
    not programmed before.

    **[Git](git.md)** — the full version of what lesson 0D deliberately defers.
    The loop, reading history, undoing things, branches, working with a team, and a
    recovery section for when you are convinced you have broken everything. No
    GitHub account required for any of it.

!!! warning "The rest of these pages are stubs"

    The handbook is deliberately the last thing built. The lessons are the curriculum;
    the handbook is where a concept goes once enough lessons touch it that it deserves
    one page instead of four partial explanations.

    Each page below currently points at the lesson that already covers the same ground.
    Going there is not a consolation prize — right now it is the better explanation.

    Handbook pages are the easiest entry point for a new author: no prerequisite graph
    to respect and no rubric to write. See [Contributing](../contributing.md).

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
| Learn from scratch, in order | [Learning Course](../learn/index.md) |
| Look up *one* concept while building something | This handbook |
| Read a real robot's code with annotations | [Robots](../robots/index.md) |
| Run an interactive widget | [Examples](../code-examples/index.md) |

The course teaches *why* and *when*; the handbook teaches *what* and *how*. Both cite the same reference robots, so you can cross-link freely.
