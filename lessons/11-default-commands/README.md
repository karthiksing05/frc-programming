# Lesson 11 — Default commands done right

> **Stage 1D · ~35 minutes · Prerequisite: 10**

Your scoring sequence finishes. The flywheels are no longer being commanded by
anything.

So what are they doing?

Whatever they were doing when the sequence ended. Which is *usually* fine, and
occasionally is a shooter that spends the rest of the match at 3000 RPM because a
command was interrupted at an awkward moment. Every subsystem has an answer to "what
do you do when nobody is asking?" — the only question is whether you chose it.

## What you'll learn

1. Give every subsystem an explicit idle behaviour.
2. Keep default commands trivial, and know why that matters.
3. Compose triggers with `.and()`, `.or()`, `.negate()`.
4. Use `.debounce()` on a sensor that chatters.

## What you'll do

### 1. An idle behaviour for the flywheels

```java
flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
```

The drivetrain already has one from lesson 07.

### 2. A composed trigger

```java
roller
    .hasGamePieceTrigger
    .and(operator.rightTrigger())
    .debounce(0.1)
    .onTrue(scoreCommand());
```

Read it: *"when we have a game piece **and** the operator pulls the right trigger,
score."*

One line. In the vocabulary of the problem. No `if` statement anywhere, and no
subsystem asking another subsystem a question.

### Default commands must be trivial

A default command should be a single unconditional behaviour: coast, hold position,
follow the sticks. If you find yourself writing this:

```java
flywheels.setDefaultCommand(
    flywheels.run(() -> {
        if (roller.hasGamePiece()) {          // ✗
            flywheels.setTarget(3000);
        } else {
            flywheels.setTarget(0);
        }
    }));
```

stop. You have discovered a *condition*, and conditions belong in triggers.

Three concrete reasons this matters:

- A default command is not shown in the scheduler's list of "what is running", so
  decision logic hidden inside one is invisible when you are debugging.
- It runs only when nothing else has claimed the subsystem, so the same condition
  produces different behaviour depending on what else is scheduled — which is a
  genuinely horrible bug to reason about.
- Written as a trigger, the same logic is one line, is visible in `RobotContainer`
  alongside every other cross-cutting behaviour, and composes with other conditions.

### What `.debounce(0.1)` buys you

A beam-break at the edge of its threshold flickers — a game piece settling, a
vibration, ambient light. Without debouncing, that flicker fires your scoring
sequence at random.

`.debounce(0.1)` means "only report true once this has been continuously true for
100 ms". Real events last much longer than 100 ms; noise does not.

Rubric check 5 verifies the sequence does *not* fire in the first couple of loops,
then does once the debounce settles.

## Run it

```bash
./tools/frcprog check 11-default-commands
```

Five checks:

1. The drivetrain and the flywheels both have a default command.
2. Idle really is idle — nothing creeps with no buttons held.
3. A game piece alone does not score.
4. A button press alone does not score.
5. Both together do, after the debounce settles.

Checks 3 and 4 are the interesting pair. `.and()` means AND, and it is worth proving
each half separately, because a trigger that fires on either condition looks
identical in casual testing and is catastrophically wrong in a match.

## See it

```bash
./tools/frcprog sim
```

In the simulator, find the **DIO** panel and toggle channel 4 — that is the
beam-break. In AdvantageScope, watch `Flywheels/TargetRPM` while you toggle the
sensor and hold the trigger separately, then together.

Toggle the sensor rapidly and watch nothing happen. That is the debounce.

## Done?

```bash
./tools/frcprog next
```

## Triggers compose

`Trigger` has a small algebra, and it repays learning:

```java
a.and(b)              // both
a.or(b)               // either
a.negate()            // not
a.debounce(0.1)       // continuously true for 100 ms
```

Because they compose, conditions that would be a nest of `if` statements spread
across several `periodic()` methods become one readable line in one file:

```java
elevator.atGoalTrigger
    .and(shoulder.atGoalTrigger)
    .and(roller.hasGamePieceTrigger)
    .debounce(0.1)
    .onTrue(scoreCommand());
```

*"When the elevator and the arm are both in position and we are holding a game
piece, score."* That is lesson 20's territory, and this is where it starts.
