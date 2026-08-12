# Hints — Lesson 09

## Hint 1 — Where to start

Write the sentence in English first, then translate it phrase by phrase:

> spin the flywheels up — **and alongside that** — wait until they are ready — **and
> then** — run the roller for 0.4 s — **and cap the whole thing** at 1.5 s

"and alongside that" is `.alongWith`. "and then" is `.andThen`. "cap at" is
`.withTimeout`.

## Hint 2 — The shape of the answer

```java
return flywheels
    .spinUpCommand()
    .alongWith( /* the wait-then-feed part */ )
    .withTimeout(1.5);
```

The inner part is itself two commands joined by `andThen`:

```java
Commands.waitUntil( /* is the shooter ready? */ )
    .andThen( /* feed for 0.4 s */ )
```

`flywheels::isReadyToShoot` is a method reference — shorthand for
`() -> flywheels.isReadyToShoot()`. Either form works.

## Hint 3 — Almost there

If check 2 fails (the roller fires too early), your composition is running the
roller in parallel with the wait instead of after it. Check the nesting: the
`waitUntil` and the roller must be joined by `andThen` *inside* the `alongWith`, not
listed as siblings.

If check 3 fails (never finishes), the `withTimeout` is either missing or attached to
the wrong part. It goes on the outermost expression, so it bounds everything.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

```java
public Command scoreCommand() {
    // "Spin the flywheels up, and alongside that: wait until they are actually at
    // speed, then feed a game piece in for four tenths of a second. Give the
    // whole thing a second and a half before giving up."
    return flywheels
        .spinUpCommand()
        .alongWith(
            Commands.waitUntil(flywheels::isReadyToShoot)
                .andThen(roller.ejectCommand().withTimeout(0.4)))
        .withTimeout(1.5)
        .withName("Score");
}
```

and in `configureBindings()`:

```java
operator.rightBumper().whileTrue(scoreCommand());
```

`.withName("Score")` is optional and worth the six characters — it is what shows up
in AdvantageScope and on the dashboard when you are trying to work out which of six
scheduled commands is holding your drivetrain.

**A version that looks equivalent and is not:**

```java
return flywheels.spinUpCommand()
    .andThen(roller.ejectCommand().withTimeout(0.4));   // ✗
```

`andThen` waits for the first command to *finish*. `spinUpCommand()` never finishes
on its own — it holds the target speed until something cancels it. So the roller
step is never reached, and the shooter spins forever. Correctly diagnosing that
requires knowing which of your commands terminate and which do not, which is a
question worth asking of every command you write.

</details>
