# Lesson Author Handbook

> The complete authoring guide for FRCProgramming.org lessons. Read this **before** writing or editing any content under `site/docs/learn/`. Every content agent and human contributor works against this spec — consistency is what makes the curriculum feel like one site instead of thirty disconnected pages.

---

## 1. Where this fits

Three documents drive a lesson's content:

| Document | What it gives you | Where to find it |
|---|---|---|
| `process/Lesson-Plan.md` | The spec for the lesson you're writing — title, time, prereqs, edits, tests, rubric, references, anti-patterns, deferrals. **The source of truth for every fact in the header table.** | `../process/Lesson-Plan.md` (sibling of `site/`) |
| `process/Reference-Robots.md` | The two reference robots (Kelpie + Presto), file-by-file. Cite this when a lesson references real production code. | `../process/Reference-Robots.md` |
| This file (`AUTHORING.md`) | How to translate the spec into a page that reads well. **You're here.** | `site/AUTHORING.md` |

If `Lesson-Plan.md` and `AUTHORING.md` disagree about a fact, `Lesson-Plan.md` wins.

---

## 2. Where lesson pages live

Each lesson is one Markdown file at:

```
site/docs/learn/<stage>/<NN-slug>/index.md
```

For example: `site/docs/learn/stage1b/05-pid-introduction-elevator/index.md`.

The slug is the kebab-cased lesson title, prefixed with the lesson number (zero-padded for lessons 01–30; the four onboarding lessons use `0a`/`0b`/`0c`/`0d`). Slugs are listed verbatim in `site/mkdocs.yml`'s `nav:` block — if you create a lesson page at a path not in the nav, it won't surface in the sidebar.

Co-located assets (images, embedded SVGs, additional notes):

```
site/docs/learn/stage1b/05-pid-introduction-elevator/
├── index.md
├── img/
│   └── step-response.png
└── notes/
    └── deeper-dive.md      # optional; link from index.md
```

Never put exemplar solutions or rubric source in `docs/`. Those live in the project repo under `.meta/exemplar/<slug>/`.

---

## 3. The lesson page template

Copy this skeleton when you start a new lesson. Then fill it in, in order. **Do not reorder sections** — the consistent structure is what lets students skim 30 lessons without rebuilding context every time.

````markdown
# Lesson NN — Title <small>· Stage 1B</small>

<span class="stage-badge">Stage 1B · Lesson 05</span>

*One sentence in italics — the Niwiden-style pain framing. The pain this lesson dissolves, named in plain words.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 1B |
    | **Time** | ~50 min |
    | **Prereqs** | [Lesson 04 — Subsystems as state machines](../04-subsystems-as-state-machines/) |
    | **Edits** | `src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java` |
    | **Tests** | `frc.robot.subsystems.elevator.ElevatorTest` (`@Tag("lesson-05")`) |
    | **Reference robot** | Kelpie · [`elevator/ElevatorSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems/elevator) |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Wire WPILib's `PIDController` into a subsystem.
2. Tune `kP`, `kI`, `kD` by reading step-response plots.
3. Read encoder position from an IO inputs struct.
4. Send **voltage** to a motor, not normalized throttle, and explain why.

---

## The real-world problem

[Two short paragraphs. Establish the pain in concrete terms. Reference the previous lesson's anti-pattern if applicable.]

---

## What you'll do

[One short paragraph. The concrete task. End with a sentence that previews the interactive widget or code edit.]

<iframe class="lesson-widget"
        src="/examples/elevator-pid-poc/index.html"
        width="100%"
        height="640"
        title="Elevator PID — interactive PoC"></iframe>

---

## Starter code

```java
public class ElevatorSubsystem extends SubsystemBase {
  private final ElevatorIO io;
  private final PIDController pid = new PIDController(0.0, 0.0, 0.0);

  // TODO (LESSON 05): wire pid.calculate(...) into periodic()
  @Override
  public void periodic() {
    // ...
  }
}
```

---

## Rubric

The test class `ElevatorTest` asserts:

1. Elevator reaches each setpoint within ±2 cm.
2. Settles within 1.5 s for each setpoint.
3. Doesn't oscillate (no >5 cm overshoot).
4. `Lesson05/Pass` stays true through a full 4-setpoint sweep.

Run locally:

```bash
./gradlew test --tests '*ElevatorTest' -DincludeTags='lesson-05'
```

---

## See it run

```bash
./gradlew simulateJava
```

Open AdvantageScope, connect to NetworkTables 4 at `localhost`, and plot:

- `Elevator/Inputs/positionMeters`
- `Elevator/setpointMeters`

Drop a `Mechanism2d` viewer onto the same tab to watch the elevator carriage climb.

---

## Going further

- Tune the same loop with a step input twice as large. Does your tune still settle in 1.5 s?
- Add a `kFF` term so the elevator holds against gravity at rest.
- Read Kelpie's [`ElevatorIOSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIOSim.java) — how is their `kG` different from a textbook example?

