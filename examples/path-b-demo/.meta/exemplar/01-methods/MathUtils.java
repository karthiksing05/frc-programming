/* AUTHOR-ONLY reference solution for lesson 01. Not shipped to students.
 * Drop into src/main/java/frc/robot/util/ to verify the rubric.
 */
package frc.robot.util;

public class MathUtils {
    public static double applyDeadband(double value, double threshold) {
        return Math.abs(value) < threshold ? 0.0 : value;
    }
}
