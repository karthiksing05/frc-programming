package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj.simulation.DIOSim;
import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 15 — Capstone.
 *
 * <p>No new concepts. Five scenarios, each one a thing a driver would actually do in a match, run
 * against the whole robot rather than against one subsystem in isolation. This is the difference
 * between "every part works" and "the robot works", and the gap between those two is where most of a
 * competition weekend goes.
 *
 * <p>If a scenario here fails while every earlier lesson passes, the bug is in an interaction — two
 * commands fighting for a subsystem, a default command stomping something, a sequence that assumed
 * a mechanism was already where it wanted it. Those are the interesting bugs, and finding them on a
 * laptop is enormously cheaper than finding them on a field.
 */
@Tag("lesson")
@Tag("lesson-15")
class CapstoneIntegrationTest extends RobotTestBase {

  private RobotContainer container;
  private XboxControllerSim driver;
  private XboxControllerSim operator;
  private DIOSim beamBreak;

  @BeforeEach
  void setUp() {
    container = manage(new RobotContainer());
    driver = new XboxControllerSim(Constants.OperatorInterface.DRIVER_PORT);
    operator = new XboxControllerSim(Constants.OperatorInterface.OPERATOR_PORT);
    beamBreak = new DIOSim(Constants.Roller.BEAM_BREAK_DIO);
    setGamePiecePresent(false);
    driver.notifyNewData();
    operator.notifyNewData();
  }

  @Test
  @DisplayName("1. Drive: push the stick, the robot moves; release, it stops")
  void scenarioDrive() {
    driver.setRawAxis(1, -1.0); // left Y, pushed forward (negative on Xbox)
    driver.notifyNewData();
    stepSeconds(1.0);

    double travelled = container.getDrive().getAverageDistanceMeters();
    assertTrue(travelled > 0.5, "The robot should have driven forward, went " + travelled + " m");

    driver.setRawAxis(1, 0.0);
    driver.notifyNewData();
    step(5);
    assertEquals(
        0.0,
        container.getDrive().getLeftVolts(),
        1e-6,
        "Releasing the stick must stop the drivetrain");
  }

  @Test
  @DisplayName("2. Intake: hold B until the beam breaks, then the roller backs off on its own")
  void scenarioIntake() {
    operator.setBButton(true);
    operator.notifyNewData();
    step(5);
    assertEquals(
        RollerSubsystem.State.INTAKING, container.getRoller().getMode(), "B should intake");
    assertTrue(container.getRoller().getOutput() > 0.0, "The roller should be pulling in");

    // A game piece arrives.
    setGamePiecePresent(true);
    step(5);
    assertEquals(
        0.0,
        container.getRoller().getOutput(),
        1e-6,
        "Once the beam breaks the roller should stop, without the operator doing anything. That "
            + "decision lives inside the subsystem, which is why it works even though the "
            + "operator is still holding the button.");
  }

  @Test
  @DisplayName("3. Score: the full sequence runs and returns everything to idle")
  void scenarioScore() {
    setGamePiecePresent(true);
    Command score = container.scoreCommand();
    CommandScheduler.getInstance().schedule(score);

    stepSeconds(2.0);

    assertTrue(!score.isScheduled(), "The score sequence should have finished by now");
    assertEquals(
        0.0,
        container.getFlywheels().getTargetRpm(),
        1e-6,
        "The flywheels should be back to idle once the sequence ends");
    assertEquals(
        RollerSubsystem.State.OFF, container.getRoller().getMode(), "The roller should be off");
  }

  @Test
  @DisplayName("4. Autonomous: a routine is selectable and it runs to completion")
  void scenarioAutonomous() {
    Command auto = container.getAutonomousCommand();
    assertNotNull(auto, "getAutonomousCommand must never return null");

    CommandScheduler.getInstance().schedule(auto);
    stepSeconds(10.0);

    assertTrue(
        !auto.isScheduled(),
        "The selected auto is still running after ten seconds. Autonomous is fifteen seconds "
            + "long; everything in it needs a bound.");
    assertTrue(
        Math.abs(container.getDrive().getLeftVolts()) < 0.1,
        "The drivetrain should be stopped when auto ends");
  }

  @Test
  @DisplayName("5. No subsystem is ever left in an undefined state")
  void scenarioNoUndefinedState() {
    // Do several things at once, then let go of everything, and check that the
    // robot settles into a defined idle rather than holding the last thing it
    // was told.
    driver.setRawAxis(1, -0.8);
    operator.setAButton(true);
    operator.setBButton(true);
    driver.notifyNewData();
    operator.notifyNewData();
    stepSeconds(0.5);

    driver.setRawAxis(1, 0.0);
    operator.setAButton(false);
    operator.setBButton(false);
    driver.notifyNewData();
    operator.notifyNewData();
    stepSeconds(0.5);

    assertEquals(0.0, container.getDrive().getLeftVolts(), 1e-6, "Drivetrain not idle");
    assertEquals(0.0, container.getDrive().getRightVolts(), 1e-6, "Drivetrain not idle");
    assertEquals(0.0, container.getFlywheels().getTargetRpm(), 1e-6, "Flywheels not idle");
    assertEquals(
        RollerSubsystem.State.OFF,
        container.getRoller().getMode(),
        "The roller is still in "
            + container.getRoller().getMode()
            + " with nothing held. Every mechanism needs a defined answer to 'what do you do when "
            + "nobody is asking?' — that is what default commands and startEnd's end-lambda are "
            + "for.");
  }

  private void setGamePiecePresent(boolean present) {
    beamBreak.setValue(!present);
  }
}
