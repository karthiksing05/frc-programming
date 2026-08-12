package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.autos.SimpleAuto;
import frc.robot.autos.TrajectoryAuto;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.elevator.ElevatorSubsystem;
import frc.robot.subsystems.flywheels.Flywheels;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.subsystems.shoulder.ShoulderSubsystem;

/**
 * Where the robot is assembled: subsystems are owned here, controllers are owned here, and every
 * decision about how the two connect is made here.
 *
 * <p>The organising principle — and it is the single most valuable idea in this curriculum —
 * is that <strong>subsystems never talk to each other</strong>. The elevator does not know the
 * roller exists. The flywheels have never heard of the drivetrain. Anything that needs two
 * mechanisms to cooperate is written in this file, as a command built out of their public factories.
 *
 * <p>Follow that rule and every subsystem stays independently readable and independently testable,
 * and there is exactly one file to open when you want to know what the robot does when a button is
 * pressed. Break it — {@code intake.scoreThenStow(elevator)} — and within a season you will have a
 * dependency graph nobody can hold in their head.
 *
 * <p>Three habits, from the FRC community's current consensus (Oblarg, 2025):
 *
 * <ol>
 *   <li>Control subsystems with command <em>factories</em>.
 *   <li>Get information out of subsystems with <em>triggers</em>.
 *   <li>Coordinate between subsystems by <em>binding commands to triggers</em>, here.
 * </ol>
 */
public class RobotContainer implements AutoCloseable {

  // ─── Subsystems ────────────────────────────────────────────────────────────
  private final Drive drive = new Drive();
  private final ElevatorSubsystem elevator = new ElevatorSubsystem();
  private final ShoulderSubsystem shoulder = new ShoulderSubsystem();
  private final Flywheels flywheels = new Flywheels();

  /**
   * The intake roller. Now that {@link Robot} no longer owns a motor on PWM 5, this subsystem can —
   * and the channel-allocation rule that made those two mutually exclusive is the framework
   * enforcing single ownership for you.
   */
  private final RollerSubsystem roller = new RollerSubsystem();

  // ─── Human inputs ──────────────────────────────────────────────────────────
  // CommandXboxController is the command-based flavour of XboxController: every
  // button is a Trigger you can bind commands to, rather than a boolean you have
  // to poll. Lesson 08 is where that difference starts to pay.
  private final CommandXboxController driver =
      new CommandXboxController(Constants.OperatorInterface.DRIVER_PORT);
  private final CommandXboxController operator =
      new CommandXboxController(Constants.OperatorInterface.OPERATOR_PORT);

  /**
   * The list of autonomous routines the driver may pick from, published to the dashboard so the
   * choice can be made at the field five minutes before a match without a rebuild.
   */
  private final SendableChooser<Command> autoChooser = new SendableChooser<>();

  public RobotContainer() {
    configureDefaultCommands();
    configureBindings();
    configureAutos();
  }

  /** Populates the dashboard's auto picker. */
  private void configureAutos() {
    // Always first, always safe. A robot that does nothing scores zero; a robot
    // running a half-finished routine can foul, or drive into a partner.
    autoChooser.setDefaultOption("Do Nothing", SimpleAuto.doNothing());

    autoChooser.addOption("Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));

    autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));

    SmartDashboard.putData("Auto Routine", autoChooser);
  }

  /**
   * Gives each subsystem something to do when nothing else has claimed it.
   *
   * <p>A default command runs whenever no other command requires that subsystem, and is interrupted
   * automatically the moment one does. It is the answer to "what should this mechanism do when
   * nobody is asking for anything?" — a question that has an answer whether or not you write one
   * down. Leave it blank and the answer becomes "whatever it was doing when the last command was
   * cancelled", which is how mechanisms drift.
   *
   * <p>Default commands should be <em>trivial</em>. If you find yourself writing an {@code if}
   * inside one, the decision belongs in a trigger instead.
   */
  private void configureDefaultCommands() {
    // The minus signs: an Xbox stick reads negative when pushed forward, because
    // the axis is measured screen-style with +Y pointing down.
    //
    // The `() ->`: without it, this would capture whatever the stick read during
    // robot startup — zero — and hold that value for the rest of the match.
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));

    // Trivial on purpose. A default command with an `if` in it is a decision
    // that wanted to be a trigger.
    flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
  }

  /**
   * Wires human intent to robot behaviour. Every binding in the robot lives here (until lesson 14
   * splits them into per-role classes).
   */
  private void configureBindings() {
    // Hold to spin, release to coast — the driver never has to remember whether
    // the shooter is currently on, because their thumb is the state.
    operator.a().whileTrue(flywheels.spinUpCommand());

    // Hold to intake, hold to eject. Two buttons rather than one toggle.
    operator.b().whileTrue(roller.intakeCommand());
    operator.x().whileTrue(roller.ejectCommand());

    // onTrue, not whileTrue: "go to L4" is a destination, not a thing you hold.
    // The command finishes by itself when the carriage arrives.
    operator.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    operator.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    operator.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));

    operator.rightBumper().whileTrue(scoreCommand());

    // "When we have a game piece AND the operator pulls the right trigger, score."
    // One line, in the vocabulary of the problem, with no if-statement anywhere
    // and no subsystem asking another subsystem a question.
    //
    // .debounce(0.1) ignores anything that has not been continuously true for
    // 100 ms — the standard cure for a beam-break that chatters at its threshold.
    roller
        .hasGamePieceTrigger
        .and(operator.rightTrigger())
        .debounce(0.1)
        .onTrue(scoreCommand());
  }

  /**
   * Spin up, wait for speed, feed the game piece, stop.
   *
   * <p>This is a <em>composition</em>: several simple commands glued together with operators that
   * read like English. It lives in {@code RobotContainer} rather than inside a subsystem precisely
   * because it needs two subsystems, and neither one should have to know about the other.
   */
  public Command scoreCommand() {
    // "Spin the flywheels up, and alongside that: wait until they are actually at
    // speed, then feed a game piece in for four tenths of a second. Give the
    // whole thing a second and a half before giving up."
    return flywheels
        .spinUpCommand()
        .alongWith(
            Commands.waitUntil(flywheels::isReadyToShoot)
                .andThen(roller.ejectCommand().withTimeout(0.4)))
        .withTimeout(1.5)
        .withName("Score");
  }

  /**
   * @return the command the robot runs during the fifteen-second autonomous period, or a
   *     do-nothing command if none is selected
   */
  public Command getAutonomousCommand() {
    Command selected = autoChooser.getSelected();
    return selected == null ? Commands.none() : selected;
  }

  // ─── Accessors, mostly for tests and for the lesson rubrics ────────────────

  public Drive getDrive() {
    return drive;
  }

  public ElevatorSubsystem getElevator() {
    return elevator;
  }

  public ShoulderSubsystem getShoulder() {
    return shoulder;
  }

  public Flywheels getFlywheels() {
    return flywheels;
  }

  /** @return the roller subsystem. */
  public RollerSubsystem getRoller() {
    return roller;
  }

  public CommandXboxController getDriverController() {
    return driver;
  }

  public CommandXboxController getOperatorController() {
    return operator;
  }

  @Override
  public void close() {
    drive.close();
    elevator.close();
    shoulder.close();
    flywheels.close();
    roller.close();
  }
}
