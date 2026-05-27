package frc.robot.subsystems.drive;

/**
 * Real-hardware IO. Stub until the team gets a real drivetrain to wire
 * up — fleshing this out is the start-of-season ritual.
 *
 * In a real impl this would:
 *   - Construct a {@code TalonFX} (or {@code SparkMax}) per side
 *   - Configure current limits, neutral mode, gear ratios
 *   - Subscribe to position/velocity {@code StatusSignal}s
 *   - Wire a Pigeon 2 or NavX for {@code gyroYawRad}
 *
 * For lessons this empty version is fine — running the lessons in
 * SIM mode bypasses this entirely. Including the stub here is itself
 * pedagogical: students see the shape of "this is where real hardware
 * goes" without being overwhelmed.
 */
public class DriveIOReal implements DriveIO {
    // TODO (later lesson): wire real motors.
}
