# Path B — what got built, what didn't, and what changed

[Path-B-Implementation.md](Path-B-Implementation.md) is a roadmap for shipping a
VS Code + WPILib curriculum online. This repository is that roadmap built
**offline**: everything runs from a WPILib install with no network.

This page maps the roadmap onto what actually exists, so the gaps are visible
instead of implied. Where the implementation departs from the roadmap it says so
and gives the reason.

Re-verify any row with the commands in [How to check this](#how-to-check-this).

---

## A note on the branch

Work started on a branch called `repo-tutorial`. It was renamed to `main`
mid-project, and the layout was then reorganised into three branches on request:

| Branch | Holds |
|---|---|
| `dev` | everything — curriculum, site, process docs, tooling |
| `main` | the Gradle project at the repo root, so students clone and open |
| `website` | the MkDocs site alone, shaped for Vercel |

`repo-tutorial` no longer exists as a name. `git reflog` still shows the rename if
you want the history.

---

## Phase 0 — prove the demo end-to-end (§2.1)

| # | Deliverable | State |
|---|---|---|
| P0.1 | Real `gradle/wrapper/` | **Done** |
| P0.2 | Real vendordep JSONs | **Done** — `WPILibNewCommands.json`. AdvantageKit deliberately excluded, see [Deviations](#deviations) |
| P0.3 | `.vscode/settings.json` + `tasks.json` | **Done** |
| P0.4 | `./gradlew build` on Mac + Windows + Ubuntu | **Partial** — verified on macOS only. No Windows or Linux machine was available |
| P0.5 | `lesson01` fails on TODO, passes on exemplar | **Done, and generalised** — `.meta/verify-rubrics.sh` proves both halves for all 16 graded lessons, not just lesson 01 |
| P0.6 | `simulateJava` opens HALSim; AdvantageScope reads NT4 | **Partial** — the sim launches and NT4 binds on `localhost:5810`. The AdvantageScope side needs a human to look at a screen; documented in [setup/simulator.md](../site/docs/setup/simulator.md) but not machine-verified |
| P0.7 | One AdvantageScope layout JSON per lesson | **Not done** — see [Known gaps](#known-gaps) |
| P0.8 | `.github/workflows/ci.yml` with a sticky PR comment | **Not done** — see [Known gaps](#known-gaps) |
| P0.9 | Convert to a GitHub template repository | **Not applicable here** — an org setting, not a change to any file |
| P0.10 | One-page first-run doc | **Done** — [lesson 0A](../curriculum/lessons/0a-first-run-install/README.md), mirrored on the site |
| P0.11 | Test student session (90 min, silent observation) | **Not done** — needs a person |
| P0.12 | Triage the session into a backlog | **Not done** — depends on P0.11 |

### The Phase 0 gate (§2.2)

> A teenager who has never seen the project completes Lesson 01 in under 30 minutes.

**Untested.** It requires a teenager. The four failure modes the roadmap predicts
are all handled, three of them by `frcprog doctor`:

| Predicted failure | Handling |
|---|---|
| JDK mismatch — system Java, not WPILib's | `doctor` checks it. Not theoretical: this session hit exactly this, and Gradle's error was `Unsupported class file major version 69` — meaningless to a student |
| OneDrive-synced project folder | `doctor` checks for OneDrive, Google Drive and Dropbox in the path |
| Non-ASCII characters in the path | `doctor` checks it |
| School Wi-Fi blocking `frcmaven.wpi.edu` | **Fixed architecturally rather than mitigated.** The roadmap suggests telling students to run their first build at home. Instead `settings.gradle` forces `startParameter.offline = true`, so no build ever reaches the network |

---

## Phase 1 — team pilot (§3.1)

### Content

The roadmap budgets 8–10 lessons for Phase 1. **34 exist**, 16 with graded
rubrics, covering all of [Lesson-Plan.md](Lesson-Plan.md). Content is ahead of the
roadmap; tooling is where the gaps are.

### Tooling

| # | Deliverable | State |
|---|---|---|
| P1.T1 | VS Code extension MVP | **Not done** — the roadmap defers it (§2.3) and the CLI covers the same ground |
| P1.T2 | `frcprog` CLI in Node, or Bash + ShellCheck | **Done, in Java** — see [Deviations](#deviations) |
| P1.T3 | `doctor` covers every observed failure mode | **Done** — 9 checks |
| P1.T4 | `frcprog new-lesson <slug>` generator | **Done** |
| P1.T5 | `makeRebase` task for a new WPILib season | **Done** |

### Authoring infrastructure

| Deliverable | State |
|---|---|
| Lesson template | **Done** — `frcprog new-lesson` emits one that passes `checkLessons` on the first run |
| Markdown linter for required sections | **Done** — `./gradlew checkLessons` |
| Rubric validator (`lesson.json` references real classes) | **Done** — `checkLessons` fails if any path in `edits` or class in `tests` is missing |

### Mentor enablement

| Deliverable | State |
|---|---|
| Mentor's guide | **Done** — [MENTOR-GUIDE.md](../curriculum/docs/MENTOR-GUIDE.md) |
| PR review checklist | **Done in substance** — the guide's *Reviewing work* section, written as what to look for rather than a form to tick |

---

## Deviations

Five places where this build knowingly departs from the roadmap.

**Offline instead of online.** The roadmap assumes network access. The
requirement here was the opposite. This works because the WPILib installer already
ships a complete Maven repository at `~/wpilib/2026/maven` — GradleRIO, every
WPILib artifact, the native simulation libraries, and JUnit. `settings.gradle`
points `pluginManagement` at it and forces offline mode. This is the load-bearing
decision in the whole repository; if it were wrong, nothing else would matter.

**No AdvantageKit vendordep.** AdvantageKit is a download, which breaks the
offline guarantee. The IO-layer pattern it exists to teach is hand-written instead,
so students learn the pattern rather than the library. Lessons that genuinely need
a vendordep are marked `track=extension` and are honest about needing one online
build.

**The CLI is Java, not Node or Bash.** The roadmap offers those two. Node would
be a second runtime to install; Bash is not on Windows by default. WPILib already
ships a JDK, and Java 11+ runs a single source file directly, so `frcprog` is one
`.java` file with no build step and no new dependency.

**Lesson README headings differ from the §15 spec.** The spec asks for
*What you'll do* / *Run it* / *See it in action* / *Done?*. These are
*Do this* / *Check it* / *See it* / *Done* — same sections, shorter names, after a
rewrite that made the lessons read as task lists. `checkLessons` enforces the new
names, so the two cannot drift apart.

**Verification is stricter than asked.** P0.5 asks for lesson 01 to be checked by
hand. Instead `verify-rubrics.sh` applies every pristine starter and requires the
rubric to **fail**, then applies every reference answer and requires it to pass.
A rubric that passes on the untouched starter grades nothing, and nobody finds out
for months.

---

## Known gaps

Two roadmap items are genuinely missing, and both were decisions rather than
oversights.

### P0.7 — AdvantageScope layout files

The roadmap wants a `.json` layout per lesson so a student imports plots instead
of building them by hand.

Not shipped, because a layout that fails to import is worse than none — it fails
at the exact moment a student is trying to see their code work for the first time,
and AdvantageScope's error for a malformed file is just *"not a recognized
format"*. Verifying an import needs a human driving the GUI.

The format was reverse-engineered from AdvantageScope 26.0.0 so whoever picks this
up does not start cold. Import validates exactly this:

```js
"version" in data && "hubs" in data && Array.isArray(data.hubs)
                  && "satellites" in data && Array.isArray(data.satellites)
```

A file failing that check is rejected outright; one whose `version` differs from
the running app warns *"Compatability is not guaranteed"* but still loads. The
inner shape of a `hubs` entry is the app's own window state — the reliable way to
get one is **File → Export Layout** from a session set up by hand, which is also
the only way to be sure it round-trips.

Until then, every graded lesson's *See it* section names the exact fields to plot
in prose, which is what the layout would have automated.

### P0.8 — CI

The roadmap wants GitHub Actions running the rubrics and posting a sticky PR
comment.

Not shipped, because CI would have to solve a problem the design deliberately
avoids: a GitHub runner has no `~/wpilib/2026/maven`, so CI must either install
WPILib on every run or build online — and an online CI green-lights a repository
whose entire promise is that it builds offline. The check that matters is
`verify-rubrics.sh`, which runs locally in about a minute and is what a maintainer
should run before pushing regardless.

If you add CI, install WPILib in the runner rather than switching to online
resolution, so CI tests the same offline path a student uses.

---

## How to check this

Everything above is reproducible. From `curriculum/`:

```bash
./gradlew checkLessons          # structure, required sections, cross-references
bash .meta/verify-rubrics.sh    # 16/16 fail on starter, pass on answer
python3 .meta/audit-lessons.py  # 133 static checks
./tools/frcprog doctor          # your install, before you blame the code
```

If `java -version` reports anything other than WPILib's JDK 17, Gradle fails with
`Unsupported class file major version`. `doctor` says so in English; run through
`./tools/frcprog`, which sets `JAVA_HOME` itself.