---

??? tip "Full reveal — only open if you're really stuck"

    The minimal completion of `periodic()` is:

    ```java
    @Override
    public void periodic() {
      io.updateInputs(inputs);
      Logger.processInputs("Elevator", inputs);
      double output = pid.calculate(inputs.positionMeters, setpoint);
      io.setVoltage(output);
    }
    ```

    Try to derive this before peeking — the test failures will guide you.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 04**
    Subsystems as state machines

    [:octicons-arrow-left-24: Back to lesson 04](../04-subsystems-as-state-machines/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 06**
    Arm with gravity feedforward

    [:octicons-arrow-right-24: Continue to lesson 06](../06-arm-with-gravity-feedforward/)

</div>
````

---

## 4. Style guide

### 4.1 Length

- **Body:** 600–1200 words. Below 600, you're probably underexplaining; above 1200, you're probably scope-creeping. The header table, code blocks, and footer don't count toward the budget.
- **Code samples:** prefer multiple small fences over one giant one. ~15 lines max per fence; if you need more, link to the file on GitHub.

### 4.2 Material admonitions

Use these — and *only* these — call-out types. Each carries pedagogical signal; mixing them up trains the student wrong.

| Admonition | When to use |
|---|---|
| `!!! tip` | A small, optional improvement or shortcut. |
| `!!! note` | Useful aside; not required reading. |
| `!!! warning` | A way the student is likely to hurt themselves (footgun, captured value, `Thread.sleep`). |
| `!!! example` | A concrete worked example. Pair with a code fence. |
| `!!! danger` | A way the student can damage hardware or lose data. Use sparingly — overuse dilutes it. |
| `!!! success` | The student has just done something correctly; reinforces the win. |
| `!!! info` | Background context, links to docs. |
| `!!! quote` | Direct quote from WPILib docs, a reference robot's comments, or a Chief Delphi thread. |
| `!!! abstract` | The header stats block at the top of every lesson. **Don't use it anywhere else.** |

Collapsible variants — `???` opens closed, `???+` opens by default — are for content that's optional or a "full reveal" hint.

### 4.3 Code fences

- Java: triple-backtick `java`
- Shell / Gradle: triple-backtick `bash`
- Config (JSON, YAML, etc.): triple-backtick `json` / `yaml`
- Diff snippets: triple-backtick `diff`

Add `linenums="1"` (the `pymdownx.highlight` syntax) when line numbers help, e.g.,

````markdown
```java linenums="1"
public class ElevatorSubsystem extends SubsystemBase { ... }
```
````

Highlight changed lines with `hl_lines="3 7"`. Use sparingly — usually the prose around the fence is enough.

### 4.4 Links

| Target | How to link |
|---|---|
| Another lesson in the same stage | `[Lesson 02](../02-variables-and-types/)` — relative path from the current lesson's directory. |
| Another lesson in a different stage | `[Lesson 04](../../stage1b/04-subsystems-as-state-machines/)` — walk up to `learn/` then back down. |
| A process doc (sibling of `site/`) | `[Lesson-Plan.md](../../../../process/Lesson-Plan.md)` — four levels up from a lesson page (`<lesson>/ → <stage>/ → learn/ → docs/ → site/`), then into `process/`. **Verify the dot-count by previewing.** |
| An interactive PoC (browser widget) | `/examples/functions-poc/` — **absolute from the server root.** Works because `serve.sh` symlinks `../examples/` into `docs/examples/`. |
| WPILib docs, Chief Delphi, etc. | Absolute external URL, no special markup. Material opens external links in the same tab by default. |
| GitHub source (Kelpie / Presto) | Absolute URL to the file on the pinned commit (eventually) — pinned SHA listed in `process/Reference-Robots.md`. |

### 4.5 Section dividers

Use `---` horizontal rules between major sections (the ones in the template above). They give the eye a place to rest. Do **not** use them inside a section.

### 4.6 Lesson navigation footer

Every lesson ends with a two-card grid (Previous / Next). The first lesson of a stage links Previous to the last lesson of the prior stage. The very first lesson (0A) has no Previous; replace that card with a "Start of course" indicator. The very last lesson (30) has no Next; replace with a "Course complete" indicator.

---

## 5. Embedding interactive PoCs (browser widgets)

Some lessons ship a working in-browser simulator. Embed it with an iframe:

```html
<iframe class="lesson-widget"
        src="/examples/elevator-pid-poc/index.html"
        width="100%"
        height="640"
        title="Elevator PID — interactive PoC"></iframe>
```

The `.lesson-widget` class in `stylesheets/extra.css` makes the iframe full-width with rounded corners and a soft shadow.

### 5.1 Which lessons need an iframe?

| Lesson | PoC | URL |
|---|---|---|
| 01 — Methods (Functions) | `examples/functions-poc/` | `/examples/functions-poc/index.html` |
| 03 — Conditionals (anti-pattern) | (optional — could reuse functions-poc) | — |
| 05 — PID introduction (Elevator) | `examples/elevator-pid-poc/` | `/examples/elevator-pid-poc/index.html` |
| 07 — Tank drive wiring | `examples/tank-drive-poc/` | `/examples/tank-drive-poc/index.html` |

For Stage 1C onward, lessons typically **don't** ship a browser widget — they ship Gradle commands and AdvantageScope screenshots instead. The browser PoC story tops out at the Path A ceiling described in `process/Infrastructure-Analysis.md §2.3`.

### 5.2 Height suggestions

| Widget type | Height (px) |
|---|---|
| Editor + small canvas | 480 |
| Editor + simulation pane | 640 |
| Full lesson UI (editor + canvas + readout + graph) | 720 |

If your widget needs > 800 px, suggest the student "Open in full screen" via a link to the same path without the iframe wrapping.

### 5.3 Origin gotcha

The PoCs partition `localStorage` per origin. They MUST be served from the same origin as the parent MkDocs page — which is exactly what the symlink in `serve.sh` arranges. If a contributor opens a PoC by double-clicking the HTML file (`file://` URL), the cross-lesson filesystem demo will silently break.

---

## 6. Referencing code from `examples/path-b-demo/`

`examples/path-b-demo/` is the architectural skeleton for the Path B (VS Code + WPILib) lessons. When a lesson references code from it, use the **GitHub URL** of the file (the demo lives in this same repo) — *not* a relative path. The reason: the path-b-demo is meant to be cloned by the student as their own project. The site shouldn't make it look like the demo lives inside the site.

For example:

```markdown
Compare this skeleton to [the demo's RobotContainer.java](https://github.com/karthiksing05/FRC-Programming/blob/main/examples/path-b-demo/src/main/java/frc/robot/RobotContainer.java).
```

Until SHA-pinning is set up (`process/Reference-Robots.md §5.2`), use `blob/main/...` URLs. Once pinned, switch to `blob/<sha>/...`.

---

## 7. Per-stage tone guide

Tone shifts with the student's experience. Hold this consistent across a stage.

### Stage 0 — friendly + welcoming

The student hasn't written code yet. They might be a sophomore who's read about FRC for two weeks. Use plain language. Avoid jargon without unpacking it first. Anecdotes are fine. Address the reader as "you." If something is going to be frustrating (the WPILib install), acknowledge it.

> "First-time install takes about an hour. Don't panic if it feels slow — the download is large because it ships its own copy of Java, VS Code extensions, and the simulation toolkit. Make yourself a tea."

### Stage 1 — patient + concrete

The student is writing their first real Java code. They will misplace semicolons. Be patient. Be concrete. Show the *exact* file path and the *exact* command. Explain *why* a thing works, not just that it does. Never assume the student knows what an "interface" or a "package" is until you've introduced it.

> "Open `src/main/java/frc/robot/util/MathUtils.java`. You'll see a method named `applyDeadband` with an empty body. Your job this lesson is to write that body."

### Stage 2 — direct + assumes knowledge

The student has finished Stage 1 and is committed. Assume they know what a subsystem is, what `periodic()` does, what `Logger.recordOutput` looks like. Get to the point. Cite WPILib docs by URL rather than re-explaining their content. Push the student to read the reference robot code first and ask questions second.

> "Refactor `Drive.java` into the four-file IO pattern. Use Presto's `flywheels/` package as your reference — the structure is identical."

---

## 8. Citing the reference robots

Every lesson should ground itself in real production code from **Kelpie** (Team 8033, Reefscape 2025) or **Presto** (Team 6328, Crescendo 2024). Use this section as your cheat sheet.

### 8.1 When to cite which robot

| Topic | Primary robot | Why |
|---|---|---|
| IO interfaces, `@AutoLog`, AdvantageKit boilerplate | **Presto** | 6328 wrote AdvantageKit. Their code is the canonical reference. |
| Elevator / shoulder / wrist / roller subsystems | **Kelpie** | Cleanest one-mechanism-per-subsystem split. |
| Flywheel + indexer + pivot shooter | **Presto** | Whole shooter game. |
| Swerve (advanced) + maple-sim | **Kelpie** | Only candidate with maple-sim integration. |
| Multi-camera AprilTag fusion | **Presto** | `apriltagvision/` is the textbook example. |
| Top-level superstructure coordination | Either | Both robots show the pattern at different scales. |

### 8.2 Citation patterns

**One-line citation** (most common — the lesson is using the robot as inspiration, not transcribing it):

> Compare your code to Kelpie's [`elevator/ElevatorSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java). Notice the IO interface is the same; the difference is that Kelpie's `ElevatorIOReal` talks to actual Krakens.

**Snippet quote** (when a specific 5–10 lines from the reference is worth showing inline):

````markdown
Presto's `Flywheels` subsystem looks like this — note how the inputs struct
flows through `processInputs`:

```java
io.updateInputs(inputs);
Logger.processInputs("Flywheels", inputs);
double leftVolts = leftPid.calculate(inputs.leftVelocityRPM, target);
io.setLeftVoltage(leftVolts);
```

Source: [`Flywheels.java`](https://github.com/Mechanical-Advantage/RobotCode2024Public/blob/main/src/main/java/org/littletonrobotics/frc2024/subsystems/flywheels/Flywheels.java).
````

**Tour citation** (in the Robots section pages — `docs/robots/`):

> Kelpie carries a vertical **elevator** (`elevator/`) that lifts the **shoulder** (`shoulder/`) + **wrist** (`wrist/`) + **roller** (`roller/`) end effector to one of four scoring heights. The IO layer is real: `ElevatorIOReal` talks to Krakens, `ElevatorIOSim` plugs into WPILib's `ElevatorSim`.

### 8.3 What NOT to do

- **Don't paste long blocks** (>20 lines) verbatim from a reference robot. Link instead.
- **Don't paraphrase comments** that include team-specific jokes or context. Quote them directly with attribution or skip.
- **Don't claim a reference robot's code is "the right way."** It's *a* right way. Use phrases like "Kelpie's approach is …" rather than "the correct pattern is …."

---

## 9. Quality checklist before merging a lesson

- [ ] Header table matches `Lesson-Plan.md` (stage, time, prereqs, edits, tests, reference).
- [ ] All seven sections present in order: What you'll learn → Real-world problem → What you'll do → (optional widget) → (optional code) → Rubric → See it run → Going further → (optional reveal) → Footer.
- [ ] Word count 600–1200 in the body.
- [ ] Tone matches the stage (Stage 0 friendly, Stage 1 patient, Stage 2 direct).
- [ ] At least one reference-robot citation (Kelpie or Presto).
- [ ] Internal links use relative paths within `docs/`; external links use absolute URLs.
- [ ] iframe (if used) has `class="lesson-widget"` and a `title` attribute.
- [ ] Previous/Next footer cards exist and point to the right lessons.
- [ ] `./serve.sh` renders the page without warnings — no broken admonitions, no malformed tables.

---

*Last updated: alongside the Phase 1 site scaffolding. If you change the lesson template, update this file in the same PR.*
