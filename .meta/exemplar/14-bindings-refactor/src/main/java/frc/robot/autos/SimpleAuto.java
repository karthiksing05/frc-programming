package frc.robot.autos;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.flywheels.Flywheels;
import frc.robot.subsystems.roller.RollerSubsystem;

/**
 * Autonomous routines built by hand, out of commands you already have.
 *
 * <p>An FRC match opens with fifteen seconds in which no human may touch a controller. Whatever the
 * robot does in that window, it decided by itself. That sounds like it needs new machinery; it does
 * not. An auto routine is just a command — usually a composition of the same factories teleop uses —
 * that happens to be scheduled by {@code autonomousInit()} instead of by a button.
 *
 * <p>That reuse is the payoff for lessons 07 through 11. If your teleop is built from small,
 * composable, requirement-aware commands, your auto is an afternoon's work. If it is built from a
 * pile of {@code if} statements in {@code teleopPeriodic}, you start over.
 *
 * <p>These are static factory methods rather than a class you instantiate: a routine is a recipe,
 * not a thing with state.
 */
public final class SimpleAuto {
  private SimpleAuto() {}

  /**
   * Drives forward two meters, then scores.
   *
   * @param drive the drivetrain
   * @param flywheels the shooter
   * @param roller the indexer feeding the shooter
   * @return a command suitable for returning from {@code getAutonomousCommand()}
   */
  public static Command driveAndScore(Drive drive, Flywheels flywheels, RollerSubsystem roller) {
    return Commands.sequence(
            drive.driveDistanceCommand(2.0, 6.0), scoreOnce(flywheels, roller))
        .withTimeout(8.0)
        .withName("Drive and Score");
  }

  /**
   * Spins up, waits for the wheels to reach speed, feeds one game piece.
   *
   * <p>Shared by the auto routines and (via {@code RobotContainer.scoreCommand}) by teleop, so that
   * fixing the scoring sequence fixes it everywhere.
   */
  public static Command scoreOnce(Flywheels flywheels, RollerSubsystem roller) {
    return flywheels
        .spinUpCommand()
        .alongWith(
            Commands.waitUntil(flywheels::isReadyToShoot)
                .andThen(roller.ejectCommand().withTimeout(0.4)))
        .withTimeout(2.0);
  }

  /** Does nothing for the whole autonomous period. A legitimate strategy, and a safe default. */
  public static Command doNothing() {
    return Commands.none().withName("Do Nothing");
  }
}
