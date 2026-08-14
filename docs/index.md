---
title: FRCProgramming
hide:
  - navigation
---

# FRCProgramming

*An FRC robot programming curriculum that runs on your laptop, with no network.*

Thirty-four lessons that teach you to program a competition robot, using the same
tools a real FRC team uses. You write real robot code in a real robot project, and it
runs in a simulator on your own laptop — no robot required, and nothing to download
once you are set up.

If you have never touched robot code before, start with **Start here**. It explains
what the competition is, what a robot is made of, and what every tool in the stack
actually does, before asking you to install anything.

[Start here :material-rocket-launch:](orientation/index.md){ .md-button .md-button--primary }
[I know all that, set me up](setup/index.md){ .md-button }
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
