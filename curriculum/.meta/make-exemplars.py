#!/usr/bin/env python3
"""Regenerate .meta/exemplar/<slug>/ from the starter sources in src/.

WHO THIS IS FOR
    Curriculum authors and mentors. Students never run it — for them the
    exemplars are just files that `frcprog solution <lesson>` copies into place.

WHY IT EXISTS
    Every lesson's reference answer is "the starter file, with one specific TODO
    replaced". Storing thirty hand-maintained near-duplicates of the same six
    Java files is how exemplars silently drift out of sync with starters: someone
    fixes a typo in a comment in src/, nobody updates .meta/, and six months
    later `frcprog solution 07-tank-drive` hands a student a file that no longer
    matches the lesson they are reading.

    So the answers live here, as explicit (before -> after) text patches, and the
    exemplars are generated. Patches are applied CUMULATIVELY in lesson order,
    which is what makes lesson 09's exemplar of RobotContainer.java contain
    lessons 07 and 08's work too — exactly as a real student's file would.

    If a patch's `before` text is not found, this script fails loudly. That is
    the point: editing a starter in a way that invalidates an answer should break
    something immediately, not eventually.

USAGE
    python3 .meta/make-exemplars.py            # regenerate everything
    python3 .meta/make-exemplars.py --check    # verify committed exemplars match
"""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXEMPLAR_ROOT = ROOT / ".meta" / "exemplar"

MATH_UTILS = "src/main/java/frc/robot/util/MathUtils.java"
CONSTANTS = "src/main/java/frc/robot/Constants.java"
ROBOT = "src/main/java/frc/robot/Robot.java"
CONTAINER = "src/main/java/frc/robot/RobotContainer.java"
ROLLER = "src/main/java/frc/robot/subsystems/roller/RollerSubsystem.java"
ELEVATOR = "src/main/java/frc/robot/subsystems/elevator/ElevatorSubsystem.java"
SHOULDER = "src/main/java/frc/robot/subsystems/shoulder/ShoulderSubsystem.java"
DRIVE = "src/main/java/frc/robot/subsystems/drive/Drive.java"
DRIVE_IO_SIM = "src/main/java/frc/robot/subsystems/drive/DriveIOSim.java"
FLYWHEELS = "src/main/java/frc/robot/subsystems/flywheels/Flywheels.java"
SIMPLE_AUTO = "src/main/java/frc/robot/autos/SimpleAuto.java"
TRAJ_AUTO = "src/main/java/frc/robot/autos/TrajectoryAuto.java"
DRIVER_BINDINGS = "src/main/java/frc/robot/bindings/DriverBindings.java"
OPERATOR_BINDINGS = "src/main/java/frc/robot/bindings/OperatorBindings.java"


# ─────────────────────────────────────────────────────────────────────────────
#  The answers.
#
#  LESSONS is an ordered list of (slug, [(path, before, after), ...]).
#  Text is matched exactly and must appear exactly once.
# ─────────────────────────────────────────────────────────────────────────────

LESSONS: list[tuple[str, list[tuple[str, str, str]]]] = []


def lesson(slug: str, *patches: tuple[str, str, str]) -> None:
    LESSONS.append((slug, list(patches)))


# ── 01 · Methods ────────────────────────────────────────────────────────────
lesson(
    "01-methods",
    (
        MATH_UTILS,
        """    // TODO (LESSON 01): Return 0.0 when the magnitude of `value` is smaller than
    //   `threshold`, and return `value` untouched otherwise. `Math.abs(x)` gives
    //   you the magnitude of x.
    //
    //   Two calls that must both work:
    //     applyDeadband( 0.05, 0.1)  ->  0.0    (inside the band, both signs)
    //     applyDeadband(-0.80, 0.1)  -> -0.80   (outside the band, sign kept)
    //
    //   Run `./tools/frcprog check 01-methods` when you think you have it.
    return value;""",
        """    // Math.abs() collapses the two sign cases into one comparison, so this
    // handles a stick pushed either way without a second branch.
    if (Math.abs(value) < threshold) {
      return 0.0;
    }
    return value;""",
    ),
)

