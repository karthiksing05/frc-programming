package frc.robot.subsystems.drive;

/**
 * The hardware boundary of the drivetrain.
 *
 * <p>Everything above this line is logic: arcade mixing, odometry, commands, deadbands. Everything
 * below it is wiring: which motor controller, which encoder, which vendor's library. Drawing that
 * line explicitly — as an interface, with a struct of readings crossing it in one direction and a
 * pair of voltages crossing in the other — is the single most consequential structural decision in
 * modern FRC code. It is what 6328's AdvantageKit calls the <em>IO Layer</em>, and it is the shape
 * Presto, Kelpie, and 254's robots all use.
 *
 * <p>Three things become possible the moment the line exists:
 *
 * <ul>
 *   <li><strong>Simulation stops being a special case.</strong> {@link DriveIOSim} is not a mode
 *       your subsystem switches into; it is a different object implementing the same interface. The
 *       subsystem cannot tell, and therefore cannot have a sim-only bug.
 *   <li><strong>Swapping vendors is a one-file change.</strong> Kraken today, SparkFlex next season:
 *       write a new implementation, change one line where it is constructed, delete nothing else.
 *   <li><strong>Log replay becomes possible.</strong> If every sensor reading the subsystem ever
 *       sees arrives through {@code updateInputs}, then recording those readings and feeding them
 *       back later re-runs your control logic <em>exactly</em>, on your laptop, days after the
 *       match. That is what AdvantageKit adds on top of this pattern, and it is covered in
 *       lesson 19.
 * </ul>
 *
 * <p>Note the discipline the interface enforces: the subsystem may read from {@link DriveIOInputs}
 * and it may call {@link #setVoltage}. It has no other way to reach hardware. Any sensor read that
 * sneaks around this interface silently breaks replay, which is why lesson 19 cannot work if lesson
 * 16 was done halfway.
 */
public interface DriveIO extends AutoCloseable {

  /**
   * Everything the drivetrain can sense, in one struct, refreshed once per loop.
   *
   * <p>A plain mutable class rather than a record, and filled in place rather than reallocated,
   * because this is touched fifty times a second for the whole match and the garbage collector is
   * not your friend during autonomous. (AdvantageKit generates a class exactly like this from an
   * {@code @AutoLog} annotation; writing it by hand once is how you understand what it generates.)
   */
  class DriveIOInputs {
    /** Distance the left wheels have travelled, in meters. */
    public double leftPositionMeters = 0.0;

    /** Distance the right wheels have travelled, in meters. */
    public double rightPositionMeters = 0.0;

    /** Left wheel speed, meters per second. */
    public double leftVelocityMetersPerSec = 0.0;

    /** Right wheel speed, meters per second. */
    public double rightVelocityMetersPerSec = 0.0;

    /** Volts most recently commanded to the left side. */
    public double leftAppliedVolts = 0.0;

    /** Volts most recently commanded to the right side. */
    public double rightAppliedVolts = 0.0;

    /** Robot heading in radians, counter-clockwise positive. */
    public double gyroYawRadians = 0.0;
  }

  /**
   * Reads every sensor and writes the results into {@code inputs}.
   *
   * <p>Called exactly once at the top of {@code Drive.periodic()}, before any logic runs. Reading
   * everything at one instant means the subsystem's view of the world is internally consistent —
   * position and velocity from the same moment, not from 3 ms apart.
   *
   * @param inputs the struct to fill
   */
  void updateInputs(DriveIOInputs inputs);

  /**
   * Commands both sides.
   *
   * @param leftVolts volts for the left side
   * @param rightVolts volts for the right side
   */
  void setVoltage(double leftVolts, double rightVolts);

  /**
   * Releases hardware resources.
   *
   * <p>Default no-op, so an implementation that owns nothing — like {@link DriveIOSim} — does not
   * have to say so. Declared without {@code throws} (unlike {@link AutoCloseable#close()}) because
   * nothing here can fail in a way a caller could do anything about.
   */
  @Override
  default void close() {}
}
