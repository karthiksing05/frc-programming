# Hints — Lesson 02

## Hint 1 — Where to start

Search the file for `TODO (LESSON 02)`. There are two such comments, covering four
constants between them. The values you need are written in the comment right above
each one.

This lesson has no cleverness in it. If it is taking more than ten minutes, you are
probably overthinking it.

## Hint 2 — The shape of the answer

Each fix is one line. Change only what is to the right of the `=`:

```java
public static final double DEADBAND = 0.0;    // before
public static final double DEADBAND = 0.10;   // after
```

Do not rename anything, do not remove `final`, do not move the field to a different
class. The rubric checks the modifiers and the location as well as the value.

## Hint 3 — Almost there

If check 3 (`public static final`) is failing, something got dropped from a
declaration. All four should read exactly:

```java
public static final double NAME = value;
```

If check 4 (grouping) is failing, a constant ended up directly inside `Constants`
rather than inside one of its nested classes. Only genuinely robot-wide values —
`LOOP_PERIOD_SECONDS`, `NOMINAL_BATTERY_VOLTS` — belong at the top level.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

Inside `Constants.Drive`, delete the `TODO` block and leave:

```java
/** Joystick readings smaller than this are treated as zero. */
public static final double DEADBAND = 0.10;

/** Ceiling on commanded voltage, so a runaway PID can't ask for 40 volts. */
public static final double MAX_VOLTS = 12.0;

/** Motor rotations per wheel rotation. */
public static final double GEAR_RATIO = 8.45;
```

Inside `Constants.Flywheels`:

```java
/** Free speed of the shooter motor, in RPM. */
public static final double MAX_RPM = 5800.0;
```

Note that the Javadoc comment stayed. A constant's name says *what* it is; the
comment says *why that value*. `GEAR_RATIO = 8.45` is meaningless six months later
without "stock toughbox mini" written down somewhere.

</details>
