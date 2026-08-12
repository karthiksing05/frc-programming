# Hints — Lesson 01

Work down. Stop as soon as you can carry on alone.

## Hint 1 — Where to start

You need to answer one question about `value`: *is it small?*

"Small" here means small in either direction, because a stick drifts both ways.
`-0.05` is just as much noise as `+0.05`. Java gives you `Math.abs(x)`, which turns
any number into its distance from zero: `Math.abs(-0.05)` is `0.05`.

So the question becomes: is `Math.abs(value)` less than `threshold`?

## Hint 2 — The shape of the answer

Two cases, one of each:

- Inside the band → hand back `0.0`
- Outside the band → hand back `value`, exactly as it arrived

An `if` and two `return` statements is all this needs. There is no loop, no
variable to keep, nothing to accumulate.

Watch the boundary: rubric check 5 wants a reading exactly *at* the threshold to
pass through. That means `<`, not `<=`.

## Hint 3 — Almost there

```java
public static double applyDeadband(double value, double threshold) {
    if ( /* is the magnitude of value below threshold? */ ) {
        return 0.0;
    }
    return value;
}
```

Only the condition is missing. It is one call to `Math.abs` and one `<`.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
public static double applyDeadband(double value, double threshold) {
    // Math.abs() collapses the two sign cases into one comparison, so this
    // handles a stick pushed either way without a second branch.
    if (Math.abs(value) < threshold) {
        return 0.0;
    }
    return value;
}
```

The ternary operator says the same thing in one line, if you prefer it:

```java
return Math.abs(value) < threshold ? 0.0 : value;
```

Both are fine. The `if` version is easier to read the first time and easier to
extend later, which is why the reference uses it.

**A version that looks right and is not:**

```java
if (value < threshold) {          // ✗ no Math.abs
    return 0.0;
}
```

This zeroes *everything below the threshold*, which includes every negative number
there is. Your robot would drive forward normally and refuse to reverse at all —
and it would pass a test that only ever tried positive inputs. That is why rubric
check 2 exists.

</details>
