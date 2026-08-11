package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.AnalogGyroSim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.util.MathUtils;
import java.util.function.DoubleSupplier;

/**
 * The drivetrain — and the first place a {@link Command} earns its keep.
 *
 * <p>Every mechanism so far took discrete orders: "intake", "go to L4", "level the arm". A
 * drivetrain does not work that way. It needs a fresh number from the joystick fifty times a second,
 * for as long as a human is holding the stick, and it needs to stop the moment they let go. There is
 * no state to set. There is only "keep doing this until something else needs the drivetrain".
 *
 * <p>That is what a Command is: a unit of work with a beginning, a repeated middle, and an end,
 * which <em>requires</em> a subsystem so that two things can never fight over the same motors.
 *
 * <p><strong>The suppliers matter.</strong> {@link #arcadeDriveCommand} takes {@link DoubleSupplier}
 * arguments, not {@code double} arguments. A {@code double} would be read once, when the command is
 * built during robot startup, and would then be stuck at whatever the stick read at power-on — which
 * is zero. A supplier is re-read every loop. This is the single most common bug in beginner
 * command-based code, and the lesson makes you cause it on purpose so you will recognise it later.
 */
public class Drive extends SubsystemBase implements AutoCloseable {

  private final PWMSparkMax leftMotor = new PWMSparkMax(Constants.Drive.LEFT_MOTOR_PWM);
  private final PWMSparkMax rightMotor = new PWMSparkMax(Constants.Drive.RIGHT_MOTOR_PWM);

  private final Encoder leftEncoder =
      new Encoder(Constants.Drive.LEFT_ENCODER_A_DIO, Constants.Drive.LEFT_ENCODER_B_DIO);
  private final Encoder rightEncoder =
      new Encoder(Constants.Drive.RIGHT_ENCODER_A_DIO, Constants.Drive.RIGHT_ENCODER_B_DIO);

  private final AnalogGyro gyro = new AnalogGyro(0);

  private final DifferentialDriveOdometry odometry;

  private double leftVolts = 0.0;
  private double rightVolts = 0.0;

  // ─── Simulation-only state ────────────────────────────────────────────────
  private final DifferentialDrivetrainSim physics =
      DifferentialDrivetrainSim.createKitbotSim(
          DifferentialDrivetrainSim.KitbotMotor.kDualCIMPerSide,
          DifferentialDrivetrainSim.KitbotGearing.k8p45,
          DifferentialDrivetrainSim.KitbotWheelSize.kSixInch,
          null);
  private final EncoderSim leftEncoderSim = new EncoderSim(leftEncoder);
  private final EncoderSim rightEncoderSim = new EncoderSim(rightEncoder);
  private final AnalogGyroSim gyroSim = new AnalogGyroSim(gyro);

  public Drive() {
    // The right side of a differential drive is mounted mirror-image to the
    // left, so positive voltage spins it the opposite way in field terms.
    // Inverting it here, once, means every caller can just say "forward".
    rightMotor.setInverted(true);

    double metersPerPulse =
        2.0 * Math.PI * Constants.Drive.WHEEL_RADIUS_METERS / Constants.Drive.ENCODER_CPR;
    leftEncoder.setDistancePerPulse(metersPerPulse);
    rightEncoder.setDistancePerPulse(metersPerPulse);

    // Seed the simulated sensors to match the freshly-constructed physics model.
    // Simulated encoder counts live in the HAL, not in this object, so without
    // this a second Drive constructed in the same process would inherit the
    // first one's odometry and believe it had already driven across the field.
    leftEncoderSim.setDistance(0.0);
    rightEncoderSim.setDistance(0.0);
    leftEncoderSim.setRate(0.0);
    rightEncoderSim.setRate(0.0);
    gyroSim.setAngle(0.0);
    gyroSim.setRate(0.0);

    odometry =
        new DifferentialDriveOdometry(
            gyro.getRotation2d(), leftEncoder.getDistance(), rightEncoder.getDistance());
  }

  // ───────────────────────────────────────────────────────────────────────────
  //  Command factories — the public API of this subsystem
  // ───────────────────────────────────────────────────────────────────────────

  /**
   * Drives the robot from a pair of live joystick axes, forever, until something else needs the
   * drivetrain.
   *
   * <p>"Arcade" mixing: one axis is throttle, the other is turn. Left wheel gets {@code forward +
   * rotation}, right wheel gets {@code forward - rotation}.
   *
   * @param forward supplier of the throttle axis, in [-1, 1]
   * @param rotation supplier of the turn axis, in [-1, 1]
   * @return a command that never finishes on its own — perfect as a default command
   */
  public Command arcadeDriveCommand(DoubleSupplier forward, DoubleSupplier rotation) {
    // `run(...)` builds a Command that calls the given lambda every loop and
    // requires this subsystem. It is a method inherited from SubsystemBase.
    return run(
        () -> {
          // Read the sticks HERE, inside the lambda, every loop. This is the
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

          setVoltage(left * Constants.Drive.MAX_VOLTS, right * Constants.Drive.MAX_VOLTS);
        });
  }

