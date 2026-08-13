# FRCProgramming — the offline curriculum

Thirty-four lessons that teach FRC robot programming using the tools a real team
uses: Java, WPILib, Gradle, JUnit, the WPILib simulator, and AdvantageScope.

Everything runs on your laptop with no network, no account, and no server. You write
real robot code in a real robot project, and a real test suite tells you whether it
works.

---

## Start here

```bash
./tools/frcprog doctor      # is my install right?
./tools/frcprog next        # what should I do now?
```

If `doctor` is unhappy, fix what it says before anything else. If you have not
installed WPILib yet, that is lesson 0A:

```bash
./tools/frcprog read 0a-first-run-install
```

---

## The loop

Leave this running in a terminal while you work:

```bash
./tools/frcprog watch
```

Edit a file, save, and the rubric re-runs by itself. Pass a lesson and it moves to
the next one. That is the whole loop.

The rest, when you want them:

```bash
./tools/frcprog next                 # what to do, and which file
./tools/frcprog read 07-tank-drive   # the lesson
./tools/frcprog check 07-tank-drive  # grade once, without watching
```

Stuck? `./tools/frcprog hint 07-tank-drive` gives you **one** hint. Ask again for
the next. `hints` (plural) prints all four including the answer.

Want to see it move? `./tools/frcprog sim` starts the robot simulator, and
`./tools/frcprog scope` opens AdvantageScope for plots.

---

## What is in here

```
curriculum/
├── src/main/java/frc/robot/     the robot. One tree, growing lesson by lesson.
├── src/test/java/frc/robot/     the rubrics. Read them — they are the spec.
├── lessons/<NN>-<slug>/         README.md, hints.md, lesson.json
├── tools/frcprog                the command line (and Frcprog.java, which is it)
├── .meta/                       reference answers and pristine starters
├── OFFLINE.md                   how the no-network guarantee works
└── lessons/EXTENSIONS.md        the five lessons that need a download
```

**One source tree, and it grows.** The `applyDeadband` method you write in lesson 01
is called by the drivetrain you build in lesson 07 and is still there in the capstone.
Nothing restarts. By lesson 15 this folder holds a robot you could deploy.

**The tests are not hidden.** `src/test/java` contains the exact code that grades
each lesson. Reading a rubric before you start is not cheating; it is reading the
specification.

---

## The curriculum

| Stage | Lessons | What you end up with |
|---|---|---|
| **0** — Onboarding | 0A–0D | Installed, oriented, and able to save your work |
| **1A** — Java in context | 01–03 | A method, named constants, and a deliberately messy `teleopPeriodic` |
| **1B** — Subsystems & control | 04–06 | A state machine, a PID position loop, and gravity feedforward |
| **1C** — Command-based | 07–10 | A drivable robot with buttons, sequences, and live plots |
| **1D** — Composition & autos | 11–15 | Default commands, two autos, a refactor, and a capstone |
| **2A** — Structure & logging | 16–20 | The IO Layer pattern, logging discipline, replay, superstructure |
| **2B** — Swerve | 21–23 | Holonomic drive, pose estimation, trajectory following |
| **2C** — Vision | 24–26 | AprilTags, multi-tag fusion, physics-accurate simulation |
| **2D** — Advanced | 27–30 | Motion profiling, SysId, state machines, a season-scale capstone |

Lessons **01–16 are graded**: starter code with a TODO, a JUnit rubric, and a
reference answer. Lessons **17–30 are guided**: a clear goal, working code to model
yourself on, and the simulator as your check. That shift is deliberate — at some
point somebody has to stop writing exercises for you.

`./tools/frcprog list` shows the whole thing with your progress against it.

---

## Every command

```
frcprog watch                re-runs the rubric every time you save
frcprog next                 what to do now, and where
frcprog read <lesson>        the lesson text, in your terminal
frcprog check <lesson>       run the rubric and grade yourself
frcprog check --all          run every rubric — your local CI
frcprog hint <lesson>        one more hint than last time
frcprog hints <lesson>       all four at once, answer included
frcprog list                 every lesson and its status
frcprog progress             how far through you are
frcprog sim                  launch the robot simulator
frcprog scope                launch AdvantageScope
frcprog site                 serve the lesson site at localhost:8000
frcprog reset <lesson>       restore the starter code for a lesson
frcprog solution <lesson>    overwrite with the reference answer
frcprog doctor               check your install before blaming your code
frcprog build [--online]     build; --online only when adding a vendordep
```

`<lesson>` accepts an id (`07`) or a slug (`07-tank-drive`).

On Windows, `tools\frcprog.cmd` instead of `./tools/frcprog`.

---

## Reading the lessons on a page instead

Every lesson also renders as a website, served from your own machine:

```bash
./tools/frcprog site      # then open http://localhost:8000
```

The site is the same lesson text with the annotated source alongside it. Some people
prefer the terminal, some prefer the page; both are the same content, so use
whichever you will actually read.

---

## The offline guarantee

Every dependency this project needs already lives inside your WPILib install, and
Gradle is configured to run offline for every build — including the ones VS Code's
WPILib buttons fire.

Five lessons teach vendor libraries and need one online build each. They are marked
⬇ in `frcprog list`, and [`lessons/EXTENSIONS.md`](lessons/EXTENSIONS.md) explains
exactly what and why.

Details in [`OFFLINE.md`](OFFLINE.md).

---

## If you are a mentor

[`docs/MENTOR-GUIDE.md`](docs/MENTOR-GUIDE.md) covers running this with a group:
pacing, where students actually get stuck, what to say when they do, how to review
work, and how to add lessons of your own.

---

## Version pins

WPILib **2026.2.1**, Java **17**, JUnit **5.10.1**.

Do not bump these without re-running `.meta/verify-rubrics.sh`. WPILib makes
breaking changes every January, and every lesson has to be re-validated against them
— see [`process/Path-B-Implementation.md` §6.1](../process/Path-B-Implementation.md)
for the yearly ritual.

---

## Where this branch comes from

This branch is **generated**. It is the student-facing robot project, with the
Gradle build at the repository root so that WPILib's VS Code commands work when
you open the folder directly.

- Everything — this project plus the website, the design documents and the
  browser playgrounds — lives on the **`dev`** branch.
- The website, laid out for a static host, is on the **`website`** branch.

Do not send pull requests against this branch; it is overwritten by
`tools/publish-branches.sh` on every publish. Work on `dev`.
