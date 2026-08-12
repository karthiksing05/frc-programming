package frc.robot.subsystems.roller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj.simulation.DIOSim;
import edu.wpi.first.wpilibj.simulation.PWMSim;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.testing.RobotTestBase;
import frc.robot.testing.SourceInspector;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 04 — Subsystems as state machines.
 *
 * <p>Six checks, and they split cleanly in two.
 *
 * <p>Checks 1–4 are behavioural: does {@code setMode} plus {@code periodic} produce the right motor
 * output? These are the same four scenarios lesson 03 tested, which is the point — the robot's
 * behaviour must not change during a refactor. If it does, you did not refactor, you rewrote.
 *
 * <p>Checks 5–6 are structural, and they are what make this lesson different from lesson 03. A
 * subsystem that keeps its hardware {@code private final}, and a {@code Robot} that no longer
 * mentions a motor at all, are the actual deliverables. You could pass the behavioural tests with
 * everything still in {@code teleopPeriodic}; you cannot pass these.
 */
@Tag("lesson")
@Tag("lesson-04")
class RollerSubsystemTest extends RobotTestBase {

  /**
   * How close a PWM readback has to be.
   *
   * <p>Not 1e-6, and the reason is worth knowing. A PWM output is a pulse width, quantised to
   * microseconds by the roboRIO's FPGA. Asking for 0.6 produces a pulse that reads back as
   * 0.59960..., because 0.6 is not exactly representable in that grid. Real hardware has the same
   * granularity — this is not a simulation artefact — so a rubric that demanded exact equality
   * would be demanding something the electronics cannot deliver.
   */
  private static final double PWM_TOLERANCE = 0.005;


  private RollerSubsystem roller;
  private DIOSim beamBreak;
  private PWMSim motor;

  @BeforeEach
  void setUp() {
    roller = manage(new RollerSubsystem());
    beamBreak = new DIOSim(Constants.Roller.BEAM_BREAK_DIO);
    motor = new PWMSim(Constants.Roller.MOTOR_PWM);
    setGamePiecePresent(false);
  }

  @Test
  @DisplayName("1. It is a real SubsystemBase, so the scheduler will run it")
  void extendsSubsystemBase() {
    assertTrue(
        roller instanceof SubsystemBase,
        "RollerSubsystem must extend SubsystemBase. That is what registers it with the "
            + "CommandScheduler so periodic() gets called fifty times a second.");
  }

  @Test
  @DisplayName("2. INTAKING with an empty throat runs the roller inward")
  void intakingRunsMotor() {
    setGamePiecePresent(false);
    roller.setMode(RollerSubsystem.State.INTAKING);
    step();

    assertEquals(
        Constants.Roller.INTAKE_SPEED,
        motor.getSpeed(),
        PWM_TOLERANCE,
        "setMode(INTAKING) then one periodic() should command INTAKE_SPEED");
    assertEquals(
        RollerSubsystem.State.INTAKING,
        roller.getMode(),
        "setMode must actually store the state — check that you assigned it to the field");
  }

  @Test
  @DisplayName("3. INTAKING with a piece already loaded stops the roller")
  void intakingStopsWhenLoaded() {
    setGamePiecePresent(true);
    roller.setMode(RollerSubsystem.State.INTAKING);
    step();

    assertTrue(roller.hasGamePiece(), "A broken beam means a game piece is present");
    assertEquals(
        0.0,
        motor.getSpeed(),
        PWM_TOLERANCE,
        "The decision 'stop once we have one' belongs to the roller, not to whoever pressed the "
            + "button. That move is the whole lesson.");
  }

  @Test
  @DisplayName("4. EJECTING always ejects; OFF always stops")
  void ejectingAndOff() {
    setGamePiecePresent(true);
    roller.setMode(RollerSubsystem.State.EJECTING);
    step();
    assertEquals(
        Constants.Roller.EJECT_SPEED,
        motor.getSpeed(),
        PWM_TOLERANCE,
        "Ejecting must work with a piece present — that is the only time you would ever do it");

    roller.setMode(RollerSubsystem.State.OFF);
    step();
    assertEquals(0.0, motor.getSpeed(), 1e-6, "OFF means off");
  }

  @Test
  @DisplayName("5. The hardware is private and final — nobody outside can touch it")
  void hardwareIsEncapsulated() {
    int found = 0;
    for (Field field : RollerSubsystem.class.getDeclaredFields()) {
      String type = field.getType().getSimpleName();
      if (type.equals("PWMSparkMax") || type.equals("DigitalInput")) {
        found++;
        assertTrue(
            Modifier.isPrivate(field.getModifiers()),
            "Field '"
                + field.getName()
                + "' must be private. A public motor is an invitation for two pieces of code to "
                + "command it at once, and the resulting bug is invisible in a code review.");
        assertTrue(
            Modifier.isFinal(field.getModifiers()),
            "Field '" + field.getName() + "' must be final — hardware is not reassigned at runtime");
      }
    }
    assertEquals(
        2, found, "Expected the motor and the beam-break to both still live in RollerSubsystem");
  }

  @Test
  @DisplayName("6. Robot.java no longer owns a motor or a sensor")
  void robotJavaIsClean() {
    String robotSource = "src/main/java/frc/robot/Robot.java";

    assertFalse(
        SourceInspector.mentionsOutsideComments(robotSource, "PWMSparkMax"),
        "Robot.java should no longer construct a motor controller. Delete the rollerMotor field "
            + "— the roller subsystem owns it now.");
    assertFalse(
        SourceInspector.mentionsOutsideComments(robotSource, "DigitalInput"),
        "Robot.java should no longer construct a DigitalInput. Delete the beamBreak field.");
    assertTrue(
        SourceInspector.mentionsOutsideComments(robotSource, "setMode"),
        "teleopPeriodic() should now express intent — roller.setMode(...) — rather than motor "
            + "speeds.");
  }

  private void setGamePiecePresent(boolean present) {
    beamBreak.setValue(!present);
  }
}
