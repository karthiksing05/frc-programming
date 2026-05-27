# Hints — Lesson 01

Use these in order. Each hint reveals a little more.

## Hint 1 — Where to start

Java's `Math.abs(x)` returns the **absolute value** of `x`. It works for
`double` and `int` and returns the same type.

```java
Math.abs(-0.5)   // → 0.5
Math.abs(0.03)   // → 0.03
```

You need to compare the absolute value of `value` to `threshold`.

## Hint 2 — The shape of the answer

You're returning a `double`. You have two cases:
- One case → return `0.0`
- Other case → return `value`

That's an `if`. Or, if you're feeling tidy, a ternary `?:`.

## Hint 3 — Almost there

```java
if ( ... what condition? ... ) {
    return 0.0;
}
return value;
```

The condition is: "is the magnitude of `value` smaller than the
threshold?"

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
public static double applyDeadband(double value, double threshold) {
    if (Math.abs(value) < threshold) {
        return 0.0;
    }
    return value;
}
```

Or, equivalently:

```java
public static double applyDeadband(double value, double threshold) {
    return Math.abs(value) < threshold ? 0.0 : value;
}
```

Both pass the rubric. The ternary version is a single line, but the
`if` version is easier to read out loud — pick whichever feels clearer
to you. WPILib's own `MathUtil.applyDeadband` is actually fancier (it
linearly scales the remaining range so there's no discontinuity at the
threshold) — we'll get to that in a later lesson.
</details>
