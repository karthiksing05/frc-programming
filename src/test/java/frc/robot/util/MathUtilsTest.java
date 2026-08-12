package frc.robot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 01 — Methods.
 *
 * <p>Five assertions, each one a case a real joystick will produce within the first ten seconds of
 * being plugged in. Read them before you write the method: a rubric you have read is a specification,
 * and writing to a specification is most of professional programming.
 *
 * <p>No {@code HAL.initialize} here and no {@link frc.robot.testing.RobotTestBase} — this lesson
 * touches no hardware, so the test needs none. That is itself worth noticing: pure functions are the
 * cheapest things in the world to test, which is one reason to keep the interesting parts of your
 * code pure.
 */
@Tag("lesson")
@Tag("lesson-01")
class MathUtilsTest {

  private static final double THRESHOLD = 0.1;

  @Test
  @DisplayName("1. A small positive reading collapses to zero")
  void smallPositiveIsZeroed() {
    assertEquals(
        0.0,
        MathUtils.applyDeadband(0.05, THRESHOLD),
        "A stick reading 0.05 with a 0.1 deadband is noise, not intent — return 0.0");
  }

  @Test
  @DisplayName("2. A small negative reading collapses to zero")
  void smallNegativeIsZeroed() {
    assertEquals(
        0.0,
        MathUtils.applyDeadband(-0.05, THRESHOLD),
        "Sticks drift both ways. Math.abs() is what makes one comparison cover both signs.");
  }

  @Test
  @DisplayName("3. A real push passes through untouched, sign and all")
  void largeValuesPassThrough() {
    assertEquals(
        0.8, MathUtils.applyDeadband(0.8, THRESHOLD), 1e-9, "Values outside the band must not change");
    assertEquals(
        -0.8,
        MathUtils.applyDeadband(-0.8, THRESHOLD),
        1e-9,
        "Outside the band, keep the sign — the driver is asking to reverse");
  }

  @Test
  @DisplayName("4. The threshold is a parameter, not a hard-coded 0.1")
  void respectsANonDefaultThreshold() {
    assertEquals(
        0.0,
        MathUtils.applyDeadband(0.15, 0.20),
        "0.15 is inside a 0.20 band. If this fails you probably hard-coded 0.1 instead of using "
            + "the threshold parameter.");
    assertEquals(
        0.25,
        MathUtils.applyDeadband(0.25, 0.20),
        1e-9,
        "0.25 is outside a 0.20 band and must pass through");
  }

  @Test
  @DisplayName("5. Exactly zero stays zero, and the boundary is not exclusive")
  void handlesZeroAndTheBoundary() {
    assertEquals(0.0, MathUtils.applyDeadband(0.0, THRESHOLD), "Zero in, zero out");
    assertEquals(
        THRESHOLD,
        MathUtils.applyDeadband(THRESHOLD, THRESHOLD),
        1e-9,
        "A reading exactly AT the threshold is outside the band. Use `<`, not `<=`, so that the "
            + "deadband never eats a value the driver deliberately produced.");
  }
}
