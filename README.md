# FRC-Programming

An FRC robot programming curriculum that runs entirely offline — real Java, real
WPILib, real simulation, graded on your own laptop.

Thirty-four lessons. No accounts, no CI server, no cloud service that can be down on
the day of your meeting.

---

## Two halves

### `curriculum/` — the project students actually work in

A real GradleRIO project. Students copy this folder, open it in WPILib's VS Code,
and work through it lesson by lesson. It grows in place: the method written in lesson
01 is still there in the capstone, being called by a drivetrain built in lesson 07.

```bash
cd curriculum
./tools/frcprog doctor      # is my install right?
./tools/frcprog next        # what should I do now?
```

Sixteen lessons ship a JUnit rubric that grades the work locally. Eighteen are
guided — a clear goal, working code to model, and the simulator as the check.

[`curriculum/README.md`](curriculum/README.md) is the student-facing entry point.

### `site/` — the same lessons, as a local website

```bash
cd site && ./serve.sh       # http://localhost:8000
```

MkDocs Material, served from `localhost`. Every lesson page **includes** the
canonical lesson text and the real source files out of `curriculum/` rather than
copying them, so the site and the project cannot drift apart.

---

## Also here

| Path | What it is |
|---|---|
| [`process/`](process/) | The design documents this was built from — architecture, pedagogy, the 34-lesson spec, the two reference robots, and the phased roadmap |
| [`examples/`](examples/) | Three small browser playgrounds (deadband, PID tuning, drive mixing). Optional intuition-builders, not part of the curriculum |

---

## Verifying it

Three commands. Run them before shipping any change to a lesson.

```bash
cd curriculum

./gradlew build                 # compiles; runs the smoke test, not the lesson rubrics
./gradlew checkLessons          # lesson structure, required sections, cross-references
.meta/verify-rubrics.sh         # every rubric: MUST fail on the starter, MUST pass on the exemplar
```

That last one is the important one, and its first half is why.

**A rubric that also passes on the untouched starter grades nothing** — a student
would sail through the lesson without learning it, and nobody would notice for
months. `verify-rubrics.sh` applies the pristine starter, requires the rubric to
fail, then applies the reference answer and requires it to pass. Both halves, every
graded lesson.

```bash
cd site && source .venv/bin/activate && mkdocs build --strict
```

builds the site and fails loudly if any included file has moved.

---

## Requirements

**WPILib 2026** and nothing else.

Its installer brings a Java 17 JDK, its own VS Code, AdvantageScope, the simulation
tools, and — the part that makes this work — a complete offline Maven repository
containing GradleRIO, every WPILib library, and JUnit 5.

Gradle is configured to run offline for every invocation, including the builds VS
Code's WPILib buttons fire. Five lessons teach vendor libraries (AdvantageKit,
PathPlanner or Choreo, PhotonVision, maple-sim) and need one online build each;
they are marked ⬇ and documented in
[`curriculum/lessons/EXTENSIONS.md`](curriculum/lessons/EXTENSIONS.md).

The site needs Python 3 and one `pip install` for MkDocs. After that it serves
offline. If you would rather not, `frcprog read <lesson>` renders every lesson in the
terminal.

---

## Pinned versions

WPILib **2026.2.1** · Java **17** · JUnit **5.10.1**

Do not bump these without re-running `.meta/verify-rubrics.sh`. WPILib makes breaking
changes every January and every lesson has to be re-validated — see
[`process/Path-B-Implementation.md` §6.1](process/Path-B-Implementation.md) for the
yearly ritual, and [`curriculum/docs/MENTOR-GUIDE.md`](curriculum/docs/MENTOR-GUIDE.md)
for the short version.

---

## Adding a lesson

The machinery is built for it: add a manifest entry and a Gradle task appears; add a
tagged test and `frcprog check` finds it; add a patch to `.meta/make-exemplars.py` and
the reference answer is generated cumulatively.

Full instructions in [`curriculum/docs/MENTOR-GUIDE.md`](curriculum/docs/MENTOR-GUIDE.md).
