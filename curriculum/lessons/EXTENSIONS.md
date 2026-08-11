# Extension lessons

Most of this curriculum runs with no network at all. Five lessons do not, because
they teach a **vendor library** — third-party code that is not part of WPILib and
therefore is not in the offline Maven repository your installer provided.

They are marked ⬇ in `frcprog list`. Everything about them is written and ready; you
just need one online build before you start.

| Lesson | Needs | Why it is worth it |
|---|---|---|
| 19 — Log replay | [AdvantageKit](https://docs.advantagekit.org/) | Re-run a match on your laptop, days later, with a debugger |
| 23 — Trajectory following | [PathPlanner](https://pathplanner.dev/) or [Choreo](https://choreo.autos/) | Paths designed in a GUI by someone who does not write Java |
| 24 — Single-tag vision | [PhotonVision](https://docs.photonvision.org/) | The robot knows where it is on the field |
| 25 — Multi-tag vision | PhotonVision | Removes single-tag pose ambiguity |
| 26 — Physics simulation | [maple-sim](https://shenzhen-robotics-alliance.github.io/maple-sim/) | Walls you cannot drive through, game pieces that exist |

---

## The one-time install

Same four steps for every vendor library.

### 1. Get the vendordep JSON

Each library publishes a small JSON file describing where its artifacts live.

**With the WPILib VS Code extension** (easiest): press `Ctrl/Cmd-Shift-P`, run
**WPILib: Manage Vendor Libraries → Install new libraries (online)**, and paste the
URL:

| Library | URL |
|---|---|
| AdvantageKit | `https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json` |
| PathPlanner | `https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json` |
| PhotonVision | `https://maven.photonvision.org/repository/internal/org/photonvision/photonlib-json/1.0/photonlib-json-1.0.json` |
| maple-sim | `https://shenzhen-robotics-alliance.github.io/maple-sim/vendordep.json` |

**By hand:** download the JSON into `vendordeps/`.

> Check the library's own documentation for the current URL and for the version that
> matches WPILib 2026. Vendordep URLs move between seasons, and a 2025 vendordep in a
> 2026 project produces confusing errors.

### 2. Build once, online

```bash
./tools/frcprog build --online
```

This is the only step that needs a network. Gradle downloads the library into
`~/.gradle/caches`.

### 3. Confirm you are offline again

```bash
./gradlew build
```

No flags. If this succeeds, the download is cached and you are back to normal —
every future build, including the ones VS Code fires, runs offline.

### 4. Do the lesson

```bash
./tools/frcprog read 19-log-replay
```

---

## What this costs, honestly

**Version pinning becomes your problem.** WPILib and its vendor libraries release
together every January, and they are not forward-compatible. AdvantageKit's own
documentation is blunt about it: *"manually updating projects is not recommended due
to the risk of subtle breaking changes."* Every vendordep you add is one more thing to
re-validate at kickoff.

**The offline guarantee weakens.** A teammate who copies your project onto a fresh
laptop now needs a network for their first build, not just for the WPILib installer.
That is a real cost on a team where laptops get reimaged.

**Each one is genuinely worth it** — these are the libraries competitive teams
actually run, and lessons 19 and 24 in particular teach things you cannot fake. Add
them deliberately, one at a time, rather than all at once at the start of a season.

---

## Doing them without the vendordep

Every extension lesson's README explains the concept in full before it asks you to
install anything. If you cannot get a network at all, read them anyway — the ideas
are the durable part, and you will recognise them the first time you open a real
team's code.

Two of the five also have honest offline substitutes already built into this
curriculum:

**Instead of lesson 23 (Choreo / PathPlanner):** lesson 13 follows a trajectory using
WPILib's own `TrajectoryGenerator` and `LTVUnicycleController`. Same feedback loop,
same kinematics, same feedforward. What you lose is the GUI, which matters for a team
and not for understanding.

**Instead of lesson 19 (AdvantageKit replay):** lesson 16 builds the IO Layer pattern
by hand, which is the whole structural prerequisite for replay. What you lose is the
replay itself. Reading the lesson tells you what you are missing and why the pattern
was worth adopting anyway.

There is no substitute for lessons 24–26. Vision and rigid-body physics need real
libraries.
