package frc.robot.subsystems.flywheels;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

/**
 * The shooter flywheels — a spinning mass held at a target speed.
 *
 * <p>Unlike the elevator and the arm, a flywheel has no position worth caring about. It has a
 * <em>speed</em>, and the only question is whether that speed is close enough to the target to fire.
 * That makes it the natural home for the telemetry lesson: "why isn't my flywheel getting up to
 * speed" is a question you cannot answer by staring at the code, and can answer in five seconds by
 * looking at a plot.
 *
 * <p>The control loop here is already written. Notice its shape: a feedforward term ({@code kV *
 * targetRpm}, "the volts it takes to hold this speed once you are there") plus a small proportional
 * correction. That ordering — feedforward carrying the load, feedback cleaning up the difference —
 * is how essentially every velocity loop in FRC is built, and it is why flywheels tune easily while
 * position loops do not.
 *
 * <p>Presto's {@code flywheels/Flywheels.java} is the production version, and the file everyone in
 * FRC copies when they need a shooter.
 */
public class Flywheels extends SubsystemBase implements AutoCloseable {

  private final PWMSparkMax motor = new PWMSparkMax(Constants.Flywheels.MOTOR_PWM);
  private final Encoder encoder =
      new Encoder(Constants.Flywheels.ENCODER_A_DIO, Constants.Flywheels.ENCODER_B_DIO);

  private double targetRpm = 0.0;
  private double appliedVolts = 0.0;

  // ─── Telemetry publishers ─────────────────────────────────────────────────
  // A "publisher" is a handle to one value on one NetworkTables topic. Creating
  // it once here and reusing it is much cheaper than looking the topic up by
  // string every loop, which is what SmartDashboard.putNumber does internally.
  //
  // Anything published here shows up in AdvantageScope the moment you connect to
  // NetworkTables at localhost. The table name and the field name become the
  // path: /Flywheels/TargetRPM.
  private final NetworkTable table = NetworkTableInstance.getDefault().getTable("Flywheels");
  private final DoublePublisher targetPublisher = table.getDoubleTopic("TargetRPM").publish();

  private final DoublePublisher actualPublisher = table.getDoubleTopic("ActualRPM").publish();
  private final DoublePublisher errorPublisher = table.getDoubleTopic("ErrorRPM").publish();

  // ─── Simulation-only state ────────────────────────────────────────────────
  private final FlywheelSim physics =
      new FlywheelSim(
          LinearSystemId.createFlywheelSystem(
              DCMotor.getKrakenX60Foc(1),
              Constants.Flywheels.MOI_KG_M2,
              Constants.Flywheels.GEARING),
          DCMotor.getKrakenX60Foc(1));
  private final EncoderSim encoderSim = new EncoderSim(encoder);

  public Flywheels() {
    encoder.setDistancePerPulse(1.0 / 2048.0); // one unit == one wheel revolution
    // Simulated encoder state lives in the HAL and outlives this object, so a
    // fresh Flywheels must clear it or it starts up believing it is already
    // spinning at whatever the last one was doing.
    encoderSim.setRate(0.0);
    encoderSim.setDistance(0.0);
  }

  // ───────────────────────────────────────────────────────────────────────────
  //  Command factories
  // ───────────────────────────────────────────────────────────────────────────

  /**
   * Spins the wheels up to shooting speed and holds them there for as long as the command runs.
   *
   * <p>Bound with {@code whileTrue}, this means "spin while I hold the button, coast when I let go"
   * — which is what a driver expects, and why {@code finallyDo} sets the target back to zero.
   */
  public Command spinUpCommand() {
    return spinUpCommand(Constants.Flywheels.SHOOT_RPM);
  }

  /**
   * Spins the wheels up to an arbitrary speed.
   *
   * @param rpm target speed in revolutions per minute
   */
  public Command spinUpCommand(double rpm) {
    return run(() -> targetRpm = rpm).finallyDo(() -> targetRpm = 0.0);
  }

  /** Lets the wheels coast down to a stop. */
  public Command stopCommand() {
    return runOnce(() -> targetRpm = 0.0);
  }

  // ───────────────────────────────────────────────────────────────────────────
  //  State
  // ───────────────────────────────────────────────────────────────────────────

  /** @return the speed we are asking for, in RPM. */
  public double getTargetRpm() {
    return targetRpm;
  }

  /** @return the speed the encoder reports, in RPM. */
  public double getVelocityRpm() {
    return encoder.getRate() * 60.0;
  }

  /** @return how far off we are, in RPM. Positive means "not up to speed yet". */
  public double getErrorRpm() {
    return targetRpm - getVelocityRpm();
  }

  /**
   * @return true when the wheels are spinning fast enough to shoot. Asking for a fraction of target
   *     rather than an absolute tolerance means the answer stays sensible at every target speed.
   */
  public boolean isReadyToShoot() {
    return targetRpm > 0.0
        && getVelocityRpm() >= targetRpm * Constants.Flywheels.READY_FRACTION;
  }

  /** @return the voltage last commanded. */
  public double getAppliedVolts() {
    return appliedVolts;
  }

  @Override
  public void periodic() {
    // Feedforward: the volts it takes to SUSTAIN the target speed.
    double feedforward = Constants.Flywheels.kV * targetRpm;
    // Feedback: a nudge proportional to how far off we currently are.
    double feedback = Constants.Flywheels.kP * getErrorRpm();

    appliedVolts = targetRpm <= 0.0 ? 0.0 : MathUtil.clamp(feedforward + feedback, 0.0, 12.0);
    motor.setVoltage(appliedVolts);

    targetPublisher.set(targetRpm);

    actualPublisher.set(getVelocityRpm());
    errorPublisher.set(getErrorRpm());

    // Three traces on one chart in AdvantageScope, and the shape of the gap
    // between target and actual tells you which gain to reach for. That picture
    // is the difference between tuning and guessing.
  }

  @Override
  public void simulationPeriodic() {
    physics.setInputVoltage(appliedVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);
    // FlywheelSim reports rad/s; the encoder is scaled in wheel revolutions, and
    // getRate() returns units per second, so convert rad/s -> rev/s.
    encoderSim.setRate(physics.getAngularVelocityRadPerSec() / (2.0 * Math.PI));
  }

  @Override
  public void close() {
    motor.close();
    encoder.close();
    targetPublisher.close();
    actualPublisher.close();
    errorPublisher.close();
  }
}
