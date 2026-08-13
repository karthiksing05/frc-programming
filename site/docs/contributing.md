# Contributing

The machinery is built for extension. Adding a lesson is a documented, checkable
process, not a favour somebody has to grant you.

## Adding a lesson

Start with the generator. `checkLessons` is strict, and a hand-written lesson
usually takes a few rounds to satisfy it:

```bash
./tools/frcprog new-lesson 31-swerve-odometry --graded \
    --test frc.robot.util.SwerveOdometryTest \
    --edits src/main/java/frc/robot/util/SwerveOdometry.java
```

That writes the lesson directory, a rubric class carrying both required tags, and
the manifest entry — so the `lesson31` Gradle task exists straight away and the
whole thing lints on the first run. The rubric it generates **fails on purpose**: a
lesson that starts green teaches nothing.

For a graded lesson you have to name the test class and the file the student edits.
Those two decide whether the lesson teaches anything, so the generator asks instead
of guessing.

Then fill it in:

1. **Write the starter code** with a `TODO (LESSON NN)` comment saying what to write
   and why.
2. **Write the rubric assertions before the prose.** The rubric is the
   specification; the lesson text only has to walk a student to it.
3. **Fill the TODOs** in `README.md` and `hints.md`.
4. **Add the answer** as a patch in `curriculum/.meta/make-exemplars.py`, then run it.
5. **Regenerate the site page:** the lesson pages here are generated wrappers that
   include the canonical content, so a new lesson needs its page emitted and its nav
   entry added.

## Validating it

Two commands, and the second one is the important one.

```bash
./gradlew checkLessons          # structure, required sections, cross-references
.meta/verify-rubrics.sh NN      # fails on starter, passes on exemplar
```

`checkLessons` verifies that every lesson has its three files, that `lesson.json`'s
required fields are present, that every file named in `edits` exists, that every class
named in `tests` exists, that prerequisites resolve, and that the answer is in
`hints.md` behind a `<details>` rather than in the README.

`verify-rubrics.sh` applies the pristine starter, runs the rubric, and **requires it
to fail**; then applies the exemplar, runs it again, and requires it to pass.

That first half matters more than it sounds. **A rubric that passes on the untouched
starter grades nothing**, and a student will sail through the lesson without learning
it. This is the single easiest mistake to make when writing a lesson and the hardest
to notice by eye.

## Writing a good rubric

**Grade behaviour, not shape** — unless the lesson *is* about shape. Lesson 03's
rubric still passes after lesson 04 refactors all of its code into a subsystem, which
is exactly right: the robot's behaviour must not change during a refactor.

**Write failure messages as advice.** "Expected 0.6 but was 0.0" is useless.
"Holding B with nothing in the intake should run the roller inward" tells the student
where to look. They read the failure message far more often than they read the lesson.

**One check, one mistake.** A test that can fail for four reasons tells the student
almost nothing.

**Grade the mechanism when the result is cheatable.** Lesson 06 verifies that the
holding voltage comes from feedforward rather than from PID error — because a large
enough `kP` passes a naive position check while missing the entire point of the
lesson.

## Writing a good lesson

**Open with the pain.** Not "today we will learn about PID" but "the carriage slams
into the target and bounces". The concept is the answer to a question the student
should already have.

**Say what it is not.** Every lesson has a "not taught" boundary, and stating it is a
contract: *we are not covering X today; here is where it lives.* It also stops lessons
from growing.

**Explain a decision at least once.** Why `<` and not `<=`. Why volts and not
throttle. Why the sensor is inverted here rather than at every call site. Those are
the parts that transfer.

**Put the answer only in `hints.md`, behind a `<details>`.** `checkLessons` enforces
it. The student has to choose to reveal it, which is most of what makes hints work.

## Style

- Java: two-space indent, 100-column soft limit, Google Java Format.
- Comments explain *why*. The code already says what.
- Every `TODO (LESSON NN)` names a lesson that exists — a test enforces this.
- Markdown: sentence case headings, no trailing whitespace.

## The yearly ritual

WPILib ships a new version every January and it is not backwards-compatible.

Install the new WPILib, then:

```bash
./gradlew makeRebase           # what would change, and where
./gradlew makeRebase -Papply   # write it
```

The season is written down in six files, and missing one gives you a project that
builds on your laptop and fails on a student's. `makeRebase` reads the year and
GradleRIO version out of your actual install rather than taking your word for it.

1. Re-download each vendordep from its vendor's URL for the new season —
   `makeRebase` retargets the year but cannot know new dependency versions.
2. `./gradlew build` and triage what breaks.
3. `.meta/verify-rubrics.sh` — every lesson, both halves. WPILib changes sim physics
   between seasons, so a gain tuned last year can fail this year.
4. Fix, re-verify, tag a release for the season.

Budget two to four weeks of part-time work. Skipping it once is how a curriculum
quietly dies: the following year the breakage is two seasons deep and nobody wants to
start.