# ── 02 · Variables & types ──────────────────────────────────────────────────
lesson(
    "02-variables-and-types",
    (
        CONSTANTS,
        """    // ────────────────────────────────────────────────────────────────────────
    // TODO (LESSON 02): three of the four constants below are placeholders.
    //
    //   DEADBAND    — should be 0.10. Right now it is 0.0, which means "no
    //                 deadband at all", which is exactly the bug lesson 01
    //                 taught you to fix. Set it to the real value.
    //   MAX_VOLTS   — should be 12.0. The most voltage we will ever command.
    //   GEAR_RATIO  — should be 8.45. Motor rotations per wheel rotation on a
    //                 stock AndyMark toughbox mini.
    //
    // Leave the field names and the `public static final double` exactly as
    // they are — the rubric checks the modifiers by reflection, not just the
    // values, because a constant that isn't `final` isn't a constant.
    // ────────────────────────────────────────────────────────────────────────

    /** Joystick readings smaller than this are treated as zero. */
    public static final double DEADBAND = 0.0;

    /** Ceiling on commanded voltage, so a runaway PID can't ask for 40 volts. */
    public static final double MAX_VOLTS = 0.0;

    /** Motor rotations per wheel rotation. */
    public static final double GEAR_RATIO = 1.0;""",
        """    /** Joystick readings smaller than this are treated as zero. */
    public static final double DEADBAND = 0.10;

    /** Ceiling on commanded voltage, so a runaway PID can't ask for 40 volts. */
    public static final double MAX_VOLTS = 12.0;

    /** Motor rotations per wheel rotation. */
    public static final double GEAR_RATIO = 8.45;""",
    ),
    (
        CONSTANTS,
        """    // TODO (LESSON 02): should be 5800.0 — the free speed of the motor in RPM.
    public static final double MAX_RPM = 0.0;""",
        """    /** Free speed of the shooter motor, in RPM. */
    public static final double MAX_RPM = 5800.0;""",
    ),
)

# ── 03 · Conditionals in teleopPeriodic ─────────────────────────────────────
lesson(
    "03-conditionals-in-teleop",
    (
        ROBOT,
        """    // TODO (LESSON 03): drive the roller from the operator's buttons and the
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
    rollerMotor.set(0.0);""",
        """    // The beam-break pulls high when nothing interrupts it, so a broken beam —
    // a game piece sitting in the throat — reads false.
    boolean hasGamePiece = !beamBreak.get();

    if (operator.getXButton()) {
      // Eject wins over everything. Checking it first is what makes it win.
      rollerMotor.set(Constants.Roller.EJECT_SPEED);
    } else if (operator.getBButton() && !hasGamePiece) {
      rollerMotor.set(Constants.Roller.INTAKE_SPEED);
    } else {
      rollerMotor.set(0.0);
    }

    // Count the lines. Now imagine the elevator, the arm, the shooter and the
    // climber all living here too, each with its own sensor and its own set of
    // buttons, all sharing one `else`. That is lesson 04's problem to solve.""",
    ),
)

