# Implementation Plan
### How the seven design docs and five example artifacts become a real, working FRCProgramming.org

> **What this doc is.** Everything else in `process/` is *what* we're building and *why*. This is *how we actually build it* — sequenced, with concrete first-90-day steps, named integration points between the existing artifacts, and an honest gap list.
>
> **Distinct from [Path-B-Implementation.md](Path-B-Implementation.md)?** Yes. That doc covers Path B in isolation. This one covers **the hybrid** — Path A onboarding + bridge + Path B graduation — and ties together every artifact already in this repo.

---

## TL;DR

We already have the **ideas** done. What's missing is the **glue**:

1. The three browser PoCs need to become a real MkDocs site (Path A onboarding).
2. The path-b-demo needs to actually build (real `gradle/wrapper/`, real vendordeps).
3. **The bridge** between them — "export your browser project as a real WPILib repo" — has to exist.
4. One real student has to complete one real end-to-end run from Stage 0 to Stage 1B.

**Cost to first pilot: ~12 weeks of focused work by 1-2 people.** Cost is not the blocker; *sequence and discipline* are.

---

## 1. The current state of the repo

What we already have, mapped to what each piece does:

### Design documents (in `process/`)

| Doc | Layer | Status |
|---|---|---|
| [FRCDesign-Analysis.md](FRCDesign-Analysis.md) | Reference — the model we're cloning | ✓ Complete |
| [Infrastructure-Analysis.md](Infrastructure-Analysis.md) | Architecture (Path A + Path B + hybrid) | ✓ Complete |
| [Path-B-Implementation.md](Path-B-Implementation.md) | Phased roadmap for the VS Code path only | ✓ Complete |
| [Curriculum-Flow.md](Curriculum-Flow.md) | Pedagogy (Niwiden + Oblarg + DAG) | ✓ Complete |
| [Reference-Robots.md](Reference-Robots.md) | The two robots (Kelpie + Presto) | ✓ Complete (pending §5 action items) |
| [Lesson-Plan.md](Lesson-Plan.md) | All 34 lesson blocks specified | ✓ Complete |
| [README.md](README.md) | One-line placeholder | △ Needs to become real index |
| **[Implementation-Plan.md](Implementation-Plan.md)** | **This doc — the synthesis** | ✓ Now |

### Example artifacts (in `examples/`)

| Artifact | Path | Status |
|---|---|---|
| [elevator-pid-poc/](../examples/elevator-pid-poc/) | Path A | Browser PoC — works, complete |
| [functions-poc/](../examples/functions-poc/) | Path A | Browser PoC — works, complete |
| [tank-drive-poc/](../examples/tank-drive-poc/) | Path A | Browser PoC — works, complete |
| [shared/](../examples/shared/) | Path A support | filesystem.js + lesson-extras.css |
| [path-b-demo/](../examples/path-b-demo/) | Path B | Skeletal, doesn't actually build yet |

### What's missing for a working pilot

1. **The MkDocs site spine** — no actual site yet, just standalone HTML PoCs.
2. **The bridge** between Path A and Path B (export-to-WPILib feature).
3. **A buildable path-b-demo** — missing gradle wrapper, real vendordeps, real WPILib install verification.
4. **The frcprog CLI as something more than a shell script.**
5. **The lesson-template generator.**
6. **All of Lesson-Plan.md's 34 lessons** in actual `lessons/<slug>/README.md` form.
7. **A real test student.**

These seven items are the implementation plan.

---

## 2. The destination: what "everything working together" feels like

Three personas. If all three of these stories work, the curriculum is shipping.

### Persona A — The brand-new student
Sits down at a Chromebook in the school library. Goes to `frcprogramming.org/learn`. Clicks **Lesson 01 — Methods**. Sees prose on the left, a code editor + joystick visualization on the right. Edits a function. Clicks Run. Watches the green graph line snap flat. **The whole experience never leaves the browser.**

