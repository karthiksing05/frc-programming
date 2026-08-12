package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 02 — Variables & types.
 *
 * <p>Two kinds of check here, and the second one is the interesting one.
 *
 * <p>The value checks are obvious: {@code DEADBAND} should be 0.10 and not 0.0.
 *
 * <p>The <em>reflection</em> checks are not about values at all. They ask the JVM to describe the
 * fields themselves and assert that they are {@code public static final}. A constant that is not
 * {@code final} is not a constant — it is a variable that nobody has reassigned yet, and the
 * difference matters at 11pm on a Friday when somebody's autocomplete assigns to it. Reflection
 * lets a test enforce a promise about the shape of code, not just its behaviour.
 */
@Tag("lesson")
@Tag("lesson-02")
class ConstantsTest {

  @Test
  @DisplayName("1. Drive.DEADBAND is the real 0.10, not the placeholder")
  void deadbandIsSet() {
    assertEquals(
        0.10,
        Constants.Drive.DEADBAND,
        1e-9,
        "Lesson 01 taught the deadband; lesson 02 gives it a name and a home. 0.0 means the "
            + "placeholder is still there.");
  }

  @Test
  @DisplayName("2. The other three constants have real values too")
  void otherConstantsAreSet() {
    assertEquals(12.0, Constants.Drive.MAX_VOLTS, 1e-9, "A full FRC battery is about 12 volts");
    assertEquals(
        8.45,
        Constants.Drive.GEAR_RATIO,
        1e-9,
        "Stock toughbox mini: 8.45 motor rotations per wheel rotation");
    assertEquals(
        5800.0, Constants.Flywheels.MAX_RPM, 1e-9, "Free speed of the shooter motor, in RPM");
  }

  @Test
  @DisplayName("3. Every constant is public static final")
  void constantsAreProperlyDeclared() {
    for (String name : new String[] {"DEADBAND", "MAX_VOLTS", "GEAR_RATIO"}) {
      assertModifiers(Constants.Drive.class, name);
    }
    assertModifiers(Constants.Flywheels.class, "MAX_RPM");
  }

  @Test
  @DisplayName("4. Constants live in nested classes, not loose at the top level")
  void constantsAreGrouped() {
    // Grouping is what keeps `Constants` readable at thirty subsystems instead
    // of three. It also means a call site reads as a sentence:
    // Constants.Elevator.MAX_HEIGHT_METERS says where it belongs and what it is.
    for (String group : new String[] {"Drive", "Roller", "Elevator", "Shoulder", "Flywheels"}) {
      boolean found = false;
      for (Class<?> nested : Constants.class.getDeclaredClasses()) {
        if (nested.getSimpleName().equals(group)) {
          found = true;
          break;
        }
      }
      assertTrue(found, "Constants should contain a nested class named " + group);
    }

    // And nothing subsystem-specific should have escaped to the top level.
    for (Field f : Constants.class.getDeclaredFields()) {
      assertTrue(
          f.getName().equals("LOOP_PERIOD_SECONDS") || f.getName().equals("NOMINAL_BATTERY_VOLTS"),
          "Constants."
              + f.getName()
              + " is loose at the top level. Only genuinely robot-wide values belong there; "
              + "move it into the nested class for the mechanism it configures.");
    }
  }

  private static void assertModifiers(Class<?> owner, String fieldName) {
    Field field;
    try {
      field = owner.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new AssertionError(
          owner.getSimpleName() + "." + fieldName + " does not exist — did you rename it?", e);
    }
    int m = field.getModifiers();
    assertTrue(Modifier.isPublic(m), fieldName + " must be public so other classes can read it");
    assertTrue(
        Modifier.isStatic(m), fieldName + " must be static — you never `new Constants.Drive()`");
    assertTrue(
        Modifier.isFinal(m),
        fieldName + " must be final. Without it, this is a global variable, not a constant.");
  }
}