# ── 04 · Subsystems as state machines ───────────────────────────────────────
lesson(
    "04-subsystems-state-machines",
    (
        ROLLER,
        """    // TODO (LESSON 04): store `desired` in the `state` field.
    //   One line. Note what this method does NOT do: it does not touch the
    //   motor. Deciding is instant; acting happens in periodic(), fifty times
    //   a second. Keeping those two things separate is most of what a
    //   subsystem is for.""",
        """    state = desired;""",
    ),
    (
        ROLLER,
        """    // TODO (LESSON 04): translate `state` into a motor output, then send it.
    //
    //   OFF       -> 0.0
    //   INTAKING  -> Constants.Roller.INTAKE_SPEED, unless hasGamePiece() is
    //                already true, in which case 0.0. (Do not keep grinding a
    //                game piece against a hard stop; that is how rollers eat
    //                belts.)
    //   EJECTING  -> Constants.Roller.EJECT_SPEED, always. Ejecting has to work
    //                even when the sensor says a piece is present, because a
    //                piece being present is the whole reason you are ejecting.
    //
    //   A `switch` on the enum reads best. Whatever you compute, finish with:
    //       lastOutput = output;
    //       motor.set(output);
    //   so that getOutput() and the real motor never disagree.
    lastOutput = 0.0;
    motor.set(0.0);""",
        """    double output =
        switch (state) {
          case OFF -> 0.0;
          case INTAKING -> hasGamePiece() ? 0.0 : Constants.Roller.INTAKE_SPEED;
          case EJECTING -> Constants.Roller.EJECT_SPEED;
        };

    lastOutput = output;
    motor.set(output);""",
    ),
    (
        ROBOT,
        """import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;""",
        """import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.roller.RollerSubsystem;""",
    ),
    (
        ROBOT,
        """  // ───────────────────────────────────────────────────────────────────────────
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
      new XboxController(Constants.OperatorInterface.OPERATOR_PORT);""",
        """  // The motor and the sensor are gone. They live in RollerSubsystem now, where
  // they are private and nobody outside that file can reach them. What is left
  // here is the only thing that was ever Robot's business: reading a human and
  // saying what the robot should be doing about it.
  private final XboxController operator =
      new XboxController(Constants.OperatorInterface.OPERATOR_PORT);""",
    ),
    (
        ROBOT,
        """    // The beam-break pulls high when nothing interrupts it, so a broken beam —
    // a game piece sitting in the throat — reads false.
    boolean hasGamePiece = !beamBreak.get();

    if (operator.getXButton()) {
      // Eject wins over everything. Checking it first is what makes it win.
      rollerMotor.set(Constants.Roller.EJECT_SPEED);
    } else if (operator.getBButton() && !hasGamePiece) {
      rollerMotor.set(Constants.Roller.INTAKE_SPEED);
    } else {
      rollerMotor.set(0.0);
    }

    // Count the lines. Now imagine the elevator, the arm, the shooter and the
    // climber all living here too, each with its own sensor and its own set of
    // buttons, all sharing one `else`. That is lesson 04's problem to solve.""",
        """    // Four lines, and not one of them mentions a motor, a voltage, or a sensor.
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
    }""",
    ),
    (
        ROBOT,
        """  @Override
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
  }""",
        """  @Override
  public void disabledInit() {
    // Still worth doing, but notice it is now expressed as intent rather than as
    // a raw motor write.
    robotContainer.getRoller().setMode(RollerSubsystem.State.OFF);
  }

  @Override
  public void close() {
    robotContainer.close();
    super.close();
  }""",
    ),
    (
        CONTAINER,
        """  /**
   * The intake roller — deliberately not constructed yet.
   *
   * <p>At the start of the curriculum the roller's motor and sensor are owned by {@link Robot},
   * because that is where lesson 03 puts them. WPILib allocates a PWM channel to exactly one object;
   * constructing a {@link RollerSubsystem} here as well would fail at startup with "PWM 5 already
   * allocated". That error is not a nuisance, it is the framework telling you the truth: two things
   * cannot own one motor.
   *
   * <p>Lesson 04 resolves it by deleting the fields from {@code Robot} and constructing the
   * subsystem here instead.
   */
  private RollerSubsystem roller = null;""",
        """  /**
   * The intake roller. Now that {@link Robot} no longer owns a motor on PWM 5, this subsystem can —
   * and the channel-allocation rule that made those two mutually exclusive is the framework
   * enforcing single ownership for you.
   */
  private final RollerSubsystem roller = new RollerSubsystem();""",
    ),
    (
        CONTAINER,
        """  /** @return the roller subsystem, or null before lesson 04 has constructed it. */
  public RollerSubsystem getRoller() {
    return roller;
  }""",
        """  /** @return the roller subsystem. */
  public RollerSubsystem getRoller() {
    return roller;
  }""",
    ),
    (
        CONTAINER,
        """    if (roller != null) {
      roller.close();
    }""",
        """    roller.close();""",
    ),
)

# ── 05 · PID (elevator) ─────────────────────────────────────────────────────
lesson(
    "05-pid-elevator",
    (
        ELEVATOR,
        """    // TODO (LESSON 05): compute the feedback term and command the motor.
    //
    //   1. Ask the controller what to do about the gap between where we are and
    //      where we want to be:
    //          double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);
    //
    //   2. Add the gravity term, then clamp so a wild gain cannot ask for 40 V:
    //          double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
    //
    //   3. Send it, and record it:
    //          appliedVolts = volts;
    //          motor.setVoltage(volts);
    //
    //   Then open Constants.Elevator and tune kP / kI / kD. The recipe:
    //     · Everything at zero. Raise kP until the carriage arrives quickly but
    //       rings (bounces past the setpoint and back).
    //     · Raise kD until the ringing damps out.
    //     · If it parks a centimetre or two short forever, add a sliver of kI.
    //
    //   `./tools/frcprog check 05-pid-elevator` drives all four setpoints and
    //   checks arrival, settling time, and overshoot.
    appliedVolts = 0.0;
    motor.setVoltage(0.0);""",
        """    // Feedback: how hard to push, given how far off we are right now.
    double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);

    // Clamping is not optional. A large kP multiplied by a large startup error
    // asks for a voltage that does not exist, and on real hardware the motor
    // controller's own limiting will produce behaviour you did not model.
    double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);

    appliedVolts = volts;
    motor.setVoltage(volts);""",
    ),
    (
        CONSTANTS,
        """    // TODO (LESSON 05): tune these three. They start at zero, which means the
    //   controller does nothing at all and the carriage sits on the floor.
    public static final double kP = 0.0;
    public static final double kI = 0.0;
    public static final double kD = 0.0;""",
        """    /**
     * Tuned by the recipe in lesson 05: raise kP until it rings, add kD until the ringing stops,
     * add kI only if it parks short. These numbers are for THIS mechanism at THIS mass; copying
     * them onto a different elevator is how tuning lore starts.
     */
    public static final double kP = 40.0;

    public static final double kI = 0.0;
    public static final double kD = 6.0;""",
    ),
)

