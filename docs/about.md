# About

An FRC robot programming curriculum that runs entirely on a laptop.

## What it is

Thirty-four lessons that teach robot programming using the tools a real team uses:
Java, WPILib, Gradle, JUnit, the WPILib simulator, and AdvantageScope. Students write
real robot code in a real robot project, and a real test suite tells them whether it
works.

Sixteen lessons ship a JUnit rubric that grades the work locally. The rest are guided —
a clear goal, working code to model, and the simulator as the check.

## The offline decision

This is a deliberately offline curriculum, and that shaped almost every other choice.

The reason is not ideology. It is that a first team meeting is thirty students on one
school access point, a build-season Saturday is a district captive portal blocking
Maven Central, and a competition pit has no useful internet at all. A toolchain that
reaches out on every build is a toolchain that fails in exactly those moments.

So: every dependency comes from the Maven repository inside the WPILib install,
Gradle is forced offline for every invocation, grading runs locally, progress lives in
a JSON file in the project folder, and this site is served from `localhost`.

There are no accounts, no telemetry, no server, and nothing to sign into.

Five lessons teach vendor libraries — AdvantageKit, PathPlanner or Choreo,
PhotonVision, maple-sim — and need one online build each. They are marked, and they
say why. [Details](setup/offline.md).

## The pedagogy

Three ideas, borrowed and credited.

**Pain before abstraction.** From
[Katie Niwiden's work on teaching FRC programming](https://docs.google.com/presentation/d/15O2Xo5cHsYG3hVvQbMSB2SuvU9ED0Y3feaKdCbgaQyM/preview).
Lesson 03 makes students write a messy `teleopPeriodic` on purpose, so that lesson
04's subsystem arrives as relief rather than as ceremony. An abstraction handed over
before the problem is felt is just a rule to memorise.

**Factories, triggers, bindings.** From
[Oblarg's command-based best practices](https://www.chiefdelphi.com/t/command-based-best-practices-for-2025-community-feedback/465602),
distilled by [BoVLB](https://bovlb.github.io/frc-tips/commands/best-practices.html):
control subsystems with command factories, get information out with triggers, and
coordinate between them by binding commands to triggers. Every lesson from 07 onward
enforces those three.

**The IO Layer.** From [AdvantageKit](https://docs.advantagekit.org/) and Team 6328.
Introduced in Stage 2A rather than Stage 1, because without subsystems and commands
already in hand it looks like paperwork.

## The reference robots

Two real competition robots, public and readable, that the lessons keep returning to.

**[Presto](robots/presto-crescendo-tour.md)** — Team 6328 Mechanical Advantage,
Crescendo 2024. MIT licensed. Written by AdvantageKit's authors, so it is definitionally
the canonical reference for that pattern.

**[Kelpie](robots/kelpie-reefscape-tour.md)** — Team 8033 Highlander Robotics,
Reefscape 2025. Cleanly separated elevator, shoulder, wrist and roller subsystems that
map one-to-one onto Stage 1 lessons, plus a public training repository alongside it.

Using the same two throughout means that by Stage 1C "Kelpie's elevator" is something
a student already knows, and the only new thing in a lesson is the idea it is about.

## What it deliberately does not do

**No real hardware.** Everything is simulation. Nobody wires a motor or flashes a
roboRIO here. That is a real gap, it needs an actual robot and a mentor, and mixing it
in would have doubled the install friction.

**No Git beyond three commands.** Enough to never lose work. Branching and pull
requests are real and useful and are not this.

**Java only.** WPILib supports C++ and Python; Java is the FRC majority and has the
best library support. A parallel Python track would be a legitimate separate project.

**Tank drive before swerve.** Every competitive team runs swerve. Starting there would
mean four modules of kinematics before a student has written a subsystem.

## Credit where it is due

The three-pillar structure — course, handbook, reference examples — is taken wholesale
from [FRCDesign.org](https://frcdesign.org), which does the same job for mechanical
design and does it well.

WPILib, AdvantageKit, AdvantageScope, PhotonVision, PathPlanner, Choreo and maple-sim
are all other people's work, freely given. This curriculum is a way of learning to use
them.
