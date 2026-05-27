package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveIO;
import frc.robot.subsystems.drive.DriveIOReal;
import frc.robot.subsystems.drive.DriveIOSim;

/**
 * Subsystem ownership + command-binding root.
 *
 * The mode switch here is the bottom of the IO-layer pattern: pick the
 * right hardware implementation based on whether we're running on a
 * real RoboRIO or in the sim/replay. Subsystem classes never see this
 * distinction.
 */
public class RobotContainer {

    private final XboxController driver = new XboxController(0);
    private final Drive drive;

    public RobotContainer() {
        DriveIO driveIO = switch (Constants.currentMode) {
            case REAL   -> new DriveIOReal();
            case SIM    -> new DriveIOSim();
            case REPLAY -> new DriveIO() { /* no-op; replay overwrites inputs */ };
        };
        drive = new Drive(driveIO, driver);
        // command bindings would go here as the curriculum advances
    }
}
