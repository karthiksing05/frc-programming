package frc.robot.bindings;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.drive.Drive;

/**
 * Everything the person holding the driving controller can do.
 *
 * <p>By lesson 14, {@code RobotContainer.configureBindings()} has grown into a wall of unrelated
 * statements: drive bindings, scoring bindings, and climber bindings interleaved in the order you
 * happened to add them. Nothing is wrong with any single line. The problem is that answering "what
 * does the driver's left bumper do?" now means reading all of it.
 *
 * <p>The fix is boring and effective: one class per human. A bindings class takes the subsystems and
 * the controller it needs as constructor arguments, wires them in the constructor, and holds no
 * other state. {@code RobotContainer} shrinks back to "own the subsystems, hand them to the people
 * who use them".
 *
 * <p>This is <em>constructor injection</em>, and it is the same idea as a subsystem taking its IO
 * layer as an argument (lesson 16): a class should be handed what it depends on rather than reaching
 * out and finding it. Handed dependencies are visible in the signature, swappable in a test, and
 * impossible to acquire by accident.
 */
public class DriverBindings {

  /**
   * Wires the driver's controls. All the work happens here; there is nothing to call afterwards.
   *
   * @param drive the drivetrain
   * @param controller the driver's controller
   */
  public DriverBindings(Drive drive, CommandXboxController controller) {
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));

    // A panic button: hold to stop the drivetrain regardless of the sticks.
    controller.leftBumper().whileTrue(drive.stopCommand());
  }
}
