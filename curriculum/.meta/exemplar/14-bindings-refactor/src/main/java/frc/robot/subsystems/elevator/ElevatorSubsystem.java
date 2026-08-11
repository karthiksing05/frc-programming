package frc.robot.subsystems.elevator;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants;

/**
 * A carriage that travels straight up and down, and stops where you tell it to.
 *
 * <p>The roller from lesson 04 had three states and each one was a fixed motor speed. That works
 * because a roller does not care where it is. An elevator cares enormously: "go to L4" means travel
 * to a specific height and <em>stay</em> there — not 5 cm short, not 5 cm past, not bouncing.
 *
 * <p>The naive version of this — full power until you are close, then off — is called bang-bang
 * control, and it behaves exactly as unpleasantly as it sounds. A {@link PIDController} is the
 * standard answer. Treat it as a recipe today; the control theory will keep.
 *
 * <p><strong>Two things in this file are already done for you and are worth understanding:</strong>
 *
 * <ul>
 *   <li>{@code simulationPeriodic()} runs a physics model ({@link ElevatorSim}) and feeds the result
 *       back into a simulated encoder. That is what makes this subsystem controllable from a laptop
 *       with no robot attached. On a real robot the method simply never runs.
 *   <li>The gravity term. An elevator always pulls down with the same force no matter where the
 *       carriage is, so a single constant number of volts holds it still. That constant is supplied
 *       here. In lesson 06 you will meet an arm, where the gravity term changes with angle, and you
 *       will have to compute it yourself.
 * </ul>
 */
public class ElevatorSubsystem extends SubsystemBase implements AutoCloseable {

  private final PWMSparkMax motor = new PWMSparkMax(Constants.Elevator.MOTOR_PWM);
  private final Encoder encoder =
      new Encoder(Constants.Elevator.ENCODER_A_DIO, Constants.Elevator.ENCODER_B_DIO);

  private final PIDController pid =
      new PIDController(Constants.Elevator.kP, Constants.Elevator.kI, Constants.Elevator.kD);

  /** Where we have been asked to go, in meters above the floor. */
  private double setpointMeters = Constants.Elevator.STOW_METERS;

  /** Volts we last commanded. Kept so the physics sim and telemetry agree with reality. */
  private double appliedVolts = 0.0;

  // ─── Simulation-only state ────────────────────────────────────────────────
  // On a real robot these objects are constructed and then never touched, because
  // simulationPeriodic() is only called when RobotBase.isSimulation() is true.
  private final ElevatorSim physics =
      new ElevatorSim(
          DCMotor.getKrakenX60Foc(1),
          Constants.Elevator.GEARING,
          Constants.Elevator.CARRIAGE_MASS_KG,
          Constants.Elevator.DRUM_RADIUS_METERS,
          Constants.Elevator.MIN_HEIGHT_METERS,
          Constants.Elevator.MAX_HEIGHT_METERS,
          true, // simulate gravity — without this the lesson would be trivial
          Constants.Elevator.STOW_METERS);
  private final EncoderSim encoderSim = new EncoderSim(encoder);

  public ElevatorSubsystem() {
    encoder.setDistancePerPulse(Constants.Elevator.METERS_PER_PULSE);
    encoderSim.setDistance(Constants.Elevator.STOW_METERS);
    pid.setTolerance(Constants.Elevator.TOLERANCE_METERS);
  }

  /**
   * Asks the carriage to travel to a height.
   *
   * @param meters target height above the floor; clamped to the physical travel of the mechanism so
   *     a typo cannot command the carriage through the top of the frame
   */
  public void setGoal(double meters) {
    setpointMeters =
        MathUtil.clamp(
            meters, Constants.Elevator.MIN_HEIGHT_METERS, Constants.Elevator.MAX_HEIGHT_METERS);
  }

  /** @return the height we are trying to reach, in meters. */
  public double getGoal() {
    return setpointMeters;
  }

  /**
   * Sends the carriage to a height and holds it there.
   *
   * <p>A command rather than a bare method call so that the scheduler knows the elevator is claimed
   * while this is running — two commands can never fight over the same mechanism.
   *
   * @param meters target height above the floor
   * @return a command that finishes once the carriage has arrived
   */
  public Command goToCommand(double meters) {
    return runOnce(() -> setGoal(meters)).andThen(run(() -> {}).until(this::atGoal));
  }

  /**
   * A {@link Trigger} that is true whenever the carriage is at its goal.
   *
   * <p>Exposing state as a trigger rather than as a getter is the second of the three
   * command-based habits: the subsystem makes the judgement once, in its own vocabulary, and
   * callers compose that judgement instead of re-deriving it.
   */
  public final Trigger atGoalTrigger = new Trigger(this::atGoal);

  /** @return the height the encoder currently reports, in meters. */
  public double getHeightMeters() {
    return encoder.getDistance();
  }

  /** @return true when the carriage is within {@code TOLERANCE_METERS} of the goal. */
  public boolean atGoal() {
    return Math.abs(getHeightMeters() - setpointMeters) <= Constants.Elevator.TOLERANCE_METERS;
  }

  /** @return the voltage last commanded to the motor. */
  public double getAppliedVolts() {
    return appliedVolts;
  }

  @Override
  public void periodic() {
    // The constant volts it takes to hold this carriage against gravity. On an
    // elevator this number does not depend on height, so it is just a number.
    // (Lesson 06: on an arm it depends on angle, so it becomes a function.)
    double gravityVolts = Constants.Elevator.kG_HOLD;

    // Feedback: how hard to push, given how far off we are right now.
    double feedbackVolts = pid.calculate(getHeightMeters(), setpointMeters);

    // Clamping is not optional. A large kP multiplied by a large startup error
    // asks for a voltage that does not exist, and on real hardware the motor
    // controller's own limiting will produce behaviour you did not model.
    double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);

    appliedVolts = volts;
    motor.setVoltage(volts);
  }

  @Override
  public void simulationPeriodic() {
    // Push the voltage we commanded into the physics model, advance it one loop
    // period, then write the resulting position back into the simulated encoder.
    // From periodic()'s point of view nothing about this is visible: it reads an
    // encoder, same as on a real robot.
    physics.setInputVoltage(appliedVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);
    encoderSim.setDistance(physics.getPositionMeters());
    encoderSim.setRate(physics.getVelocityMetersPerSecond());
  }

  @Override
  public void close() {
    motor.close();
    encoder.close();
    pid.close();
  }
}
