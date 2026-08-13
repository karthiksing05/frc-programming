# Lesson 11 — Default commands and trigger logic

**Stage 1D · 35 min · Needs: 10**

Your scoring sequence ends. What are the flywheels doing now?

## Do this

**1. Idle behaviour** in `configureDefaultCommands()`:

```java
flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
```

**2. A composed trigger** in `configureBindings()`:

```java
roller
    .hasGamePieceTrigger
    .and(operator.rightTrigger())
    .debounce(0.1)
    .onTrue(scoreCommand());
```

Read it: when we have a game piece **and** the operator asks, score.

## Check it

```bash
./tools/frcprog check 11-default-commands
```

Five checks. Numbers 3 and 4 prove each condition alone does nothing, because a
trigger that fires on either looks identical in casual testing and is badly wrong
in a match.

## Why

**Every subsystem has an answer to "what do you do when nobody is asking?"** The
only question is whether you chose it. Without a default command the answer is
"whatever it was doing when the last command got cancelled".

**Keep default commands trivial.** If you write an `if` inside one, stop. You found
a condition, and conditions belong in triggers. Three reasons:

- a default command does not show in the scheduler's list, so logic hidden there is
  invisible while debugging
- it only runs when nothing else claimed the subsystem, so the same condition
  behaves differently depending on what else is scheduled
- as a trigger it is one line, visible next to every other cross-cutting behaviour

**`.debounce(0.1)`** means "only true once it has been continuously true for
100 ms". Beam-breaks flicker at their threshold. Real events last much longer than
100 ms; noise does not.

**Ordering matters.** `a.and(b).debounce(0.1)` debounces the combination.
`a.debounce(0.1).and(b)` debounces only `a`, so a flickering `b` still fires. That
is a real bug and nearly invisible in review.

## See it

```bash
./tools/frcprog sim
```

Toggle **DIO 4** in the simulator (the beam-break) while watching
`Flywheels/TargetRPM`. Try each condition alone, then both. Toggle it rapidly and
watch nothing happen: that is the debounce.

## Done

Rubric is green.

```bash
./tools/frcprog next
```

**Triggers compose:** `.and()`, `.or()`, `.negate()`, `.debounce(t)`. Conditions
that would be a nest of `if`s across several `periodic()` methods become one line.
