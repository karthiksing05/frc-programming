package frc.robot.util;

/**
 * Small math helpers shared across the robot.
 *
 * <p>This is a <em>utility class</em>: it is {@code final}, its constructor is {@code private}, and
 * every method on it is {@code static}. That combination says "you call methods on the class itself,
 * you never make one of these with {@code new}". WPILib's own {@code edu.wpi.first.math.MathUtil} is
 * built exactly this way.
 */
public final class MathUtils {
  private MathUtils() {} // utility class — never instantiated

  /**
   * Collapses joystick noise to zero.
   *
   * <p>An Xbox stick at rest does not read 0.0. The potentiometers inside it are cheap and the ADC
   * reading them is noisy, so a stick nobody is touching reports something like ±0.05. Feed that
   * straight to a drivetrain and the robot creeps across the field on its own — annoying at the
   * practice field, genuinely dangerous next to a teammate in the pit.
   *
   * <p>The fix is a <em>deadband</em>: ignore any reading whose magnitude is below some threshold.
   *
   * @param value the raw axis reading, normally in [-1.0, 1.0]
   * @param threshold the size of the dead zone, e.g. 0.1
   * @return 0.0 if {@code |value| < threshold}, otherwise {@code value} unchanged
   */
  public static double applyDeadband(double value, double threshold) {
    // Math.abs() collapses the two sign cases into one comparison, so this
    // handles a stick pushed either way without a second branch.
    if (Math.abs(value) < threshold) {
      return 0.0;
    }
    return value;
  }
}