# ── 06 · Arm with gravity feedforward ───────────────────────────────────────
lesson(
    "06-arm-gravity-ff",
    (
        SHOULDER,
        """    // TODO (LESSON 06): compute the gravity feedforward and add it in.
    //
    //   Replace the 0.0 below with the volts needed to hold the arm at its
    //   CURRENT angle:
    //       double gravityVolts = Constants.Shoulder.kG * Math.cos(getAngleRadians());
    //
    //   Read that again and notice what it is not: it is not cos(setpoint). You
    //   are cancelling the force acting on the arm right now, not the force that
    //   will act on it when it arrives. Using the setpoint is a real bug that
    //   looks correct and behaves badly during long travels.
    //
    //   Then open Constants.Shoulder and set kG to 0.12 (volts). The rubric
    //   deliberately re-runs with kG = 0 and requires that the arm FAILS to hold
    //   position — proving the feedforward is doing real work rather than being
    //   masked by a large kP.
    double gravityVolts = 0.0;""",
        """    // cos() of the CURRENT angle, not of the setpoint: we are cancelling the
    // torque acting on the arm at this instant. At 0 rad (horizontal) cos is 1
    // and gravity is at its worst; at ±90° cos is 0 and gravity does nothing.
    double gravityVolts = Constants.Shoulder.kG * Math.cos(getAngleRadians());""",
    ),
    (
        CONSTANTS,
        """    // TODO (LESSON 06): gravity feedforward gain, in volts. This is how many
    //   volts it takes to hold the arm perfectly horizontal against gravity.
    //   Starts at 0.0, which means "no gravity compensation" — the arm will
    //   droop below every setpoint. The right value for this arm is 0.12.
    public static final double kG = 0.0;""",
        """    /**
     * Volts needed to hold the arm horizontal against gravity. Measured, not guessed: command the
     * arm to sit level, raise the voltage until it stops falling, write that number down.
     */
    public static final double kG = 0.12;""",
    ),
)

# ── 07 · Tank drive (factories + suppliers) ─────────────────────────────────
lesson(
    "07-tank-drive",
    (
        DRIVE,
        """          // TODO (LESSON 07): implement arcade drive.
          //
          //   1. Read the axes THROUGH THE SUPPLIERS, inside this lambda:
          //          double fwd = forward.getAsDouble();
          //          double rot = rotation.getAsDouble();
          //      (Reading them outside the lambda is the bug this lesson is
          //      about. The hints file walks you through causing it on purpose.)
          //
          //   2. Apply your own deadband from lesson 01 to each one, using the
          //      constant you named in lesson 02:
          //          fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
          //          rot = MathUtils.applyDeadband(rot, Constants.Drive.DEADBAND);
          //
          //   3. Mix, scale to volts, and send:
          //          double left  = MathUtil.clamp(fwd + rot, -1.0, 1.0);
          //          double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);
          //          setVoltage(left * Constants.Drive.MAX_VOLTS,
          //                     right * Constants.Drive.MAX_VOLTS);
          //
          //   Three lessons meet in step 2. That is not decoration — it is the
          //   point of a curriculum that grows one project instead of thirty.
          setVoltage(0.0, 0.0);""",
        """          // Read the sticks HERE, inside the lambda, every loop. This is the
          // whole reason the parameters are suppliers.
          double fwd = forward.getAsDouble();
          double rot = rotation.getAsDouble();

          // Lesson 01's method, called with lesson 02's constant.
          fwd = MathUtils.applyDeadband(fwd, Constants.Drive.DEADBAND);
          rot = MathUtils.applyDeadband(rot, Constants.Drive.DEADBAND);

          // Arcade mixing. Clamp before scaling so that full throttle plus full
          // turn saturates at one side stopped rather than asking for 24 volts.
          double left = MathUtil.clamp(fwd + rot, -1.0, 1.0);
          double right = MathUtil.clamp(fwd - rot, -1.0, 1.0);

          setVoltage(left * Constants.Drive.MAX_VOLTS, right * Constants.Drive.MAX_VOLTS);""",
    ),
    (
        CONTAINER,
        """    // TODO (LESSON 07): give the drivetrain its default command.
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
    //   you through breaking it on purpose so you can see the failure.""",
        """    // The minus signs: an Xbox stick reads negative when pushed forward, because
    // the axis is measured screen-style with +Y pointing down.
    //
    // The `() ->`: without it, this would capture whatever the stick read during
    // robot startup — zero — and hold that value for the rest of the match.
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));""",
    ),
)

