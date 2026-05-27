# Path B Demo — FRCProgramming.org in real WPILib

This is a **skeletal, illustrative** WPILib + AdvantageKit project that
shows what FRCProgramming.org's Path B (Git-clone-in-VS-Code) looks like
in practice. The browser PoCs (`../functions-poc/`, `../tank-drive-poc/`,
`../elevator-pid-poc/`) demonstrate the same two lessons in a different
form.

> ⚠ This is a **demonstration of structure**, not a fully-runnable
> project. Tests reference WPILib + AdvantageKit APIs and use real class
> names, but `gradlew build` won't succeed here without a full WPILib
> install (~2.5 GB) and AdvantageKit's actual vendordep JSON. The
> vendordep files here are stubs pointing at the real URLs.

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
│   ├── AdvantageKit.json                ← stub; real URL in the file
│   └── WPILibNewCommands.json           ← stub
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

- A real `vendordeps/AdvantageKit.json` (pull from
  [AdvantageKit's release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json))
- A real `gradlew` wrapper + `gradle/wrapper/` (from
  [GradleRIO](https://github.com/wpilibsuite/GradleRIO))
- A `.vscode/settings.json` + `tasks.json` for the WPILib extension
- The actual VS Code extension on top of the CLI (this is its own ~2-week project)
- A `frcprog doctor` command that checks JDK/Gradle/Network/install
  health (high-leverage onboarding step per the research)
- An AdvantageKit-replay-based CI grader (no team has built this yet —
  we'd be first)

See [Infrastructure-Analysis.md §3](../../process/Infrastructure-Analysis.md) for
the full picture.
