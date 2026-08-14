# The software, explained

Setting up an FRC project means installing several tools at once, and most guides —
including, until now, this one — just tell you to install them. That leaves you typing
commands that work for reasons you cannot see, which is a bad place to debug from.

So: what each piece is, why it exists, and what it does for you.

---

## The short version

<figure markdown="span">
<svg viewBox="0 0 760 300" xmlns="http://www.w3.org/2000/svg" role="img"
     aria-label="Your Java code uses WPILib; Gradle compiles it; it then either deploys to a roboRIO or runs in the simulator, which publishes data over NetworkTables to AdvantageScope."
     style="max-width:100%;height:auto">
  <style>
    .b   { fill: var(--md-code-bg-color); stroke: var(--md-default-fg-color--lighter); stroke-width: 1.5; }
    .bp  { fill: var(--md-primary-fg-color); opacity: 0.14; stroke: var(--md-primary-fg-color); stroke-width: 1.5; }
    .tt  { fill: var(--md-default-fg-color); font: 600 13px/1 var(--md-text-font-family, system-ui); }
    .ss  { fill: var(--md-default-fg-color--light); font: 400 11px/1 var(--md-text-font-family, system-ui); }
    .aa  { stroke: var(--md-default-fg-color--light); stroke-width: 1.6; fill: none; }
    .ee  { fill: var(--md-default-fg-color--light); font: 400 10px/1 var(--md-code-font-family, monospace); }
  </style>
  <defs>
    <marker id="b" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto">
      <path d="M0,0 L10,5 L0,10 z" fill="var(--md-default-fg-color--light)"/>
    </marker>
  </defs>

  <rect class="bp" x="8" y="118" width="120" height="56" rx="6"/>
  <text class="tt" x="68" y="141" text-anchor="middle">Your code</text>
  <text class="ss" x="68" y="159" text-anchor="middle">Java, + WPILib</text>

  <rect class="b" x="176" y="118" width="120" height="56" rx="6"/>
  <text class="tt" x="236" y="141" text-anchor="middle">Gradle</text>
  <text class="ss" x="236" y="159" text-anchor="middle">compiles it</text>

  <rect class="b" x="356" y="26" width="150" height="56" rx="6"/>
  <text class="tt" x="431" y="49" text-anchor="middle">roboRIO</text>
  <text class="ss" x="431" y="67" text-anchor="middle">the real robot</text>

  <rect class="b" x="356" y="118" width="150" height="56" rx="6"/>
  <text class="tt" x="431" y="141" text-anchor="middle">Simulator</text>
  <text class="ss" x="431" y="159" text-anchor="middle">a pretend robot</text>

  <rect class="b" x="356" y="216" width="150" height="56" rx="6"/>
  <text class="tt" x="431" y="239" text-anchor="middle">JUnit</text>
  <text class="ss" x="431" y="257" text-anchor="middle">grades your work</text>

  <rect class="b" x="570" y="118" width="180" height="56" rx="6"/>
  <text class="tt" x="660" y="141" text-anchor="middle">AdvantageScope</text>
  <text class="ss" x="660" y="159" text-anchor="middle">draws the graphs</text>

  <path class="aa" d="M128,146 H174" marker-end="url(#b)"/>
  <path class="aa" d="M296,146 V54 H354" marker-end="url(#b)"/>
  <path class="aa" d="M296,146 H354" marker-end="url(#b)"/>
  <path class="aa" d="M296,146 V244 H354" marker-end="url(#b)"/>
  <path class="aa" d="M506,146 H568" marker-end="url(#b)"/>
  <text class="ee" x="308" y="48">deploy</text>
  <text class="ee" x="516" y="138">NetworkTables</text>
</svg>
<figcaption>You only ever write the leftmost box. Everything else is a tool that runs it or watches it.</figcaption>
</figure>

---

## Java

The programming language. FRC also supports C++ and Python, but Java is what most
teams use and what this curriculum is written in.

Java is *compiled*, which means the text you write has to be translated into a form
the robot can run before anything happens. That translation step is where you find out
about typos, and it is why you will spend time reading compiler errors before you ever
see a motor move. This is a feature — a compiler catching a mistake takes seconds,
whereas the same mistake caught by a robot driving into a wall takes an afternoon.

## WPILib

A **library** is a pile of code somebody else wrote that you use instead of writing it
yourself. WPILib is the library FIRST publishes for FRC, and it is enormous.

What is in it, roughly:

- Classes representing hardware, so `new PWMSparkMax(0)` gives you an object that
  controls the motor controller in port 0. You do not write the electrical protocol.
- The maths teams need over and over — PID controllers, trajectory following,
  kinematics, filters.
- The **command-based framework**, a structure for organising robot behaviour that
  Stage 1C is entirely about.
- The simulation layer that lets all of the above run on your laptop with no robot.

When you see `import edu.wpi.first.*` at the top of a file, that is WPILib.

