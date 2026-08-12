# Hints — Lesson 03

## Hint 1 — Where to start

Before any `if`, give the sensor a name that reads the way you think:

```java
boolean hasGamePiece = !beamBreak.get();
```

Then work out the order of your branches. Which condition must win when two are
true at once? That one goes first.

## Hint 2 — The shape of the answer

Three branches and a final `else`:

```java
if ( /* eject? */ ) {
    rollerMotor.set( /* eject speed */ );
} else if ( /* intake, and not already loaded? */ ) {
    rollerMotor.set( /* intake speed */ );
} else {
    rollerMotor.set(0.0);
}
```

The final `else` is not optional. Without it, whenever no branch matches, the motor
simply keeps whatever it was last told — forever. Rubric check 4 exists precisely
to catch that.

## Hint 3 — Almost there

The middle condition has two parts, joined by "and":

- the B button is held, **and**
- we do not already have a game piece

In Java that is `&&`:

```java
} else if (operator.getBButton() && !hasGamePiece) {
```

And use the constants from lesson 02, not the literals: `Constants.Roller.INTAKE_SPEED`
and `Constants.Roller.EJECT_SPEED`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
@Override
public void teleopPeriodic() {
    // The beam-break pulls high when nothing interrupts it, so a broken beam —
    // a game piece sitting in the throat — reads false.
    boolean hasGamePiece = !beamBreak.get();

    if (operator.getXButton()) {
        // Eject wins over everything. Checking it first is what makes it win.
        rollerMotor.set(Constants.Roller.EJECT_SPEED);
    } else if (operator.getBButton() && !hasGamePiece) {
        rollerMotor.set(Constants.Roller.INTAKE_SPEED);
    } else {
        rollerMotor.set(0.0);
    }
}
```

**Two ways to get this subtly wrong.**

Checking B first:

```java
if (operator.getBButton() && !hasGamePiece) { ... }
else if (operator.getXButton()) { ... }        // ✗ X now loses to B
```

Holding both buttons runs the intake instead of the eject. The operator, trying to
spit out a jammed piece, gets the opposite of what they asked for.

Forgetting the `else`:

```java
if (operator.getXButton()) { rollerMotor.set(-0.6); }
else if (operator.getBButton() && !hasGamePiece) { rollerMotor.set(0.6); }
// ✗ nothing here
```

Release everything and the roller keeps running at whatever it was last set to.
This is the single most common bug in first-week FRC code.

</details>
