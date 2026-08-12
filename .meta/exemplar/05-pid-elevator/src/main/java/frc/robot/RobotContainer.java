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

    // TODO (LESSON 12): add your first real routine.
    //
    //   autoChooser.addOption(
    //       "Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));

    // TODO (LESSON 13): add the trajectory routine.
    //
    //   autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));

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
    // TODO (LESSON 07): give the drivetrain its default command.
    //
    //   drive.setDefaultCommand(
    //       drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));
    //
    //   The minus signs are not a typo. An Xbox stick reports NEGATIVE when
    //   pushed forward, because the underlying axis is measured screen-style with
    //   +Y pointing down. Every FRC codebase negates it; now you know why.
    //
    //   Note the `() ->`. Without it you would be passing the value of the stick
    //   at robot-startup time — which is zero, forever. Lesson 07's hints walk
    //   you through breaking it on purpose so you can see the failure.

    // TODO (LESSON 11): give the flywheels an idle behaviour too.
    //
    //   flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());
  }

  /**
   * Wires human intent to robot behaviour. Every binding in the robot lives here (until lesson 14
   * splits them into per-role classes).
   */
  private void configureBindings() {
    // TODO (LESSON 08): bind the operator's buttons.
    //
    //   operator.a().whileTrue(flywheels.spinUpCommand());
    //   operator.b().whileTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    //   operator.y().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
    //
    //   Four semantics exist and the difference matters:
    //     onTrue     — fire once when the button goes down; the command then runs
    //                  to its own completion, button or no button.
    //     whileTrue  — run while held; cancel on release.
    //     onFalse    — fire once when released.
    //     whileFalse — run while NOT held. Rare, and usually a sign you inverted
    //                  something upstream.
    //
    //   There is a fifth, toggleOnTrue, and this curriculum recommends against it
    //   for driver controls. A toggle forces the human to remember robot state
    //   that they cannot see, and in a match, under pressure, they will not.

    // TODO (LESSON 09): bind the scoring sequence.
    //
    //   operator.rightBumper().whileTrue(scoreCommand());

    // TODO (LESSON 11): compose triggers, don't nest ifs.
    //
    //   Fire the score sequence only when a game piece is actually present AND
    //   the operator asks for it:
    //
    //   new Trigger(() -> roller.hasGamePiece())
    //       .and(operator.rightTrigger())
    //       .debounce(0.1)
    //       .onTrue(scoreCommand());
    //
    //   `.debounce(0.1)` ignores anything that has not been continuously true for
    //   100 ms — the standard cure for a sensor that chatters at the threshold.
  }

  /**
   * Spin up, wait for speed, feed the game piece, stop.
   *
   * <p>This is a <em>composition</em>: several simple commands glued together with operators that
   * read like English. It lives in {@code RobotContainer} rather than inside a subsystem precisely
   * because it needs two subsystems, and neither one should have to know about the other.
   */
  public Command scoreCommand() {
    // TODO (LESSON 09): build the sequence.
    //
    //   return flywheels
    //       .spinUpCommand()
    //       .alongWith(
    //           Commands.waitUntil(flywheels::isReadyToShoot)
    //               .andThen(roller.ejectCommand().withTimeout(0.4)))
    //       .withTimeout(1.5);
    //
    //   Read it aloud: "spin the flywheels up, and alongside that, wait until
    //   they are ready and then run the roller for four tenths of a second —
    //   and give the whole thing a second and a half before you give up."
    //
    //   Two rules this encodes:
    //     · NEVER Thread.sleep() or Timer.delay() in robot code. They block the
    //       scheduler, which means every other command on the robot stops too.
    //       Commands.waitSeconds / waitUntil yield instead of blocking.
    //     · Put a timeout on anything that waits for a sensor. A beam-break that
    //       fails during a match should cost you one scoring cycle, not the
    //       remaining ninety seconds.
    return Commands.none();
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
