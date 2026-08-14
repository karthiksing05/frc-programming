# What FRC is, and where the code fits

If you have never been on a robotics team, most of this curriculum will assume things
you have no reason to know yet. This page and the two after it exist to fix that. No
programming here — just what the competition is, what the robot is made of, and what
all the software is for.

If you have been on a team for a season already, skim it and move on.

---

## The competition

FIRST Robotics Competition is a high-school robotics league. Every year in early
January, FIRST releases a brand-new game — new field, new scoring rules, new objects
to pick up and put somewhere. Every team in the world sees it for the first time on
the same Saturday morning. That day is called **kickoff**.

From kickoff you get a few weeks to design and build a robot that plays that game.
Then you compete: regional and district events through the spring, then championships.
The next January the game is thrown away and replaced with a different one.

That yearly reset matters more to a programmer than it sounds. The mechanisms change
every season, so code written around *this year's* mechanism is worth very little next
year. Code written around ideas — how to hold a position, how to run a sequence of
actions, how to know where you are on the field — carries over completely. This
curriculum spends most of its time on the second kind.

### A match

Robots do not play alone. Three teams form an **alliance**, red or blue, and the two
alliances play a two-and-a-half minute match against each other.

<figure class="r3d-fig">
<svg viewBox="-10 0 740 120" xmlns="http://www.w3.org/2000/svg" role="img"
     aria-label="A match timeline: 15 seconds autonomous, then teleoperated, ending with endgame."
     style="max-width:100%;height:auto">
  <style>
    .bar  { stroke: var(--md-default-fg-color--lightest); stroke-width: 1; }
    .lbl  { fill: var(--md-default-fg-color); font: 600 13px/1 var(--md-text-font-family, system-ui); }
    .sub  { fill: var(--md-default-fg-color--light); font: 400 11px/1 var(--md-text-font-family, system-ui); }
    .tick { fill: var(--md-default-fg-color--light); font: 400 11px/1 var(--md-code-font-family, monospace); }
  </style>
  <rect class="bar" x="10"  y="34" width="118" height="34" rx="4" fill="var(--md-primary-fg-color)" opacity="0.85"/>
  <rect class="bar" x="132" y="34" width="440" height="34" rx="4" fill="var(--md-default-fg-color--lightest)"/>
  <rect class="bar" x="576" y="34" width="134" height="34" rx="4" fill="var(--md-accent-fg-color)" opacity="0.55"/>
  <text class="lbl" x="69"  y="56" text-anchor="middle">Autonomous</text>
  <text class="lbl" x="352" y="56" text-anchor="middle">Teleoperated</text>
  <text class="lbl" x="643" y="56" text-anchor="middle">Endgame</text>
  <text class="sub" x="69"  y="88" text-anchor="middle">robot runs your code alone</text>
  <text class="sub" x="352" y="88" text-anchor="middle">drivers control the robot</text>
  <text class="sub" x="643" y="88" text-anchor="middle">last ~30 seconds</text>
  <text class="tick" x="10"  y="24">0:00</text>
  <text class="tick" x="132" y="24">0:15</text>
  <text class="tick" x="710" y="24" text-anchor="end">2:30</text>
</svg>
<figcaption>The match structure your robot code is written against.</figcaption>
</figure>

The first fifteen seconds are **autonomous**. Nobody is allowed to touch the controls.
Whatever the robot does in that window, it does because of code somebody wrote. If it
sits still, that is also because of code somebody wrote.

Then the drivers pick up controllers and play the rest of the match. This is
**teleoperated**, usually shortened to *teleop*. Code is still running the whole time —
it is reading the controller, deciding what each motor should do, and stopping the arm
from driving itself into the floor.

The last stretch is the **endgame**, which usually involves climbing something or
parking somewhere for extra points.

Those three periods show up directly in the code you will write. WPILib gives you a
robot class with `autonomousPeriodic()` and `teleopPeriodic()` methods, and they run
during exactly those parts of the match. That is not a coincidence or an analogy;
it is a literal mapping.

---

## Where programming sits on a team

A team is usually split into subteams, and they are far more connected than they look
from the outside.

**Mechanical** designs and builds the mechanisms — the drivetrain, the arm, the
intake. They decide the gear ratios, the range of motion, and how heavy things are.
Every one of those decisions becomes a number in your code. When they change a
gearbox, your gear ratio constant is wrong until somebody updates it.

**Electrical** wires the motors, sensors, and the control system. They decide which
motor plugs into which port. Those port numbers are in your code too, and a swapped
pair is a classic afternoon-long bug where the robot turns when you ask it to drive
straight.

**Programming** — you — writes the code that reads sensors and controllers and decides
what the motors do.

**Drive team** actually plays the matches, and they are the people who find out
first when your code does something surprising.

The thing worth internalising early is that a programmer cannot work in isolation.
Most of the numbers in your code came from somebody else's decision, and most of the
bugs you will chase are disagreements between what mechanical built, what electrical
wired, and what you assumed. Asking "which port is the intake motor on?" is not a
beginner question; it is the question.

---

## What this curriculum uses

We are not going to have a real robot in front of us, so everything here runs in a
**simulator** — a program that pretends to be a robot, complete with fake motors that
respond the way real ones do and fake sensors that report what a real one would report.

This is closer to real practice than it sounds. Real teams simulate constantly,
because the robot is usually in pieces on a cart, and because a mistake in simulation
costs nothing while a mistake on a real robot can strip a gearbox.

The robots the examples are drawn from are two real, publicly-documented machines:
[Presto](../robots/presto-crescendo-tour.md) from the 2024 game *Crescendo*, and
[Kelpie](../robots/kelpie-reefscape-tour.md) from the 2025 game *Reefscape*.

---

Next: what the robot is actually made of, and what all the parts are called.

[The robot, part by part :material-arrow-right:](robot.md){ .md-button .md-button--primary }
