# Mentor's guide

For the person running this with a group. Everything here is about the humans; the
[README](../README.md) covers the tooling.

---

## The one rule

> **Do not type.** The person typing is the person learning.

Sit next to them, point at the screen, ask what they think the error means. When they
are genuinely stuck and out of ideas, describe the fix in words and let them type it.

Taking the keyboard fixes the bug in ninety seconds and teaches nothing, and it
teaches something worse than nothing: that when things get hard, an adult arrives and
does it. That lesson sticks.

This is Katie Niwiden's framing, and the rest of her mantra is worth memorising:

> **Ask** when they are off task. **Show** when they do not know. **Do** when they
> have no clue.

Most interventions should be *ask*. Almost none should be *do*.

---

## Get them running `watch` on day one

```bash
./tools/frcprog watch
```

It re-runs the current lesson's rubric on every save and advances when one passes.
Students who use it iterate several times a minute. Students who do not will edit
for ten minutes, run a check, and get five failures at once with no idea which
change caused which.

The single highest-leverage thing you can say in the first session is "leave that
running in the other terminal".

---

## Pacing

The `estimatedMinutes` in each lesson is a median for a motivated beginner working
alone. In a meeting with distractions, assume 1.5×.

A realistic season:

| When | Target | Notes |
|---|---|---|
| Meeting 1 | Lesson 0A only | Install night. Budget the whole meeting; it will use it. |
| Meetings 2–3 | 0B–03 | Fast. Lesson 03 is short and the payoff is lesson 04. |
| Meetings 4–6 | 04–06 | Lesson 05 is the first genuinely hard one. |
| Meetings 7–10 | 07–10 | Command-based. Lesson 07 is the conceptual jump. |
| Meetings 11–14 | 11–15 | Ends with a working robot. |
| Off-season | 16–30 | Stage 2 at whatever pace people want. |

**Do lesson 0A as its own session, at home if possible.** Thirty students installing
2.5 GB on one school access point is a tradition and a bad one. If they must do it at
a meeting, stagger it or bring the installer on USB sticks.

---

## Where students actually get stuck

In order of how often it happens.

**Lesson 0A — install.** More students quit here than in the rest of the curriculum
combined. `frcprog doctor` catches most of it; the remainder is antivirus, cloud-sync
folders, and one machine with a mysterious system Java. Budget real time for this and
treat it as a first-class part of the course, not a preliminary.

**Lesson 05 — tuning.** The first lesson with no single right answer. Students raise
`kP` by 0.5 at a time from zero and conclude PID does not work. Tell them to move in
big steps: 10, then 40, then back off. Get them plotting before they tune, not after.

**Lesson 07 — suppliers.** The `() ->` is genuinely strange the first time. Do not
explain lambdas in the abstract — have them break it on purpose (the hints file walks
through it), watch the robot not move, and then fix it. Two minutes of that beats
twenty minutes of theory.

**Lesson 09 — composition nesting.** `alongWith` inside `andThen` inside
`withTimeout` is a lot of structure at once. Get them to say the sentence in English
first, then translate phrase by phrase. If they cannot say it, the code will not
help.

**Lesson 14 — the refactor.** Some students move code without deleting the original
and end up with two copies of every binding. `frcprog check --all` catches it. This
is also the first time the test suite pays for itself visibly, and it is worth saying
so out loud.

---

## What to say when they are stuck

Roughly in order — try each before moving down.

1. **"What does the error say?"** Most students have not read it. A surprising number
   of them can solve it themselves once they have.
2. **"What did you expect to happen, and what happened?"** Forces them to state the
   model they are testing, which is often where the bug is.
3. **"Where would you look to find out?"** Points at the plot, the rubric, the hints —
   and builds the habit of having somewhere to look.
4. **"Run `frcprog hint`."** It gives one hint and remembers. Sending them there is
   not giving up. Discourage `hints` (plural), which prints the answer.
5. **Describe the fix in words.** Not the code. "You need to read the joystick inside
   the lambda rather than outside it."
6. **Only then**, if they have been stuck fifteen minutes and are demoralised, sit
   down and work through it together — with them typing.

---

## Reviewing work

The rubric is the floor, not the ceiling. Green tests mean it works; they do not mean
it is good.

Worth checking by eye:

- **Naming.** Does `setMode(State.INTAKING)` read like the problem, or like the
  implementation?
- **Constants.** Did a magic number sneak back in?
- **Encapsulation.** Is hardware still `private final`?
- **Comments.** Do they explain *why*, or restate the code?
- **Copy-paste.** Two nearly-identical blocks usually want to be one method.

