package frc.robot.subsystems.drive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 16 — The IO Layer pattern.
 *
 * <p>Look at what this test does <em>not</em> need. No {@code Drive}. No {@code RobotContainer}. No
 * PWM channels, no DIO channels, no {@code CommandScheduler}. It constructs a {@link DriveIOSim},
 * hands it voltages, and reads the struct back.
 *
 * <p>That is the argument for the IO layer, made concrete. Once the hardware boundary is an
 * interface, the thing on the far side of it is an ordinary Java object: several of them can exist at
 * once, they can be constructed in a loop, and testing them requires nothing but {@code new}. The
 * subsystem above the line becomes equally testable, because you can hand it a fake.
 *
 * <p>It is also the precondition for log replay (lesson 19). If every reading the subsystem sees
 * arrives through {@code updateInputs}, then a recording of those readings is a complete recording
 * of the subsystem's world, and re-running the season's worst match on a laptop becomes possible.
 * Any sensor read that sneaks around the interface silently breaks that.
 */
@Tag("lesson")
@Tag("lesson-16")
class DriveIOSimTest extends RobotTestBase {

  /** One loop period of physics. */
  private static void advance(DriveIO io, DriveIO.DriveIOInputs inputs, int cycles) {
    for (int i = 0; i < cycles; i++) {
      io.updateInputs(inputs);
    }
  }

  @Test
  @DisplayName("1. Voltage in, motion out — the model is actually being stepped")
  void voltageProducesMotion() {
    DriveIOSim io = new DriveIOSim();
    var inputs = new DriveIO.DriveIOInputs();

    io.setVoltage(6.0, 6.0);
    advance(io, inputs, 50); // one second

    assertTrue(
        inputs.leftPositionMeters > 0.3,
        String.format(
            "After a second at 6 V the left side reports %.3f m. Either physics.update(...) is "
                + "not being called or the position is not being copied into the struct — check "
                + "the TODO in updateInputs.",
            inputs.leftPositionMeters));
    assertEquals(
        inputs.leftPositionMeters,
        inputs.rightPositionMeters,
        0.01,
        "Equal voltages should produce equal travel");
  }

  @Test
  @DisplayName("2. Velocity is reported, not just position")
  void velocityIsReported() {
    DriveIOSim io = new DriveIOSim();
    var inputs = new DriveIO.DriveIOInputs();

    io.setVoltage(6.0, 6.0);
    advance(io, inputs, 50);

    assertTrue(
        inputs.leftVelocityMetersPerSec > 0.5,
        "The wheels are moving, so velocity should be non-zero. Fill in "
            + "leftVelocityMetersPerSec and rightVelocityMetersPerSec too — a subsystem that can "
            + "only see position has to differentiate it itself, badly.");
  }

  @Test
  @DisplayName("3. Applied voltage is reported back")
  void appliedVoltsAreReported() {
    DriveIOSim io = new DriveIOSim();
    var inputs = new DriveIO.DriveIOInputs();

    io.setVoltage(4.0, -4.0);
    advance(io, inputs, 5);

    assertEquals(
        4.0,
        inputs.leftAppliedVolts,
        1e-6,
        "The struct should report what was commanded. This looks redundant — you just set it — "
            + "but during log replay it is the only record of what the code decided to do, and "
            + "comparing 'what I asked for' against 'what happened' is most of debugging.");
    assertEquals(-4.0, inputs.rightAppliedVolts, 1e-6, "Same for the right side");
  }

  @Test
  @DisplayName("4. Opposite voltages spin the robot, and the gyro says so")
  void gyroTracksRotation() {
    DriveIOSim io = new DriveIOSim();
    var inputs = new DriveIO.DriveIOInputs();

    io.setVoltage(6.0, -6.0);
    advance(io, inputs, 50);

    assertTrue(
        Math.abs(inputs.gyroYawRadians) > 0.2,
        String.format(
            "Driving one side forward and the other back turns the robot in place, but the "
                + "reported yaw is %.4f rad. Copy physics.getHeading().getRadians() into "
                + "gyroYawRadians.",
            inputs.gyroYawRadians));
  }

  @Test
  @DisplayName("5. Zero volts means the robot coasts to a stop rather than running away")
  void zeroVoltsStops() {
    DriveIOSim io = new DriveIOSim();
    var inputs = new DriveIO.DriveIOInputs();

    io.setVoltage(8.0, 8.0);
    advance(io, inputs, 25);

    io.setVoltage(0.0, 0.0);
    advance(io, inputs, 100);

    assertTrue(
        Math.abs(inputs.leftVelocityMetersPerSec) < 0.1,
        String.format(
            "Two seconds after cutting power the wheels are still doing %.3f m/s",
            inputs.leftVelocityMetersPerSec));
  }

  @Test
  @DisplayName("6. Both implementations satisfy the same interface")
  void bothImplementationsExist() {
    // Constructed through the interface type on purpose: if this compiles and
    // runs, then Drive could be handed either one and would not know which.
    DriveIO sim = new DriveIOSim();
    DriveIO real = manage(new DriveIOReal());

    var simInputs = new DriveIO.DriveIOInputs();
    var realInputs = new DriveIO.DriveIOInputs();

    // Identical calls, through the interface, with no idea which is which.
    for (DriveIO io : new DriveIO[] {sim, real}) {
      io.setVoltage(3.0, 3.0);
      io.updateInputs(io == sim ? simInputs : realInputs);
    }

    assertEquals(
        3.0,
        simInputs.leftAppliedVolts,
        1e-6,
        "The simulated implementation should report the voltage it was given");
    assertEquals(
        3.0,
        realInputs.leftAppliedVolts,
        1e-6,
        "So should the real one. Neither the test nor Drive can tell them apart — which is the "
            + "entire point of the interface.");
  }
}
