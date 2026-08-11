package frc.robot.bindings;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants;
import frc.robot.subsystems.elevator.ElevatorSubsystem;
import frc.robot.subsystems.flywheels.Flywheels;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.subsystems.shoulder.ShoulderSubsystem;

/**
 * Everything the person holding the second controller can do: mechanisms, not driving.
 *
 * <p>Sibling to {@link DriverBindings}. Splitting by <em>human role</em> rather than by subsystem is
 * deliberate — when a driver says "the intake button isn't working", you want one file to open, and
 * the file you want is the one named after them.
 *
 * <p>Note what this class takes and what it does not. It takes four subsystems and a controller. It
 * does not take {@code RobotContainer}, and it has no way to reach the drivetrain. A class that
 * cannot reach something cannot accidentally couple to it.
 */
public class OperatorBindings {

  /**
   * @param elevator the elevator
   * @param shoulder the arm
   * @param flywheels the shooter
   * @param roller the intake
   * @param controller the operator's controller
   * @param scoreCommand the multi-subsystem scoring sequence, built in {@code RobotContainer}
   *     because it spans subsystems this class holds and ones it does not
   */
  public OperatorBindings(
      ElevatorSubsystem elevator,
      ShoulderSubsystem shoulder,
      Flywheels flywheels,
      RollerSubsystem roller,
      CommandXboxController controller,
      Command scoreCommand) {
    controller.a().whileTrue(flywheels.spinUpCommand());
    controller.b().whileTrue(roller.intakeCommand());
    controller.x().whileTrue(roller.ejectCommand());
    controller.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    controller.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    controller.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
    controller.rightBumper().whileTrue(scoreCommand);

    roller.hasGamePieceTrigger.and(controller.rightTrigger()).debounce(0.1).onTrue(scoreCommand);
  }
}
