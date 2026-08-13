# Reefscape Tour — Meet Kelpie

*Team 8033 Highlander Robotics's 2025 Reefscape robot — the pick-and-place reference that anchors every Stage 1B mechanism lesson.*

<figure markdown="span">
<svg viewBox="0 0 780 320" role="img" aria-label="Kelpie picks up coral through a funnel into a roller, then positions it with a chain of three joints — elevator, shoulder and wrist — all of which must be in position at once before the roller can place the piece on the reef." style="max-width:100%;height:auto;color:inherit">
  <defs>
    <marker id="ka" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="currentColor"/>
    </marker>
  </defs>

  <!-- the positioning chain -->
  <text x="30" y="34" font-size="10.5" fill="currentColor" opacity="0.7" letter-spacing="1.2">POSITIONING CHAIN</text>

  <rect x="30"  y="48" width="130" height="54" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <text x="95"  y="72" text-anchor="middle" font-size="13" fill="currentColor" font-weight="600">Elevator</text>
  <text x="95"  y="90" text-anchor="middle" font-size="11" fill="currentColor" opacity="0.7">height</text>

  <rect x="215" y="48" width="130" height="54" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <text x="280" y="72" text-anchor="middle" font-size="13" fill="currentColor" font-weight="600">Shoulder</text>
  <text x="280" y="90" text-anchor="middle" font-size="11" fill="currentColor" opacity="0.7">reach</text>

  <rect x="400" y="48" width="130" height="54" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <text x="465" y="72" text-anchor="middle" font-size="13" fill="currentColor" font-weight="600">Wrist</text>
  <text x="465" y="90" text-anchor="middle" font-size="11" fill="currentColor" opacity="0.7">orientation</text>

  <rect x="585" y="48" width="130" height="54" rx="3" fill="#C8791A" fill-opacity="0.12" stroke="#C8791A" stroke-width="2"/>
  <text x="650" y="72" text-anchor="middle" font-size="13" fill="currentColor" font-weight="600">Roller</text>
  <text x="650" y="90" text-anchor="middle" font-size="11" fill="currentColor" opacity="0.7">grip / release</text>

  <line x1="160" y1="75" x2="208" y2="75" stroke="currentColor" stroke-width="1.5" marker-end="url(#ka)"/>
  <line x1="345" y1="75" x2="393" y2="75" stroke="currentColor" stroke-width="1.5" marker-end="url(#ka)"/>
  <line x1="530" y1="75" x2="578" y2="75" stroke="currentColor" stroke-width="1.5" marker-end="url(#ka)"/>
  <text x="184" y="66" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">carries</text>
  <text x="369" y="66" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">carries</text>
  <text x="554" y="66" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">carries</text>

  <!-- the simultaneity constraint, which is the whole point -->
  <path d="M 30 120 L 30 132 L 530 132 L 530 120" fill="none" stroke="#C8791A" stroke-width="1.5"/>
  <line x1="280" y1="132" x2="280" y2="150" stroke="#C8791A" stroke-width="1.5" marker-end="url(#ka)"/>
  <text x="280" y="167" text-anchor="middle" font-size="12" fill="currentColor" font-weight="600">all three must be in position at the same time</text>
  <text x="280" y="185" text-anchor="middle" font-size="11" fill="currentColor" opacity="0.7">a composite Trigger — lesson 20</text>

  <!-- the game-piece path -->
  <text x="30" y="228" font-size="10.5" fill="currentColor" opacity="0.7" letter-spacing="1.2">GAME PIECE PATH</text>

  <rect x="30"  y="242" width="130" height="50" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <text x="95"  y="272" text-anchor="middle" font-size="12.5" fill="currentColor">Funnel</text>

  <rect x="215" y="242" width="130" height="50" rx="3" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <text x="280" y="272" text-anchor="middle" font-size="12.5" fill="currentColor">Roller</text>

  <rect x="400" y="242" width="130" height="50" rx="3" fill="none" stroke="currentColor" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="465" y="272" text-anchor="middle" font-size="12.5" fill="currentColor">Reef, 4 levels</text>

  <line x1="160" y1="267" x2="208" y2="267" stroke="currentColor" stroke-width="1.5" marker-end="url(#ka)"/>
  <line x1="345" y1="267" x2="393" y2="267" stroke="currentColor" stroke-width="1.5" marker-end="url(#ka)"/>
  <text x="184" y="258" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">coral in</text>
  <text x="369" y="258" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">placed</text>

  <line x1="650" y1="102" x2="650" y2="242" stroke="currentColor" stroke-width="1.5" stroke-dasharray="4 3"/>
  <line x1="650" y1="242" x2="352" y2="267" stroke="currentColor" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#ka)"/>
  <text x="700" y="180" text-anchor="middle" font-size="10.5" fill="currentColor" opacity="0.75">same roller</text>
