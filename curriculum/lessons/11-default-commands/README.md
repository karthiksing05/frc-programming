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
trigger that fires on either looks identical in casual testing and is badly wrong in
a match.

## How it works

### Every subsystem has an idle answer

The question "what does this mechanism do when nobody is asking?" has an answer
whether or not you write one down.

Without a default command the answer is "whatever it was doing when the last command
was cancelled". Usually zero, because most commands clean up. Occasionally 3000 RPM,
because a command was interrupted at an awkward moment.

`setDefaultCommand` makes you choose. The scheduler runs it whenever no other
command requires that subsystem, and interrupts it the moment one does.

??? info "Why .repeatedly()"

    `stopCommand()` is built with `runOnce`, so it finishes immediately.

    A default command that finishes gets restarted by the scheduler straight away,
    so it works, but you get a new command object scheduled and ended every single
    loop. `.repeatedly()` wraps it in something that never finishes, which is what a
    default command wants to be.

### Keep default commands trivial

If you find yourself writing an `if` inside one, stop.

```java
flywheels.setDefaultCommand(
    flywheels.run(() -> {
        if (roller.hasGamePiece()) { setTarget(3000); }
        else                       { setTarget(0);    }
    }));
```

Three concrete problems with that:

**It is invisible.** A default command does not appear in the scheduler's list of
running commands the way a scheduled one does, so logic hidden in it is logic you
will not think to look at.

**It is conditional on scheduling.** It only runs when nothing else claimed the
subsystem. So the same sensor state produces different behaviour depending on what
else happens to be scheduled, which is horrible to reason about.

**It does not compose.** Written as a trigger, that condition can be `.and`ed with
another one. Written as an `if` inside a default command, it cannot.

The same logic as a trigger is one line, sits next to every other cross-cutting
behaviour, and shows up in the command list when it fires.

### Trigger algebra

```java
a.and(b)          // both
a.or(b)           // either
a.negate()        // not
a.debounce(0.1)   // continuously true for 100 ms
```

Because they compose, conditions that would be a nest of `if`s spread across several
`periodic()` methods become one readable line in one file.

??? question "Predict: what does the ordering of .debounce() change?"

    ```java
    a.and(b).debounce(0.1)     // debounces the COMBINATION
    a.debounce(0.1).and(b)     // debounces only a, then ANDs
    ```

    The first requires both conditions to have been true **together** for 100 ms.

    The second requires `a` to have been true for 100 ms, then ANDs the result with
    `b` right now. A flickering `b` still fires the command.

    That is a real bug and it is nearly invisible in code review, because the two
    lines look almost identical. Put the debounce on the thing you actually want
    settled.

### What debounce is for

A beam-break at the edge of its threshold flickers: a game piece settling,
vibration, ambient light. Without debouncing, that flicker fires your scoring
sequence at random.

`.debounce(0.1)` ignores anything that has not been continuously true for 100 ms.
Real events last much longer than 100 ms. Noise does not.

## See it

```bash
./tools/frcprog sim
./tools/frcprog scope        # second terminal
```

Setup: **[Running the simulator](../../../setup/simulator.md)**.

Plot `Flywheels/TargetRPM`, and open **Hardware → DIO** in the simulator.

Work through the truth table by hand:

| DIO 4 | Right trigger | Expected |
|---|---|---|
| high (no piece) | released | nothing |
| low (piece) | released | nothing |
| high (no piece) | held | nothing |
| low (piece) | held | score fires after ~100 ms |

Then click DIO 4 rapidly on and off while holding the trigger. Nothing should fire.
That is the debounce doing its job, and it is the clearest way to see what it is for.

## Done

The rubric passes, and every subsystem now does something sensible when nobody is
asking it for anything in particular.

```bash
./tools/frcprog next
```

This composes further. Lesson 20 builds triggers out of three subsystems agreeing at
once:

```java
elevator.atGoalTrigger
    .and(shoulder.atGoalTrigger)
    .and(roller.hasGamePieceTrigger)
    .debounce(0.1)
```
