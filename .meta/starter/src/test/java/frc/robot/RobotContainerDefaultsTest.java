package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj.simulation.DIOSim;
import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 11 — Default commands done right, and trigger composition.
 *
 * <p>Two ideas, and they are two halves of the same one.
 *
 * <p><strong>Default commands</strong> answer "what does this mechanism do when nobody is asking?"
 * Every subsystem has an answer to that question whether or not you write one down; the difference
 * is whether the answer is "coast at zero" or "whatever it was doing when the last command got
 * cancelled".
 *
 * <p><strong>Trigger composition</strong> is what keeps default commands trivial. The moment you
 * want a default command that behaves differently depending on a sensor, you have discovered a
 * condition — and conditions belong in triggers, where they are declared once, in one place, in the
 * vocabulary of the problem.
 */
@Tag("lesson")
@Tag("lesson-11")
class RobotContainerDefaultsTest extends RobotTestBase {

  private RobotContainer container;
  private XboxControllerSim operator;
  private DIOSim beamBreak;

  @BeforeEach
  void setUp() {
    container = manage(new RobotContainer());
    operator = new XboxControllerSim(Constants.OperatorInterface.OPERATOR_PORT);
    beamBreak = new DIOSim(Constants.Roller.BEAM_BREAK_DIO);
    setGamePiecePresent(false);
    operator.setRawAxis(3, 0.0); // right trigger released
    operator.notifyNewData();
  }

  @Test
  @DisplayName("1. The drivetrain and the flywheels both have a default command")
  void defaultCommandsAreSet() {
    step(2);

    assertNotNull(
        container.getDrive().getDefaultCommand(),
        "The drivetrain's default command is the arcade drive from lesson 07. Without it the "
            + "robot ignores the sticks whenever nothing else is scheduled — which is most of "
            + "the match.");
    assertNotNull(
        container.getFlywheels().getDefaultCommand(),
        "The flywheels need an idle behaviour too. Give them flywheels.stopCommand().repeatedly() "
            + "so that 'nothing scheduled' means 'wound down', not 'still spinning from the last "
            + "shot'.");
  }

  @Test
  @DisplayName("2. Idle really is idle — nothing creeps when no button is held")
  void idleIsQuiet() {
    stepSeconds(0.5);

    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "With no buttons held the flywheels should be asking for zero");
    assertEquals(
        RollerSubsystem.State.OFF, container.getRoller().getMode(), "The roller should be off");
    assertEquals(
        0.0, container.getDrive().getLeftVolts(), 1e-6, "The drivetrain should not be creeping");
  }

  @Test
  @DisplayName("3. The composed trigger needs BOTH conditions — a game piece is not enough")
  void gamePieceAloneDoesNotScore() {
    setGamePiecePresent(true);
    stepSeconds(0.5);

    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "Having a game piece must not, by itself, start the scoring sequence. The robot picking "
            + "something up is not the robot deciding to shoot it.");
  }

  @Test
  @DisplayName("4. The composed trigger needs BOTH conditions — a button press is not enough")
  void buttonAloneDoesNotScore() {
    setGamePiecePresent(false);
    pullRightTrigger();
    stepSeconds(0.5);

    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "With nothing loaded, asking to score should do nothing. .and() means AND.");
  }

  @Test
  @DisplayName("5. Both conditions together fire the sequence, after the debounce settles")
  void bothConditionsScore() {
    setGamePiecePresent(true);
    pullRightTrigger();

    // The trigger is debounced by 100 ms, so nothing should happen instantly.
    step(2);
    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "The .debounce(0.1) should swallow the first 100 ms. If the sequence fires immediately, "
            + "the debounce is missing — and a beam-break that chatters at its threshold will "
            + "fire the shooter at random.");

    stepSeconds(0.5);
    assertTrue(
        container.getFlywheels().getTargetRpm() > 0.0,
        "Game piece present AND the operator asking should start the score sequence once the "
            + "debounce has settled.");
  }

  private void setGamePiecePresent(boolean present) {
    beamBreak.setValue(!present);
  }

  private void pullRightTrigger() {
    // Axis 3 is the right trigger on an Xbox controller.
    operator.setRawAxis(3, 1.0);
    operator.notifyNewData();
  }
}
