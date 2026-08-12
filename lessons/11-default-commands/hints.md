# Hints — Lesson 11

## Hint 1 — Where to start

Two TODOs. The first is in `configureDefaultCommands()` and is one line. The second
is in `configureBindings()` and is the composed trigger.

Do the default command first and run the check — getting checks 1 and 2 green
narrows down where any remaining problem is.

## Hint 2 — The shape of the answer

Default command:

```java
flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
```

`.repeatedly()` matters. `stopCommand()` is built with `runOnce`, which finishes
immediately — and a default command that finishes is immediately restarted by the
scheduler, which works but churns. `.repeatedly()` makes it a command that simply
never ends, which is what a default command wants to be.

Composed trigger:

```java
roller.hasGamePieceTrigger
    .and( /* the operator's right trigger */ )
    .debounce(0.1)
    .onTrue(scoreCommand());
```

`roller.hasGamePieceTrigger` already exists on the subsystem. The operator's right
trigger is `operator.rightTrigger()`.

## Hint 3 — Almost there

If check 3 or 4 fails — one condition alone fires the sequence — check that you used
`.and()` and not `.or()`, and that you have not accidentally bound `scoreCommand()`
to something else as well. Two bindings for the same command is a common way to get
this symptom.

If check 5 fails at the *start* (it fires immediately), the `.debounce(0.1)` is
missing or is in the wrong position — it has to come after the `.and()`, so it
debounces the combined condition rather than one input.

If check 2 fails, the flywheels' default command is missing or is not `.repeatedly()`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

In `configureDefaultCommands()`:

```java
// Trivial on purpose. A default command with an `if` in it is a decision
// that wanted to be a trigger.
flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
```

In `configureBindings()`:

```java
// "When we have a game piece AND the operator pulls the right trigger, score."
// One line, in the vocabulary of the problem, with no if-statement anywhere
// and no subsystem asking another subsystem a question.
//
// .debounce(0.1) ignores anything that has not been continuously true for
// 100 ms — the standard cure for a beam-break that chatters at its threshold.
roller
    .hasGamePieceTrigger
    .and(operator.rightTrigger())
    .debounce(0.1)
    .onTrue(scoreCommand());
```

**Note the ordering of the fluent calls.** `.debounce()` applies to the trigger it is
called on, so:

- `a.and(b).debounce(0.1)` debounces the *combination* — both must have been true
  together for 100 ms. This is what you want.
- `a.debounce(0.1).and(b)` debounces only `a` and then ANDs the result — so a
  flickering `b` still fires the command.

The second one is a real bug and it is nearly invisible in review.

</details>
