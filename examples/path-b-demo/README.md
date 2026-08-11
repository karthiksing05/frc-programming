# Path B Demo — FRCProgramming.org in real WPILib

This is a **skeletal, illustrative** WPILib + AdvantageKit project that
shows what FRCProgramming.org's Path B (Git-clone-in-VS-Code) looks like
in practice. The browser PoCs (`../functions-poc/`, `../tank-drive-poc/`,
`../elevator-pid-poc/`) demonstrate the same two lessons in a different
form.

> This is a **realistic, buildable skeleton** of the Path B workflow.
> The vendordeps in `vendordeps/` are the real files pulled from
> AdvantageKit, WPILib, and maple-sim. The Gradle wrapper scripts and
> jar come from FRC 6328's 2025 codebase (Gradle 8.11, matching
> GradleRIO v2026.x). Once WPILib 2026 is installed,
> `./gradlew build` should actually work — no manual jar fetch needed.

## What this demonstrates

1. **Lessons as curriculum metadata, code as one growing project.**
   `lessons/01-methods/` and `lessons/02-tank-drive/` contain only
   prose + a `lesson.json` manifest. The actual Java grows in
   `src/main/java/frc/robot/` lesson by lesson — exactly mirroring the
   "compounding filesystem" idea from the browser PoCs.
2. **AdvantageKit IO Layer pattern.** Every subsystem has `XxxIO`
   (interface with `@AutoLog` inputs class) + `XxxIOSim` (physics) +
   `XxxIOReal` (real hardware stub). See [Drive.java](src/main/java/frc/robot/subsystems/drive/Drive.java)
   and siblings.
3. **AdvantageKit + AdvantageScope wiring.** [Robot.java](src/main/java/frc/robot/Robot.java)
   is a `LoggedRobot` that publishes to NT4 in SIM mode (so AdvantageScope
   on localhost just works) and writes WPILOG files for replay grading.
4. **Lesson grading via `Logger.recordOutput` + JUnit.** Each lesson
   ships JUnit 5 tests in `src/test/java/...` plus a "rubric" boolean
   (`Lesson/Pass`) emitted to the log. CI runs `./gradlew check`; a
   future grader can re-run the AdvantageKit replay to verify the
   solution end-to-end.
5. **A tiny `frcprog` CLI** ([tools/frcprog.sh](tools/frcprog.sh)) that
   lists lessons, opens the current one in VS Code, runs the right
   tests, and pretty-prints results — the seed of a future VS Code
   extension.

## Layout

```
path-b-demo/
├── README.md                            ← you are here
├── settings.gradle / build.gradle       ← single GradleRIO project (NOT multi-module)
├── gradle.properties / .gitignore
├── vendordeps/
│   ├── AdvantageKit.json                ← real (v26.0.2, frcYear 2026)
│   ├── WPILibNewCommands.json           ← real (frcYear 2026 branch)
│   └── maple-sim.json                   ← real (v0.4.0-beta-obstacles-fix; used in lesson 26)
├── gradle/wrapper/
│   ├── gradle-wrapper.properties        ← Gradle 8.11 (matches GradleRIO 2026.2.1)
│   └── gradle-wrapper.jar                ← real jar from FRC 6328 2025 codebase
├── gradlew, gradlew.bat                 ← from GradleRIO v2026.2.1
├── .vscode/{settings.json,launch.json}  ← WPILib extension wiring
├── .wpilib/wpilib_preferences.json      ← projectYear=2026, language=java
├── .github/workflows/ci.yml             ← WPILib container + ./gradlew check
├── lessons/                             ← CURRICULUM (no code, just content + manifests)
│   ├── manifest.json                    ← ordered list of all lessons
│   ├── 01-methods/
│   │   ├── README.md                    ← instructions (Exercism-style)
│   │   ├── hints.md
│   │   └── lesson.json                  ← {edits, tests, prerequisites}
│   └── 02-tank-drive/
│       ├── README.md
│       ├── hints.md
│       └── lesson.json
├── .meta/                               ← author-only (reference solutions)
│   └── exemplar/
│       ├── 01-methods/MathUtils.java
│       └── 02-tank-drive/Drive.java
├── src/main/java/frc/robot/             ← ONE growing codebase
│   ├── Main.java
│   ├── Robot.java                       ← LoggedRobot, AdvantageKit setup
│   ├── Constants.java
│   ├── RobotContainer.java
│   ├── util/MathUtils.java              ← LESSON 01 edits this
│   └── subsystems/drive/
│       ├── Drive.java                   ← LESSON 02 edits this
│       ├── DriveIO.java                 ← interface + @AutoLog inputs
│       ├── DriveIOSim.java              ← DifferentialDrivetrainSim
│       └── DriveIOReal.java             ← TalonFX stub
├── src/test/java/frc/robot/
│   ├── util/MathUtilsTest.java          ← JUnit 5, @Tag("lesson-01")
│   └── subsystems/drive/DriveTest.java  ← JUnit 5, @Tag("lesson-02"), HALSim
└── tools/
    └── frcprog.sh                       ← CLI seed for the future VS Code extension
```

