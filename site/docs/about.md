# About FRCProgramming.org

*Like FRCDesign.org, but for code.*

## The mission

FRCProgramming.org exists to teach FRC robot programming the way competitive teams actually write it — Java, WPILib, AdvantageKit, AdvantageScope, Git, Gradle, and the IO Layer pattern that 6328, 254, and 8033 all converge on. Every lesson is grounded in real production code from real championship robots, and every concept is introduced as the answer to a pain the previous lesson made you feel. The site is open-source, self-paced, and free.

## The model

We borrowed the architecture wholesale from [FRCDesign.org](https://frcdesign.org), which does the same job for mechanical design. Three pillars:

- **A [Learning Course](learn/)** — 30 lessons, sequential, prerequisite-respecting
- **A [Programming Handbook](handbook/)** — the topic-organized reference wiki you land on from search
- **[Reference robots](robots/)** — two real championship robots whose code grounds every lesson

The two reference robots are **Kelpie** (Team 8033 Highlander Robotics, Reefscape 2025) and **Presto** (Team 6328 Mechanical Advantage, Crescendo 2024). Both ship the full AdvantageKit + IO Layer stack; both have public source; both are good enough at different things that one curriculum can lean on both without contradiction.

## The pedagogy

Three principles, each named in our [Curriculum-Flow.md](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Curriculum-Flow.md):

1. **Pain before abstraction** — borrowed from Katie Niwiden's "From 0 to Robot" deck. Students write the messy `teleopPeriodic` version *before* they learn subsystems, so the abstraction is felt as relief, not imposed as ceremony.
2. **Command-based factories, not subclasses** — aligned with Oblarg's 2025 best-practices post and the current WPILib official position. Factories are the default; subclasses are reserved for genuinely stateful commands.
3. **The IO Layer is Stage 2, not Stage 1** — 6328's pattern is taught after the student has felt the pain of "I can't test this without real hardware," not before.

## The two paths

The curriculum runs in two modes — and serious students do both:

- **In your browser** (Stages 0–1B) — embedded interactive widgets, zero install, works on a Chromebook
- **In VS Code + WPILib** (Stage 1C onward) — real toolchain, real `./gradlew simulateJava`, AdvantageScope, Git

A single "Graduate to VS Code" button at the end of Stage 1B exports the browser project as a real WPILib repo. The code you wrote in CodeMirror is the code you open in VS Code; the transition is one click, not a fresh start.

## Acknowledgments

This site stands on the work of people who didn't have to share theirs:

- **Team 6328 Mechanical Advantage** — for AdvantageKit, AdvantageScope, and the public RobotCode repos that anchor half the curriculum.
- **Team 8033 Highlander Robotics** — for Kelpie's code, the [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) repo, and the existence proof that a team-internal training pipeline can be open-source.
- **Team 254 The Cheesy Poofs** — for the institutional pattern that everyone else's command-based is implicitly compared against.
- **The WPILib team** — for the underlying framework and decade-plus of evolving docs.
- **Katie Niwiden** — whose ["From 0 to Robot: Teaching Programming to Beginners"](https://docs.google.com/presentation/d/15O2Xo5cHsYG3hVvQbMSB2SuvU9ED0Y3feaKdCbgaQyM/preview) deck is the pedagogical spine of this curriculum.
- **FRCDesign.org's maintainers** — for the architecture this site copies.

## Project status

**Phase 0** as of writing. The site spine, the three browser PoCs, the path-b-demo skeleton, and the design documents in [`process/`](https://github.com/karthiksing05/FRC-Programming/tree/main/process) are in place. Lesson content authoring is in progress; the Programming Handbook is stubbed pending Phase 2. The full 18-month plan is in [Implementation-Plan.md](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md).

If you want to help — and we'd love that — start at [Contributing](contributing.md).
