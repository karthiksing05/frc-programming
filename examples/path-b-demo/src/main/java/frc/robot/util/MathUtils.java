package frc.robot.util;

/**
 * Reusable math helpers used throughout the robot code.
 *
 * Lesson 01: implement {@link #applyDeadband(double, double)}.
 *
 * Once you finish lesson 01, this file is saved in your repo and
 * later lessons import it. Be careful what you change here — break
 * it and lesson 02 won't compile.
 */
public final class MathUtils {
    private MathUtils() {}

    /**
     * Returns {@code 0.0} when {@code |value|} is below the threshold,
     * otherwise returns {@code value} unchanged. Used to ignore tiny
     * joystick readings caused by sensor noise.
     *
     * @param value     raw input, typically in [-1.0, 1.0]
     * @param threshold magnitude below which input is ignored
     */
    public static double applyDeadband(double value, double threshold) {
        // TODO (LESSON 01): implement me!
        return value;
    }
}