  /**
   * Drives straight forward at a fixed voltage until the robot has travelled a distance.
   *
   * <p>Used by the autonomous lessons. Deliberately dumb — no closed loop, no profile — because
   * lesson 12 is about composing commands, not about control.
   *
   * @param meters how far to travel from wherever the robot is when the command starts
   * @param volts how hard to push; positive is forward
   */
  public Command driveDistanceCommand(double meters, double volts) {
    // A one-element array is the standard Java trick for "a variable a lambda is
    // allowed to write to". Lambdas may only capture effectively-final locals,
    // but they may mutate the contents of a captured object.
    final double[] startMeters = new double[1];
    return runOnce(() -> startMeters[0] = getAverageDistanceMeters())
        .andThen(run(() -> setVoltage(volts, volts)))
        .until(() -> Math.abs(getAverageDistanceMeters() - startMeters[0]) >= Math.abs(meters))
        .finallyDo(() -> setVoltage(0.0, 0.0));
  }

  /** Stops the drivetrain and holds it stopped. */
  public Command stopCommand() {
    return run(() -> setVoltage(0.0, 0.0));
  }

  // ───────────────────────────────────────────────────────────────────────────
  //  Plumbing
  // ───────────────────────────────────────────────────────────────────────────

  /**
   * Sends voltage to both sides. The only place in the codebase that touches the drive motors.
   *
   * @param left volts for the left side
   * @param right volts for the right side
   */
  public void setVoltage(double left, double right) {
    leftVolts = MathUtil.clamp(left, -12.0, 12.0);
    rightVolts = MathUtil.clamp(right, -12.0, 12.0);
    leftMotor.setVoltage(leftVolts);
    rightMotor.setVoltage(rightVolts);
  }

  /** @return volts last commanded to the left side. */
  public double getLeftVolts() {
    return leftVolts;
  }

  /** @return volts last commanded to the right side. */
  public double getRightVolts() {
    return rightVolts;
  }

  /** @return distance the left wheels have travelled since the last reset, in meters. */
  public double getLeftDistanceMeters() {
    return leftEncoder.getDistance();
  }

  /** @return distance the right wheels have travelled since the last reset, in meters. */
  public double getRightDistanceMeters() {
    return rightEncoder.getDistance();
  }

  /** @return the average of both sides — i.e. how far the robot itself moved. */
  public double getAverageDistanceMeters() {
    return (getLeftDistanceMeters() + getRightDistanceMeters()) / 2.0;
  }

  /** @return the robot's best guess at where it is on the field. */
  public Pose2d getPose() {
    return odometry.getPoseMeters();
  }

  /** @return which way the robot is facing. */
  public Rotation2d getHeading() {
    return gyro.getRotation2d();
  }

  /**
   * Declares that the robot is, right now, at the given pose.
   *
   * <p>Note what this does <em>not</em> do: it does not zero the encoders. Odometry keeps its own
   * record of the wheel distances it last saw and works in deltas from there. Zeroing the hardware
   * would fight the physics simulation, which has no idea you decided to renumber the world.
   *
   * @param pose where the robot actually is
   */
  public void resetPose(Pose2d pose) {
    odometry.resetPosition(
        gyro.getRotation2d(), getLeftDistanceMeters(), getRightDistanceMeters(), pose);
  }

  /** Declares that the robot is at the field origin, facing +X. */
  public void resetPose() {
    resetPose(new Pose2d());
  }

  @Override
  public void periodic() {
    // Odometry integrates wheel travel plus heading into a field pose. Doing it
    // in periodic() — rather than inside whatever command happens to be running
    // — means the answer is always current, no matter who is driving.
    odometry.update(gyro.getRotation2d(), getLeftDistanceMeters(), getRightDistanceMeters());
  }

  @Override
  public void simulationPeriodic() {
    physics.setInputs(leftVolts, rightVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);

    leftEncoderSim.setDistance(physics.getLeftPositionMeters());
    leftEncoderSim.setRate(physics.getLeftVelocityMetersPerSecond());
    rightEncoderSim.setDistance(physics.getRightPositionMeters());
    rightEncoderSim.setRate(physics.getRightVelocityMetersPerSecond());
    gyroSim.setAngle(-physics.getHeading().getDegrees());
  }

  @Override
  public void close() {
    leftMotor.close();
    rightMotor.close();
    leftEncoder.close();
    rightEncoder.close();
    gyro.close();
  }
}