# ── 08 · Triggers & bindings ────────────────────────────────────────────────
lesson(
    "08-triggers-bindings",
    (
        CONTAINER,
        """    // TODO (LESSON 08): bind the operator's buttons.
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
    //   that they cannot see, and in a match, under pressure, they will not.""",
        """    // Hold to spin, release to coast — the driver never has to remember whether
    // the shooter is currently on, because their thumb is the state.
    operator.a().whileTrue(flywheels.spinUpCommand());

    // Hold to intake, hold to eject. Two buttons rather than one toggle.
    operator.b().whileTrue(roller.intakeCommand());
    operator.x().whileTrue(roller.ejectCommand());

    // onTrue, not whileTrue: "go to L4" is a destination, not a thing you hold.
    // The command finishes by itself when the carriage arrives.
    operator.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    operator.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    operator.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));""",
    ),
)

# ── 09 · Command composition ────────────────────────────────────────────────
lesson(
    "09-command-composition",
    (
        CONTAINER,
        """    // TODO (LESSON 09): build the sequence.
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
    return Commands.none();""",
        """    // "Spin the flywheels up, and alongside that: wait until they are actually at
    // speed, then feed a game piece in for four tenths of a second. Give the
    // whole thing a second and a half before giving up."
    return flywheels
        .spinUpCommand()
        .alongWith(
            Commands.waitUntil(flywheels::isReadyToShoot)
                .andThen(roller.ejectCommand().withTimeout(0.4)))
        .withTimeout(1.5)
        .withName("Score");""",
    ),
    (
        CONTAINER,
        """    // TODO (LESSON 09): bind the scoring sequence.
    //
    //   operator.rightBumper().whileTrue(scoreCommand());""",
        """    operator.rightBumper().whileTrue(scoreCommand());""",
    ),
)

# ── 10 · Telemetry ──────────────────────────────────────────────────────────
lesson(
    "10-telemetry",
    (
        FLYWHEELS,
        """  // TODO (LESSON 10): two more publishers, built exactly like targetPublisher.
  //   Name them actualPublisher ("ActualRPM") and errorPublisher ("ErrorRPM").
  //   Uncomment and finish:
  //
  // private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
  // private final DoublePublisher errorPublisher  = table.getDoubleTopic("ErrorRPM").publish();""",
        """  private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
  private final DoublePublisher errorPublisher = table.getDoubleTopic("ErrorRPM").publish();""",
    ),
    (
        FLYWHEELS,
        """    // TODO (LESSON 10): publish the other two values, every loop.
    //
    //   actualPublisher.set(getVelocityRpm());
    //   errorPublisher.set(getErrorRpm());
    //
    //   Then run `./tools/frcprog sim`, open AdvantageScope, connect to
    //   NetworkTables at localhost, and drop all three onto one line chart.
    //   Hold the shoot button. What you are looking at is a step response: the
    //   gap between the flat target line and the rising actual line IS the error
    //   line. Being able to read that picture is most of what debugging a
    //   mechanism means.""",
        """    actualPublisher.set(getVelocityRpm());
    errorPublisher.set(getErrorRpm());

    // Three traces on one chart in AdvantageScope, and the shape of the gap
    // between target and actual tells you which gain to reach for. That picture
    // is the difference between tuning and guessing.""",
    ),
    (
        FLYWHEELS,
        """    motor.close();
    encoder.close();
    targetPublisher.close();""",
        """    motor.close();
    encoder.close();
    targetPublisher.close();
    actualPublisher.close();
    errorPublisher.close();""",
    ),
)

