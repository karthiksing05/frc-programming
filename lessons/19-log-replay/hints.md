# Hints — Lesson 19

## Hint 1 — Where to start

Read `lessons/EXTENSIONS.md` first. This lesson needs a vendordep downloaded, and doing that mid-lesson on a bad network is how an hour disappears.

## Hint 2 — The shape of the answer

Convert exactly one subsystem, not all of them. Get one round trip — log, replay, see the same numbers — before converting anything else.

## Hint 3 — What usually goes wrong

Half-converting a subsystem, so some reads go through inputs and some do not. The replay runs and the numbers are subtly wrong, which is worse than a clean failure.

Forgetting `setUseTiming(false)` in replay mode, so the replay runs in real time instead of as fast as the CPU allows.

Expecting replay to answer 'what if I had done something different'. It cannot.

## Hint 4 — Reference

<details>
<summary>Click to reveal</summary>

Consult the [AdvantageKit documentation](https://docs.advantagekit.org/) — it is
genuinely good and it is maintained by the people who wrote the tool.

The shape, once the vendordep is installed:

```java
public class Robot extends LoggedRobot {
  @Override
  public void robotInit() {
    if (isReal()) {
      Logger.addDataReceiver(new WPILOGWriter());
      Logger.addDataReceiver(new NT4Publisher());
    } else {
      String path = LogFileUtil.findReplayLog();
      Logger.setReplaySource(new WPILOGReader(path));
      Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(path, "_replay")));
      setUseTiming(false);   // run as fast as the CPU allows
    }
    Logger.start();
  }
}
```

and, on the inputs class you wrote by hand in lesson 16:

```java
@AutoLog
public static class DriveIOInputs {
  public double leftPositionMeters = 0.0;
  // ...
}
```

The annotation processor generates `DriveIOInputsAutoLogged`, which is your class
plus the serialisation code. Compare the generated file with what you wrote by hand
— it is the same idea, written out mechanically.

</details>
