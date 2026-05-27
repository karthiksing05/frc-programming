package frc.robot;

/**
 * Static configuration — paths, modes, magic numbers.
 *
 * The `Mode` enum + `currentMode` constant is the AdvantageKit-recommended
 * way to switch between real hardware, sim, and deterministic replay.
 * See docs.advantagekit.org/getting-started/template-projects.
 */
public final class Constants {
    private Constants() {}

    public enum Mode {
        REAL,    // robot deployed on a real RoboRIO
        SIM,     // ./gradlew simulateJava
        REPLAY,  // re-running a saved WPILOG to debug or grade
    }

    /** Switched automatically; manual override possible if you want to
     *  force REPLAY locally. */
    public static final Mode currentMode =
        edu.wpi.first.wpilibj.RobotBase.isReal() ? Mode.REAL : Mode.SIM;

    /** Where SIM mode writes its WPILOG (read back in REPLAY mode). */
    public static final String SIM_LOG_DIR = "logs/";
}
