package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

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

  // ───────────────────────────────────────────────────────────────────────────
  //  LESSON 03 HARDWARE — deliberately in the wrong place
  //
  //  These three fields belong to a mechanism, not to the robot's lifecycle.
  //  Putting them here means anybody who edits Robot.java can move the roller,
  //  and nothing stops two different pieces of code commanding it at once.
  //
  //  Lesson 04 deletes all three. Do not get attached to them.
  // ───────────────────────────────────────────────────────────────────────────
  private final PWMSparkMax rollerMotor = new PWMSparkMax(Constants.Roller.MOTOR_PWM);
  private final DigitalInput beamBreak = new DigitalInput(Constants.Roller.BEAM_BREAK_DIO);
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
    // TODO (LESSON 03): drive the roller from the operator's buttons and the
    //   beam-break sensor. Write this the obvious way — `if` / `else if` /
    //   `else`, directly here. You are not supposed to organise it yet.
    //
    //   The behaviour your team's drivers asked for:
    //
    //     · Hold B to intake  -> run the roller at Constants.Roller.INTAKE_SPEED
    //     · ...unless a game piece is already in the throat, in which case stop.
    //       The beam-break reads FALSE when the beam is broken (piece present),
    //       so `!beamBreak.get()` means "we have one".
    //     · Hold X to eject   -> Constants.Roller.EJECT_SPEED, and this WINS
    //       over B, because ejecting a piece you already have is the whole point.
    //     · Nothing held      -> 0.0
    //
    //   Useful calls:
    //       operator.getBButton()      // true while B is held
    //       operator.getXButton()      // true while X is held
    //       beamBreak.get()            // true when the beam is UNBROKEN
    //       rollerMotor.set(speed)     // speed in [-1, 1]
    //
    //   Check yourself with `./tools/frcprog check 03-conditionals-in-teleop`.
    //
    //   When it passes, count the lines. Then read the top of lesson 04.
    rollerMotor.set(0.0);
  }

  @Override
  public void disabledInit() {
    // Belt and braces: the FMS already cut motor output, but leaving a stale
    // non-zero command sitting in the motor controller means the mechanism jumps
    // the instant the robot is re-enabled.
    rollerMotor.set(0.0);
  }

  @Override
  public void close() {
    rollerMotor.close();
    beamBreak.close();
    robotContainer.close();
    super.close();
  }

  /** Exposed so tests can drive the same object the Driver Station would. */
  public RobotContainer getRobotContainer() {
    return robotContainer;
  }
}
