# Hints — Lesson 04

## Hint 1 — Where to start

Do it in this order, and check the rubric between steps:

1. `setMode` in `RollerSubsystem` — one line
2. `periodic` in `RollerSubsystem` — the switch
3. `RobotContainer` — construct the roller
4. `Robot.java` — delete the fields, rewrite `teleopPeriodic`

Doing 4 before 3 leaves the project in a state that will not compile, which is
confusing rather than informative.

## Hint 2 — The shape of the answer

`setMode` assigns its parameter to the field. That is genuinely all.

`periodic` computes one number and then sends it. A `switch` over the enum reads
best, and Java's arrow form does not need `break`:

```java
double output = switch (state) {
    case OFF -> 0.0;
    case INTAKING -> /* ... */;
    case EJECTING -> /* ... */;
};
```

`INTAKING` is the only case with a condition in it, and you already wrote that
condition in lesson 03 — it is the `hasGamePiece()` check.

Finish with both lines:

```java
lastOutput = output;
motor.set(output);
```

so that `getOutput()` and the real motor can never disagree.

## Hint 3 — Almost there

`Robot.teleopPeriodic()` after the move:

```java
RollerSubsystem roller = robotContainer.getRoller();
if ( /* X held */ ) {
    roller.setMode(RollerSubsystem.State.EJECTING);
} else if ( /* B held */ ) {
    roller.setMode(RollerSubsystem.State.INTAKING);
} else {
    roller.setMode(RollerSubsystem.State.OFF);
}
```

No `hasGamePiece` here. That check moved into the subsystem, and the fact that it
no longer appears in this file is the clearest signal the refactor worked.

Do not forget `disabledInit()` — it also refers to the motor you deleted.

## Hint 4 — Reference answer

<details>
<summary>Click to reveal</summary>

**RollerSubsystem.setMode**

```java
public void setMode(State desired) {
    state = desired;
}
```

**RollerSubsystem.periodic**

```java
@Override
public void periodic() {
    double output =
        switch (state) {
            case OFF -> 0.0;
            case INTAKING -> hasGamePiece() ? 0.0 : Constants.Roller.INTAKE_SPEED;
            case EJECTING -> Constants.Roller.EJECT_SPEED;
        };

    lastOutput = output;
    motor.set(output);
}
```

**RobotContainer** — replace the null field:

```java
private final RollerSubsystem roller = new RollerSubsystem();
```

and simplify `close()` to just `roller.close();`.

**Robot.java** — delete the `rollerMotor` and `beamBreak` fields and the
`PWMSparkMax` / `DigitalInput` imports; add `import frc.robot.subsystems.roller.RollerSubsystem;`.

```java
@Override
public void teleopPeriodic() {
    RollerSubsystem roller = robotContainer.getRoller();
    if (operator.getXButton()) {
        roller.setMode(RollerSubsystem.State.EJECTING);
    } else if (operator.getBButton()) {
        roller.setMode(RollerSubsystem.State.INTAKING);
    } else {
        roller.setMode(RollerSubsystem.State.OFF);
    }
}

@Override
public void disabledInit() {
    robotContainer.getRoller().setMode(RollerSubsystem.State.OFF);
}

@Override
public void close() {
    robotContainer.close();
    super.close();
}
```

**A tempting mistake:** putting the motor write in `setMode` and leaving `periodic`
empty. It passes several of the checks. It also means the roller only ever acts at
the instant somebody presses a button — so if the game piece arrives *after* the
press, nothing notices, because nothing is looking. `periodic` is what makes a
subsystem continuously responsible for its mechanism rather than occasionally
poked.

</details>
