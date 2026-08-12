package frc.robot.subsystems.roller;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants;

/**
 * The intake roller: one motor, one beam-break sensor.
 *
 * <p>This is the first real subsystem in the curriculum, and it exists to solve a problem you
 * created yourself in lesson 03 — a {@code teleopPeriodic()} that had grown to twenty-five lines of
 * interleaved sensor reads, button reads, and motor writes with no name for any of it.
 *
 * <p>A subsystem is two promises:
 *
 * <ol>
 *   <li><strong>It owns its hardware.</strong> The motor and the sensor are {@code private final}.
 *       Nothing outside this file can call {@code motor.set(...)}. If the roller misbehaves, the bug
 *       is in this file — you never have to go looking.
 *   <li><strong>It exposes intent, not mechanism.</strong> Callers say {@code
 *       setMode(State.INTAKING)}. They do not say "0.6 volts unless the beam is broken". Deciding
 *       what 0.6 means is this class's job.
 * </ol>
 *
 * <p>Kelpie's {@code roller/RollerSubsystem.java} is the production version of this exact class, one
 * IO-layer refactor further along (you will do that refactor in lesson 16).
 */
public class RollerSubsystem extends SubsystemBase implements AutoCloseable {

  /**
   * What the roller is currently trying to do.
   *
   * <p>An {@code enum} is the right tool here because the roller has exactly three modes and no
   * fourth is meaningful. Compare with a {@code boolean isIntaking} — as soon as you add ejecting
   * you need a second boolean, and then two booleans can encode a state ("intaking AND ejecting")
   * that the mechanism cannot physically be in. Enums make illegal states unrepresentable.
   */
  public enum State {
    /** Motor off. */
    OFF,
    /** Pulling a game piece in — but stop once we actually have one. */
    INTAKING,
    /** Spitting a game piece out, unconditionally. */
    EJECTING
  }

  private final PWMSparkMax motor = new PWMSparkMax(Constants.Roller.MOTOR_PWM);
  private final DigitalInput beamBreak = new DigitalInput(Constants.Roller.BEAM_BREAK_DIO);

  private State state = State.OFF;

  /** Last value we commanded, kept so tests and telemetry can see it without touching the motor. */
  private double lastOutput = 0.0;

  /**
   * Asks the roller to do something. Cheap and idempotent — call it every loop if you like.
   *
   * @param desired what the roller should be doing from now on
   */
  public void setMode(State desired) {
    state = desired;
  }

  /** @return what the roller is currently trying to do. */
  public State getMode() {
    return state;
  }

  /** @return true when the beam is broken, i.e. a game piece is sitting in the throat. */
  public boolean hasGamePiece() {
    // A beam-break is wired so that an unbroken beam pulls the input high.
    // Broken beam (piece present) reads false. Inverting it here, once, means
    // nobody else ever has to remember which way round it is.
    return !beamBreak.get();
  }

  /** @return the duty cycle we last sent to the motor, in [-1, 1]. */
  public double getOutput() {
    return lastOutput;
  }

  // ─── Command factories ─────────────────────────────────────────────────────
  // Thin wrappers around setMode. They exist so that callers can bind them to
  // buttons and compose them into sequences, and so that the scheduler knows the
  // roller is claimed while one is running.

  /** Runs the roller inward while this command is scheduled; stops on the way out. */
  public Command intakeCommand() {
    return startEnd(() -> setMode(State.INTAKING), () -> setMode(State.OFF));
  }

  /** Runs the roller outward while this command is scheduled; stops on the way out. */
  public Command ejectCommand() {
    return startEnd(() -> setMode(State.EJECTING), () -> setMode(State.OFF));
  }

  /** True whenever a game piece is sitting in the throat. */
  public final Trigger hasGamePieceTrigger = new Trigger(this::hasGamePiece);

  @Override
  public void periodic() {
    double output =
        switch (state) {
          case OFF -> 0.0;
          case INTAKING -> hasGamePiece() ? 0.0 : Constants.Roller.INTAKE_SPEED;
          case EJECTING -> Constants.Roller.EJECT_SPEED;
        };

    lastOutput = output;
    motor.set(output);
  }

  @Override
  public void close() {
    motor.close();
    beamBreak.close();
  }
}