# ── 11 · Default commands done right ────────────────────────────────────────
lesson(
    "11-default-commands",
    (
        CONTAINER,
        """    // TODO (LESSON 11): give the flywheels an idle behaviour too.
    //
    //   flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());""",
        """    // Trivial on purpose. A default command with an `if` in it is a decision
    // that wanted to be a trigger.
    flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());""",
    ),
    (
        CONTAINER,
        """    // TODO (LESSON 11): compose triggers, don't nest ifs.
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
    //   100 ms — the standard cure for a sensor that chatters at the threshold.""",
        """    // "When we have a game piece AND the operator pulls the right trigger, score."
    // One line, in the vocabulary of the problem, with no if-statement anywhere
    // and no subsystem asking another subsystem a question.
    //
    // .debounce(0.1) ignores anything that has not been continuously true for
    // 100 ms — the standard cure for a beam-break that chatters at its threshold.
    roller
        .hasGamePieceTrigger
        .and(operator.rightTrigger())
        .debounce(0.1)
        .onTrue(scoreCommand());""",
    ),
)

# ── 12 · Auto routines (basic) ──────────────────────────────────────────────
lesson(
    "12-auto-basic",
    (
        SIMPLE_AUTO,
        """    // TODO (LESSON 12): compose the routine.
    //
    //   return Commands.sequence(
    //           drive.driveDistanceCommand(2.0, 6.0),
    //           scoreOnce(flywheels, roller))
    //       .withTimeout(8.0);
    //
    //   Three things to notice, all of which will save you a match one day:
    //
    //   1. Commands.sequence runs its arguments one after another, each waiting
    //      for the previous to FINISH. That is different from Commands.parallel
    //      (all at once, done when the last finishes) and from
    //      Commands.deadline (all at once, done when the FIRST one finishes).
    //
    //   2. .withTimeout(8.0) on the whole routine. Auto is fifteen seconds. A
    //      routine that waits forever for a sensor that has come unplugged will
    //      still be waiting when teleop starts, and it will fight your driver
    //      for the drivetrain. Bound everything.
    //
    //   3. There is no Thread.sleep anywhere, and there never will be. Sleeping
    //      blocks the single thread the scheduler runs on — every subsystem's
    //      periodic() stops, odometry stops updating, and the robot goes deaf
    //      for the duration. Commands.waitSeconds yields instead.
    return Commands.none();""",
        """    return Commands.sequence(
            drive.driveDistanceCommand(2.0, 6.0), scoreOnce(flywheels, roller))
        .withTimeout(8.0)
        .withName("Drive and Score");""",
    ),
    (
        CONTAINER,
        """    // TODO (LESSON 12): add your first real routine.
    //
    //   autoChooser.addOption(
    //       "Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));""",
        """    autoChooser.addOption("Drive and Score", SimpleAuto.driveAndScore(drive, flywheels, roller));""",
    ),
)

# ── 13 · Trajectory auto ────────────────────────────────────────────────────
lesson(
    "13-trajectory-auto",
    (
        TRAJ_AUTO,
        """                  // TODO (LESSON 13): close the loop on the trajectory.
                  //
                  //   1. Ask the trajectory where we are SUPPOSED to be right now:
                  //          Trajectory.State goal = trajectory.sample(timer.get());
                  //
                  //   2. Ask the controller how to get from where we ARE to there.
                  //      It returns chassis speeds — forward m/s and rotation rad/s:
                  //          ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);
                  //
                  //   3. Split chassis speeds into per-side wheel speeds:
                  //          DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
                  //
                  //   4. Turn wheel speeds into volts. kV is "volts per meter per
                  //      second" — the feedforward that does the actual driving:
                  //          drive.setVoltage(
                  //              wheels.leftMetersPerSecond  * Constants.Drive.kV_LINEAR,
                  //              wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);
                  //
                  //   Notice there is no PID on wheel velocity here. On a real
                  //   robot you would add one; in simulation the feedforward model
                  //   and the physics model are the same model, so it tracks
                  //   almost perfectly. That is worth remembering as a limitation
                  //   of simulation, not a triumph of your tuning.
                  drive.setVoltage(0.0, 0.0);""",
        """                  // Where the path says we should be, right now.
                  Trajectory.State goal = trajectory.sample(timer.get());

                  // How to get from where we are to there, as chassis speeds.
                  ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);

                  // Chassis speeds -> per-side wheel speeds -> volts.
                  DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
                  drive.setVoltage(
                      wheels.leftMetersPerSecond * Constants.Drive.kV_LINEAR,
                      wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);""",
    ),
    (
        CONTAINER,
        """    // TODO (LESSON 13): add the trajectory routine.
    //
    //   autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));""",
        """    autoChooser.addOption("S-Curve", TrajectoryAuto.sCurveAuto(drive));""",
    ),
)

