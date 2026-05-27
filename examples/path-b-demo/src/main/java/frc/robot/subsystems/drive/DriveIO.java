package frc.robot.subsystems.drive;

import org.littletonrobotics.junction.AutoLog;

/**
 * The hardware boundary for the drive subsystem.
 *
 * Every read from a sensor or write to a motor goes through this
 * interface. Two implementations:
 *   - {@link DriveIOReal}  → real CTRE TalonFX motors on a roboRIO
 *   - {@link DriveIOSim}   → WPILib DifferentialDrivetrainSim
 *
 * The Drive subsystem never sees the difference. This is what makes
 * AdvantageKit's deterministic log replay possible: if we record every
 * value in the inputs class each cycle, we can later replay them and
 * get bit-identical behavior — useful for debugging real matches and
 * for grading lessons.
 *
 * Pattern reference:
 *   https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/
 */
public interface DriveIO {

    /** All sensor readings + state we want logged each cycle.
     *
     *  The {@code @AutoLog} annotation generates a sibling class
     *  {@code DriveIOInputsAutoLogged} that knows how to serialize and
     *  deserialize these fields to/from the WPILOG. Always instantiate
     *  the generated class, not this one directly.
     */
    @AutoLog
    class DriveIOInputs {
        public double leftPositionMeters    = 0.0;
        public double rightPositionMeters   = 0.0;
        public double leftVelocityMetersPerSec  = 0.0;
        public double rightVelocityMetersPerSec = 0.0;
        public double leftAppliedVolts      = 0.0;
        public double rightAppliedVolts     = 0.0;
        public double leftCurrentAmps       = 0.0;
        public double rightCurrentAmps      = 0.0;
        public double gyroYawRad            = 0.0;
    }

    /** Called every periodic; fills {@code inputs} with fresh data. */
    default void updateInputs(DriveIOInputs inputs) {}

    /** Commands the motor voltage for each side (clamped to ±12 V). */
    default void setVoltage(double leftVolts, double rightVolts) {}
}
