# Path B Implementation Roadmap
### Shipping VS Code + WPILib + AdvantageKit lessons at team, multi-team, and public scale

> **What this document is.** [Infrastructure-Analysis.md §3](Infrastructure-Analysis.md#3-path-b--vs-code--wpilib--git-the-deep-exploration) answers "what should we build and why?" — the architecture. This doc answers "how do we actually ship it, in what order, with what resources, and how do we know it's working?" Read the architecture doc first.
>
> **Who this is for.** The project lead (you), future contributors who join, and mentors who need to evaluate whether to pilot it on their team. Optimized for the person doing the work.

---

## TL;DR for skimmers

Four phases over ~18 months. Each phase has one concrete gate that must be cleared before moving on. Total dollar cost: $0–150/month at full scale. Total people cost: 1 lead at all times + 2–3 contributors at peak.

| Phase | Goal | Duration | Headline gate |
|---|---|---:|---|
| **0** Prove | The demo actually builds and one lesson is end-to-end completable | 4–6 weeks | A teenager who's never seen the project completes Lesson 01 in <30 min, unassisted, on their own laptop |
| **1** Team pilot | Your own team uses it for Stage 1 | 3–4 months | ≥50% of pilot students self-teach at least one lesson without mentor help |
| **2** Multi-team alpha | 3–5 invited teams use it | 3–4 months | <10% of students hit unrecoverable install issues; >60% of starters complete Stage 1A |
| **3** Public beta | Open enrollment, FRC-community-wide | 6+ months | Organic growth — teams sign up without being asked |

Beyond Phase 3 is steady-state: a yearly upgrade ritual at WPILib kickoff and a growing content library.

---

## 1. Architecture refresher (one minute)

Path B is a **single GitHub-template-instantiated Gradle project** that grows lesson-by-lesson. The student edits real Java in WPILib's bundled VS Code; lessons ship as content (`lessons/<slug>/README.md` + `lesson.json`) plus a tagged JUnit rubric (`@Tag("lesson-NN")`). Subsystems follow [AdvantageKit's IO Layer pattern](https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/) so the same code runs against a real RoboRIO or against WPILib's `*Sim` classes. Visualization happens in [AdvantageScope](https://docs.advantagescope.org/) over NetworkTables 4. CI runs `./gradlew check` in the [WPILib Docker image](https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html) and posts a sticky PR comment.