# ── 14 · Bindings refactor ──────────────────────────────────────────────────
lesson(
    "14-bindings-refactor",
    (
        DRIVER_BINDINGS,
        """    // TODO (LESSON 14): move the driver's bindings out of RobotContainer and
    //   into this constructor. At minimum, the default drive command:
    //
    //   drive.setDefaultCommand(
    //       drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));
    //
    //   controller.leftBumper().whileTrue(drive.stopCommand());
    //
    //   Then delete the equivalent lines from RobotContainer and construct this
    //   class there instead:
    //       new DriverBindings(drive, driver);
    //
    //   The object is never stored in a field. That looks wrong the first time
    //   you see it and it is not: the constructor's entire job is the side
    //   effect of registering bindings with the scheduler, and there is nothing
    //   left to talk to afterwards.""",
        """    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -controller.getLeftY(), () -> -controller.getRightX()));

    // A panic button: hold to stop the drivetrain regardless of the sticks.
    controller.leftBumper().whileTrue(drive.stopCommand());""",
    ),
    (
        OPERATOR_BINDINGS,
        """    // TODO (LESSON 14): move the operator's bindings here from RobotContainer.
    //
    //   controller.a().whileTrue(flywheels.spinUpCommand());
    //   controller.b().whileTrue(roller.intakeCommand());
    //   controller.x().whileTrue(roller.ejectCommand());
    //   controller.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    //   controller.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    //   controller.rightBumper().whileTrue(scoreCommand);
    //
    //   Then delete those lines from RobotContainer.""",
        """    controller.a().whileTrue(flywheels.spinUpCommand());
    controller.b().whileTrue(roller.intakeCommand());
    controller.x().whileTrue(roller.ejectCommand());
    controller.y().onTrue(elevator.goToCommand(Constants.Elevator.HIGH_METERS));
    controller.povDown().onTrue(elevator.goToCommand(Constants.Elevator.STOW_METERS));
    controller.povUp().onTrue(shoulder.goToCommand(Constants.Shoulder.UP_RADIANS));
    controller.rightBumper().whileTrue(scoreCommand);

    roller.hasGamePieceTrigger.and(controller.rightTrigger()).debounce(0.1).onTrue(scoreCommand);""",
    ),
    (
        OPERATOR_BINDINGS,
        """import frc.robot.subsystems.elevator.ElevatorSubsystem;""",
        """import frc.robot.Constants;
import frc.robot.subsystems.elevator.ElevatorSubsystem;""",
    ),
    (
        CONTAINER,
        """import frc.robot.autos.SimpleAuto;""",
        """import frc.robot.autos.SimpleAuto;
import frc.robot.bindings.DriverBindings;
import frc.robot.bindings.OperatorBindings;""",
    ),
    (
        CONTAINER,
        """    // The minus signs: an Xbox stick reads negative when pushed forward, because
    // the axis is measured screen-style with +Y pointing down.
    //
    // The `() ->`: without it, this would capture whatever the stick read during
    // robot startup — zero — and hold that value for the rest of the match.
    drive.setDefaultCommand(
        drive.arcadeDriveCommand(() -> -driver.getLeftY(), () -> -driver.getRightX()));

    // Trivial on purpose. A default command with an `if` in it is a decision
    // that wanted to be a trigger.
    flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());""",
        """    // Moved into DriverBindings and OperatorBindings, alongside the buttons they
    // belong with. What a mechanism does when nobody is asking is part of that
    // human's control scheme, not a separate concern.
    flywheels.setDefaultCommand(flywheels.stopCommand().repeatedly());""",
    ),
    (
        CONTAINER,
        """    // Hold to spin, release to coast — the driver never has to remember whether
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
        .onTrue(scoreCommand());""",
        """    // Every binding that used to be here now lives with the human it belongs to.
    // Nothing about the robot's behaviour changed — this is a pure refactor, and
    // the fact that lessons 07 to 13's rubrics still pass is the proof.
    //
    // Neither object is stored in a field. Their constructors' entire job is the
    // side effect of registering bindings with the scheduler; once that has
    // happened there is nothing left to talk to.
    new DriverBindings(drive, driver);
    new OperatorBindings(elevator, shoulder, flywheels, roller, operator, scoreCommand());""",
    ),
)

# ── 15 · Capstone ───────────────────────────────────────────────────────────
# No new patches: the capstone is integration and polish over everything above.
lesson("15-capstone-teleop")

