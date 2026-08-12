# Hints — Lesson 20

## Hint 1 — Where to start

Start with the trigger, not the command. `elevator.atGoalTrigger` and friends already exist — combining them is one expression, and getting it plotted in AdvantageScope tells you immediately whether your conditions are what you thought.

## Hint 2 — The shape of the answer

Then add one command factory. `scoreHigh()` is a composition of things you already have; it is lesson 09 with three subsystems instead of two.

## Hint 3 — What usually goes wrong

Making `Superstructure` extend `SubsystemBase`. It owns no hardware, and doing so introduces a requirement that will block commands that should be able to run together.

Storing state in it. If it has mutable fields beyond the subsystem references, ask what they are for — usually they duplicate something a subsystem already knows.

Forgetting `.debounce()` on a composite trigger. Three noisy conditions ANDed together are noisier than any one of them.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

```java
public final Trigger readyToScore;

public Superstructure(ElevatorSubsystem e, ShoulderSubsystem s, RollerSubsystem r) {
  elevator = e;
  shoulder = s;
  roller = r;

  readyToScore =
      elevator
          .atGoalTrigger
          .and(shoulder.atGoalTrigger)
          .and(roller.hasGamePieceTrigger)
          .debounce(0.1);
}
```

Publish it so you can see it:

```java
BooleanPublisher readyPub =
    NetworkTableInstance.getDefault()
        .getTable("Superstructure")
        .getBooleanTopic("ReadyToScore")
        .publish();
```

and set it from `RobotContainer`'s periodic path, or bind it to a trigger action.
Watching a boolean indicator go green exactly when three separate conditions
converge is the clearest possible confirmation that your composition says what you
meant.

</details>
