package frc.robot.subsystems.shoulder;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants;

/**
 * A single-jointed arm that pivots the end effector — and the lesson where a constant becomes a
 * function.
 *
 * <p>Lesson 05's elevator fought a gravity load that never changed: the carriage weighs the same at
 * 0.2 m and at 1.4 m, so one constant number of volts held it anywhere. An arm is different. Held
 * straight out horizontally, the full weight of the arm acts on the longest possible lever and
 * gravity fights hardest. Rotated straight up or straight down, the lever is zero and gravity fights
 * not at all.
 *
 * <p>The torque gravity applies is proportional to {@code cos(angle)}, measuring the angle from
 * horizontal. So the volts needed to cancel it are {@code kG * cos(angle)} — a number that changes
 * every loop, computed from the sensor you already have.
 *
 * <p>You could in principle skip this and let a big {@code kP} fight gravity. It even sort of works.
 * What you get is an arm that sags a few degrees below every setpoint, an integral term winding up
 * to paper over the sag, and a lurch when the wind-up finally discharges. Feedforward is the
 * difference between a controller that <em>corrects</em> a known force and one that is perpetually
 * <em>surprised</em> by it.
 *
 * <p>Kelpie's {@code shoulder/} is this mechanism on a real robot.
 */
public class ShoulderSubsystem extends SubsystemBase implements AutoCloseable {

  private final PWMSparkMax motor = new PWMSparkMax(Constants.Shoulder.MOTOR_PWM);
  private final Encoder encoder =
      new Encoder(Constants.Shoulder.ENCODER_A_DIO, Constants.Shoulder.ENCODER_B_DIO);

  private final PIDController pid =
      new PIDController(Constants.Shoulder.kP, Constants.Shoulder.kI, Constants.Shoulder.kD);

  private double setpointRadians = Constants.Shoulder.DOWN_RADIANS;
  private double appliedVolts = 0.0;

  // ─── Simulation-only state ────────────────────────────────────────────────
  private final SingleJointedArmSim physics =
      new SingleJointedArmSim(
          DCMotor.getKrakenX60Foc(1),
          Constants.Shoulder.GEARING,
          SingleJointedArmSim.estimateMOI(
              Constants.Shoulder.ARM_LENGTH_METERS, Constants.Shoulder.ARM_MASS_KG),
          Constants.Shoulder.ARM_LENGTH_METERS,
          Constants.Shoulder.MIN_ANGLE_RADIANS,
          Constants.Shoulder.MAX_ANGLE_RADIANS,
          true, // simulate gravity — the entire point of this lesson
          Constants.Shoulder.DOWN_RADIANS);
  private final EncoderSim encoderSim = new EncoderSim(encoder);

  public ShoulderSubsystem() {
    encoder.setDistancePerPulse(Constants.Shoulder.RADIANS_PER_PULSE);
    encoderSim.setDistance(Constants.Shoulder.DOWN_RADIANS);
    pid.setTolerance(Constants.Shoulder.TOLERANCE_RADIANS);
  }

  /**
   * Asks the arm to rotate to an angle.
   *
   * @param radians target angle, measured from horizontal: 0 is level, positive is up
   */
  public void setGoal(double radians) {
    setpointRadians =
        MathUtil.clamp(
            radians, Constants.Shoulder.MIN_ANGLE_RADIANS, Constants.Shoulder.MAX_ANGLE_RADIANS);
  }

  /** @return the angle we are trying to reach, in radians from horizontal. */
  public double getGoal() {
    return setpointRadians;
  }

  /**
   * Rotates the arm to an angle and holds it there.
   *
   * @param radians target angle from horizontal
   * @return a command that finishes once the arm has arrived
   */
  public Command goToCommand(double radians) {
    return runOnce(() -> setGoal(radians)).andThen(run(() -> {}).until(this::atGoal));
  }

  /** True whenever the arm is sitting at its goal angle. */
  public final Trigger atGoalTrigger = new Trigger(this::atGoal);

  /** @return the angle the encoder reports, in radians from horizontal. */
  public double getAngleRadians() {
    return encoder.getDistance();
  }

  /** @return the angle the encoder reports, in degrees from horizontal. Convenience for humans. */
  public double getAngleDegrees() {
    return Units.radiansToDegrees(getAngleRadians());
  }

  /** @return true when the arm is within {@code TOLERANCE_RADIANS} of the goal. */
  public boolean atGoal() {
    return Math.abs(getAngleRadians() - setpointRadians) <= Constants.Shoulder.TOLERANCE_RADIANS;
  }

  /** @return the voltage last commanded to the motor. */
  public double getAppliedVolts() {
    return appliedVolts;
  }

  @Override
  public void periodic() {
    double feedbackVolts = pid.calculate(getAngleRadians(), setpointRadians);

    // TODO (LESSON 06): compute the gravity feedforward and add it in.
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
    double gravityVolts = 0.0;

    double volts = MathUtil.clamp(feedbackVolts + gravityVolts, -12.0, 12.0);
    appliedVolts = volts;
    motor.setVoltage(volts);
  }

  @Override
  public void simulationPeriodic() {
    physics.setInputVoltage(appliedVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);
    encoderSim.setDistance(physics.getAngleRads());
    encoderSim.setRate(physics.getVelocityRadPerSec());
  }

  @Override
  public void close() {
    motor.close();
    encoder.close();
    pid.close();
  }
}