Three lessons later they hit a "Graduate to VS Code" banner. One button: **Download your project as a WPILib repo**. They get a zip. They install WPILib (one-time pain). They open the zip in VS Code. They see *the same code they wrote in the browser*, now in a real IDE. They press the new "Lesson 04" button in the extension's lesson tree. The next lesson opens in a side panel. They keep going.

### Persona B — The team mentor
Opens a per-student dashboard. Sees: "Alice is on Lesson 07. Bob is on Lesson 03. Charlie hasn't started." Clicks Alice's name. Sees her recent PRs. Reviews. Comments. Approves.

### Persona C — The curriculum contributor
Wants to add a new lesson on swerve odometry. Runs `frcprog new-lesson 22-odometry "Odometry & pose estimation"`. The template generator creates the right files. They write prose, write tests, push a PR. CI runs the lessons against the exemplar, verifies the rubric works.

**Today none of these stories run end-to-end.** Phase 0 ships Persona A's first lesson. Phase 1 ships all three.

---

## 3. The hybrid architecture, one diagram

```
                ╔═══════════════════════════════════════════════╗
                ║          frcprogramming.org (MkDocs)          ║
                ╠═══════════════════════════════════════════════╣
                ║   Stage 0 onboarding   Stage 1A-1B browser    ║
                ║   ──────────────────   ────────────────────   ║
                ║   • Install guide       PATH A WIDGETS        ║
                ║   • Meet Presto/Kelpie  in iframes / inlined  ║
                ║   • Git tour            (current PoCs evolved)║
                ╚════════════════════════════╤══════════════════╝
                                             │
                       ┌─── Stage 1C "Graduate" banner ───┐
                       │  "Export project to WPILib" ⮕     │
                       └────────────┬─────────────────────┘
                                    │
                              [JSZip → .zip]
                                    │  (the bridge — TO BE BUILT)
                                    ▼
                  ╔═══════════════════════════════════════╗
                  ║   Student opens .zip in VS Code       ║
                  ║   File contents match what they       ║
                  ║   wrote in the browser (real paths!)  ║
                  ║                                       ║
                  ║         PATH B — VS Code              ║
                  ║   ─────────────────────────────       ║
                  ║   • Real WPILib + AdvantageKit        ║
                  ║   • AdvantageScope                    ║
                  ║   • Real Git workflow                 ║
                  ║   • Real JUnit rubric                 ║
                  ║   • Lessons 04 → 30                   ║
                  ║                                       ║
                  ║   Reference: Presto + Kelpie          ║
                  ║   (Reference-Robots.md picks)         ║
                  ╚═══════════════════════════════════════╝
```

Three integration seams to build:

1. **Browser widget ↔ MkDocs page** — host the existing HTML/JS PoCs as embedded widgets inside MkDocs pages. (Iframe is simplest; eventually inline.)
2. **Browser filesystem ↔ exported `.zip`** — the [shared/filesystem.js](../examples/shared/filesystem.js) writes WPILib-shaped paths already; serialize that to a zip with the path-b-demo skeleton as the wrapper.
3. **VS Code import ↔ existing student work** — when a student unzips and opens in VS Code, the `frcprog` CLI's `doctor` + `next` commands recognize where they left off and pick up from there.

---

## 4. The integration map between existing artifacts

Each row is a connection point that doesn't exist yet but needs to be built:

| From | To | Bridge code needed |
|---|---|---|
| [Lesson-Plan.md](Lesson-Plan.md) entries | Actual `lessons/<slug>/README.md` files | Lesson-template generator + 34 lesson authoring tasks (per [Path-B-Implementation.md §10.4](Path-B-Implementation.md#104-realistic-content-velocity)) |
| [shared/filesystem.js](../examples/shared/filesystem.js) | `path-b-demo/`-shaped zip download | New `exportProject(fs)` function using JSZip; ~50 LoC per [Infrastructure-Analysis.md §6](Infrastructure-Analysis.md#6-concrete-next-steps) |
| [path-b-demo/](../examples/path-b-demo/) | Buildable, runnable project | Phase 0 work per [Path-B-Implementation.md §2.1](Path-B-Implementation.md#21-deliverables) |
| [Reference-Robots.md §5 action items](Reference-Robots.md#5-action-items-before-locking-these-in) | License clarification + pinned SHAs | Email 8033; create `references.json` in the repo |
| [Curriculum-Flow.md](Curriculum-Flow.md) appendix A | Lesson-Plan.md cross-references | Already done in Lesson-Plan §"Anti-pattern preempt audit" |
| Browser PoCs | MkDocs site spine | MkDocs setup + iframe-embed mechanic + nav matching `manifest.json` |
| [Lesson-Plan.md](Lesson-Plan.md) manifest entries | A real `lessons/manifest.json` | One-time data extraction (the doc already has all the rows) |
| `frcprog.sh` (Bash) | A real VS Code extension | Per [Path-B-Implementation.md §16 Appendix C](Path-B-Implementation.md#16-appendix-c--vs-code-extension-feature-priority-one-screen): Tree view + Webview + Testing API; ~2 weeks |

That's the integration backlog. Sequencing it is §5.

---

## 5. The first 90 days (week-by-week)

Assumes ~15 hr/week from one lead + ~8 hr/week from one helper. Adjust for actual capacity. The goal is **a pilot-ready system with Lessons 01-04 fully shipping** at day 90.

### Weeks 1-2 — Foundation
Goal: prove the architecture survives contact with reality.

- **W1 D1-2:** Replace stubs in [path-b-demo/](../examples/path-b-demo/) with real assets — gradle wrapper from a fresh `WPILib: Create new project`, real [AdvantageKit.json](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json), real `WPILibNewCommands.json`, `.vscode/settings.json`.
- **W1 D3:** Confirm `./gradlew build` works on Mac + Windows + Ubuntu (use the WPILib Docker image where you can't borrow a machine).
- **W1 D4-5:** Confirm `./gradlew lesson01` and `lesson02` actually pass with the exemplars.
- **W2 D1-2:** Wire `./gradlew simulateJava`; confirm AdvantageScope on localhost shows live data.
- **W2 D3:** Author AdvantageScope layout JSONs for Lessons 01 + 02.
- **W2 D4-5:** Wire `.github/workflows/ci.yml` — confirm a clean push passes CI; confirm a deliberately broken push fails. Sticky PR comment renders.

**Phase 0 gate ([per Path-B-Implementation.md §2.2](Path-B-Implementation.md#22-phase-0-gate)):** by end of Week 2, the test student should complete Lesson 01 in <30 min unassisted. **If they don't, fix what they hit before Week 3.**

### Weeks 3-4 — Email 8033 + MkDocs site spine
Goal: lock the robot references in writing; stand up the curriculum site.

- **W3 D1:** Email Team 8033 per [Reference-Robots.md §5.1](Reference-Robots.md#51-get-the-license-question-answered-kelpie). Ask for explicit license clarification.
- **W3 D2:** Create `references.json` with pinned commit SHAs for both Kelpie and Presto.
- **W3 D3-5:** Stand up the MkDocs site. Copy FRCDesign.org's `mkdocs.yml` structure. Host on GitHub Pages. **Don't write lesson content yet** — just navigation skeleton, theme, search, the three top-level tabs (Learn, Handbook, Examples).
- **W4 D1-2:** Build the iframe-embed convention. The site's "Lesson 01" page embeds [examples/functions-poc/](../examples/functions-poc/) via `<iframe>`.
- **W4 D3-5:** Author *all* Stage 0 lessons (0A install, 0B Meet Presto, 0C Meet Kelpie, 0D Git tour) — they're prose-only, fast to write.

**Phase 0.5 gate:** test student goes through Stage 0 + Lesson 01 + Lesson 02 from a fresh laptop using just the MkDocs site. Total time <90 minutes.

### Weeks 5-6 — The bridge

Goal: students can graduate from browser to VS Code at the end of Stage 1B.

- **W5 D1-2:** Implement `exportProject(fs)` in shared JS — uses [JSZip](https://stuk.github.io/jszip/). Inputs: the student's filesystem from [shared/filesystem.js](../examples/shared/filesystem.js). Outputs: a zip containing the path-b-demo skeleton + the student's edited files merged in.
- **W5 D3:** Add a "Graduate to VS Code" banner that appears after Lesson 03 completes in the browser. Clicking → triggers download.
- **W5 D4-5:** Test the round-trip: write a function in lesson 01, advance to lesson 03, click graduate, unzip the result, open in VS Code, confirm the file you wrote in the browser is there with correct paths.
- **W6 D1-2:** Document the graduation step as Lesson 03.5 in the curriculum.
- **W6 D3-5:** Polish the bridge. Edge cases: what if the student hasn't finished Lesson 03 (warn but allow). What if they re-export later (zip with timestamp; don't overwrite work in progress).

**Bridge gate:** a student completes lessons 01-03 in browser, exports, opens in VS Code, runs `./tools/frcprog.sh next`, sees "Lesson 04: edit src/main/java/frc/robot/subsystems/roller/RollerSubsystem.java." Their previously-written `MathUtils.java` is intact.

### Weeks 7-9 — Lesson authoring sprint (the content factory)

Goal: author Lessons 03 (browser version) through 08 (VS Code version). 5-6 lessons in 3 weeks at the [Path-B-Implementation.md §10.4](Path-B-Implementation.md#104-realistic-content-velocity) pace.

- **W7:** Lesson 03 (the deliberate anti-pattern). Lesson 04 (Subsystems as state machines). Each: prose + starter code + JUnit rubric + AS layout + hints + exemplar.
- **W8:** Lesson 05 (PID intro — port the elevator-pid-poc to a real WPILib lesson). Lesson 06 (Arm with gravity FF).
- **W9:** Lesson 07 (Tank drive — factory pattern). Lesson 08 (Triggers & bindings).

Each lesson goes through the **two-pass authoring rhythm** from [Path-B-Implementation.md §10.2](Path-B-Implementation.md#102-the-two-pass-authoring-rhythm): rough draft → silent student test → revise.

### Weeks 10-11 — VS Code extension MVP

Goal: VS Code experience matches the browser polish.

- **W10:** Build the extension MVP per [Path-B-Implementation.md §16 Appendix C](Path-B-Implementation.md#16-appendix-c--vs-code-extension-feature-priority-one-screen) — Tree view + Webview + Testing API.
- **W11:** Polish + walkthrough for first-run onboarding. Bundle as a `.vsix`. Publish to the VS Code marketplace (or sideload-only for v0).

**Phase 1 gate ([per Path-B-Implementation.md §3.2](Path-B-Implementation.md#32-phase-1-gate)):** Of your own team's 5-10 pilot students, ≥50% self-teach at least one lesson without mentor help.

### Week 12 — The pilot

- **W12 D1-2:** Recruit 5-8 team members for the pilot.
- **W12 D3-5:** They start. You observe. Triage.

This is when you find out which lessons are wrong, which need rewriting, and where the bridge actually fails. Take notes; you'll need them for Months 4-6.

---

## 6. Cross-cutting workstreams

Three threads run concurrent to the timeline above:

### Workstream A — Content authoring

| Sprint | Lessons added | Mode |
|---|---|---|
| Weeks 1-4 | 0A, 0B, 0C, 0D, 01, 02 (and verify the existing browser PoCs as Stage 1A lessons) | Browser only |
| Weeks 5-9 | 03, 04, 05, 06, 07, 08 | Browser for 03, VS Code for 04+ |
| Weeks 10-12 | 09, 10 + polish | VS Code |
| Months 4-6 | 11 through 20 | VS Code; Phase 2 alpha territory |
| Months 7-12 | 21 through 30 | VS Code; Phase 3 prep |

Content velocity expectation per [Path-B-Implementation.md §10.4](Path-B-Implementation.md#104-realistic-content-velocity): **1-2 days per lesson** once the template generator is stable, **2-3 days** before. Account for ~1 day per lesson revising after student-testing.

### Workstream B — Tech infrastructure

| Sprint | Build |
|---|---|
| Weeks 1-2 | path-b-demo buildable, CI green |
| Weeks 3-4 | MkDocs site + iframe embed mechanic |
| Weeks 5-6 | The bridge (JSZip export) |
| Weeks 7-9 | Lesson template generator |
| Weeks 10-11 | VS Code extension MVP |
| Months 4-6 | AdvantageKit-replay grader prototype |
| Months 7-12 | Stickbot PR comment improvements; GitHub Classroom integration |

### Workstream C — Community

| Sprint | Build |
|---|---|
| Weeks 1-4 | Email 8033; set up Discord (`#help`, `#feedback`, `#mentor-only`) |
| Weeks 5-9 | Mentor's guide drafted; PR-review checklist |
| Weeks 10-12 | First pilot students; mentor onboarding flow |
| Months 4-6 | Alpha-team recruiting (3-5 outside teams) |
| Months 7-12 | Chief Delphi launch; broader community contribution |

These three threads should run as concurrently as headcount allows. If you're solo for Phase 0, sequence them (foundation → content → community). If you're three people, run them in parallel.

---

## 7. The first ten things to build (priority queue)

If you only had time for ten artifacts, build them in this order. Everything else can wait.

1. **A buildable [path-b-demo/](../examples/path-b-demo/)** — gradle wrapper + real vendordeps + `.vscode/settings.json`. Without this, every downstream piece is theoretical.
2. **`frcprog doctor` covering every Phase-0-observed friction** — the single most leveraged onboarding fix.
3. **The MkDocs site spine** — nav skeleton, theme, no lesson content yet.
4. **Stage 0 lessons (0A-0D)** — all prose, fast to write, high leverage.
5. **One real end-to-end lesson: Lesson 01** — browser PoC inlined into MkDocs, link to Lesson 02 works, full UX validated.
6. **The bridge (`exportProject(fs)`)** — without this, Path A and Path B are two disconnected products, not one curriculum.
7. **Lesson template generator (`frcprog new-lesson`)** — multiplies your authoring velocity by 3x.
8. **Lessons 04-08 in real WPILib** — proves the architecture survives past Stage 1A.
9. **VS Code extension MVP** — Tree view + Webview + Testing API.
10. **The pilot cohort recruitment** — without students, none of the above proves anything.

If you stall at any step, **stop and fix that step before continuing**. Phase 0 discipline is what makes the rest possible.

---

## 8. Integration gates

Five concrete checkpoints. If you miss one, regroup before continuing.

| Gate | When | Criterion | What to do if it fails |
|---|---|---|---|
| **G1 — Foundation** | End of W2 | path-b-demo builds end-to-end on 2+ platforms | Fix the platform-specific friction (probably Windows path or macOS Gatekeeper) |
| **G2 — Browser baseline** | End of W4 | Test student does Stage 0 + Lesson 01 + 02 in <90 min unassisted | Trace where they got stuck. Probably the install or the AdvantageScope step. |
| **G3 — Bridge** | End of W6 | Round-trip: browser-edited file shows up in VS Code project | Most likely failure: localStorage scoping. Need a single origin server during dev. |
| **G4 — Content factory** | End of W9 | 6+ Stage-1 lessons live; new ones take <2 days to author | If >2 days/lesson, the template generator isn't paying off — fix it before adding more lessons |
| **G5 — Pilot** | End of W12 | Phase 1 gate from Path-B-Implementation.md §3.2: ≥50% of pilots self-teach | Honest evaluation: is the gap in prose, in install friction, or in pedagogy? |

Don't try to skip ahead. Each gate validates an assumption the next one depends on.

---

## 9. The honest gap list

What's designed but not built, and what's needed but not yet designed.

### Designed but not built (the easier list)

- The MkDocs site itself — pure execution
- The 30 lessons' README/hints/exemplar files — pure execution at known velocity
- The bridge code — designed (JSZip + filesystem-as-zip), unwritten
- The VS Code extension — designed in detail (Path-B-Implementation §16), unwritten
- AdvantageScope layouts per lesson — designed, unauthored
- `references.json` with pinned SHAs — designed, unwritten
- The lesson template generator — designed, unwritten
- `frcprog` as a real Node CLI (the shell version exists as a seed)

These are 100% execution risk. No new design needed.

### Designed but with open questions

- **AdvantageKit-replay-based grading** — designed in [Path-B-Implementation.md §3.8](Path-B-Implementation.md#38-grading-junit-today-advantagekit-replay-tomorrow), but novel territory; the CI integration is FRCProgramming.org-original work. Treat as a Phase 2-3 stretch, not Phase 0.
- **GitHub Classroom vs. hand-rolled per-student-repo** — both viable; depends on the size of the pilot.
- **The 3D model for Kelpie** — three options in [Reference-Robots.md §5.3](Reference-Robots.md#53-decide-on-kelpies-3d-model-strategy); fall back to Mechanism2d for now.
- **Java vs Python tracks** — defer per [Curriculum-Flow.md §7.1](Curriculum-Flow.md#71-java-vs-c-vs-python). Don't make the call before Lesson 05.

### Not designed yet (gaps in the documentation)

- **Real-hardware "Stage 0.5" lessons** — videos + checklists for radio, RoboRIO flashing, electrical. Out of scope for the Path B simulator-only curriculum; needs its own track.
- **Mentor dashboard** — Persona B from §2. Listed in [Path-B-Implementation.md §11 Risk register](Path-B-Implementation.md#11-risk-register) but the UX is undesigned.
- **A privacy/data policy** — needed before Phase 3 public beta. Should be short, since we collect almost nothing (just GitHub commits).
- **The `Reefscape-Tour.md` and `Crescendo-Tour.md`** mentioned in [Reference-Robots.md §5.5](Reference-Robots.md#55-build-a-reefscape-tourmd-and-crescendo-tourmd-in-the-curriculum) — author these as Stage 0 lesson supplements.

### Genuinely uncertain

- Whether the browser-to-VS-Code bridge feels natural to students or breaks their flow. Will know after G3.
- Whether `frcprog doctor` actually catches the friction points or whether something new breaks each install. Will know after G2.
- Whether mentors actually use the mentor's guide. Will know after Phase 2 alpha.
- Whether AdvantageKit's annual breaking changes consume more time than the kickoff ritual budgets. Will know after the first kickoff post-launch.

---

## 10. The "Phase 0 ship-ready" checklist

What it takes to declare Phase 0 complete and start the team pilot. Copy this into a GitHub issue and check it off literally.

```
PHASE 0 — SHIP-READY CHECKLIST

Foundation (Weeks 1-2)
[ ] path-b-demo builds on Mac, Windows, Ubuntu (./gradlew build green)
[ ] ./gradlew lesson01 passes with exemplar
[ ] ./gradlew lesson02 passes with exemplar
[ ] ./gradlew simulateJava opens HALSim
[ ] AdvantageScope connects to NT4 localhost; live signals visible
[ ] .github/workflows/ci.yml runs and posts sticky PR comment

Site spine (Weeks 3-4)
[ ] MkDocs site live on GitHub Pages
[ ] Domain (frcprogramming.org) configured
[ ] Material theme matching FRCDesign.org's visual identity
[ ] Top-level nav: Learn / Handbook / Examples
[ ] First lesson page renders the functions-poc widget inline
[ ] Stage 0 lessons (0A install, 0B Meet Presto, 0C Meet Kelpie, 0D Git tour) authored and published

Bridge (Weeks 5-6)
[ ] JSZip export converts browser filesystem to a path-b-demo skeleton .zip
[ ] Round-trip: browser-edited file appears unmodified in the extracted VS Code project
[ ] "Graduate to VS Code" banner triggers after Stage 1B
[ ] Documented as Lesson 03.5

Robot references (Weeks 3-4 in parallel)
[ ] Email sent to Team 8033 re: Kelpie license
[ ] references.json with pinned SHAs for Kelpie and Presto committed
[ ] Reefscape-Tour.md authored as Stage 0 supplement
[ ] Crescendo-Tour.md authored as Stage 0 supplement

Content (Weeks 7-9)
[ ] Lesson template generator (`frcprog new-lesson`) works
[ ] Lessons 03, 04, 05, 06, 07, 08 fully authored (prose + tests + AS layout + hints)
[ ] AdvantageScope layouts shipped for each lesson

Tooling (Weeks 10-11)
[ ] VS Code extension MVP: Tree view + Webview + Testing API integration
[ ] `frcprog doctor` extended to cover every friction the test student hit
[ ] Extension installable as a .vsix; documented install steps

Community (throughout)
[ ] Discord server stood up (channels: #help, #feedback, #mentor-only, #pilot-team)
[ ] Mentor's guide drafted (per Path-B-Implementation.md §3.1)
[ ] PR review checklist drafted

Pilot (Week 12)
[ ] 5-8 pilot students from own team recruited
[ ] Each completes at least Stage 0 + 1A
[ ] Phase 1 gate ≥50% self-teach metric measured
[ ] Triage notes filed for Phase 1 work

GATE
[ ] All of the above checked
[ ] One independent observer (mentor not involved in development) signs off
```

---

## 11. After Phase 0

What happens at days 91-180 (Months 4-6) maps to [Path-B-Implementation.md §4](Path-B-Implementation.md#4-phase-2--multi-team-alpha-34-months) — the multi-team alpha. It's the same plan; this doc just got you ready to enter it.

The shape of months 4-6:
- **Content:** Lessons 11-20 (Stage 1D + Stage 2A — the AdvantageKit IO Layer arc)
- **Tech:** AdvantageKit-replay grader prototype; sticky PR comment improvements; observability dashboard
- **Community:** Recruit 3-5 alpha teams; build the mentor recruitment funnel

Months 7-12 maps to Phase 3 (public beta). The whole 18-month arc is in [Path-B-Implementation.md §1](Path-B-Implementation.md#tl-dr-for-skimmers).

---

## 12. Closing thoughts

The biggest practical risk is **not** technical — it's lead burnout. Most curriculum projects die at month 4, when the novelty has worn off and the work has become administrative. Three guardrails:

1. **Phase gates are enforced rests.** Don't push through G4 if G3 failed. Stop, fix, regroup.
2. **Bus factor from day one.** Every artifact has a documented owner + backup. Every doc has a "how do I update this?" section.
3. **Find one external collaborator before Phase 1 ends.** A solo project survives Phase 0. It rarely survives Phase 2 without help.

The system itself is designed to be **simple to maintain after launch**:
- Static site (no server to keep running)
- GitHub Actions for CI (no separate infra)
- AdvantageScope is upstream-maintained (not our problem)
- WPILib + AdvantageKit do their own yearly cycle (we follow, don't lead)

The work is mostly in the first 90 days. After that, the curriculum maintains itself proportional to how much content you keep adding.

> **Where to start tomorrow:** open [Phase 0 ship-ready checklist](#10-the-phase-0-ship-ready-checklist) above, copy it into a GitHub issue, and clear the first checkbox. Then the second. The plan works if and only if it's followed; the doc is here to make following it easier.