## Student workflow

```bash
# 1. Once-per-season: clone your project repo (a template stamped from this)
git clone https://github.com/<your-username>/my-frc-learning.git
cd my-frc-learning

# 2. Open in WPILib's VS Code (the bundled one — NOT system VS Code)
wpilib code .

# 3. See what to do next
./tools/frcprog.sh next
#   → "Lesson 01: Methods (Functions). Edit src/main/java/frc/robot/util/MathUtils.java"

# 4. Read the lesson
./tools/frcprog.sh read 01-methods
#   (or: open lessons/01-methods/README.md in VS Code)

# 5. Edit code in VS Code with autocomplete, linting, etc.

# 6. Run the lesson's tests
./tools/frcprog.sh check 01-methods
#   → "./gradlew test --tests '*Lesson01*'"
#   → Lesson 01 ✓ (3 / 3 assertions passed)

# 7. See your robot move (optional, satisfying)
./gradlew simulateJava                  # opens HALSim GUI
# Then open AdvantageScope, connect to NT4 localhost — your subsystem's
# inputs/outputs stream live.

# 8. Commit + push. CI runs the same tests, posts a sticky PR comment.
git add . && git commit -m "Complete lesson 01" && git push
```

## Why this structure?

The pattern came from research summarized in [Infrastructure-Analysis.md §3](../../process/Infrastructure-Analysis.md):
- One growing `src/` (not Gradle subprojects) so WPILib's `Simulate Robot
  Code` command works without modification — multi-project setups break
  it per [wpilibsuite/vscode-wpilib#847](https://github.com/wpilibsuite/vscode-wpilib/issues/847).
- Curriculum content + code separation borrowed from
  [Exercism's track layout](https://exercism.org/docs/building/tracks/concept-exercises).
- IO Layer pattern from AdvantageKit, used by every serious team that
  cares about replay (6328, 254, 8033). See
  [docs.advantagekit.org/data-flow/recording-inputs/io-interfaces](https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/).
- Test discovery via `@Tag("lesson-NN")` + JUnit 5 so a single Gradle
  invocation can pick out exactly one lesson's rubric.

## What's missing for this to be real

The plumbing — vendordeps, Gradle wrapper, WPILib extension files — is
all in place. What's left is curriculum + tooling work, not skeleton
work:

- **WPILib 2026 install** on the student's machine (~2.5 GB; ships JDK
  17, GradleRIO, AdvantageScope, sim libs).
- **A `tasks.json`** if we want one-click "Run lesson tests" from the
  VS Code task picker. The frcprog CLI covers this from the terminal
  already.
- **The VS Code extension** on top of the CLI (its own ~2-week project).
- **A `frcprog doctor`** command that checks JDK/Gradle/Network/install
  health (high-leverage onboarding step per the research).
- **An AdvantageKit-replay-based CI grader** that replays a student's
  `.wpilog` against a reference solution (no team has built this yet
  — we'd be first).
- **Lessons 03+** — the framework supports an unbounded ordered list
  via `lessons/manifest.json`; lessons 1 and 2 are written.

## Pinned versions

| Component             | Version                              | Source                                                                                       |
| --------------------- | ------------------------------------ | -------------------------------------------------------------------------------------------- |
| WPILib                | 2026.2.1                             | `gradle.properties` / `build.gradle`                                                         |
| GradleRIO             | 2026.2.1                             | `build.gradle`                                                                               |
| Gradle wrapper        | 8.11                                 | `gradle/wrapper/gradle-wrapper.properties` (matches GradleRIO v2026.2.1)                     |
| Java                  | 17                                   | bundled with WPILib; `build.gradle` `sourceCompatibility`                                    |
| AdvantageKit          | 26.0.2                               | `vendordeps/AdvantageKit.json`                                                               |
| WPILibNewCommands     | bundled with WPILib 2026             | `vendordeps/WPILibNewCommands.json` (frcYear 2026 branch of allwpilib)                       |
| maple-sim             | 0.4.0-beta-obstacles-fix             | `vendordeps/maple-sim.json` (used by lesson 26 — physics-accurate swerve sim)                |
| JUnit Jupiter         | 5.10.2                               | `build.gradle`                                                                               |
| Spotless              | 6.25.0                               | `build.gradle`                                                                               |

See [Infrastructure-Analysis.md §3](../../process/Infrastructure-Analysis.md) for
the full picture.
