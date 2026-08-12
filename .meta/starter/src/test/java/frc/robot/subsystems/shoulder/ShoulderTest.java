package frc.robot.subsystems.shoulder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.util.Units;
import frc.robot.Constants;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 06 — Arm with gravity feedforward.
 *
 * <p>The interesting assertion in this file is the last one, and it is worth explaining because it
 * is doing something a naive rubric would not.
 *
 * <p>An arm can be made to hold horizontal <em>without</em> feedforward, by cranking {@code kP} until
 * the steady-state error is small enough to sneak under the tolerance. That passes a position check
 * while completely missing the lesson. So the last test looks at where the holding voltage is coming
 * from: with correct feedforward, the arm sits at its setpoint with almost no error, which means the
 * PID term is contributing almost nothing and essentially all of the holding voltage is {@code kG *
 * cos(angle)}. If the feedforward is missing, the arm must sit off-target to generate the voltage
 * it needs, and the two numbers diverge.
 *
 * <p>That is a general and useful trick: when you want to grade <em>why</em> something works rather
 * than <em>that</em> it works, look at the decomposition rather than the result.
 */
@Tag("lesson")
@Tag("lesson-06")
class ShoulderTest extends RobotTestBase {

  private static final double TOLERANCE_RADIANS = Units.degreesToRadians(2.0);
  private static final double SETTLE_SECONDS = 2.0;

  private ShoulderSubsystem shoulder;

  @BeforeEach
  void setUp() {
    shoulder = manage(new ShoulderSubsystem());
  }

  @Test
  @DisplayName("1. Holds horizontal — the hardest angle, where gravity has the longest lever")
  void holdsHorizontal() {
    driveToAndAssert(Constants.Shoulder.LEVEL_RADIANS);

    // Hold it there for another two seconds. An arm with no gravity term does
    // not fall immediately; it settles into a droop and stays there, which is
    // exactly why a single snapshot would not catch the bug.
    stepSeconds(2.0);
    double errorDeg =
        Math.abs(Units.radiansToDegrees(shoulder.getAngleRadians() - Constants.Shoulder.LEVEL_RADIANS));
    assertTrue(
        errorDeg <= 2.0,
        String.format(
            "The arm drooped %.2f° below level and stayed there. That sag IS the gravity torque "
                + "you have not cancelled: the PID has to let error build up until kP * error "
                + "happens to equal the holding voltage. Feedforward supplies that voltage "
                + "directly, so the error never has to exist.",
            errorDeg));
  }

  @Test
  @DisplayName("2. Reaches the down setpoint")
  void reachesDown() {
    driveToAndAssert(Constants.Shoulder.DOWN_RADIANS);
  }

  @Test
  @DisplayName("3. Reaches the up setpoint")
  void reachesUp() {
    driveToAndAssert(Constants.Shoulder.UP_RADIANS);
  }

  @Test
  @DisplayName("4. kG has been given a real value")
  void gravityGainIsSet() {
    assertTrue(
        Constants.Shoulder.kG > 0.05,
        "Constants.Shoulder.kG is still "
            + Constants.Shoulder.kG
            + ". It is the number of volts needed to hold this arm horizontal; zero means no "
            + "gravity compensation at all.");
  }

  @Test
  @DisplayName("5. The holding voltage comes from feedforward, not from PID fighting an error")
  void feedforwardIsCarryingTheLoad() {
    driveToAndAssert(Constants.Shoulder.LEVEL_RADIANS);
    stepSeconds(1.0);

    double expectedFeedforward = Constants.Shoulder.kG * Math.cos(shoulder.getAngleRadians());
    double actual = shoulder.getAppliedVolts();
    double pidContribution = Math.abs(actual - expectedFeedforward);

    assertTrue(
        pidContribution < 0.35,
        String.format(
            "At rest the arm is drawing %.3f V, but kG * cos(angle) is only %.3f V — so %.3f V is "
                + "coming from the feedback term.%n"
                + "  A settled arm should need almost nothing from PID: the feedforward already "
                + "knows how much voltage gravity is asking for. A large gap here means either "
                + "kG is wrong or the feedforward is not being added at all.",
            actual, expectedFeedforward, pidContribution));
  }

  private void driveToAndAssert(double setpointRadians) {
    shoulder.setGoal(setpointRadians);
    stepSeconds(SETTLE_SECONDS);

    double errorRad = Math.abs(shoulder.getAngleRadians() - setpointRadians);
    assertTrue(
        errorRad <= TOLERANCE_RADIANS,
        String.format(
            "Asked for %.1f°, ended at %.1f° after %.1f s (off by %.2f°, allowed 2.0°).",
            Units.radiansToDegrees(setpointRadians),
            shoulder.getAngleDegrees(),
            SETTLE_SECONDS,
            Units.radiansToDegrees(errorRad)));
  }
}
