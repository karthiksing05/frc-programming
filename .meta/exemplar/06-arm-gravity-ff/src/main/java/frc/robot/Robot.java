package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.roller.RollerSubsystem;

/**
 * The robot's lifecycle. WPILib calls the methods in this class for you, in a fixed order, fifty
 * times a second.
 *
 * <p>The four states that matter:
 *
 * <ul>
 *   <li><strong>disabled</strong> — powered on, motors forced off by the Field Management System.
 *       Everything still runs except actuator output.
 *   <li><strong>autonomous</strong> — the first fifteen seconds of a match. No driver input.
 *   <li><strong>teleop</strong> — the rest of the match. Drivers in control.
 *   <li><strong>test</strong> — pit-only mode for checking mechanisms.
 * </ul>
 *
 * <p>Each has an {@code xxxInit()} (runs once on entry) and an {@code xxxPeriodic()} (runs every 20
 * ms while in that state), plus {@link #robotPeriodic()} which runs in all of them.
 *
 * <p><strong>This file is a time capsule.</strong> Lesson 03 asks you to put real logic directly
 * into {@code teleopPeriodic()} — sensor reads, button reads, motor writes, all interleaved. That is
 * not a mistake. It is the shape almost every FRC codebase starts in, it works, and by the time you
 * have finished writing it you will be able to feel exactly what is wrong with it. Lesson 04 then
 * takes it away from you, and the relief is the lesson.
 */
public class Robot extends TimedRobot {

  private final RobotContainer robotContainer = new RobotContainer();

  private Command autonomousCommand;

  // The motor and the sensor are gone. They live in RollerSubsystem now, where
  // they are private and nobody outside that file can reach them. What is left
  // here is the only thing that was ever Robot's business: reading a human and
  // saying what the robot should be doing about it.
  private final XboxController operator =
      new XboxController(Constants.OperatorInterface.OPERATOR_PORT);

  @Override
  public void robotPeriodic() {
    // One line, and everything command-based depends on it.
    //
    // The scheduler is what calls periodic() on every registered subsystem, runs
    // whatever commands are scheduled, and enforces the rule that two commands
    // may never require the same subsystem at once. It runs in EVERY mode,
    // including disabled, which is why telemetry keeps updating when the robot
    // is sitting on the field waiting for the match to start.
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    autonomousCommand = robotContainer.getAutonomousCommand();
    if (autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    // Autonomous is over; whatever it was doing must stop before a human takes
    // over. Forgetting this is how a robot drives itself into a wall while the
    // driver frantically pushes a stick that is being ignored.
    if (autonomousCommand != null) {
      CommandScheduler.getInstance().cancel(autonomousCommand);
    }
  }

  @Override
  public void teleopPeriodic() {
    // Four lines, and not one of them mentions a motor, a voltage, or a sensor.
    // Robot.java now says WHAT the operator wants; RollerSubsystem decides HOW.
    //
    // The beam-break check disappeared entirely — not because we stopped caring,
    // but because "don't keep intaking once you have one" was never the driver's
    // decision to make. It belonged to the roller, and now it lives there.
    RollerSubsystem roller = robotContainer.getRoller();
    if (operator.getXButton()) {
      roller.setMode(RollerSubsystem.State.EJECTING);
    } else if (operator.getBButton()) {
      roller.setMode(RollerSubsystem.State.INTAKING);
    } else {
      roller.setMode(RollerSubsystem.State.OFF);
    }
  }

  @Override
  public void disabledInit() {
    // Still worth doing, but notice it is now expressed as intent rather than as
    // a raw motor write.
    robotContainer.getRoller().setMode(RollerSubsystem.State.OFF);
  }

  @Override
  public void close() {
    robotContainer.close();
    super.close();
  }

  /** Exposed so tests can drive the same object the Driver Station would. */
  public RobotContainer getRobotContainer() {
    return robotContainer;
  }
}
