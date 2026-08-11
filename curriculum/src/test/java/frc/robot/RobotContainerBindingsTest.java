package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 08 — Joystick bindings & Triggers.
 *
 * <p>Lesson 07 gave the drivetrain a default command: something that runs continuously, driven by a
 * stick that is always producing a number. Buttons are a different shape of input — discrete events,
 * not continuous values — and they want a different mechanism.
 *
 * <p>These tests press buttons and watch what happens, including what happens on <em>release</em>,
 * because that is where the difference between {@code onTrue} and {@code whileTrue} lives and where
 * beginners most often pick the wrong one.
 */
@Tag("lesson")
@Tag("lesson-08")
class RobotContainerBindingsTest extends RobotTestBase {

  private RobotContainer container;
  private XboxControllerSim operator;

  @BeforeEach
  void setUp() {
    container = manage(new RobotContainer());
    operator = new XboxControllerSim(Constants.OperatorInterface.OPERATOR_PORT);
    releaseAll();
  }

  @Test
  @DisplayName("1. Holding A spins the flywheels up; releasing lets them coast")
  void aButtonSpinsFlywheels() {
    press(() -> operator.setAButton(true));
    step(5);

    assertTrue(
        container.getFlywheels().getTargetRpm() > 0.0,
        "Holding A should ask the flywheels for a non-zero target. Bind it with "
            + "operator.a().whileTrue(flywheels.spinUpCommand()).");

    press(() -> operator.setAButton(false));
    step(5);

    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "Releasing A must cancel the command. If the flywheels keep spinning you used onTrue, "
            + "which fires once and then leaves the command running — a shooter that never stops.");
  }

  @Test
  @DisplayName("2. Holding B intakes; holding X ejects")
  void rollerButtons() {
    press(() -> operator.setBButton(true));
    step(3);
    assertEquals(
        RollerSubsystem.State.INTAKING,
        container.getRoller().getMode(),
        "B should run the intake while held");

    press(
        () -> {
          operator.setBButton(false);
          operator.setXButton(true);
        });
    step(3);
    assertEquals(
        RollerSubsystem.State.EJECTING,
        container.getRoller().getMode(),
        "X should eject while held");

    press(() -> operator.setXButton(false));
    step(3);
    assertEquals(
        RollerSubsystem.State.OFF,
        container.getRoller().getMode(),
        "Releasing both must return the roller to OFF. That is what startEnd's second lambda is "
            + "for — it runs when the command ends, however it ends.");
  }

  @Test
  @DisplayName("3. Tapping Y sends the elevator to the high setpoint and it keeps going")
  void yButtonIsOnTrue() {
    press(() -> operator.setYButton(true));
    step(3);
    // Let go straight away — a tap, not a hold.
    press(() -> operator.setYButton(false));
    step(3);

    assertEquals(
        Constants.Elevator.HIGH_METERS,
        container.getElevator().getGoal(),
        1e-6,
        "Y should be bound with onTrue, not whileTrue. 'Go to L4' is a destination: the operator "
            + "taps it and the elevator finishes the journey by itself. With whileTrue, letting go "
            + "of the button abandons the elevator wherever it happens to be.");

    // And it should actually get there.
    stepSeconds(2.5);
    assertTrue(
        container.getElevator().atGoal(),
        String.format(
            "The elevator was commanded to %.2f m but is at %.2f m",
            Constants.Elevator.HIGH_METERS, container.getElevator().getHeightMeters()));
  }

  @Test
  @DisplayName("4. Nothing is bound to a toggle")
  void noTogglesOnDriverControls() {
    String source = frc.robot.testing.SourceInspector.read("src/main/java/frc/robot/RobotContainer.java");
    assertTrue(
        !frc.robot.testing.SourceInspector.mentionsOutsideComments(
            "src/main/java/frc/robot/RobotContainer.java", "toggleOnTrue"),
        "WPILib supports toggleOnTrue and this curriculum recommends against it for human "
            + "controls: a toggle asks the driver to remember state they cannot see, and under "
            + "match pressure they will not. Use whileTrue, or two separate buttons.");
    assertTrue(source.length() > 0);
  }

  private void press(Runnable buttons) {
    buttons.run();
    operator.notifyNewData();
  }

  private void releaseAll() {
    operator.setAButton(false);
    operator.setBButton(false);
    operator.setXButton(false);
    operator.setYButton(false);
    operator.notifyNewData();
  }
}