See [path-b-demo/](../examples/path-b-demo/) for the architecture rendered as code; see [Infrastructure-Analysis.md §3](Infrastructure-Analysis.md#3-path-b--vs-code--wpilib--git-the-deep-exploration) for the full justification.

---

## 2. Phase 0 — Prove the demo end-to-end (4–6 weeks)

Goal: take [path-b-demo/](../examples/path-b-demo/) from "architecturally honest sketch" to "actually runs, one student tested it." This is the cheapest, riskiest phase — you'll learn whether the architecture survives contact with reality.

### 2.1 Deliverables

| # | Artifact | Effort | Owner |
|---|---|---:|---|
| P0.1 | Add a real `gradle/wrapper/` (copy from a fresh WPILib project) | 1 hr | Tech lead |
| P0.2 | Replace stub vendordep JSONs with the real ones from upstream URLs | 1 hr | Tech lead |
| P0.3 | Add `.vscode/settings.json` and `.vscode/tasks.json` (desktop support on; tasks: build, simulate, test, lesson-01, lesson-02) | 2 hr | Tech lead |
| P0.4 | Verify `./gradlew build` succeeds on Mac + Windows + Ubuntu | 1 day | Tech lead |
| P0.5 | Verify `./gradlew lesson01` fails (TODO not filled) and passes (with exemplar) | 30 min | Tech lead |
| P0.6 | Verify `./gradlew simulateJava` opens HALSim + AdvantageScope connects to NT4 localhost and shows live `Drive/Inputs/*` values | 1 day | Tech lead |
| P0.7 | Author one AdvantageScope layout JSON per lesson; document the import flow | 4 hr | Curriculum lead |
| P0.8 | Wire `.github/workflows/ci.yml` to actually run + post a sticky PR comment | 4 hr | Tech lead |
| P0.9 | Convert [path-b-demo/](../examples/path-b-demo/) into a GitHub template repository under your org | 1 hr | Tech lead |
| P0.10 | Write a one-page "first run" doc for the test student: what to install, in what order, what to expect | 2 hr | Curriculum lead |
| P0.11 | Test student session (silent observation; ~90 min) | 90 min | Both |
| P0.12 | Triage notes from the test session into a fixes backlog | 2 hr | Both |

### 2.2 Phase 0 gate

**A teenager who has never seen the project completes Lesson 01 in under 30 minutes, on their own laptop, with no help beyond the one-page first-run doc.**

This gate is non-negotiable. If the student fails, you have a real bug in onboarding — fix it before moving on. Track exactly where they got stuck; the friction points enumerated in [Infrastructure-Analysis.md §3.10](Infrastructure-Analysis.md#310-what-path-b-costs-the-student--the-honest-friction-list) are the prior — assume one of them will trigger.

**Likely failure modes** (handle them defensively):
- JDK mismatch (system Java, not WPILib's). Mitigation: `frcprog doctor` explicitly checks and refuses to continue.
- Windows Firewall popup eaten without thought. Mitigation: screenshot in the first-run doc.
- School Wi-Fi blocking `frcmaven.wpi.edu`. Mitigation: instruct first runs from home, not at the team meeting.
- `OneDrive`-synced project folder. Mitigation: doctor checks for `~/OneDrive` in the path.

### 2.3 What Phase 0 does NOT include

- The VS Code extension (use the shell CLI for Phase 0)
- The AdvantageKit-replay-based grader (use JUnit only for now)
- More than two lessons
- Anyone outside your team

Strict scope discipline here saves months downstream.

---

## 3. Phase 1 — Team pilot (3–4 months)

Goal: 5–10 students on your own team complete Stage 1A through 1C using Path B. By the end, you know whether the approach is sustainable for actual classroom-grade teaching.

### 3.1 Deliverables

#### Content (8–10 lessons total)
A realistic Stage 1 cadence:

| # | Lesson | Stage | Builds on | New concepts |
|---|---|---|---|---|
| 01 | Methods (Functions) | 1A | — | static methods, parameters, return values |
| 02 | Tank Drive Wiring | 1C | 01 | SubsystemBase, IO interface, periodic() |
| 03 | Subsystems as State Machines | 1C | 02 | enums, state transitions, Trigger |
| 04 | PID Introduction (Elevator) | 1B | 02 | PID, feedforward, ElevatorSim, encoder reads |
| 05 | Commands & Composition | 1B | 03, 04 | Command, sequence/parallel groups |
| 06 | Arm with PID + Gravity Compensation | 1B | 04 | SingleJointedArmSim, kG, feedforward |
| 07 | Joystick Bindings & Triggers | 1C | 05 | onTrue/whileTrue, CommandXboxController |
| 08 | Telemetry & SmartDashboard | 1C | all | Logger.recordOutput, NT publishing |
| 09 | Auto Routines (basic) | 1C | 05 | RunCommand, simple two-step autos |
| 10 | Full Teleop Robot — capstone | 1C | all | Pulling it together |

Estimated authoring effort per lesson: **2–3 days** the first time, **1 day** once the template is solid. Total Phase 1 content: ~3 weeks of full-time equivalent work.

#### Tooling

| # | Artifact | Effort |
|---|---|---:|
| P1.T1 | VS Code extension MVP — Tree view + webview + Testing API | 2 weeks |
| P1.T2 | `frcprog` CLI rewritten in Node (or kept in Bash but ShellChecked) | 3 days |
| P1.T3 | `frcprog doctor` covers every Phase-0-observed failure mode | 2 days |
| P1.T4 | Lesson template generator (`frcprog new-lesson <slug>`) | 1 day |
| P1.T5 | `make-rebase` Gradle task that prepares a release for a new WPILib season | 1 day |

#### Content authoring infrastructure

- A **lesson template** that authors copy when starting a new lesson. Includes `README.md` skeleton, `hints.md` skeleton, `lesson.json` with all required fields commented, starter `*.java` with TODO markers, test class skeleton with `@Tag("lesson-NN")`.
- A **markdown linter** that fails CI if a lesson README lacks the required sections (objectives, instructions, run-it, see-it, done-check, next).
- A **rubric validator** that fails CI if `lesson.json`'s `tests` field references test classes that don't exist.

#### Mentor enablement

- A **mentor's guide** mirroring FRCDesign's Educator's Guide: per-lesson estimated time, learning objectives, common stuck points (Phase 0 + Phase 1 observations), and assessment criteria (what good submissions look like beyond green tests).
- A **review checklist** template for PR reviews (does it compile? does it pass tests? does it match the style? does the student understand it, or did they copy-paste?).

### 3.2 Phase 1 gate

**At least 50% of pilot students self-teach at least one lesson without mentor help.** "Self-teach" means: read the lesson, attempt it, debug their own failures, pass the rubric, commit + push — all without asking a mentor.

This is the hard gate. If your students need hand-holding throughout, the lesson prose is broken or the failure modes are too obscure, not the architecture. Re-write before scaling.

**Secondary gates:**
- ≥80% of students get through Lesson 01 (low bar; install + first PR cycle).
- Median completion time per lesson within ±30% of the `estimatedMinutes` field. Wildly over → lesson is too hard or under-explained. Wildly under → lesson is trivial; merge with neighbor.
- Zero "I lost all my work" incidents.

### 3.3 What you'll learn in Phase 1

- Which lessons are too long (split them).
- Which lessons are too easy (merge them or skip).
- Which platforms break most often (probably some specific Windows config).
- How long a single lesson really takes to author end-to-end.
- Whether students enjoy it (the only honest signal — talk to them).

---

## 4. Phase 2 — Multi-team alpha (3–4 months)

Goal: 3–5 teams beyond your own pilot Stage 1 through Stage 2A. By the end, you'll know whether the project survives without you in the room.

### 4.1 Recruit the right alpha teams

You want teams that:
- Have at least one mentor or senior student who can champion it locally
- Span a range of resourcing (one well-funded team + one underfunded team is the right test)
- Span platforms (at least one Windows-heavy team + one Mac-heavy team + one Linux/Chromebook-mixed team)
- Are willing to give honest feedback when things break

Recruit through Chief Delphi, Discord (`#programming`, `#frcprogramming` if you stand it up), or direct asks at off-season events. Aim for 30–60 students total in the alpha.

### 4.2 Deliverables

#### Content (push to ~20 lessons total)

Stage 1D + Stage 2A content:

| # | Lesson | Stage | New concepts |
|---|---|---|---|
| 11 | Logging Discipline (Lesson 03b deepening) | 1D | When to log what; how to read AdvantageScope plots |
| 12 | Driverstation, Robot Lifecycle | 1D | Init vs periodic vs disabled vs auto vs teleop |
| 13 | Auto Routines (Path-following intro) | 1D | Trajectories, Choreo or PathPlanner basic |
| 14 | Refactoring & Extracting Helpers | 1D | When a method becomes a class; readability |
| 15 | Vision basics — PhotonVision in sim | 2A | Camera object, target detection, pose estimate |
| 16 | Subsystem composition (two subsystems coordinating) | 2A | Inter-subsystem state, Trigger composition |
| 17 | Power & current limiting | 2A | Current limits, brownouts, BatterySim |
| 18 | Climber prototype (linear extension) | 2A | ElevatorSim + safety interlocks |
| 19 | Intake state machine (real-game style) | 2A | Sensors, debouncing, hand-off between subsystems |
| 20 | Build-week sprint capstone | 2A | Time-pressured integration project |

#### Tooling upgrades

| # | Artifact | Effort |
|---|---|---:|
| P2.T1 | AdvantageKit-replay-based grader prototype (one lesson) | 1 weekend |
| P2.T2 | Sticky PR comment now reads `Logger.recordOutput` rubric values, not just JUnit results | 1 week |
| P2.T3 | VS Code extension: "Reset Lesson" button (`git checkout HEAD -- <edits>`) | 1 day |
| P2.T4 | VS Code extension: "Show Solution" after N failed attempts (configurable) | 3 days |
| P2.T5 | `frcprog doctor` extended with school-network mode (proxy detection, suggestions) | 2 days |
| P2.T6 | Lesson dependency-graph viewer (so mentors can see who's stuck where across the team) | 1 week |

#### Infrastructure

| # | Artifact | Effort |
|---|---|---:|
| P2.I1 | Real domain (`frcprogramming.org`, $15/yr) | 1 hr |
| P2.I2 | MkDocs Material site hosted on GitHub Pages, content auto-generated from `lessons/*/README.md` | 1 week |
| P2.I3 | GitHub Classroom org standup (or equivalent script-based per-student-repo creator) | 3 days |
| P2.I4 | Discord server with `#help`, `#feedback`, `#mentor-only`, `#alpha-team-<N>` channels | 1 hr |
| P2.I5 | A simple metrics dashboard — how many students completed each lesson, median time, common failure points | 1 week |

### 4.3 Phase 2 gate

**Two metrics that must both be true:**
1. **<10% of starters hit unrecoverable install issues** — defined as: opened a help ticket about install/build problems that took >2 hours to resolve OR caused them to abandon.
2. **>60% of starters complete Stage 1A** (lessons 01–05).

Both are reasonable for a real curriculum. If you can't hit them, the friction story is broken (not the content), and Phase 3 will go badly.

**Plus qualitative checks:**
- At least one alpha team's mentor independently authors a new lesson and submits it as a PR. (This validates contributor model.)
- AdvantageKit-replay grading actually catches at least one bug that JUnit alone missed. (This validates the novelty bet.)

### 4.4 What you'll learn in Phase 2

- Whether the project survives without you in the room.
- Which schools' network/firewall configs are uniquely terrible.
- Whether the AdvantageKit-replay grader gives real signal or just looks impressive in a demo.
- Whether mentors actually use the mentor's guide or improvise.
- The right ratio of self-paced to live-help.

---

## 5. Phase 3 — Public beta (6+ months)

Goal: open enrollment for anyone in FRC. By the end, "use FRCProgramming.org" is a thing FRC teams just know about.

### 5.1 Deliverables

#### Content (push to ~30 lessons, finishing Stage 2)

Stage 2B–2D content (vision, swerve basics, advanced control). See the original brainstorm in [FRCDesign-Analysis.md §13](FRCDesign-Analysis.md). Each lesson follows the now-validated pattern:
- README (Exercism-style), hints, exemplar
- Source file in the growing `src/` tree
- Tagged JUnit rubric
- AdvantageScope layout
- ~1 day of authoring effort once the pipeline is mature

#### Marketing & community

| # | Artifact | Effort |
|---|---|---:|
| P3.C1 | Chief Delphi launch post + demo video | 2 days |
| P3.C2 | Submit a talk for FIRST Championship Conference or RobotPy meetup | 1 day proposal + weeks of slot waiting |
| P3.C3 | "Showcase" page on the site featuring teams who completed Stage 1 | 1 week |
| P3.C4 | Per-language Slack/Discord cross-posts (FIRST Discord, RobotPy, etc.) | 1 day |
| P3.C5 | Mentor onboarding flow (signup → mentor dashboard → first PR review) | 1 week |
| P3.C6 | Contributor's guide (style guide, lesson authoring guide, PR conventions) | 1 week |

#### Hardening

| # | Artifact | Effort |
|---|---|---:|
| P3.H1 | Migration scripts for Phase 2 alpha repos to the new template structure if it changed | 1 week |
| P3.H2 | Translation infrastructure (English-only at first, but support i18n in the lesson manifest) | 2 weeks (deferred until demand) |
| P3.H3 | Privacy review (what student data flows where; we shouldn't be collecting much) | 1 week |
| P3.H4 | Code of conduct + reporting flow | 2 days |
| P3.H5 | Bus-factor mitigation — every doc has a documented owner and a documented backup | ongoing |

### 5.2 Phase 3 gate

**Organic growth.** Teams sign up without being asked. Concretely: in the last 30 days of Phase 3, >50% of new team signups came from referrals or organic search (not from your direct outreach). If you're still hand-selling, you're not at "scale" yet.

**Quantitative targets:**
- 20+ teams enrolled
- 200+ active students (logged in, completed ≥1 lesson in last 30 days)
- 50+ pull requests reviewed and merged
- Lesson completion rate stable across new cohorts (not degrading as you scale)

### 5.3 What you'll learn in Phase 3

- The real ratio of mentor capacity needed per student.
- Which lessons are still confusing after two rounds of revision (these are content debt).
- Whether the project can survive a "burst" — e.g., 200 sign-ups in a week after a Chief Delphi post.
- Whether you should support FRC's Python option (only do this if there's organic demand, not pre-emptively).

---

## 6. Steady state — life after Phase 3

The work doesn't stop after Phase 3; it changes shape. The yearly cadence becomes:

### 6.1 The kickoff-season ritual (every January)

```
Week 1 (after WPILib kickoff release)
  └─ Update gradle.properties version pins:
     - wpiVersion = 2027.x
     - advantageKitVersion = 27.x (when AK releases for the new season)
  └─ Run ./gradlew check across every lesson
  └─ Triage breakages by category:
     - "Just a version bump" — fix in place
     - "API removed" — rewrite the lesson, mark old version archived
     - "Sim API changed" — rebuild the sim configuration
Week 2-3
  └─ Patch test student session (one student each fixes a real breakage live)
  └─ Cut "Season N" release branch
Week 4
  └─ Re-shoot any embedded videos that show season-specific UI
  └─ Update the "current season" callout on the landing page
```

Realistic effort: **2–4 weeks of focused work by 1–2 people**.

This is the single most important sustainability practice. AdvantageKit explicitly warns that forward-compat across seasons is not guaranteed — [see the docs](https://docs.advantagekit.org/whats-new/) — and lessons must be re-validated.

### 6.2 Per-build-season cadence

```
January  Kickoff + version bump + content patch
February Build season — minimal new content; bug fixes only
March    Competition season — DO NOT push major content changes
April    Champs + decompression
May      Post-season retrospective; plan next year's curriculum additions
June-Sep Off-season: write new lessons, expand stages, recruit alpha teams
October  Pre-season: stress-test with returning students
November-December Polish; recruit mentors for next cohort
```

### 6.3 Content debt management

By the end of Phase 3 you'll have 30+ lessons. Some will age badly:
- API drift (most common — fix at kickoff)
- Conceptual drift (game changes; the 2023-style robot in your Stage 1 example feels dated). Solution: keep the *mechanism* (elevator, arm) but refresh the *game framing* every few years.
- Pedagogical improvement (you'll learn what teaches better). Solution: a "lesson revision queue" where the top-3-most-friction lessons get rewritten each off-season.

Budget: **20% of authoring time on revision**, 80% on new content.

---

## 7. The build backlog at a glance (cross-phase)

A flat prioritized list, ignoring phase boundaries. Pull from this when you have unallocated time.

### Critical path (do these first; everything else depends on them)
1. [ ] [path-b-demo/](../examples/path-b-demo/) actually builds end-to-end (Phase 0)
2. [ ] One real student finishes one real lesson (Phase 0)
3. [ ] VS Code extension MVP — Tree view + webview + Testing API (Phase 1)
4. [ ] Lesson template generator (Phase 1)
5. [ ] Mentor's guide (Phase 1)
6. [ ] CI with sticky PR comment (Phase 1)
7. [ ] AdvantageKit-replay grader (Phase 2)
8. [ ] Real domain + MkDocs site (Phase 2)
9. [ ] GitHub Classroom or equivalent (Phase 2)
10. [ ] Public Chief Delphi launch (Phase 3)

### Important but parallelizable
- [ ] Per-lesson AdvantageScope layout JSONs (Phase 0+, accumulating)
- [ ] `frcprog doctor` failure-mode coverage (Phase 0+, accumulating)
- [ ] Mentor recruitment funnel (Phase 2+)
- [ ] Lesson dependency-graph viewer (Phase 2)
- [ ] Metrics dashboard (Phase 2)

### Defer until evidence demands it
- [ ] Translation / i18n
- [ ] Python lesson track
- [ ] Mobile reading experience for lessons (read-only fine)
- [ ] Self-hosted Maven mirror (only if school networks become a chronic problem)
- [ ] Custom desktop sim app (use AdvantageScope until it's truly insufficient)
- [ ] Certification / completion badges (vanity feature; do last)

---

## 8. Roles & responsibilities

### 8.1 Three load-bearing roles

| Role | Owns | Effort estimate at peak |
|---|---|---|
| **Curriculum Lead** | Lesson content, pedagogy, mentor's guide, student feedback synthesis | 20–30 hr/wk at peak |
| **Tech Lead** | Extension, CI, doctor, build infra, AK-replay grader | 15–25 hr/wk at peak |
| **Community Lead** | Discord/forum, mentor onboarding, alpha-team recruiting, contributor PR triage | 5–15 hr/wk at peak |

At your team's scale (Phase 1), one person probably wears all three hats. By Phase 2 you genuinely need three people. Phase 3 needs three full-time-equivalent people OR a much larger volunteer pool.

### 8.2 Contributor model

Borrow from FRCDesign.org and other FRC open-source projects (PathPlanner, AdvantageKit, etc.):
- **PRs are the unit of contribution.** Even mentors who "just" review do so via PR comments.
- **Style guide is in-repo.** Don't make people guess.
- **Triage rota** — one person rotates through new PRs each week so nothing rots.
- **Recognition matters.** Public contributor list, "thanks to" in each season's release notes.

### 8.3 Bus-factor mitigation

Every load-bearing document/system should have:
- A documented **owner** (who fixes it when it breaks)
- A documented **backup** (who fixes it when the owner is on vacation)
- A documented **runbook** (how to fix it, written so the backup can actually use it)

Start this from Phase 0. The temptation to "I'll document later" is real and it always becomes a Phase 3 problem.

---

## 9. Hosting, ops, and costs

### 9.1 Stack

| Component | Choice | Cost |
|---|---|---:|
| Static site | MkDocs Material on GitHub Pages | $0 |
| Lesson template repos | GitHub (template repo feature) | $0 (free for OSS / education) |
| CI | GitHub Actions | $0 (public repos) / 2000 min/mo free (private) |
| Discussion | Discord server | $0 (or $9/mo for Nitro perks if needed) |
| AdvantageScope hosting | Students download from upstream | $0 |
| Domain | `frcprogramming.org` | ~$15/yr |
| Optional: hosted metrics | Plausible Analytics or PostHog cloud | $0–19/mo |
| Optional: backend (if ever needed) | Cloudflare Workers | $0 free tier |
| Optional: Maven mirror (if school networks become chronic) | A small DigitalOcean droplet | $5–10/mo |

**Total realistic cost at full scale: $15–150/month.** This is *cheap* for a curriculum that potentially serves thousands of students.

### 9.2 What you'll never need

- A custom server for the website itself
- Paid hosting for the lesson repos (GitHub Education is free)
- A paid CI provider (GitHub Actions covers it)
- A paid LMS (PRs + sticky comments are the gradebook)

### 9.3 What you might eventually need (Phase 3+)

- **Hosted authentication** — only if you decide to track per-student progress *outside* of GitHub. Cloudflare Workers + GitHub OAuth is the cheapest viable option.
- **Email notifications** — for mentor review pings. SendGrid free tier or Mailgun first 100/mo free.
- **A real metrics warehouse** — when you genuinely have >1000 students and you want to A/B test lesson revisions. Push to PostHog or self-hosted Umami.

Don't pre-build any of these. Wait until a user complains.

---

## 10. Content authoring at scale

### 10.1 The lesson template (the single most important productivity multiplier)

Every new lesson should start from a `frcprog new-lesson <slug> <title>` command that stamps out:

```
lessons/<NN>-<slug>/
├── README.md                    pre-filled with required sections, TODOs
├── hints.md                     skeleton with progressive hint structure
└── lesson.json                  pre-filled fields, validation-friendly

src/main/java/frc/robot/...
└── <NewFile>.java              with TODO markers and exemplar pre-staged in .meta/

src/test/java/frc/robot/...
└── <NewFile>Test.java          with @Tag("lesson-NN"), HAL setup, one example test
```

Authoring without this template takes 2–3 days per lesson. Authoring with it takes 4–6 hours once the author is fluent. Build this early.

### 10.2 The two-pass authoring rhythm

| Pass | What | When |
|---|---|---|
| **Pass 1 — draft** | Get the lesson written end-to-end, including code, tests, prose, hints. Don't polish prose. | Off-season |
| **Pass 2 — student-tested** | Watch one student do the lesson cold. Rewrite the bits where they got stuck. | Off-season |
| **Pass 3 (optional) — polish** | Editing for prose tightness, tone consistency, terminology alignment | Pre-season |

Skipping Pass 2 is the single biggest content-quality mistake. Lessons written in a vacuum *always* have unspoken assumptions that block students. You won't see them; the student will trip over them.

### 10.3 Style enforcement

A CI lint should fail any lesson that lacks:
- A "What you'll learn" section with explicit numbered objectives
- A "Run it" code block with the exact command
- A "Done check" with the specific success criteria
- A "Next" link to the next lesson in the manifest
- A `<details>`-collapsed answer in `hints.md` (don't put the answer in `README.md`)

This is cheap to enforce and saves dozens of review rounds.

### 10.4 Realistic content velocity

| Phase | New lessons | Days/lesson | Total person-days |
|---|---:|---:|---:|
| Phase 0 | 2 (already drafted) | — | 0 |
| Phase 1 | 8 new (Lessons 03–10) | 1–2 (template + student-test) | 12–20 |
| Phase 2 | 10 new (Lessons 11–20) | 1 (template mature) | 10–14 |
| Phase 3 | 10 new (Lessons 21–30) | 1 + revision work | 12–18 |
| **Total to 30 lessons** | 28 | — | **34–52 person-days** |

That's roughly 2–3 months of one person's full-time work, spread over the 12+ months of Phases 1–3. Realistic.

---

## 11. Risk register

The risks worth actively managing. Each has a concrete mitigation owner.

| Risk | Likelihood | Impact | Mitigation | Owner |
|---|---|---|---|---|
| Lead burnout | High | Project death | Phase gates enforce pause-and-evaluate; bus-factor docs from day one | Lead |
| WPILib breaking changes break all lessons | Certain (annual) | High one-time effort | Kickoff ritual (§6.1); version pinning; archive old-season tags | Tech lead |
| AdvantageKit unmaintained or replaced | Low (but possible) | High | Pattern is generic; lessons can be ported to vanilla WPILib if AK goes away | Tech lead |
| School firewall blocks `frcmaven.wpi.edu` | Medium | Onboarding death for affected teams | `frcprog doctor`; documented offline-cache USB; pre-warmed `~/.gradle/caches` | Community lead |
| Chief Delphi launch is a damp squib | Medium | Slow Phase 3 | Pre-line up testimonial quotes; have video ready; cross-post to RobotPy/Discord | Community lead |
| Quality of community PRs degrades content | Medium | Slow rot | Style lint in CI; required PR template; rotating triage owner | Curriculum lead |
| AdvantageKit-replay grader fails to differentiate from JUnit | Medium | Lost novelty bet | Have JUnit fallback as the primary grader; treat replay grading as a bonus | Tech lead |
| GitHub Classroom has subtle bugs at scale | Medium | Confused students | Build a thin custom shim that abstracts away GHC; ready to swap if needed | Tech lead |
| Mentors don't actually use the mentor's guide | High | Inconsistent help | Phase 2 observation; iterate the guide; consider a 30-min mentor onboarding video | Community lead |
| One alpha team's bad experience tanks reputation | Medium | Slow Phase 3 | Pick alpha teams carefully; have a "what to do when it breaks" doc; respond fast to issues | All |
| Legal/IP — using AdvantageKit logos, FIRST trademarks, etc. | Low | Compliance hassle | Read FIRST trademark guidelines; ask AK team for use approval; don't use FIRST logos | Lead |

### Risk *not* worth managing

- Forking by another team / fragmentation — if it happens, that's fine, the curriculum was open-source the whole time. The original outcompetes by quality, not exclusivity.
- "What if a student bricks their RoboRIO?" — they're in sim. They literally cannot.
- "What if a student writes terrible code that compiles and passes tests?" — that's what mentor review is for. The tests are the floor, not the ceiling.

---

## 12. Success metrics

Track these from Phase 1 forward. Sources are mostly free.

### 12.1 Adoption funnel

| Metric | Source | Healthy looks like (Phase 3) |
|---|---|---|
| Team signups (template repo instantiations) | GitHub API | Growing month-over-month |
| Active teams (≥1 PR in last 30 days) | GitHub API | 50%+ of signups remain active |
| Active students (≥1 commit in last 30 days) | GitHub commits | Average 5–10 per active team |
| Time from signup to first lesson completion | Computed | Median <2 hours |

### 12.2 Lesson health

| Metric | Source | Healthy looks like |
|---|---|---|
| Completion rate per lesson | CI pass logs | >70% of starters finish |
| Median time per lesson | CI start/end | Within ±30% of `estimatedMinutes` |
| Repeat-attempt rate | Commit history | <30% of students need >3 attempts to pass |
| First-attempt pass rate | CI results | >40% (lower means lesson is too tricky) |

### 12.3 Quality

| Metric | Source | Healthy looks like |
|---|---|---|
| Mentor PR review SLA | GitHub | <48 hr median |
| Student-reported satisfaction (per lesson, optional) | Survey at lesson end | >7/10 mean |
| Number of community PRs merged | GitHub | Growing |
| Lesson update frequency | Git log | Active enough to feel maintained, not so active that nothing is stable |

### 12.4 Reliability

| Metric | Source | Healthy looks like |
|---|---|---|
| `frcprog doctor` pass rate | Self-reported / opt-in telemetry | >90% on first run |
| CI pass rate on `main` | GitHub Actions | >99% — `main` should never be broken |
| Time-to-resolution for help requests | Discord/forum | Median <24 hr |

Build a simple dashboard at Phase 2.5 — a single page showing these numbers. Don't over-engineer.

---

## 13. Open decisions to make explicitly

These don't need answers now but should be on a tracked list.

| Question | When to decide | Who decides |
|---|---|---|
| Java vs Python vs both? | End of Phase 1 (before lesson 6) | Curriculum lead, with team input |
| GitHub Classroom or hand-rolled? | End of Phase 1 | Tech lead, after small PoC |
| One-button install or "follow these 12 steps"? | End of Phase 2 | Tech lead + Community lead |
| AdvantageScope layouts: per-lesson custom or one master? | End of Phase 1 | Curriculum lead |
| What's the policy on AI assistants (Copilot, Claude) in lessons? | Phase 2 onward | All; this matters for pedagogy |
| Do we partner with FIRST directly, stay independent, or both? | Phase 3 | Lead, possibly with FIRST contact |
| Real-hardware "Stage 0.5" lessons — when do those get authored? | Phase 3 once basics work | Curriculum lead |
| Do we accept industry sponsors? | Phase 3 if cost > $50/mo | Lead |

Don't agonize early. Most of these become obvious once you have data from the relevant phase.

---

## 14. Appendix A — Phase 0 detailed checklist

Copy this into a GitHub issue and check it off literally:

```
PHASE 0 — PROVE THE DEMO END-TO-END

Setup
[ ] Tech lead has WPILib 2026 installed end-to-end on a fresh machine
[ ] Curriculum lead can read the demo and understand the structure without help
[ ] Test student recruited (name + commitment confirmed)
[ ] Test student's laptop OS confirmed (Win 10/11, macOS 13+, Ubuntu 22.04)

Make the demo buildable
[ ] gradle/wrapper/ copied from a fresh WPILib new project
[ ] vendordeps/AdvantageKit.json replaced with real file from upstream
[ ] vendordeps/WPILibNewCommands.json replaced with real file
[ ] .vscode/settings.json + tasks.json added
[ ] ./gradlew build succeeds on lead's machine
[ ] ./gradlew build succeeds on a second platform (Windows or Linux)

Make Lesson 01 pass
[ ] ./gradlew lesson01 FAILS with empty TODO
[ ] Apply .meta/exemplar/01-methods/MathUtils.java to src/
[ ] ./gradlew lesson01 PASSES with exemplar
[ ] git checkout to revert

Make the sim visible
[ ] ./gradlew simulateJava opens HALSim GUI
[ ] AdvantageScope downloaded and runs on lead's machine
[ ] AdvantageScope connects to NT4 on localhost
[ ] Drive/Inputs/* values appear in AS
[ ] Document the exact steps + screenshots for the student doc

Author a Lesson 01 AdvantageScope layout
[ ] Joystick raw value on a line chart
[ ] Joystick clean value on the same chart
[ ] Lesson01/Pass boolean indicator
[ ] Export layout JSON to lessons/01-methods/AdvantageScope.json
[ ] Verify the layout imports cleanly into AS

CI
[ ] .github/workflows/ci.yml updated and pushed
[ ] CI passes on a clean push
[ ] CI fails on a deliberately broken lesson
[ ] Sticky PR comment shows up
[ ] Sticky PR comment updates on second push

Student doc
[ ] One page covering: install WPILib, install AdvantageScope, git clone,
    open in VS Code, run `./tools/frcprog.sh next`, read lesson 01, attempt it
[ ] Tested by curriculum lead (who pretends to be a student) — readable?

Student session
[ ] Schedule the session (90 min block)
[ ] Set up screen recording
[ ] Curriculum lead observes; tech lead is on standby for unrecoverable problems only
[ ] DO NOT help unless they're 15+ minutes stuck on something the doc should have covered

Triage
[ ] Watch the recording
[ ] List every friction point in a single GitHub issue
[ ] Triage into "fix now" / "fix Phase 1" / "won't fix"
[ ] Fix the "fix now" ones

GATE
[ ] A second test student completes Lesson 01 in <30 min unassisted
    on a fresh machine, using only the student doc
```

---

## 15. Appendix B — Lesson template spec (one-page)

Every lesson directory contains exactly these files:

```
lessons/<NN>-<slug>/
├── README.md              required sections (see below)
├── hints.md               progressive hints + collapsed answer
└── lesson.json            machine-readable manifest
```

### README.md required sections (in order)

````markdown
# Lesson NN — <Title>

> **Stage <X> · ~<N> minutes · <prerequisites or "No prerequisites">**

<1–2 paragraphs: real-world problem this lesson solves>

## What you'll do
<one short paragraph + the code stub block>

## Run it
```bash
./tools/frcprog.sh check <NN>-<slug>
```

<3–5 bullet list of what the rubric checks>

## See it in action
```bash
./gradlew simulateJava
```

<paragraph: which AdvantageScope plots to open, what to look for>

## Done?
<commit + push instructions, link to next lesson>

## Why this is structured like a real project
<2–3 sentences connecting to the AdvantageKit / WPILib pattern>
````

### hints.md required structure

````markdown
# Hints — Lesson NN

## Hint 1 — Where to start
<conceptual nudge>

## Hint 2 — The shape of the answer
<structural nudge, not the answer>

## Hint 3 — Almost there
<near-answer code skeleton with blanks>

## Hint 4 — Reference answer
<details>
<summary>Click to reveal</summary>

<full working code>

</details>
````

### lesson.json schema

```json
{
  "slug": "string (kebab-case, matches dir name minus the NN- prefix)",
  "title": "string",
  "stage": "string (1A | 1B | 1C | 1D | 2A | 2B | 2C | 2D | 3 | 4)",
  "tag": "string (lesson-NN)",
  "prerequisites": ["array of slug strings"],
  "edits": ["array of file paths"],
  "tests": ["array of fully-qualified JUnit test class names"],
  "rubricOutputs": ["optional: array of Logger.recordOutput key paths"],
  "simulation": {
    "subsystem": "string",
    "advantageScopeLayout": "optional: path to AS layout JSON",
    "durationSeconds": "optional number"
  },
  "estimatedMinutes": "integer"
}
```

CI fails if any required field is missing or if `tests` references a class that doesn't compile.

---

## 16. Appendix C — VS Code extension feature priority (one screen)

Build in this order. Don't skip ahead.

**MVP (Phase 1)** — these three give 80% of the value
1. **Activity-bar Tree view** — lessons + status (✓✎○🔒)
2. **Webview panel** — renders `lessons/<slug>/README.md` next to the editor
3. **Test Controller integration** — `./gradlew lesson01` runs in the native Testing panel

**v2 (Phase 2)** — these polish the loop
4. **"Reset Lesson" button** — `git checkout HEAD -- <edits>`
5. **"Show Solution" after N failed attempts** — configurable (off by default; mentors can enable)
6. **Welcome walkthrough** — first-run onboarding (5 steps max)
7. **`frcprog doctor` invoked from the command palette** with friendly UI
8. **Lesson navigation buttons** in the webview (Previous / Next)

**v3 (Phase 3)** — these matter for scale
9. **PR creation from the command palette** ("Submit lesson 03" → opens PR)
10. **Inline progress badges** in the gutter (test pass/fail history)
11. **Mentor review mode** — special view for opening any student's repo with their lesson status pre-loaded
12. **Telemetry opt-in** — anonymized completion data flowing to the metrics dashboard

**Probably never**
- Custom code editor (the bundled one is fine)
- Custom build system (Gradle is fine)
- Custom test runner (JUnit is fine)
- Custom Git UI (the VS Code built-in is fine)

Don't reinvent things VS Code already does well. The extension's job is to *orchestrate* native VS Code features around lessons, not replace them.

---

## 17. Summary

Path B at scale is a multi-year project, but each phase delivers value on its own — even if you stop at Phase 1, you've taught your own team an entire FRC programming curriculum using real tools. Each subsequent phase multiplies reach without proportionally multiplying effort, because the architecture, content, and tooling investments are durable.

**The four things that determine success:**
1. Phase 0 gate held to honestly (don't skip the student session).
2. The lesson template ships in Phase 1 (everything else compounds from this).
3. Bus factor managed from day one (write the docs as you go, not after).
4. Yearly kickoff ritual treated as non-negotiable maintenance (skip it once and the curriculum dies).

The dollar costs are negligible. The time costs are real but well-distributed. The biggest risk is your own attention — pace yourself, and lean on phase gates to force the rests.

> **Where to start tomorrow:** Open [path-b-demo/README.md](../examples/path-b-demo/README.md), pick one of the Phase 0 checklist items in §14, do it, push the change. Repeat. The shortest path from "good idea" to "shipped curriculum" is just the daily habit of clearing one checkbox.