A good review question is *"why did you do it this way?"* If they can answer, the
work is theirs. If they cannot, that is the conversation to have — kindly, and
without making it about honesty.

On that: `frcprog solution` exists and students will find it. That is fine. A student
who reads the reference answer and then re-solves it from scratch has learned
something; a student who pastes it has not, and it will show up two lessons later
when nothing works. Say that out loud once, early, and then trust them.

---

## Running it as a group

**Pair programming works well** — one typing, one reading the lesson, swapping every
fifteen minutes. It halves the machines you need, and the reader catches things the
typer misses.

**Do not let people race ahead silently.** Someone who is four lessons ahead is
either very strong or has been pasting answers, and you want to know which. Ask them
to explain lesson 05's tuning recipe.

**Have finishers teach.** A student who has just done lesson 04 explaining it to
someone on lesson 03 is the single most efficient thing in the room. They discover
what they only thought they understood, and they explain it in language a peer
actually uses.

**`frcprog progress`** gives you a per-student picture in one command, if you can see
their screens.

---

## Adding your own lessons

The machinery is designed for it.

1. Add an entry to `lessons/manifest.json`. A `lessonNN` Gradle task appears
   automatically for graded lessons.
2. Create `lessons/<NN>-<slug>/` with `README.md`, `hints.md`, `lesson.json`. Copy
   the shape from a nearby lesson.
3. Add starter code with a `TODO (LESSON NN)` comment.
4. Add a rubric in `src/test/java/`, tagged `@Tag("lesson")` **and**
   `@Tag("lesson-NN")`. The first tag keeps it out of `./gradlew build`; the second
   makes `frcprog check` find it.
5. Add the answer as a patch in `.meta/make-exemplars.py`, then run it.
6. Validate:

```bash
./gradlew checkLessons          # structure, required sections, cross-references
.meta/verify-rubrics.sh NN      # fails on starter, passes on exemplar
python3 .meta/audit-lessons.py  # the checks the other two cannot make
```

`audit-lessons.py` covers the gaps between the other two: that each lesson's edit
target really carries its `TODO` marker, that the reference answer in `hints.md`
is behaviourally the same code as the exemplar, that every class named in prose
exists somewhere real, that the prerequisite graph is acyclic, and that every
`frcprog` command a lesson tells a student to run is one the CLI implements.

That second script is the important one. **A rubric that passes on the untouched
starter grades nothing**, and a student will sail through the lesson without learning
it. `verify-rubrics.sh` checks both halves and refuses to let that ship.

### Writing a good rubric

- **Grade behaviour, not shape**, unless the lesson *is* about shape. Lesson 03's
  rubric still passes after lesson 04 refactors everything, which is how it should be.
- **Write failure messages as advice.** "Expected 0.6 but was 0.0" is useless.
  "Holding B with nothing in the intake should run the roller inward" tells them what
  to look at. Students read the failure message far more often than they read the
  lesson.
- **Make one check catch one mistake.** A test that fails for four possible reasons
  tells the student almost nothing.
- **Grade the mechanism, not the result, when the result is cheatable.** Lesson 06's
  check 5 verifies the holding voltage comes from feedforward rather than from PID
  error, because a big enough `kP` passes a naive position check while missing the
  entire point.

---

## The yearly ritual

WPILib ships a new version every January and it is not backwards-compatible.

1. Bump `wpiVersion` in `gradle.properties` and the GradleRIO version in
   `build.gradle`.
2. `./gradlew build` and triage what breaks.
3. `.meta/verify-rubrics.sh` — every lesson, both halves.
4. Fix, re-verify, and tag a release for the season.

Budget two to four weeks of part-time work. Skipping it once is how a curriculum
quietly dies: the following year the breakage is two seasons deep and nobody wants to
start.

The full version of this is
[`process/Path-B-Implementation.md` §6.1](../../process/Path-B-Implementation.md).

---

## What this curriculum deliberately does not do

Worth knowing so you can fill the gaps yourself.

**No real hardware.** Everything is simulation. Students never wire a motor, flash a
roboRIO, or configure a radio. That is a real gap and it needs its own session with
an actual robot — but it is a different kind of teaching, and mixing it in would have
doubled the install friction.

**No Git beyond three commands.** Branching, merging, and pull requests are real and
useful and are not taught here. Lesson 0D gives students a way to not lose work,
which is the only Git problem they have on day one.

**Java only.** WPILib supports C++ and Python. Java is the FRC majority and has the
best library support; a parallel Python track would be a legitimate project and is not
this one.

**No swerve until Stage 2B.** Every competitive team runs swerve, and starting there
would mean teaching four modules of kinematics before a student has written a
subsystem. Tank drive first is a pedagogical choice, not an oversight.
