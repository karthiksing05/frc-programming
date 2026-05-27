package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;

/**
 * Sim implementation: drives a {@link DifferentialDrivetrainSim} with
 * whatever volts {@link #setVoltage(double, double)} was called with,
 * then reports the resulting wheel positions, velocities, currents,
 * and gyro yaw back through the inputs struct.
 *
 * Parameters chosen to roughly match a 2-CIM, 6-wheel WCD prototype.
 * Real teams will plug in their own measurements via SysId; here the
 * point is just to have something believable for lessons.
 */
public class DriveIOSim implements DriveIO {

    private static final double LOOP_DT_SECONDS = 0.02;

    private final DifferentialDrivetrainSim drivetrain =
        DifferentialDrivetrainSim.createKitbotSim(
            DifferentialDrivetrainSim.KitbotMotor.kDualCIMPerSide,
            DifferentialDrivetrainSim.KitbotGearing.k10p71,
            DifferentialDrivetrainSim.KitbotWheelSize.kSixInch,
            null /* measurement noise std-devs */);

    private double leftVolts  = 0.0;
    private double rightVolts = 0.0;

    @Override
    public void updateInputs(DriveIOInputs inputs) {
        drivetrain.setInputs(leftVolts, rightVolts);
        drivetrain.update(LOOP_DT_SECONDS);

        inputs.leftPositionMeters         = drivetrain.getLeftPositionMeters();
        inputs.rightPositionMeters        = drivetrain.getRightPositionMeters();
        inputs.leftVelocityMetersPerSec   = drivetrain.getLeftVelocityMetersPerSecond();
        inputs.rightVelocityMetersPerSec  = drivetrain.getRightVelocityMetersPerSecond();
        inputs.leftAppliedVolts           = leftVolts;
        inputs.rightAppliedVolts          = rightVolts;
        inputs.leftCurrentAmps            = drivetrain.getLeftCurrentDrawAmps();
        inputs.rightCurrentAmps           = drivetrain.getRightCurrentDrawAmps();
        inputs.gyroYawRad                 = drivetrain.getHeading().getRadians();
    }

    @Override
    public void setVoltage(double leftVoltsCmd, double rightVoltsCmd) {
        leftVolts  = MathUtil.clamp(leftVoltsCmd,  -12.0, 12.0);
        rightVolts = MathUtil.clamp(rightVoltsCmd, -12.0, 12.0);
    }
}
