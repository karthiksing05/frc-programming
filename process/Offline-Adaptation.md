# Offline Adaptation

### What changed when Path B was built without a network — and why

> **What this document is.** [Path-B-Implementation.md](Path-B-Implementation.md) describes shipping
> the VS Code + WPILib curriculum at team, multi-team, and public scale, assuming GitHub template
> repositories, GitHub Actions CI, and pull requests as the gradebook. This one records what was
> actually built when that assumption was removed, which parts of the roadmap survived unchanged,
> and which were replaced.
>
> Read [Path-B-Implementation.md](Path-B-Implementation.md) first; this is the delta.

---

## Why offline

The roadmap's own friction list
([Infrastructure-Analysis.md §3.10](Infrastructure-Analysis.md#310-what-path-b-costs-the-student-the-honest-friction-list))
ranks school network filters and first-build dependency downloads among the top causes of lost
onboarding time. The mitigations it proposes — a USB-stick Maven mirror, pre-warmed
`~/.gradle/caches`, proxy settings in `gradle.properties` — are all workarounds for a toolchain that
assumes a network.

The observation that made this build possible: **the WPILib installer already ships a complete
offline Maven repository.** GradleRIO, every WPILib artifact, the desktop simulation natives, and
JUnit 5 are all in `~/wpilib/2026/maven` the moment the installer finishes. A curriculum that
depends on nothing outside that set needs no mirror, no cache warming, and no proxy configuration.

So offline stopped being a mitigation and became the architecture.

---

## What was kept, unchanged

Every architectural decision in §3 of the Infrastructure Analysis survived:

- **One repo, one Gradle project, one growing `src/` tree.** Not subprojects — that breaks the
  WPILib VS Code commands (wpilibsuite/vscode-wpilib#847).
- **Lesson manifest + JUnit tag as the curriculum format.** `lessons/<slug>/lesson.json` declares
  `edits`, `tests`, `prerequisites`; `@Tag("lesson-NN")` selects a rubric.
- **The IO Layer pattern as the teaching substrate** (lesson 16).
- **AdvantageScope over NetworkTables 4** as the visualisation surface.
- **The pedagogy in full** — pain before abstraction, factories over Command subclasses, triggers
  for state, coordination in `RobotContainer`. All of [Curriculum-Flow.md](Curriculum-Flow.md)
  applies verbatim.
- **The 34-lesson sequence** from [Lesson-Plan.md](Lesson-Plan.md), in order, with the same
  concepts introduced at the same points.

---

## What was replaced

| Roadmap assumed | Offline build ships | Why the substitute is honest |
|---|---|---|
| GitHub template repo, `git clone` | Copy the `curriculum/` folder | Nothing in the curriculum needs a remote. Lesson 0D still teaches the three Git commands that prevent lost work. |
| GitHub Actions running `./gradlew check` | `frcprog check --all` | Same rubrics, same JUnit, run locally in about a minute. |
| Sticky PR comment as the gradebook | `frcprog check`'s parsed report + `.frcprog/progress.json` | Reads the same JUnit XML; prints assertion messages as advice rather than stack traces. |
| PRs as the mentor review workflow | Mentor's guide + review checklist, in person | The review questions are the same; the transport is not. |
| Hosted MkDocs on GitHub Pages | `site/` served on `localhost:8000` | Identical content. Lesson pages *include* the canonical lesson text and real source out of `curriculum/`, so site and project cannot drift. |
| AdvantageKit for the IO layer (`@AutoLog`) | Hand-written inputs struct + interface (lesson 16) | The pattern is the point, and writing the struct by hand once is how the generated version stops being magic. AdvantageKit remains an extension lesson. |
| Choreo / PathPlanner for trajectories | WPILib `TrajectoryGenerator` + `LTVUnicycleController` (lesson 13) | Same sampling, same feedback, same kinematics, same `kV` feedforward. What is lost is the GUI, which matters for a team and not for understanding. |
| VS Code extension (Tree view / webview / Testing API) | `frcprog` CLI + `.vscode/tasks.json` | The extension was Phase 1 tooling for an experience the CLI already delivers. Tasks give it a menu entry. |

---

## The five lessons that still need a network

Vision and physics simulation cannot be faked, and log replay needs AdvantageKit. Lessons 19, 23,
24, 25 and 26 are marked `track: extension` in the manifest, shown as ⬇ by `frcprog list`, and
documented in `curriculum/lessons/EXTENSIONS.md`. Each needs exactly one online build; after that
Gradle's cache makes every subsequent build offline again.

Being explicit about this — rather than quietly pretending everything works everywhere — is the
point. A student on a locked-down network knows before they start which five lessons they cannot
do, and what they are missing.

---

## Two decisions worth recording

**`frcprog` is a single Java source file.** Java 11's source-file mode (`java Frcprog.java args`)
runs it with no build step. The alternative implementations each fail somewhere: a Node CLI fails on
the first laptop without Node, a Python CLI fails on Windows. The one thing every student on this
curriculum provably has is the JDK inside their WPILib install. The cost is a ~0.5 s compile per
invocation and a hand-rolled JSON reader.

**Offline mode is forced in `settings.gradle`, not `gradle.properties`.** Gradle silently ignores an
`org.gradle.offline` property in `gradle.properties` — verified, not assumed. Setting
`startParameter.offline = true` in `settings.gradle` also covers the builds VS Code's WPILib
extension fires through the Gradle API, which a `--offline` flag typed in a terminal never reaches.
An `FRCPROG_ONLINE=1` environment variable is the escape hatch.

---

## How this is kept honest

Three checks, all runnable offline:

```bash
./gradlew build                 # fresh copy compiles; smoke test only, no lesson rubrics
./gradlew checkLessons          # lesson structure, required sections, cross-references
.meta/verify-rubrics.sh         # every rubric: MUST fail on the starter, MUST pass on the exemplar
```

The third is the one that matters. A rubric that also passes on the untouched starter grades
nothing, and a student would complete the lesson without learning it. `verify-rubrics.sh` applies the
pristine starter, requires failure, then applies the reference answer and requires success — both
halves, every graded lesson. All sixteen currently pass both.

Reference answers are generated, not hand-maintained: `.meta/make-exemplars.py` holds each lesson's
answer as a text patch and applies them cumulatively, so lesson 09's exemplar of `RobotContainer`
contains lessons 07 and 08's work too. A patch whose `before` text no longer matches fails loudly,
which is what stops starters and exemplars drifting apart.

---

## Where the roadmap still applies

Everything about **phases, gates and content velocity** in
[Path-B-Implementation.md](Path-B-Implementation.md) is unchanged by going offline:

- The Phase 0 gate — *a teenager completes lesson 01 in under 30 minutes, unassisted, on their own
  laptop* — is the right gate, and is now easier to hit because there is no clone step.
- §10.2's two-pass authoring rhythm (draft → watch a student do it cold → rewrite the parts they
  tripped on) is untouched and remains the highest-value practice.
- §6.1's kickoff ritual is untouched and is *more* important here, because the offline guarantee is
  pinned to one specific WPILib version's Maven repository.

What changes is Phases 2 and 3. Multi-team alpha and public beta assumed GitHub Classroom, a
metrics dashboard, and a contributor PR flow. An offline curriculum distributes by copying a folder,
which removes the infrastructure but also removes the telemetry — you cannot measure completion rates
across teams you cannot see. That is a real trade, and the honest answer is that offline distribution
optimises for the team in the room rather than for the census.
