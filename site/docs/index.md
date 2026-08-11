---
title: FRCProgramming
hide:
  - navigation
---

# FRCProgramming

*An FRC robot programming curriculum that runs on your laptop, with no network.*

Thirty-four lessons that teach robot programming using the tools a real team uses —
Java, WPILib, Gradle, JUnit, the WPILib simulator, and AdvantageScope. You write real
robot code in a real robot project, and a real test suite tells you whether it works.

No accounts. No server. No cloud service that can be down on the day of your meeting.

[Get started :material-rocket-launch:](setup/){ .md-button .md-button--primary }
[See the lessons](learn/){ .md-button }

---

## What you actually build

One project, growing lesson by lesson. The deadband method you write in lesson 01 is
called by the drivetrain you build in lesson 07 and is still there in the capstone.
Nothing restarts.

By lesson 15 you have a robot with five subsystems, a drivable teleop, two autonomous
routines, live telemetry, and a test suite that proves all of it.

<div class="grid cards" markdown>

-   :material-school:{ .lg .middle } __The lessons__

    ---

    Thirty-four, Stage 0 through Stage 2D. Sixteen are graded by a JUnit rubric;
    the rest are guided, because at some point somebody has to stop writing
    exercises for you.

    [:octicons-arrow-right-24: Start learning](learn/)

-   :material-console:{ .lg .middle } __The tooling__

    ---

    `frcprog next`, `frcprog check`, `frcprog sim`. One command line that runs on
    the JDK WPILib already installed for you.

    [:octicons-arrow-right-24: Set up](setup/)

-   :material-book-open-page-variant:{ .lg .middle } __The handbook__

    ---

    Reference material — WPILib classes, control theory, the patterns. Read it when
    you need it, not before.

    [:octicons-arrow-right-24: Open the handbook](handbook/)

-   :material-robot:{ .lg .middle } __The reference robots__

    ---

    Two real competition robots, public and readable, that every lesson comes back
    to.

    [:octicons-arrow-right-24: Meet them](robots/)

</div>

---

## How a lesson works

```bash
./tools/frcprog next                 # what to do, and which file
./tools/frcprog read 07-tank-drive   # the lesson
#   ... edit the file, find the TODO (LESSON 07) comment ...
./tools/frcprog check 07-tank-drive  # grade yourself
./tools/frcprog sim                  # watch it move
```

Stuck? Four hints, escalating, with the answer only in the last one.

Every lesson page on this site is the same text, with the real source file and the
real rubric alongside it. The site includes those files directly out of the project,
so it cannot drift from what you are editing.

---

## Why offline

Because the alternative fails at exactly the wrong moment.

A first team meeting is thirty students on one school access point. A build-season
Saturday is a district captive portal that blocks Maven Central. A competition pit
has no useful internet at all.

Every dependency this curriculum needs already lives inside your WPILib install, and
Gradle is configured to run offline for every build — including the ones VS Code's
WPILib buttons fire. Five lessons teach vendor libraries and need one online build
each; they are marked, and they say so.

[How the offline guarantee works :material-arrow-right:](setup/offline.md)

---

## What it does not teach

Worth knowing up front.

**No real hardware.** Everything is simulation. You will not wire a motor or flash a
roboRIO here — that needs an actual robot and a mentor, and mixing it in would have
doubled the install friction.

**No Git beyond three commands.** Enough to never lose work. Branching and pull
requests are real and useful and are not this.

**Java only.** WPILib supports C++ and Python; Java is the FRC majority and has the
best library support.

**Tank drive before swerve.** Every competitive team runs swerve, and starting there
would mean four modules of kinematics before you have written a subsystem. Swerve is
Stage 2B.
