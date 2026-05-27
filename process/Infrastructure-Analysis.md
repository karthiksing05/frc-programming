# Infrastructure Analysis — FRCProgramming.org

> **Purpose:** Map out what it actually takes to ship a curriculum site like FRCDesign.org but with **live, in-browser code execution**, then weigh that against an alternative path that uses FRC's official toolchain (WPILib + VS Code) as the substrate for lessons instead.

---

## 0. What we already have (status quo)

| Piece | Purpose |
|---|---|
| [FRCDesign-Analysis.md](FRCDesign-Analysis.md) | Teardown of FRCDesign.org's stack & pedagogy — the model we're cloning |
| [elevator-pid-poc/](../examples/elevator-pid-poc/) | **Path A PoC** — lesson on PID tuning. Student edits Java, regex pulls the gain values, JS sim runs |
| [functions-poc/](../examples/functions-poc/) | **Path A PoC** — lesson on methods. Student writes a function body. JS extracts the body, runs it as JS. First lesson to write the persistent filesystem. |
| [tank-drive-poc/](../examples/tank-drive-poc/) | **Path A PoC** — lesson on subsystem wiring. Imports the file the student wrote in Functions — proof that lessons compound. |
| [shared/filesystem.js](../examples/shared/filesystem.js) | Backing store + tree renderer for the cross-lesson "student project" |
| [path-b-demo/](../examples/path-b-demo/) | **Path B PoC** — a real WPILib + AdvantageKit + JUnit project structure showing the same two lessons in the official toolchain. Skeletal but architecturally honest. |

Open all browser PoCs from the same HTTP origin (`python3 -m http.server` at the repo root) and they share one persistent project. The `path-b-demo/` is read-as-architecture, not run-as-code — it doesn't ship a Gradle wrapper or real vendordep JSON (notes inside).

---

## 1. The fork in the road

There are two fundamentally different ways to build this. Pick one, or pick both and stitch them together — but understand the trade-offs first.

### Path A — **Browser-native** (what the PoCs do now)
The site IS the IDE. Student writes code in a CodeMirror editor, a JS sandbox runs it, an HTML canvas shows the robot. Zero install. Works on a $200 Chromebook in the school library.

### Path B — **VS Code + WPILib** (the official-tools alternative)
The site is a curriculum spine; the IDE is the real WPILib install. Each lesson is a Git repo students `clone`, fill in, and run against a desktop sim. The site reduces to lesson prose + maybe a VS Code extension that drives the experience.

Neither is wrong. They just optimize for different constraints. The rest of this doc unpacks both.

---

## 2. Path A — Browser-native deep dive

### 2.1 Hosting & build

Same as FRCDesign.org. Nothing fancy:

```
Markdown (MkDocs)  ──→  Material theme  ──→  Static HTML  ──→  GitHub Pages
                                                              or Cloudflare Pages
                                                              or Vercel
```

Cost: **$0** until a CDN bill matters (which is essentially never for a docs site).

The interactive lessons are **embedded apps**. Two options for how:

- **A1 · iframe embeds.** Each lesson app (like the current PoCs) is a standalone HTML page. The MkDocs page is mostly prose, then `<iframe src="../lessons/methods/index.html">` for the interactive widget.
  - **Pro:** total isolation; widget can use any framework; no risk of style/JS bleed into the MkDocs page.
  - **Con:** filesystem sharing is harder (need `postMessage` between iframe and parent, or accept that iframe has its own localStorage scoped to its src URL).
- **A2 · Inline custom shortcode.** Write a small MkDocs plugin/macro that renders `{{ exercise("methods/deadband") }}` into a `<div id="..."></div>`, and ship one bundled JS file that mounts the right widget by ID.
  - **Pro:** one origin → trivial localStorage sharing; widgets can pull theme colors from the parent page; one CodeMirror instance can be reused.
  - **Con:** more upfront engineering. Style isolation has to be enforced by hand.

