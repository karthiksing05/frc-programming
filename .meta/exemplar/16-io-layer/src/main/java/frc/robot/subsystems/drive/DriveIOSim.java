package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import frc.robot.Constants;

/**
 * The drivetrain, as physics rather than as wiring.
 *
 * <p>This implementation owns no hardware at all — no PWM channel, no DIO channel, nothing the HAL
 * has to allocate. It owns a differential-drive model, integrates it forward one loop period at a
 * time, and reports the result as if it were an encoder. That is the whole trick: from {@link Drive}'s
 * point of view, a physics model and a real gearbox are indistinguishable, because both are just
 * something on the far side of {@link DriveIO}.
 *
 * <p>Because it allocates no HAL resources, several of these can exist at once, in a unit test, with
 * no port conflicts — which is exactly what makes IO-layer code so much easier to test than code
 * that reaches for a motor controller directly.
 */
public class DriveIOSim implements DriveIO {

  private final DifferentialDrivetrainSim physics =
      DifferentialDrivetrainSim.createKitbotSim(
          DifferentialDrivetrainSim.KitbotMotor.kDualCIMPerSide,
          DifferentialDrivetrainSim.KitbotGearing.k8p45,
          DifferentialDrivetrainSim.KitbotWheelSize.kSixInch,
          null);

  private double leftVolts = 0.0;
  private double rightVolts = 0.0;

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    physics.setInputs(leftVolts, rightVolts);
    physics.update(Constants.LOOP_PERIOD_SECONDS);

    inputs.leftPositionMeters = physics.getLeftPositionMeters();
    inputs.rightPositionMeters = physics.getRightPositionMeters();
    inputs.leftVelocityMetersPerSec = physics.getLeftVelocityMetersPerSecond();
    inputs.rightVelocityMetersPerSec = physics.getRightVelocityMetersPerSecond();
    inputs.leftAppliedVolts = leftVolts;
    inputs.rightAppliedVolts = rightVolts;
    inputs.gyroYawRadians = physics.getHeading().getRadians();
  }

  @Override
  public void setVoltage(double left, double right) {
    leftVolts = MathUtil.clamp(left, -12.0, 12.0);
    rightVolts = MathUtil.clamp(right, -12.0, 12.0);
  }

  /** Teleports the model. Useful in tests; meaningless on a real robot, which is why it is not on
   * the interface. */
  public void setPose(edu.wpi.first.math.geometry.Pose2d pose) {
    physics.setPose(pose);
  }
}
// The Constants import above is unused until you complete the TODO. That is expected.