Its documentation lives at [docs.wpilib.org](https://docs.wpilib.org), and it is
genuinely good. This curriculum is not a replacement for it.

## Gradle, and `gradlew`

**Gradle** is a *build tool*. Its job is to take your source files, work out what
depends on what, fetch the libraries you asked for, compile everything in the right
order, and produce something runnable. On a project with three files you could do this
by hand. On a project with forty files and half a dozen libraries you could not.

**GradleRIO** is the WPILib plugin for Gradle. It is what teaches Gradle the
FRC-specific parts: where WPILib lives, how to build for a roboRIO's processor, and how
to deploy over the network.

`gradlew` is the **Gradle wrapper** — a small script sitting in the project folder.
Running `./gradlew build` instead of `gradle build` matters more than it looks:

```bash
./gradlew build     # uses the Gradle version this project pins, with the right Java
gradle build        # uses whatever Gradle happens to be installed, if any
```

The wrapper means everybody on your team builds with identical tooling, and a
project from three years ago still builds today. If you take one habit from this
section, make it typing the `./` and the `w`.

??? question "Why does the first build take minutes and later ones take seconds?"

    The first run unpacks Gradle itself and reads every library in the project.
    After that it caches all of it and only recompiles files you actually changed.

    If a later build is slow again, something invalidated the cache — usually
    antivirus scanning the cache folder, or a `clean` that threw it away on purpose.

## JUnit

A **testing framework**. A test is just a small program that runs your code with a
known input and checks the answer. JUnit is the standard one for Java, and it is what
grades every lesson here.

The idea, stripped down:

```java
@Test
void aSmallReadingShouldBeIgnored() {
  // give the method a joystick reading of 0.05
  double result = MathUtils.applyDeadband(0.05, 0.1);

  // and insist the answer is 0.0
  assertEquals(0.0, result);
}
```

If `applyDeadband` returns `0.05`, the test fails and prints why. That is the whole
mechanism. When this curriculum says a rubric checks your work, it means a file full
of assertions like that one runs against the code you just wrote.

Real teams write these for the same reason: it is the only way to know that today's
change did not break last week's work.

## The Simulation GUI

Also called the **HAL simulator** or just *the sim*. It is a desktop program that
pretends to be a roboRIO. Your real robot code runs inside it, unmodified, and instead
of talking to real motors it talks to fake ones whose behaviour is modelled in
software.

It gives you a window with the robot's inputs and outputs laid out — motor outputs,
sensor values, a fake driver station you can enable, and a keyboard you can map to a
joystick. You can watch a value change while you hold a key.

This is what `./tools/frcprog sim` launches, and
[Running the simulator](../setup/simulator.md) is the full tour.

## NetworkTables

The way robot code publishes data. Think of it as a shared noticeboard: the robot pins
up named values — `Drive/LeftSpeed`, `Elevator/Height` — fifty times a second, and any
program on the network can read them.

This is how a dashboard shows you live numbers without being part of your robot code.
It is also how AdvantageScope gets its data.

## AdvantageScope

A **viewer**. It connects to NetworkTables, reads everything the robot is publishing,
and draws it — line graphs over time, a field view showing where the robot thinks it
is, a 3D model.

The thing to be clear about, because the name suggests otherwise: **AdvantageScope
does not simulate anything.** It cannot run your code and it cannot make the robot
move. It watches. The simulator does the simulating; AdvantageScope shows you what
happened.

The reason you want it is that a number scrolling past in a terminal tells you almost
nothing about whether a control loop is behaving, while the same data as a line on a
graph tells you immediately. When Stage 1B has you tuning an elevator, the graph is
how you will see the overshoot.

## Git

Version control — a tool that records the history of your project so you can see what
changed, go back to a version that worked, and merge work from several people.

It is not required for the early lessons, and this curriculum defers it deliberately so
you are not learning two unfamiliar things at once. When you are ready, the
[Git handbook page](../handbook/git.md) covers it from nothing.

---

## Putting it together

When you run `./tools/frcprog check 01-methods`, this is the actual sequence:

1. `frcprog` asks **Gradle** to run the tests tagged for lesson 01.
2. Gradle compiles your code against **WPILib**, using the **Java** compiler.
3. **JUnit** runs the rubric's assertions against what you wrote.
4. `frcprog` reads the results and prints them as advice.

And when you run `./tools/frcprog sim`:

1. Gradle compiles the same code, then starts it in the **Simulation GUI**.
2. Your robot code runs its fifty-times-a-second loop against simulated hardware.
3. It publishes values over **NetworkTables**.
4. **AdvantageScope**, if you have it open, draws them.

Nothing in either list touches the internet, which is the point of the whole setup.

---

Now you have the vocabulary. Time to install it.

[Set up your laptop :material-arrow-right:](../setup/index.md){ .md-button .md-button--primary }