**Recommendation:** A2 once you have ≥3 lessons. Use A1 for prototyping (it's what the current PoCs are).

### 2.2 Persistent student project — three layers, escalating

The PoCs use `localStorage`. That's the right starting point, but here's the full ladder:

| Layer | Storage | When you need it | What you lose by not having it |
|---|---|---|---|
| **L1: localStorage** | ~5 MB per origin, per browser | Day 1 | Nothing for v1 |
| **L2: IndexedDB** | ~50+ MB, structured | When projects grow past tens of files (whole robot codebase) | Larger projects, faster reads |
| **L3: GitHub OAuth + Gist/repo sync** | Real account | When students want to share work, get reviewed, work from multiple devices | Educator review, portability, "I lost my work when I cleared cookies" |
| **L4: Backend (Postgres + auth)** | Server | When you want analytics, leaderboards, mentor dashboards, automated grading | All of the above as a managed service |

L1→L2 is purely a code swap (Dexie.js wraps IDB with a localStorage-like API). L2→L3 is the big jump: now you need OAuth, a small backend or Cloudflare Worker to hold the GitHub token, and a sync UI ("Save to GitHub" button).

**Important constraint surfaced by the PoCs:** `file://` partitions localStorage *per file*. The cross-lesson demo only works if the lessons share an origin. This means: **never tell students to "open the HTML in their browser"** — always ship via an HTTP server. For local dev, document `python3 -m http.server`. For prod, this is automatic.

### 2.3 The hard problem: running Java in a browser

The PoCs cheat. The PID PoC regex-extracts six numbers. The Functions PoC extracts a single method body and runs it as JS (which works because deadband math is JS-compatible). The Tank Drive PoC stretches this with a tiny `javaBodyToJs` transformer.

This will hit a wall the first time a lesson needs:
- Two methods that call each other
- A new class (not just a body edit)
- A `switch` statement, `enum`, generics, or anything Java-specific
- Real WPILib API surface (`CommandScheduler`, `Trigger`, `Subsystem` lifecycle)

Four serious options, ordered by ambition:

| Option | What it is | Effort | Ceiling |
|---|---|---|---|
| **Extend the regex/AST approach** | Use a real Java parser like [java-parser](https://www.npmjs.com/package/java-parser) (npm), walk the AST, only translate well-known patterns | Low | Hits a wall ~Stage 1D |
| **Doppio / DoppioJVM** | A JVM in JavaScript. Old, slow, but real. | Medium | Full Java SE subset, but startup is ~5s and runtime is slow |
| **CheerpJ** | Commercial JVM-in-browser, very fast (Leaning Tech). Free tier exists. | Low to integrate, ongoing licensing risk | Real Java, but external dep |
| **TeaVM** | Java → JS/Wasm compiler, used in some games. Compile WPILib subset ahead of time. | High | Real-ish Java for whatever you bother to compile |

**My read:** Start with regex+AST (already proven by PoCs). When that breaks, evaluate CheerpJ vs. TeaVM seriously — they both work but pull a heavy dependency. Doppio is probably a dead end on perf.

A pragmatic middle ground: design lesson exercises to be **"fill in this method body"** or **"set these constants"**. That alone covers an enormous amount of pedagogy and keeps you in regex/AST territory through most of Stage 1 and 2. The outline you wrote already leans this way naturally.

### 2.4 Simulation engine

Each PoC currently has its own custom physics (~100-300 lines of JS). That doesn't scale to 30 lessons. Options:

- **Custom physics per lesson.** What the PoCs do. Fine for distinct, simple mechanisms (elevator, joystick, tank). Painful for swerve and arms-with-springs.
- **One shared physics core**, with per-lesson "mechanism" plug-ins. Borrow from WPILib's own `EncoderSim`, `DCMotorSim`, `ElevatorSim`, `SingleJointedArmSim` — these are well-understood and porting them to JS is a one-time job.
- **Port WPILib's sim.** WPILib already has a Java simulation framework. If you go the TeaVM route in §2.3, you can compile WPILib's `simulation` package and use the real one.
- **Use Box2D / Planck.js for top-down kinematics** (swerve, drivetrains). Use bespoke 1D sims for elevators/arms.

**Recommendation:** port WPILib's `*Sim` classes to JS as a shared library — they're battle-tested and using them in lessons means the *physics model the student learns* matches the real one. Pair that with Planck.js for drivetrains.

### 2.5 What it costs to operate

| Thing | Cost |
|---|---|
| Hosting (GitHub Pages / Cloudflare Pages) | $0 |
| Domain (`frcprogramming.org`) | ~$15/yr |
| Material for MkDocs Insiders (optional) | $0 for OSS, $15/mo otherwise |
| Backend (when you need L3+ storage) | ~$5–20/mo for a tiny instance, $0 on Cloudflare Workers free tier |
| Maintainer time | The actual cost |

The dollar cost is rounding error. The cost is **content authoring** and **keeping interactive lessons working** as you add complexity.

### 2.6 Path A — summary

| | |
|---|---|
| Strengths | Zero install. Chromebook-friendly. Mobile-viewable. Fast iteration loop on lessons. PoCs already prove core UX works. |
| Risks | Java-in-browser ceiling. Sim engine maintenance. localStorage scoping confused students who open files directly. |
| Sweet spot | Stages 1A–2D (everything up to & including "build a complete teleop robot") |

---

## 3. Path B — VS Code + WPILib + Git (the deep exploration)

The premise: the official FRC tools are already designed for FRC. Don't build a parallel toolchain — build **content** that runs in the toolchain students will use on a real team. Every lesson doubles as Git, Gradle, JUnit, and WPILib-sim practice. The website becomes a curriculum spine; the IDE is the real WPILib install.

**Skim [path-b-demo/](../examples/path-b-demo/) alongside this section.** Each subsection below references concrete files there — the demo is the architecture rendered as code, this section is the architecture explained as prose.

### 3.1 The student experience, concretely

```bash
# Once per season — clone your project (stamped from a GitHub template)
git clone https://github.com/<you>/my-frc-learning.git
cd my-frc-learning

./tools/frcprog.sh doctor      # check JDK/Gradle/Network/WPILib install
./tools/frcprog.sh next        # → "Lesson 01: edit src/main/java/frc/robot/util/MathUtils.java"

# Open in WPILib's bundled VS Code (NOT system VS Code — the bundle ships its own JDK)
wpilib code .

# Read the lesson (also lives in lessons/01-methods/README.md)
./tools/frcprog.sh read 01-methods

# Edit. Hit save. Then…
./tools/frcprog.sh check 01-methods    # → ./gradlew test --tests '*Test' -DincludeTags='lesson-01'
# Lesson 01 ✓ (6 / 6 assertions passed)

./gradlew simulateJava                  # opens HALSim GUI; AdvantageScope at localhost picks it up

git add . && git commit -m "Complete lesson 01" && git push
# CI re-runs the same tests; PR gets a sticky "Lesson 01 ✓" comment
```

The website's lesson page is now: prose + diagrams + a `git clone` button + an embedded video of the sim running. The hard part lives in the local toolchain — and that's the point.

### 3.2 Repo shape: single growing project, not Gradle subprojects

The most important architectural decision in Path B turned out the opposite way my original instinct suggested:

| Option | Verdict |
|---|---|
| One repo per lesson (separate `git clone` each time) | ✗ Loses the "compounding" feel; admin overhead |
| One repo, one branch per lesson | △ Works, but `git checkout` between lessons is confusing for new students |
| **One repo, one Gradle project, one `src/` tree** | ✓ **Winner.** Code grows lesson-by-lesson. WPILib's "Simulate Robot Code" button still works. |
| One repo, Gradle multi-project (`lessons:01-methods` subproject etc.) | ✗ **Breaks WPILib VS Code commands** per [wpilibsuite/vscode-wpilib#847](https://github.com/wpilibsuite/vscode-wpilib/issues/847). |

The compounding mental model the browser PoCs prove out (each lesson adds files to a shared filesystem) maps directly onto **one growing `src/main/java/frc/robot/`**. Lessons are differentiated not by directory but by:
- A **lesson manifest** in `lessons/<slug>/lesson.json` declaring which files this lesson edits and which tests are its rubric.
- A **JUnit tag** like `@Tag("lesson-01")` on the lesson's tests so `./gradlew lesson01` runs only that rubric.
- A **TODO-marked starter** in the source file at the start of the lesson (`// TODO (LESSON 02): ...`).

See [path-b-demo/build.gradle](../examples/path-b-demo/build.gradle) for the per-lesson Gradle tasks and [path-b-demo/lessons/manifest.json](../examples/path-b-demo/lessons/manifest.json) for the manifest.

### 3.3 Lesson manifest format (Exercism-influenced)

Lesson content + author metadata live in `lessons/<slug>/`, with the same conceptual split [Exercism uses](https://exercism.org/docs/building/tracks/concept-exercises):

```
lessons/01-methods/
├── README.md          # student-visible: prose, what to do, how to run, why it matters
├── hints.md           # progressive hints, answer hidden inside a <details>
└── lesson.json        # machine-readable: {edits, tests, prerequisites, rubricOutputs}

.meta/exemplar/01-methods/
└── MathUtils.java     # AUTHOR-ONLY reference solution
```

[path-b-demo/lessons/01-methods/lesson.json](../examples/path-b-demo/lessons/01-methods/lesson.json) shows the schema. Critical fields:

- `edits` — the file(s) the student should be editing this lesson. The `frcprog` CLI uses this to open the right file in VS Code; the future extension uses it for "Reset Lesson" (`git checkout HEAD -- <edits>`).
- `tests` — fully-qualified JUnit test class names. Used by the rubric runner and (later) by the grading bot to summarize results.
- `rubricOutputs` — AdvantageKit `Logger.recordOutput(...)` keys whose value must stay `true` for the lesson to pass when the sim runs. This is the seed of replay-based grading (§3.8).
- `prerequisites` — slugs of lessons that must be complete first. The `frcprog next` CLI walks this DAG.

### 3.4 The AdvantageKit IO Layer pattern as the teaching substrate

This is the single biggest reason Path B is worth doing.

[AdvantageKit](https://docs.advantagekit.org/) (FRC 6328 / Mechanical Advantage) introduced a pattern that **every serious competitive team in 2025-2026 uses**: separate each subsystem into three layers, with the hardware boundary defined by an interface. See it in [path-b-demo/src/main/java/frc/robot/subsystems/drive/](../examples/path-b-demo/src/main/java/frc/robot/subsystems/drive/):

```
subsystems/drive/
├── Drive.java          extends SubsystemBase; holds DriveIO; the only thing students edit
├── DriveIO.java        interface + @AutoLog DriveIOInputs (all sensor readings)
├── DriveIOSim.java     implements DriveIO using DifferentialDrivetrainSim
└── DriveIOReal.java    implements DriveIO using real TalonFX motors (stub here)
```

Every cycle, `Drive.periodic()` does exactly this:

```java
io.updateInputs(inputs);                    // sensors → inputs struct (interface call)
Logger.processInputs("Drive", inputs);      // inputs are logged (or replayed)
// ...read from inputs.leftPositionMeters, never from a motor controller directly...
io.setVoltage(leftVolts, rightVolts);       // commands → interface call
```

The `@AutoLog` annotation on the inner inputs class triggers an annotation processor that generates a `DriveIOInputsAutoLogged` class implementing `LoggableInputs`. That class knows how to serialize the inputs to a WPILOG and (this is the magic) **reconstruct them from a saved log** in REPLAY mode. So if a real match's inputs are recorded, the entire control loop can be re-run bit-identically in sim afterward.

**Why this matters for teaching:**

1. **Real vs. sim is just a constructor switch.** [RobotContainer.java](../examples/path-b-demo/src/main/java/frc/robot/RobotContainer.java) picks `DriveIOReal`, `DriveIOSim`, or a no-op based on `Constants.currentMode`. The subsystem code is identical. Students learn the same code that ships to comp.
2. **It teaches the right separation of concerns.** "Where does the hardware end and your logic begin?" is concretized as "the IO interface boundary." This is the single biggest architectural lesson new programmers need.
3. **It enables replay-based grading.** A graded lesson can record its sim run as a WPILOG, then a CI replay can re-execute the student's code against the recorded inputs and assert via `Logger.recordOutput("Lesson/Pass", ...)`. **No team is doing this for teaching yet** — that's a wide-open architectural opportunity (§3.8).

**Version pinning is non-negotiable.** Both the [AdvantageKit 2025 and 2026 release notes](https://docs.advantagekit.org/whats-new/) explicitly warn that forward-compatibility is not guaranteed: "Manually updating projects is not recommended due to the risk of subtle breaking changes." Each lesson in the demo pins `advantageKitVersion=26.0.2` in [gradle.properties](../examples/path-b-demo/gradle.properties); when WPILib 2027 ships, lessons will need re-validation.

### 3.5 AdvantageScope as the visualization surface

[AdvantageScope](https://docs.advantagescope.org/) is an Electron desktop app that reads WPILOG/Hoot/REVLOG files and connects live over NetworkTables 4. For our purposes it's the visualization layer that replaces the JS canvas the browser PoCs draw on. Teaching-useful views:

- **Line chart** with unit-aware axes — exactly the "raw vs. clean" plot the Functions PoC draws, but for real data.
- **3D field view** — drop your robot model (CAD or built-in) into the layout, render `Pose2d`/`Pose3d` arrays. Multi-camera support including driver-station and robot-relative.
- **Mechanism viewer** (`Mechanism2d`) — render articulated mechanisms like elevators, arms, pivots. New in 2026: `generate3dMechanism` projects a 2D mechanism into 3D.
- **Joystick view** — overlays input on a controller graphic.
- **Boolean indicator widgets** — perfect for `Lesson/Pass` rubric values; light up green/red live.
- **Video sync** — pair a phone video of the real robot with a log timeline.

**Live connection setup is trivial:** when the student runs `./gradlew simulateJava`, [Robot.java](../examples/path-b-demo/src/main/java/frc/robot/Robot.java) registers an `NT4Publisher` data receiver in SIM mode. AdvantageScope's File menu → Connect to NetworkTables 4 → enter `localhost` → done. Every `Logger.recordOutput(...)` value and every `@AutoLog` field becomes a queryable signal.

**Per-lesson layouts.** AdvantageScope stores tab layouts as JSON. Ship one per lesson at `lessons/<slug>/AdvantageScope.json` — when the student opens AdvantageScope, the "Import Layout" command pre-arranges the right plots for that lesson. The `frcprog open <slug>` CLI command should call this automatically.

**Where the official docs are thin:** I couldn't find an explicit recipe for "open AdvantageScope from VS Code as part of `simulateJava`" — students will manually launch AS for now. That's a candidate one-day-of-work improvement for the VS Code extension.

### 3.6 What WPILib's simulation gives you for free

WPILib's `edu.wpi.first.wpilibj.simulation` package is huge — see [its Javadoc](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/package-summary.html). The classes lessons will lean on most:

| Class | Models |
|---|---|
| `ElevatorSim` | Linear elevator with gravity, motor, gearing, drum |
| `SingleJointedArmSim` | Pivoting arm with gravity-dependent torque |
| `DCMotorSim` | Generic motor + gearbox + inertia |
| `FlywheelSim` | Spinning mass driven by a motor (shooters) |
| `DifferentialDrivetrainSim` | Tank/skid-steer kinematics with encoders + pose |
| `BatterySim` | Voltage sag from drawn current |
| `EncoderSim`, `PWMSim`, `DIOSim`, `JoystickSim`, `XboxControllerSim` | Inject simulated values into HAL |
| `SimHooks.stepTiming(dt)` | Manually advance the sim clock — essential for tests |

Tests should always call `HAL.initialize(500, 0)` in `@BeforeEach` (sets up the simulated FPGA) and ideally `HAL.shutdown()` in `@AfterEach`. See [path-b-demo/src/test/java/.../DriveTest.java](../examples/path-b-demo/src/test/java/frc/robot/subsystems/drive/DriveTest.java) for the canonical pattern.

`./gradlew simulateJava` boots the robot code with the `halsim_gui` extension by default — the [Dear-ImGui-based Simulation GUI](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/simulation-gui.html) where students drag a real gamepad (or a Keyboard 0 virtual one) into the Joysticks slot and toggle Disabled/Auto/Teleop. AdvantageScope then attaches over NT4 for the rich view.

### 3.7 The lesson runner & VS Code extension

Three layers, increasing in polish:

**Layer 1 — the shell CLI** ([path-b-demo/tools/frcprog.sh](../examples/path-b-demo/tools/frcprog.sh)). Five subcommands cover the whole loop:

```
frcprog list                # all lessons + ✓/✗/○ status
frcprog next                # next unfinished lesson + edit path + check command
frcprog read <slug>         # print the lesson README
frcprog check <slug>        # ./gradlew test --tests '*Test' -DincludeTags='lesson-NN'
frcprog sim <slug>          # launch simulateJava with FRCPROG_LESSON env var
frcprog doctor              # JDK + Gradle + network + WPILib install health check
```

Even the shell-script version solves a real problem: it turns "remember which Gradle invocation runs lesson 02's rubric" into one command. The `doctor` subcommand alone saves the hours that first-day "it doesn't work" debugging would otherwise consume.

**Layer 2 — the VS Code extension.** Per VS Code's [extension API survey](https://code.visualstudio.com/api/), the right pieces are:

- **Activity-bar Tree view** — lesson navigator: ✓ done, ✎ current, ○ pending, 🔒 locked-by-prereq. Implemented with `TreeDataProvider`. [Tree view guide](https://code.visualstudio.com/api/extension-guides/tree-view).
- **Webview panel** — renders the lesson `README.md` next to the editor, with HTML buttons that post messages back to the extension to run commands. [Webview guide](https://code.visualstudio.com/api/extension-guides/webview).
- **Testing API** (`vscode.tests.createTestController`) — register lesson tests as `TestItem`s; clicking "Run Test" shells out to `./gradlew test --tests …`, parses `build/test-results/test/*.xml`, surfaces results inline with gutter icons. [Testing API guide](https://code.visualstudio.com/api/extension-guides/testing). **This replaces a homegrown UI panel** — students get native, familiar pass/fail badges.
- **Walkthroughs** for first-run onboarding only (limited to ~5–10 steps; not the curriculum spine).
- **Skip the Notebook API.** It doesn't fit Java's compile-link-run model.

A useful prior-art mention: [Microsoft's CodeTour](https://github.com/microsoft/codetour) is the closest thing that exists. Steps reference `file` + `line` + Markdown description + optional `commands`. Last release March 2023 — usable as inspiration, not as a dependency to bet on.

**Layer 3 — eventually, an AdvantageKit-replay-based grader.** See §3.8.

### 3.8 Grading: JUnit today, AdvantageKit replay tomorrow

**Today (works):** ship each lesson's rubric as JUnit 5 tests in `src/test/java/...`, tagged `@Tag("lesson-NN")`. CI runs `./gradlew check` in the [`wpilib/roborio-cross-ubuntu`](https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html) container — ~2-5 min cold, 30-90 s warm. Pass/fail surfaces as a sticky PR comment via [marocchino/sticky-pull-request-comment](https://github.com/marocchino/sticky-pull-request-comment) parsing the JUnit XML. See [path-b-demo/.github/workflows/ci.yml](../examples/path-b-demo/.github/workflows/ci.yml).

**Tomorrow (architecturally available, not yet built by anyone):** AdvantageKit replay. The pipeline:

1. CI checks out the student's code.
2. CI runs `./gradlew simulateJava` for a fixed duration with a deterministic scripted input (simulated joystick sweep). Robot writes a WPILOG.
3. CI invokes the robot binary again in REPLAY mode, pointing at the WPILOG.
4. The replay re-runs `periodic()` against the recorded inputs. The student's code emits `Logger.recordOutput("Lesson02/Pass", boolean)` each cycle.
5. CI parses the `_sim.wpilog`, asserts every `Lesson*/Pass` sample is `true`, and exits 0/1 accordingly.

**Why this is more powerful than JUnit alone:**
- The same log opens in AdvantageScope. The grader and the student see exactly the same data.
- Mentor review is rich: "here's the log where your solution failed at t=3.4s — open it locally."
- Edge cases that are hard to unit-test (cumulative drift, settling time) become natural — the rubric is a temporal predicate on a real run.

**The honest disclaimer:** [per the research](https://docs.advantagekit.org/theory/log-replay-comparison/), AdvantageKit replay is documented as a dev iteration tool, not a CI grader. The primitives (deterministic exit, log emission, headless replay) are all there. The CI integration would be FRCProgramming.org-original. Realistic effort: 1-2 weekends to build the first version once the basic JUnit grading is working.

### 3.9 What students learn that Path A can't teach

Every lesson in Path B forces a tiny dose of:

- **Git** (clone → edit → commit → push)
- **Gradle / build systems** (`./gradlew build`, things happen)
- **Real WPILib package layout** (`frc.robot.subsystems`, `import`s actually matter)
- **IDE muscle memory** (autocomplete, go-to-definition, breakpoints)
- **Test-driven development** (`./gradlew check` failed → fix → green)
- **The AdvantageKit IO Layer pattern** (the standard separation between hardware and logic)
- **Reading AdvantageScope plots** (the universal FRC debugging dashboard)

These are the things team seniors complain new members don't know. Path A elegantly avoids teaching all of them.

### 3.10 What Path B costs the student — the honest friction list

From the research, ranked by frequency of breakage:

| Friction | Where it bites |
|---|---|
| **WPILib install: 2.2–2.8 GB**, requires Win10+/macOS 13.3+/Ubuntu 22.04 or 24.04 | First-day onboarding; school WiFi melts under 50 simultaneous installers. **Mitigation:** USB-stick mirror at the team meeting. |
| **Chromebooks are unsupported.** Crostini Linux works for some installs but most school-managed Chromebooks have Crostini policy-disabled. | The biggest equity gap. **No fix on our side** — students with only a school Chromebook must use Path A. |
| **JDK mismatch** — system Java overrides WPILib's bundled JDK if students run `gradle` instead of `./gradlew` | Compiler errors ("Unsupported class file major version") that bewilder beginners. **Mitigation:** always teach `./gradlew`, document the bundled VS Code distinctly. |
| **School network filters** — Maven Central, `frcmaven.wpi.edu`, `maven.advantagekit.org`, NetworkTables port 5810 | Builds hang on first dependency download; AdvantageScope can't connect to localhost. **Mitigation:** pre-warmed `~/.gradle/caches` on USB, `gradle.properties` with proxy settings, NT4 over the loopback works without firewall in most setups. |
| **Antivirus scanning `.gradle/caches`** — Defender/CrowdStrike multiply build time 5–10×. | Cold builds take 20 minutes instead of 2. **Mitigation:** whitelist the project root. |
| **OneDrive/iCloud-synced project folder** | Gradle file locks fight cloud sync, builds randomly fail. **Mitigation:** clone to `~/dev/` or `C:\dev\`. |
| **First-build Windows Firewall popup** for JVM listening on NT ports | Students click Cancel reflexively; NT doesn't bind. **Mitigation:** screenshot in the onboarding lesson. |
| **`HAL.initialize` per-test overhead** (~100 ms) | Test suites get slow; cross-test state leaks if `AutoCloseable` not respected. **Mitigation:** document the pattern; the `DriveTest` example shows it. |

**Performance ceiling:** First Gradle build after `git clone` takes minutes to fetch all of WPILib + AdvantageKit + vendor deps. After that, incremental builds are 5-30 seconds, tests are sub-second. The cold start is the rough one; warm iteration is fine.

### 3.11 Reference repos and teams worth modeling after

From the research, the highest-signal public codebases:

- **[Mechanical-Advantage/AdvantageKit](https://github.com/Mechanical-Advantage/AdvantageKit)** — authors of the IO pattern; `template_projects/` directory has skeleton, kit-bot, differential drive, swerve, and vision starting points. **The single best reference for "what does a real project look like."**
- **[Mechanical-Advantage/RobotCode2025Public](https://github.com/Mechanical-Advantage/RobotCode2025Public)** and **[RobotCode2026Public](https://github.com/Mechanical-Advantage/RobotCode2026Public)** — full competition codebases by the AdvantageKit authors. Definitive on the pattern at scale.
- **[Team254/FRC-2025-Public](https://github.com/Team254/FRC-2025-Public)** — Cheesy Poofs using the IO pattern across drive, intake, indexer, claw, wrist, elevator, climber, vision. Great for studying complex compositions.
- **[HighlanderRobotics/Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training)** — FRC 8033's public training repo. Sections 2.6–2.8 are AdvantageKit + Logging. **Closest existing precedent to what FRCProgramming.org wants to be.**
- **[FRC 2928's training site](https://2928-frc-programmer-training.readthedocs.io/)** — text-based curriculum with explicit AdvantageKit chapters on Romi and RoboRIO. Two-pass approach (easy then real) is excellent prior art.

The novelty bet: **no public repo uses AdvantageKit replay for auto-graded lessons.** Teams use replay for debugging; teaching teams use AdvantageKit for code organization. Path B's killer feature (replay-based grading) is unbuilt — building it first is the kind of contribution that makes the curriculum stand out.

### 3.12 Path B — summary

| | |
|---|---|
| Strengths | Real tools. Real Git. Real Gradle. Real WPILib. Real AdvantageKit pattern. Real AdvantageScope. Skills transfer 1:1 to a real season. Lessons grow naturally into a deployable codebase. |
| Risks | Install friction filters out students with limited devices. No mobile / Chromebook story. AdvantageKit version pinning needs annual maintenance. CI replay grader is novel work. |
| Sweet spot | Stage 1D onward — students who've decided to invest in programming for a season |
| What's invented for the first time here | (a) AdvantageKit-replay-based auto-grading; (b) a "lesson manifest + JUnit tag" curriculum format on top of GradleRIO; (c) the VS Code extension UX that hides Git/Gradle complexity behind one-button operations |

---

## 4. Side-by-side

| Concern | Path A · Browser | Path B · VS Code |
|---|---|---|
| Install friction | Zero | ~30 min, 2.2-2.8 GB, breaks on Chromebooks |
| Chromebook OK? | ✓ | ✗ (no JDK; Crostini often policy-disabled) |
| Mobile OK? | ✓ read, △ edit | ✗ |
| Teaches Git? | No | Yes, naturally |
| Teaches Gradle/WPILib build? | No | Yes, naturally |
| Teaches AdvantageKit IO pattern? | No (could fake it) | Yes, the real pattern that 6328/254/8033 use |
| Real Java? | Subset (regex/AST + maybe CheerpJ) | Yes, the real javac |
| Lesson compounding | localStorage (works, fragile) | Single growing repo (robust) |
| Sim quality | Hand-rolled per lesson | WPILib's `*Sim` classes + DifferentialDrivetrainSim etc |
| Visualization | Per-lesson HTML canvas | AdvantageScope (3D field, mechanism2d, line chart) |
| Auto-grading today | None | JUnit 5 + GitHub Actions in 2-5 min |
| Auto-grading next year | Limited | AdvantageKit replay-based (novel — nobody's built this yet) |
| Author can ship new lesson in | A weekend | A weekend + GH Action config + an AdvantageScope layout |
| Engineer effort to scale to Stage 3+ | High (build a Java runtime) | Medium (more lessons + IO classes; harness exists) |
| Cost to host | $0 | $0 (GitHub Pages + GH Actions free tier) |
| Educator review workflow | Requires backend | PRs (GitHub gives this free) |
| Hardware/electrical lessons | Videos only | Videos + can dry-run code against real RoboRIO over USB |

---

## 5. The hybrid (and probably the right answer)

The two paths aren't mutually exclusive — they're optimized for different stages of student commitment:

```
┌─────────────────────────────────────────────────────────────────┐
│  Stage 1A  Variables, methods, control flow ────► Path A        │
│            (browser PoCs as they exist today)                   │
│                                                                 │
│  Stage 1B  Subsystems, basic PID  ─────────────► Path A         │
│                                                                 │
│  Stage 1C  "Migrate your project" lesson ──────► Bridge         │
│            "Click here to download your saved files as a        │
│             real WPILib project. Open it in VS Code. From       │
│             now on, you'll work locally."                       │
│                                                                 │
│  Stage 1D+ Drivetrain integration, commands ───► Path B         │
│            (real tools, real Git, real sim)                     │
│                                                                 │
│  Stage 2+  Swerve, vision, advanced control ───► Path B         │
└─────────────────────────────────────────────────────────────────┘
```

The bridge is the key idea: the **filesystem the student built in the browser** (the same shape we already use in [shared/filesystem.js](../examples/shared/filesystem.js)) can literally be **zipped and exported as a WPILib project**. That's why the PoCs already use real WPILib file paths (`src/main/java/frc/robot/util/MathUtils.java`).

Onboarding stays frictionless. Graduation to real tools is one button press and feels earned — by the time you cross that line, you already have a working codebase you wrote. The first thing you do in VS Code is open the file you wrote in lesson 2 and recognize it.

---

## 6. Concrete next steps

Reordered with the research in hand. Pick the path you want to test most aggressively and lean into it; the cross-cutting steps apply either way.

### Cross-cutting

1. **Lock the lesson manifest schema** as a one-page spec. The shape in [path-b-demo/lessons/01-methods/lesson.json](../examples/path-b-demo/lessons/01-methods/lesson.json) is the starting point — `slug`, `edits`, `tests`, `prerequisites`, `rubricOutputs`. This single file becomes the source of truth used by both the browser site AND the future VS Code extension AND the future "export to WPILib project" feature.
2. **Stand up the real MkDocs site** as the shared spine. Copy FRCDesign's `mkdocs.yml`, host on GitHub Pages. The lesson pages link to either an embedded browser widget (Path A) or a `git clone` command (Path B); the manifest decides which.
3. **Find a real student.** Second-year team member who programmed last season. Watch them do lessons 1→3 silently. Ship what they hit, not what looks pretty in a design doc.

### If you commit to Path A first

4. **Author lesson 3** (Subsystems as State Machines) against the existing PoC architecture. If it can't be reused, the architecture isn't done.
5. **Pick a Java-execution strategy** before lesson 5. The regex/AST trick survives through "fill in the body" exercises but breaks on multi-class lessons. Pre-decide: CheerpJ, TeaVM, or scope-to-bodies-forever.
6. **Prototype the "export to WPILib project" bridge** (~50 lines of JS with JSZip). Until this works, you have widgets, not a curriculum.

### If you commit to Path B first

4. **Finish [path-b-demo/](../examples/path-b-demo/)** into a runnable thing — add real `gradle/wrapper/` files, pull down the actual AdvantageKit + WPILibNewCommands vendordep JSON, add `.vscode/settings.json` for desktop support. Verify `./gradlew lesson01` actually passes the rubric with the exemplar solution.
5. **Build the `frcprog` CLI as a real Node binary** (or keep shell-script but make it shellcheck-clean). The shell version in [tools/frcprog.sh](../examples/path-b-demo/tools/frcprog.sh) is the floor; a real CLI gives `frcprog doctor` more diagnostic power and tees up the VS Code extension reuse.
6. **Build the VS Code extension MVP.** Just three features: Tree-view lesson navigator, webview for `README.md`, native Testing API integration calling `./gradlew test`. ~1-2 weeks. This is the single most leveraged piece of code in Path B.
7. **Prototype the AdvantageKit-replay grader.** No team has done this — concrete steps in §3.8. ~1-2 weekends after MVP works. If it ships, that's a publishable technique on its own.

---

## 7. Open questions worth not deciding yet

- **Java vs Python lesson tracks?** WPILib supports both. The outline assumes Java; AdvantageKit also has a Python port (RobotPy AdvantageKit, less mature). Decide before lesson ~5; switching is expensive after.
- **Krayon CAD renders for the Path A widgets.** Slick visual upgrade but introduces a third-party dependency. Confirm licensing and reliability before committing.
- **Accounts at all (Path A)?** L1 localStorage gets you very far. Push the OAuth question until a real user actually complains "I lost my work."
- **GitHub Classroom (Path B)?** Per research, autograding works but has known footguns (template instantiation races, point editing wipes results). Verify the multi-test-class case yourself before designing curriculum around it — see [issue #190](https://github.com/github-education-resources/autograding/issues/190).
- **Real-hardware lessons.** At some point a kid needs to put code on a RoboRIO. Plan for a "Stage 0.5 — wire it up" track that's just video + checklist + 1:1 with a mentor, not interactive.
- **AdvantageKit-version-per-season maintenance.** When WPILib 2027 ships, every lesson needs re-validation. Build a `make-rebase-2027` test pass into the season-end ritual.

---

## TL;DR

Two viable paths, one hybrid answer:

- **Path A (browser-native)** — the existing browser PoCs. Great for Stage 1, hits a Java-runtime ceiling above that.
- **Path B (VS Code + WPILib + AdvantageKit + AdvantageScope + Git)** — [path-b-demo/](../examples/path-b-demo/) is the architectural skeleton. Real tools, real IO Layer pattern, real `*Sim` physics, real test rubric. Skills transfer 1:1 to a real season. The killer feature nobody's built yet is **AdvantageKit-replay-based auto-grading** — the primitives exist, the wiring is FRCProgramming.org-original.
- **Hybrid** — browser for onboarding (Stage 1A-1B), an export-to-WPILib bridge, real tools from Stage 1D on. The PoCs and the demo are both built so this bridge is a one-day project once you've finished both ends.

Next concrete move depends on which path you most want to pressure-test. Either way, do it with lesson 3 (Subsystems as State Machines) — the third lesson is what reveals whether the architecture generalizes.