# ── 16 · IO layer ───────────────────────────────────────────────────────────
lesson(
    "16-io-layer",
    (
        DRIVE_IO_SIM,
        """    // TODO (LESSON 16): advance the model, then fill in the inputs struct.
    //
    //   1. Push the commanded voltages into the model and step it forward one
    //      loop period:
    //          physics.setInputs(leftVolts, rightVolts);
    //          physics.update(Constants.LOOP_PERIOD_SECONDS);
    //
    //   2. Report what the model now says, into the SAME fields a real encoder
    //      would report into:
    //          inputs.leftPositionMeters        = physics.getLeftPositionMeters();
    //          inputs.rightPositionMeters       = physics.getRightPositionMeters();
    //          inputs.leftVelocityMetersPerSec  = physics.getLeftVelocityMetersPerSecond();
    //          inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
    //          inputs.leftAppliedVolts          = leftVolts;
    //          inputs.rightAppliedVolts         = rightVolts;
    //          inputs.gyroYawRadians            = physics.getHeading().getRadians();
    //
    //   Notice that stepping the physics happens HERE, inside updateInputs, and
    //   not in a simulationPeriodic() on the subsystem. That is the difference
    //   this lesson is about: with an IO layer, the subsystem has no simulation
    //   code in it at all. Compare this file against the simulationPeriodic()
    //   still sitting in Drive.java — that method is what you are replacing.""",
        """    physics.setInputs(leftVolts, rightVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);

    inputs.leftPositionMeters = physics.getLeftPositionMeters();
    inputs.rightPositionMeters = physics.getRightPositionMeters();
    inputs.leftVelocityMetersPerSec = physics.getLeftVelocityMetersPerSecond();
    inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
    inputs.leftAppliedVolts = leftVolts;
    inputs.rightAppliedVolts = rightVolts;
    inputs.gyroYawRadians = physics.getHeading().getRadians();""",
    ),
)


SRC_DIRS = ["src/main/java", "src/test/java"]


def snapshot_starters(check_only: bool, problems: list[str]) -> int:
    """Copy the pristine sources into .meta/starter/.

    `frcprog reset` restores from here. It deliberately does not use Git: a student
    who was handed this project on a USB stick may not have a repository at all,
    and "you cannot undo because you did not set up version control" is a terrible
    thing to say to somebody who has just broken their lesson.
    """
    target = ROOT / ".meta" / "starter"
    count = 0
    for src_dir in SRC_DIRS:
        for path in sorted((ROOT / src_dir).rglob("*.java")):
            rel = path.relative_to(ROOT)
            out = target / rel
            text = path.read_text()
            if check_only:
                if not out.exists() or out.read_text() != text:
                    problems.append(f"starter snapshot of {rel} is out of date")
            else:
                out.parent.mkdir(parents=True, exist_ok=True)
                out.write_text(text)
            count += 1
    return count


def main() -> int:
    check_only = "--check" in sys.argv

    # Running state of every file we touch, keyed by repo-relative path.
    state: dict[str, str] = {}
    problems: list[str] = []
    written = 0

    for slug, patches in LESSONS:
        changed_this_lesson: set[str] = set()

        for path, before, after in patches:
            if path not in state:
                source = ROOT / path
                if not source.exists():
                    problems.append(f"{slug}: starter {path} does not exist")
                    continue
                state[path] = source.read_text()

            text = state[path]
            occurrences = text.count(before)
            if occurrences != 1:
                problems.append(
                    f"{slug}: patch for {path} matched {occurrences} times "
                    f"(expected exactly 1). The starter probably changed under it.\n"
                    f"        looking for: {before.strip().splitlines()[0][:70]}..."
                )
                continue
            state[path] = text.replace(before, after)
            changed_this_lesson.add(path)

        # Snapshot every file touched SO FAR, not just this lesson's, so that a
        # student running `frcprog solution 09-command-composition` gets a
        # coherent project rather than lesson 09's file next to lesson 06's.
        target_dir = EXEMPLAR_ROOT / slug
        for path, text in state.items():
            out = target_dir / path
            if check_only:
                if not out.exists() or out.read_text() != text:
                    problems.append(f"{slug}: {path} is out of date (run without --check)")
            else:
                out.parent.mkdir(parents=True, exist_ok=True)
                out.write_text(text)
                written += 1

    starters = snapshot_starters(check_only, problems)

    if problems:
        for p in problems:
            print(f"  ✗ {p}", file=sys.stderr)
        print(f"\n{len(problems)} problem(s).", file=sys.stderr)
        return 1

    if check_only:
        print(f"✓ exemplars for {len(LESSONS)} lessons are up to date")
        print(f"✓ starter snapshots are up to date")
    else:
        print(f"✓ wrote {written} exemplar files across {len(LESSONS)} lessons")
        print(f"✓ snapshotted {starters} starter files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