</svg>
<figcaption>Kelpie is a <strong>pick-and-place</strong> robot, and the difference from Presto is structural rather than cosmetic. A shooter needs one number right. This needs three joints to agree before the gripper opens — which is exactly why Stage 1B gives the elevator and the arm a lesson each, and why lesson 20 exists at all.</figcaption>
</figure>

---

## The game: Reefscape 2025

Reefscape was FRC's 2025 season — a pick-and-place game built around two field elements. Robots picked up **coral** (white PVC-pipe-like tubes) from human-player intake stations and placed them on a multi-level **reef** structure for points. They also picked up **algae** (soft foam-rubber balls) and scored them in side **processors** or shot them into the **net**. At the end of the match, robots could climb onto a **cage** for an endgame bonus.

That mechanism stack — drive somewhere, grab a game piece, lift it to height, orient it, release — is the canonical "pick-and-place" robot. It's also exactly the mechanism stack the curriculum spends most of Stage 1B and Stage 2A teaching you to build, one subsystem at a time.

![Kelpie on the Reefscape field](https://placeholder.frcprogramming.org/kelpie-hero.jpg)
*Team 8033's Kelpie at the 2025 season. Photo placeholder — to be swapped with a real championship-event photo.*

---

## Mechanism inventory

Each subsystem maps to a folder under [`src/main/java/frc/robot/subsystems/`](https://github.com/HighlanderRobotics/Reefscape/tree/main/src/main/java/frc/robot/subsystems). The "Curriculum lessons" column is where you'll meet each one in the curriculum.

| Subsystem | Files (in `subsystems/`) | What it does | Curriculum lessons |
|---|---|---|---|
| **Swerve drive** | `swerve/{ModuleIO, ModuleIOReal, ModuleIOSim, ModuleIOMapleSim, GyroIO, GyroIOPigeon2, GyroIOSim, OdometryThreadIO, SwerveSubsystem}` | 4-module MK4i swerve with Kraken X60 drive + Falcon turning | Stage 1C drivetrain intro · Stage 2B swerve deep-dive |
| **Elevator** | `elevator/{ElevatorIO, ElevatorIOReal, ElevatorIOSim, ElevatorSubsystem}` | Vertical extension carrying the shoulder + wrist + roller end effector | Lesson 04 · Lesson 05 (PID) · Lesson 16 (IO refactor) |
| **Shoulder** | `shoulder/` | Pivots the end-effector forward and back | Lesson 06 — the canonical `kG · cos(angle)` example |
| **Wrist** | `wrist/` | Rotates the gripper at the end of the shoulder | Lesson 06 extension — second-order arm |
| **Roller** | `roller/` | Coral/algae intake rollers | Lesson 04 — state-machine subsystem |
| **Climber** | `climber/` | End-of-match cage climber | Stage 2 capstone |
| **Funnel · Manipulator · Superstructure** | `FunnelSubsystem.java`, `ManipulatorSubsystem.java`, `Superstructure.java` | Top-level coordination of the end-effector stack | Stage 1D composition · Stage 2A multi-subsystem state |
| **Vision** | `camera/` | Multi-camera PhotonVision pose estimation | Stage 2C vision |
| **LEDs · beam-break · servo** | `led/`, `beambreak/`, `servo/` | Driver feedback + sensors | Stage 2A telemetry + sensors |

---

## What's interesting about this robot

Three things make Kelpie our pick-and-place reference instead of the dozen other public 2025 codebases.

**The IO Layer naming is exactly what the curriculum teaches.** Kelpie's files are named `ElevatorIOReal.java` and `ElevatorIOSim.java` — *not* vendor-specific names like `ElevatorIOTalonFX.java`. This mirrors the abstraction we teach: "hardware vs sim, not Talon vs Spark." When a Stage 1B student reads Kelpie's elevator package, the file names map one-to-one onto the concepts they just learned. That alignment is rarer than you'd think — most teams pick one convention or the other and move on; Kelpie happens to pick the one that makes a better teaching example.

**maple-sim is integrated for the swerve drive.** The vendordep `maple-sim.json` ships in the repo, and `ModuleIOMapleSim.java` sits next to `ModuleIOSim.java`. [maple-sim](https://github.com/Shenzhen-Robotics-Alliance/maple-sim) provides *physics-accurate* swerve simulation (not just kinematic) — the simulated robot has mass, friction, and can actually push around game pieces and other robots in sim. No other shortlist candidate had maple-sim integrated. This is the difference between "the auto plan looks right on paper" and "the auto plan survives contact with another bot."

**Mechanism granularity is ideal for teaching.** Separate `shoulder/`, `wrist/`, `elevator/`, and `roller/` packages mean each can be the focus of one lesson without the others getting in the way. Many teams bundle "arm + intake + claw" into one giant 800-line subsystem class. That's fine for competing; it's a pedagogical disaster. Kelpie's grain matches the lesson grain.

---

## Code links — where to look first

When a lesson tells you to "compare your code to Kelpie's," these are the files it's pointing at. Pin these tabs.

- **The IO interface that everything else hangs off of:** [`elevator/ElevatorIO.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/elevator/ElevatorIO.java) — about 30 lines. An `@AutoLog`-annotated inputs struct plus a handful of `set*` methods.
- **The subsystem that consumes that interface:** [`shoulder/ShoulderSubsystem.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java) — the canonical `kG · cos(angle)` arm.
- **The maple-sim integration that makes Stage 2B special:** [`swerve/ModuleIOMapleSim.java`](https://github.com/HighlanderRobotics/Reefscape/blob/main/src/main/java/frc/robot/subsystems/swerve/ModuleIOMapleSim.java).

---

## See this robot in action

Watch a five-minute clip of 8033 placing coral on the reef — every motion you see traces back to a subsystem in the table above.

[Watch Kelpie at 2025 Champs :material-youtube:](https://www.youtube.com/watch?v=PLACEHOLDER_KELPIE_REVEAL){ .md-button }

*(YouTube placeholder — to be replaced with the actual reveal/match URL.)*

---

## Companion training resources

!!! info "Highlanders-Training"

    Team 8033 also maintains a free, public [Highlanders-Training](https://github.com/HighlanderRobotics/Highlanders-Training) repository covering WPILib → command-based → AdvantageKit → controls → swerve → Choreo → PhotonVision. When Kelpie's production code feels too dense, drop into Highlanders-Training, read the warm-up version, and come back stronger. This is a huge teaching asset — and it's why we picked 8033 in the first place.

---

!!! warning "License caveat"

    Kelpie's repo currently contains the WPILib boilerplate license (`WPILib-License.md`, BSD-3) but **no team-specific `LICENSE` file** — GitHub's license detector reports "Other." The WPILib boilerplate covers WPILib code shipped with the project, not the team's own code. The curriculum has reached out to Team 8033 for explicit written permission to reference the repo as a teaching resource; given they publish a public training repo, they're expected to consent, but until that's documented in writing we treat the license as **ambiguous**. Quote sparingly, always link back to the repo, and never re-host more than a few lines verbatim.