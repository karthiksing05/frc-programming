package frc.robot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Lesson 01 rubric. Pure unit tests — no HAL needed because
 * applyDeadband doesn't touch hardware.
 *
 * Tagged "lesson-01" so {@code ./gradlew lesson01} runs just these.
 */
@Tag("lesson-01")
@DisplayName("Lesson 01 · MathUtils.applyDeadband")
class MathUtilsTest {

    private static final double THRESHOLD = 0.10;
    private static final double EPS = 1e-9;

    @Test
    @DisplayName("returns 0 when |value| < threshold (positive)")
    void belowThresholdPositive() {
        assertEquals(0.0, MathUtils.applyDeadband(0.05, THRESHOLD), EPS);
    }

    @Test
    @DisplayName("returns 0 when |value| < threshold (negative)")
    void belowThresholdNegative() {
        assertEquals(0.0, MathUtils.applyDeadband(-0.05, THRESHOLD), EPS);
    }

    @Test
    @DisplayName("returns 0 at exactly zero input")
    void zeroIsZero() {
        assertEquals(0.0, MathUtils.applyDeadband(0.0, THRESHOLD), EPS);
    }

    @Test
    @DisplayName("returns value when above threshold (positive)")
    void aboveThresholdPositive() {
        assertEquals(0.5, MathUtils.applyDeadband(0.5, THRESHOLD), EPS);
    }

    @Test
    @DisplayName("returns value when above threshold (negative)")
    void aboveThresholdNegative() {
        assertEquals(-0.8, MathUtils.applyDeadband(-0.8, THRESHOLD), EPS);
    }

    @Test
    @DisplayName("respects a different threshold (0.20)")
    void differentThreshold() {
        assertEquals(0.0,   MathUtils.applyDeadband(0.15, 0.20), EPS);
        assertEquals(0.25,  MathUtils.applyDeadband(0.25, 0.20), EPS);
    }
}
