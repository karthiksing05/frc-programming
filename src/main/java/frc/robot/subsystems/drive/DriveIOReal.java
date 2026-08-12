package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import frc.robot.Constants;

/**
 * The drivetrain as it exists on an actual robot: motor controllers, encoders, a gyro.
 *
 * <p>This file is complete — nothing to fill in. It is here to be <em>compared</em> against {@link
 * DriveIOSim}. Put them side by side and notice how little they have in common: different fields,
 * different libraries, different failure modes. And notice what they do have in common: the same two
 * methods, the same struct, the same units. That shared surface is the entire value of the pattern.
 *
 * <p>On a competition robot the {@link PWMSparkMax}es here would be {@code TalonFX} or {@code
 * SparkMax} objects and the {@link AnalogGyro} would be a {@code Pigeon2}. The vendor changes; the
 * interface does not, so {@link Drive} does not either.
 *
 * <p>Because this class allocates HAL channels, constructing it in a test allocates real simulated
 * hardware — which is fine, but it is why {@link DriveIOSim} is the one the tests use.
 */
public class DriveIOReal implements DriveIO {

  private final PWMSparkMax leftMotor = new PWMSparkMax(Constants.Drive.LEFT_MOTOR_PWM);
  private final PWMSparkMax rightMotor = new PWMSparkMax(Constants.Drive.RIGHT_MOTOR_PWM);

  private final Encoder leftEncoder =
      new Encoder(Constants.Drive.LEFT_ENCODER_A_DIO, Constants.Drive.LEFT_ENCODER_B_DIO);
  private final Encoder rightEncoder =
      new Encoder(Constants.Drive.RIGHT_ENCODER_A_DIO, Constants.Drive.RIGHT_ENCODER_B_DIO);

  private final AnalogGyro gyro = new AnalogGyro(0);

  private double leftVolts = 0.0;
  private double rightVolts = 0.0;

  public DriveIOReal() {
    rightMotor.setInverted(true);
    double metersPerPulse =
        2.0 * Math.PI * Constants.Drive.WHEEL_RADIUS_METERS / Constants.Drive.ENCODER_CPR;
    leftEncoder.setDistancePerPulse(metersPerPulse);
    rightEncoder.setDistancePerPulse(metersPerPulse);
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    inputs.leftPositionMeters = leftEncoder.getDistance();
    inputs.rightPositionMeters = rightEncoder.getDistance();
    inputs.leftVelocityMetersPerSec = leftEncoder.getRate();
    inputs.rightVelocityMetersPerSec = rightEncoder.getRate();
    inputs.leftAppliedVolts = leftVolts;
    inputs.rightAppliedVolts = rightVolts;
    inputs.gyroYawRadians = gyro.getRotation2d().getRadians();
  }

  @Override
  public void setVoltage(double left, double right) {
    leftVolts = MathUtil.clamp(left, -12.0, 12.0);
    rightVolts = MathUtil.clamp(right, -12.0, 12.0);
    leftMotor.setVoltage(leftVolts);
    rightMotor.setVoltage(rightVolts);
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
