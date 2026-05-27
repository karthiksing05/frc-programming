package frc.robot.subsystems.drive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj.simulation.XboxControllerSim;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Lesson 02 rubric. Drives the real Drive subsystem against
 * DifferentialDrivetrainSim and checks behavior.
 *
 * Why HALSim:
 *   - HAL.initialize spins up the simulated FPGA so XboxController +
 *     SimHooks work the same way they would on a roboRIO.
 *   - AutoCloseable discipline matters — leaked HAL handles cause
 *     flaky cross-test contamination (see Infrastructure-Analysis.md
 *     friction notes).
 */
@Tag("lesson-02")
@DisplayName("Lesson 02 · Drive periodic wiring")
class DriveTest {

    private static final double DT = 0.02;

    private XboxController controller;
    private XboxControllerSim controllerSim;
    private DriveIOSim io;
    private Drive drive;

    @BeforeEach
    void setUp() {
        assert HAL.initialize(500, 0);
        controller = new XboxController(0);
        controllerSim = new XboxControllerSim(controller);
        io = new DriveIOSim();
        drive = new Drive(io, controller);
    }

    @AfterEach
    void tearDown() {
        // No close() on Drive in this lesson; later ones add AutoCloseable.
        HAL.shutdown();
    }

    /** Push the sim forward `seconds` worth of 20ms ticks. */
    private void stepForSeconds(double seconds) {
        int ticks = (int) Math.round(seconds / DT);
        for (int i = 0; i < ticks; i++) {
            drive.periodic();
            SimHooks.stepTiming(DT);
        }
    }

    @Test
    @DisplayName("Straight forward: robot accumulates positive position on both sides")
    void straightLine() {
        controllerSim.setLeftY(-1.0);   // up on stick = forward (negated in Drive)
        controllerSim.setRightX(0.0);
        controllerSim.notifyNewData();

        stepForSeconds(2.0);

        assertTrue(drive.getLeftPositionMeters()  > 0.5,
            "left position should advance — got " + drive.getLeftPositionMeters());
        assertTrue(drive.getRightPositionMeters() > 0.5,
            "right position should advance — got " + drive.getRightPositionMeters());
    }

    @Test
    @DisplayName("Turn-in-place: gyro yaw changes appreciably")
    void rotation() {
        controllerSim.setLeftY(0.0);
        controllerSim.setRightX(1.0);
        controllerSim.notifyNewData();

        stepForSeconds(1.5);

        assertTrue(Math.abs(drive.getGyroYawRad()) > 0.5,
            "robot should have rotated — gyro yaw " + drive.getGyroYawRad());
    }

    @Test
    @DisplayName("Sub-deadband inputs cause no motion")
    void deadbandRespected() {
        controllerSim.setLeftY(-0.05);  // below 0.10 threshold
        controllerSim.setRightX(0.05);
        controllerSim.notifyNewData();

        stepForSeconds(1.0);

        assertTrue(Math.abs(drive.getLeftPositionMeters())  < 0.01,
            "left should stay still — got " + drive.getLeftPositionMeters());
        assertTrue(Math.abs(drive.getRightPositionMeters()) < 0.01,
            "right should stay still — got " + drive.getRightPositionMeters());
    }
}
