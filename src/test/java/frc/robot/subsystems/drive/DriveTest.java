package frc.robot.subsystems.drive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 07 — Tank drive wiring (the factory pattern).
 *
 * <p>Test 4 is the one that matters. Everything else here checks arithmetic; test 4 checks that you
 * used the suppliers <em>as suppliers</em>.
 *
 * <p>The failure it catches looks like this. You write {@code arcadeDriveCommand} and, wanting to
 * tidy up, you read the axes once at the top of the method instead of inside the lambda:
 *
 * <pre>{@code
 * public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
 *   double fwd = forward.getAsDouble();      // <-- read ONCE, at construction
 *   return run(() -> setVoltage(fwd * 12, fwd * 12));
 * }
 * }</pre>
 *
 * <p>This compiles. It runs. It even drives — the first time, if the stick happened to be pushed
 * when the robot booted. In practice the command is built during {@code RobotContainer}'s
 * constructor, when the stick reads 0.0, so the robot sits still all match while the driver pushes
 * harder and harder and concludes the drivetrain is broken.
 *
 * <p>Test 4 schedules the command, changes what the suppliers return, and demands that the output
 * change with it. A captured value cannot pass it.
 */
@Tag("lesson")
@Tag("lesson-07")
class DriveTest extends RobotTestBase {

  private Drive drive;

  /** Stand-ins for the two joystick axes. Mutable so the test can "move the stick". */
  private double forwardAxis;

  private double rotationAxis;

  @BeforeEach
  void setUp() {
    drive = manage(new Drive());
    forwardAxis = 0.0;
    rotationAxis = 0.0;
  }

  @Test
  @DisplayName("1. Pushing forward drives both sides forward, equally")
  void forwardDrivesBothSides() {
    scheduleArcadeDrive();
    forwardAxis = 1.0;
    step(3);

    assertTrue(
        drive.getLeftVolts() > 6.0,
        "Full forward should command a large positive voltage on the left, got "
            + drive.getLeftVolts());
    assertEquals(
        drive.getLeftVolts(),
        drive.getRightVolts(),
        1e-6,
        "Driving straight means both sides get the same command. A difference here means your "
            + "arcade mixing has a sign error.");
  }

  @Test
  @DisplayName("2. Rotation splits the sides in opposite directions")
  void rotationTurnsInPlace() {
    scheduleArcadeDrive();
    forwardAxis = 0.0;
    rotationAxis = 1.0;
    step(3);

    assertTrue(
        drive.getLeftVolts() > 6.0 && drive.getRightVolts() < -6.0,
        String.format(
            "Turning in place means left forward and right backward. Got left=%.2f right=%.2f. "
                + "Arcade mixing is left = fwd + rot, right = fwd - rot.",
            drive.getLeftVolts(), drive.getRightVolts()));
  }

  @Test
  @DisplayName("3. A stick inside the deadband is ignored")
  void deadbandIsApplied() {
    scheduleArcadeDrive();
    forwardAxis = 0.05; // below Constants.Drive.DEADBAND (0.10)
    rotationAxis = 0.05;
    step(3);

    assertEquals(
        0.0,
        drive.getLeftVolts(),
        1e-6,
        "0.05 is inside the 0.10 deadband — this is the joystick sitting still, not the driver "
            + "asking for anything. Call MathUtils.applyDeadband on each axis before you mix them.");
    assertEquals(0.0, drive.getRightVolts(), 1e-6, "Same for the right side");
  }

  @Test
  @DisplayName("4. The command re-reads the sticks every loop (the suppliers are live)")
  void suppliersAreReadEveryLoop() {
    scheduleArcadeDrive();

    // Stick at rest while the command starts — exactly as it is during real
    // robot startup.
    forwardAxis = 0.0;
    step(3);
    assertEquals(0.0, drive.getLeftVolts(), 1e-6, "Nothing commanded yet");

    // Now the driver pushes. If the axis was captured at construction time,
    // nothing happens here — and that is the bug.
    forwardAxis = 1.0;
    step(3);
    assertTrue(
        drive.getLeftVolts() > 6.0,
        "The stick moved and the drivetrain did not.%n"
                .formatted()
            + "  You almost certainly read forward.getAsDouble() OUTSIDE the run(() -> ...) "
            + "lambda, which reads it once, at startup, when the stick is at zero.%n"
            + "  Move both getAsDouble() calls INSIDE the lambda body.");

    // And it must let go when the driver does.
    forwardAxis = 0.0;
    step(3);
    assertEquals(
        0.0,
        drive.getLeftVolts(),
        1e-6,
        "Releasing the stick must stop the robot on the very next loop");
  }

  @Test
  @DisplayName("5. Full forward plus full turn saturates instead of asking for 24 volts")
  void mixingIsClamped() {
    scheduleArcadeDrive();
    forwardAxis = 1.0;
    rotationAxis = 1.0;
    step(3);

    assertTrue(
        Math.abs(drive.getLeftVolts()) <= Constants.Drive.MAX_VOLTS + 1e-6,
        "fwd + rot is 2.0 before clamping. Clamp to [-1, 1] before scaling to volts, or you ask "
            + "for "
            + drive.getLeftVolts()
            + " V from a 12 V battery.");
  }

  @Test
  @DisplayName("6. The drivetrain actually moves in simulation")
  void robotMovesForward() {
    scheduleArcadeDrive();
    forwardAxis = 1.0;
    stepSeconds(1.0);

    assertTrue(
        drive.getAverageDistanceMeters() > 0.5,
        String.format(
            "After a second at full throttle the robot has only travelled %.2f m. Voltage is "
                + "reaching the motors but the physics is not responding — check that setVoltage "
                + "is being called with volts, not with a -1..1 throttle.",
            drive.getAverageDistanceMeters()));
  }

  /** Schedules the arcade-drive command with suppliers that read this test's mutable fields. */
  private void scheduleArcadeDrive() {
    Command command = drive.arcadeDriveCommand(() -> forwardAxis, () -> rotationAxis);
    CommandScheduler.getInstance().schedule(command);
  }
}
