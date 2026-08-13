# The feedback loop

One terminal, running the whole time you work:

```bash
./tools/frcprog watch
```

Edit a file. Save. The rubric re-runs by itself and the screen redraws with the
result. Pass a lesson and it moves to the next one without being asked.

That is the loop. Everything else on this page is detail.

---

## What you see

**While it is failing:**

```
  watch  01 — Methods (Functions)   (saved)
  ──────────────────────────────────────────────────────────────────

  ✗ 3 of 6 checks passing

  ✗ 1. A small positive reading collapses to zero
      A stick reading 0.05 with a 0.1 deadband is noise, not intent — return 0.0

  ✗ 2. A small negative reading collapses to zero
      Sticks drift both ways. Math.abs() is what makes one comparison cover both signs.

  Save to re-run.  frcprog hint 01-methods in another terminal for a nudge.
```

The failure text is written to be read. "Expected 0.0 but was 0.05" tells you
nothing; the sentence above it tells you what to look at.

**When it will not compile:**

```
  ✗ Does not compile.

    src/main/java/frc/robot/util/MathUtils.java:32: error: ';' expected
```

File and line, without the Gradle stack trace around it.

**When it passes:**

```
  ✓ Passed   6 checks

  Moving on to 02 — Variables & types
     ./tools/frcprog read 02-variables-and-types
```

---

## Why this shape

Borrowed from [Rustlings](https://github.com/rust-lang/rustlings), which teaches
Rust the same way. Four ideas, and this curriculum uses all four.

### One concept per exercise

Lesson 01 is a deadband. Lesson 02 is constants. Lesson 06 is a single line of
gravity compensation. When a check fails you know which idea you got wrong,
because only one is new.

### Instant feedback

The gap between "changed something" and "found out whether it worked" is most of
what makes a learning loop feel good or feel like homework. `watch` makes it about
two seconds and removes the decision to check at all.

### Start from failing

Every starter fails. Not because something is broken, but because your job is to
make it pass, and a red check is a precise statement of what is missing.

That property is machine-enforced here. `.meta/verify-rubrics.sh` applies the
untouched starter and **requires the rubric to fail**, then applies the reference
answer and requires it to pass. A rubric that passed on the starter would grade
nothing, and a student would sail through the lesson learning nothing. All sixteen
graded lessons pass both halves.

### Hints that escalate

```bash
./tools/frcprog hint 01-methods
```

Gives you **one** hint. Ask again for the next. It remembers where you are.

| Hint | Gives you |
|---|---|
| 1 | a conceptual nudge: what question to ask |
| 2 | the shape of the answer, no code |
| 3 | near-answer scaffolding with the key part blank |
| 4 | the working answer, and usually a version that looks right and is not |

`frcprog hints` (plural) prints all four at once, answer included. Use it when you
have decided you want it. The reason it is not the default is that the cheapest
hint is always the last one, and reading it costs you the lesson.

---

## Running it

Two terminals is the comfortable setup:

| Terminal | Running |
|---|---|
| 1 | `./tools/frcprog watch` |
| 2 | free, for `hint`, `read`, `sim`, git |

In VS Code, `Ctrl-\`` opens a panel and the `+` splits it. Put `watch` on the left
and keep the right one for everything else.

**Pin a specific lesson** instead of the next unfinished one:

```bash
./tools/frcprog watch 05-pid-elevator
```

Useful when you are re-doing something, or tuning gains and want the rubric to
re-run every time you change a number.

**Stop it** with `Ctrl-C`.

---

## What it watches

Every `.java` file under `src/main/java` and `src/test/java`.

It polls modification times about three times a second, then waits for the writes
to settle before running, so one save triggers one run even when your editor writes
a temp file and renames it.

??? info "Why polling and not a filesystem watcher"

    Java has `WatchService`, which is the obvious choice and the wrong one here.

    On macOS it has no native backend, so the JDK implements it by polling anyway,
    with a latency of up to ten seconds. A ten-second feedback loop is not a
    feedback loop.

    Polling a few dozen files three times a second costs nothing measurable and
    behaves the same on every platform.

---

## Where this does not apply

Honest about the limits.

**Lessons 17 to 30 have no rubric.** They are open-ended: build a swerve drive, add
vision, write a season capstone. There is no assertion that captures "your swerve
modules point the right way", and pretending otherwise would be worse than saying
so. Your check there is the simulator and AdvantageScope, and the lessons tell you
exactly what to look at.

**Some lessons touch more than one file.** Lesson 04 changes a subsystem, a
container and a robot class, because moving hardware ownership genuinely requires
all three. Splitting that into three exercises would teach the mechanics and hide
the point.

**There is no exercise for Java syntax on its own.** That is deliberate, not an
oversight. This curriculum introduces every concept as the answer to a problem you
just felt, so there is no lesson on "the `for` loop" waiting to be drilled. If you
want that style for the language itself, do Rustlings, or the equivalent for Java,
and come back.
