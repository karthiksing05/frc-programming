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

[Get started :material-rocket-launch:](setup/index.md){ .md-button .md-button--primary }
[See the lessons](learn/index.md){ .md-button }

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

    [:octicons-arrow-right-24: Start learning](learn/index.md)

-   :material-console:{ .lg .middle } __The tooling__

    ---

    `frcprog next`, `frcprog check`, `frcprog sim`. One command line that runs on
    the JDK WPILib already installed for you.

    [:octicons-arrow-right-24: Set up](setup/index.md)

-   :material-book-open-page-variant:{ .lg .middle } __The handbook__

    ---

    Reference material — WPILib classes, control theory, the patterns. Read it when
    you need it, not before.

    [:octicons-arrow-right-24: Open the handbook](handbook/index.md)

-   :material-robot:{ .lg .middle } __The reference robots__

    ---

    Two real competition robots, public and readable, that every lesson comes back
    to.

    [:octicons-arrow-right-24: Meet them](robots/index.md)

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

Stuck? Four hints, escalating. The answer is only in the last one.

Every lesson page here shows the same text plus the real source file and the real
rubric. Those are included straight from the project, so this site cannot drift
from what you are editing.

---

## Why offline

The alternative fails at exactly the wrong moment.

A first team meeting is thirty students on one school access point. A build-season
Saturday is a district captive portal blocking Maven Central. A competition pit has
no useful internet at all.

Everything this curriculum needs is already inside your WPILib install, and Gradle
runs offline for every build, including the ones VS Code's buttons fire. Five
lessons teach vendor libraries and need one online build each. They are marked.

[How the offline guarantee works :material-arrow-right:](setup/offline.md)

---

## What it does not teach

Worth knowing up front.

| Not covered | Why |
|---|---|
| Real hardware | Everything is simulation. Wiring a motor needs an actual robot and a mentor. |
| Git beyond three commands | Enough to never lose work. The [handbook](handbook/git.md) has the rest. |
| C++ and Python | WPILib supports both. Java is the FRC majority with the best library support. |
| Swerve before tank drive | Swerve first means four modules of kinematics before you have written a subsystem. It is Stage 2B. |
