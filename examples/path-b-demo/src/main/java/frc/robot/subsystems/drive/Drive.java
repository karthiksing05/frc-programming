package frc.robot.subsystems.drive;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import org.littletonrobotics.junction.Logger;

import frc.robot.util.MathUtils;  // <-- the file you wrote in Lesson 01!

/**
 * Tank drivetrain subsystem.
 *
 * Lesson 02: wire up the arcade-drive math in {@link #periodic()}.
 *
 * Notes on shape:
 *   - We hold an {@link DriveIO} reference, not a motor directly. The
 *     mode switch in {@code RobotContainer} picks real-or-sim. This
 *     class doesn't care which it got.
 *   - Every cycle: {@code io.updateInputs(inputs); Logger.processInputs(...);}.
 *     That sequence is what makes AdvantageKit deterministic replay
 *     work — *all* reads from hardware go through {@code inputs}.
 *   - Outputs (decisions made by this class) are emitted with
 *     {@code Logger.recordOutput(...)}. Watch them live in
 *     AdvantageScope's line chart while the sim runs.
 */
public class Drive extends SubsystemBase {

    /** Threshold below which a stick reading is treated as zero. */
    public static final double DEADBAND = 0.10;

    private final DriveIO io;
    private final DriveIOInputsAutoLogged inputs = new DriveIOInputsAutoLogged();
    private final XboxController controller;

    public Drive(DriveIO io, XboxController controller) {
        this.io = io;
        this.controller = controller;
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Drive", inputs);

        double forward  = -controller.getLeftY();   // up on stick = +forward
        double rotation =  controller.getRightX();

        // TODO 1 (LESSON 02): pass each through MathUtils.applyDeadband
        double forwardClean  = forward;
        double rotationClean = rotation;

        // TODO 2 (LESSON 02): arcade-drive mixing
        //   leftDemand  = forwardClean + rotationClean
        //   rightDemand = forwardClean - rotationClean
        double leftDemand  = 0.0;
        double rightDemand = 0.0;

        // TODO 3 (LESSON 02): send to the IO layer (multiply by 12 V).
        // io.setVoltage(leftDemand * 12.0, rightDemand * 12.0);

        Logger.recordOutput("Drive/LeftDemand",  leftDemand);
        Logger.recordOutput("Drive/RightDemand", rightDemand);
    }

    /* Helpers for tests / future lessons */
    public double getLeftPositionMeters()  { return inputs.leftPositionMeters;  }
    public double getRightPositionMeters() { return inputs.rightPositionMeters; }
    public double getGyroYawRad()          { return inputs.gyroYawRad;          }
}
