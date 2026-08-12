package frc.robot.subsystems.flywheels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 10 — Telemetry.
 *
 * <p>Everything the robot publishes to NetworkTables is visible to AdvantageScope, to the Driver
 * Station dashboard, and — as these tests demonstrate — to any other code running in the same
 * process. NetworkTables is a shared blackboard, and publishing to it is how a robot stops being a
 * black box.
 *
 * <p>The rule the lesson is really teaching: <strong>if you did not plot it, it did not happen.</strong>
 * A number you cannot see is a number you will end up guessing about at 1am the night before a
 * competition.
 */
@Tag("lesson")
@Tag("lesson-10")
class FlywheelsTelemetryTest extends RobotTestBase {

  private Flywheels flywheels;
  private NetworkTable table;

  @BeforeEach
  void setUp() {
    flywheels = manage(new Flywheels());
    table = NetworkTableInstance.getDefault().getTable("Flywheels");
  }

  @Test
  @DisplayName("1. TargetRPM, ActualRPM and ErrorRPM all exist on NetworkTables")
  void allThreeTopicsExist() {
    CommandScheduler.getInstance().schedule(flywheels.spinUpCommand());
    step(5);

    for (String key : new String[] {"TargetRPM", "ActualRPM", "ErrorRPM"}) {
      assertTrue(
          table.getTopic(key).exists(),
          "/Flywheels/"
              + key
              + " is not being published. Create a DoublePublisher for it next to targetPublisher, "
              + "and call .set(...) on it every loop in periodic().");
    }
  }

  @Test
  @DisplayName("2. TargetRPM reports what we asked for")
  void targetIsPublished() {
    CommandScheduler.getInstance().schedule(flywheels.spinUpCommand());
    step(5);

    assertEquals(
        Constants.Flywheels.SHOOT_RPM,
        table.getEntry("TargetRPM").getDouble(Double.NaN),
        1e-6,
        "TargetRPM should mirror the commanded speed");
  }

  @Test
  @DisplayName("3. ActualRPM tracks the real wheel speed as it spins up")
  void actualIsPublished() {
    CommandScheduler.getInstance().schedule(flywheels.spinUpCommand());
    stepSeconds(1.0);

    double published = table.getEntry("ActualRPM").getDouble(Double.NaN);
    assertTrue(
        published > 100.0,
        String.format(
            "After a second of spinning up, /Flywheels/ActualRPM reads %.1f. It should be tracking "
                + "getVelocityRpm(), which is %.1f.",
            published, flywheels.getVelocityRpm()));
    assertEquals(
        flywheels.getVelocityRpm(),
        published,
        1.0,
        "ActualRPM should be published from getVelocityRpm() on the same loop, not from a stale "
            + "copy");
  }

  @Test
  @DisplayName("4. ErrorRPM really is target minus actual, every loop")
  void errorIsComputedCorrectly() {
    CommandScheduler.getInstance().schedule(flywheels.spinUpCommand());

    // Check it at several points during the spin-up, so a publisher that only
    // happens to be right once cannot slip through.
    //
    // Note where the velocity is sampled: BEFORE the step, not after. Within one
    // loop the scheduler calls periodic() (which publishes) and then
    // simulationPeriodic() (which advances the physics), so the value published
    // during a loop reflects the speed at the START of that loop. Comparing
    // against the post-step speed would be comparing across a 20 ms boundary and
    // would fail for a completely correct implementation.
    for (int i = 0; i < 5; i++) {
      stepSeconds(0.2);
      double expected = flywheels.getTargetRpm() - flywheels.getVelocityRpm();
      step();
      assertEquals(
          expected,
          table.getEntry("ErrorRPM").getDouble(Double.NaN),
          1.0,
          "ErrorRPM must be target minus actual. On a plot this is the gap between the flat "
              + "target line and the rising actual line — the shape that tells you which gain to "
              + "reach for.");
    }
  }
}
