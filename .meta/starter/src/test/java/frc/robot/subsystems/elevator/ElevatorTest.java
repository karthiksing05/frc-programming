package frc.robot.subsystems.elevator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.Constants;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 05 — PID introduction.
 *
 * <p>This rubric grades a <em>response over time</em>, not a value. That is new, and it is how
 * control loops actually get judged: nobody cares what the controller outputs on any single loop;
 * they care whether the carriage gets there, gets there quickly, and does not sail past.
 *
 * <p>Three properties, checked at all four setpoints:
 *
 * <ol>
 *   <li><strong>Accuracy</strong> — ends within 2 cm of the target.
 *   <li><strong>Settling time</strong> — gets there within 1.5 s.
 *   <li><strong>Overshoot</strong> — never goes more than 5 cm past on the way.
 * </ol>
 *
 * <p>You can pass any two of those by sacrificing the third, which is exactly the trade-off tuning
 * is about. A huge {@code kP} is fast and accurate and overshoots wildly; a tiny one never
 * overshoots and never arrives either.
 */
@Tag("lesson")
@Tag("lesson-05")
class ElevatorTest extends RobotTestBase {

  /** How close counts as arrived. Matches the rubric in the lesson text. */
  private static final double TOLERANCE_METERS = 0.02;

  /** How long the carriage gets to arrive before we call it too slow. */
  private static final double SETTLE_SECONDS = 1.5;

  /** How far past the target the carriage may travel before we call it oscillating. */
  private static final double MAX_OVERSHOOT_METERS = 0.05;

  private ElevatorSubsystem elevator;

  @BeforeEach
  void setUp() {
    elevator = manage(new ElevatorSubsystem());
  }

  @Test
  @DisplayName("1. Reaches the low setpoint within tolerance and on time")
  void reachesLow() {
    driveToAndAssert(Constants.Elevator.LOW_METERS);
  }

  @Test
  @DisplayName("2. Reaches the mid setpoint")
  void reachesMid() {
    driveToAndAssert(Constants.Elevator.MID_METERS);
  }

  @Test
  @DisplayName("3. Reaches the high setpoint")
  void reachesHigh() {
    driveToAndAssert(Constants.Elevator.HIGH_METERS);
  }

  @Test
  @DisplayName("4. Survives a full four-setpoint sweep without ever overshooting")
  void fullSweep() {
    // The real test. Going up is easy — gravity helps you stop. Coming back down
    // is where an under-damped controller shows itself, because now gravity is
    // adding to the thing you are trying to arrest.
    for (double setpoint :
        new double[] {
          Constants.Elevator.LOW_METERS,
          Constants.Elevator.HIGH_METERS,
          Constants.Elevator.MID_METERS,
          Constants.Elevator.STOW_METERS
        }) {
      driveToAndAssert(setpoint);
    }
  }

  @Test
  @DisplayName("5. Holds position once it has arrived, instead of drifting back down")
  void holdsPosition() {
    driveToAndAssert(Constants.Elevator.MID_METERS);

    // Two more seconds of doing nothing. Gravity has not gone away, so if the
    // controller has stopped pushing the carriage will slide.
    stepSeconds(2.0);

    double drift = Math.abs(elevator.getHeightMeters() - Constants.Elevator.MID_METERS);
    assertTrue(
        drift <= TOLERANCE_METERS,
        String.format(
            "The carriage drifted %.3f m away from its setpoint while just sitting there. "
                + "A position loop has to keep working after it arrives — arriving is not the "
                + "same as staying.",
            drift));
  }

  /**
   * Commands a height, runs the loop, and asserts the three response properties.
   *
   * @param setpointMeters where to send the carriage
   */
  private void driveToAndAssert(double setpointMeters) {
    double startHeight = elevator.getHeightMeters();
    boolean movingUp = setpointMeters > startHeight;

    elevator.setGoal(setpointMeters);

    double worstOvershoot = 0.0;
    int cycles = (int) Math.ceil(SETTLE_SECONDS / Constants.LOOP_PERIOD_SECONDS);
    for (int i = 0; i < cycles; i++) {
      step();
      double height = elevator.getHeightMeters();
      double past = movingUp ? height - setpointMeters : setpointMeters - height;
      worstOvershoot = Math.max(worstOvershoot, past);
    }

    double finalError = Math.abs(elevator.getHeightMeters() - setpointMeters);

    assertTrue(
        finalError <= TOLERANCE_METERS,
        String.format(
            "Asked for %.2f m, ended at %.3f m after %.1f s (off by %.3f m, allowed %.3f m).%n"
                + "  Still at the starting height? The TODO in periodic() is probably unfinished, "
                + "or kP is still 0.%n"
                + "  Short of the target and stuck? kP is too small, or you need a little kI.",
            setpointMeters,
            elevator.getHeightMeters(),
            SETTLE_SECONDS,
            finalError,
            TOLERANCE_METERS));

    assertTrue(
        worstOvershoot <= MAX_OVERSHOOT_METERS,
        String.format(
            "Overshot %.2f m by %.3f m on the way (allowed %.3f m).%n"
                + "  That is the carriage racing past the target and having to come back. "
                + "Raise kD until the ringing damps, or lower kP.",
            setpointMeters, worstOvershoot, MAX_OVERSHOOT_METERS));
  }
}
