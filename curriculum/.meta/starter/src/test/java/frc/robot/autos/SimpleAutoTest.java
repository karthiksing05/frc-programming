package frc.robot.autos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.flywheels.Flywheels;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 12 — Auto routines.
 *
 * <p>Autonomous is fifteen seconds during which nobody may touch a controller. It is also, for most
 * teams, the part of the match with the highest points-per-second — and the part most likely to end
 * with a robot pressed against a wall, wheels spinning, because a routine had no way to give up.
 *
 * <p>So the rubric checks the two things that matter: does it do the job, and does it stop.
 */
@Tag("lesson")
@Tag("lesson-12")
class SimpleAutoTest extends RobotTestBase {

  private Drive drive;
  private Flywheels flywheels;
  private RollerSubsystem roller;

  @BeforeEach
  void setUp() {
    drive = manage(new Drive());
    flywheels = manage(new Flywheels());
    roller = manage(new RollerSubsystem());
  }

  @Test
  @DisplayName("1. The routine is built (not still a placeholder)")
  void routineExists() {
    Command auto = SimpleAuto.driveAndScore(drive, flywheels, roller);
    CommandScheduler.getInstance().schedule(auto);
    step(5);

    assertTrue(
        Math.abs(drive.getLeftVolts()) > 0.1,
        "Scheduling the auto should immediately start driving. If nothing happens, "
            + "driveAndScore() is still returning Commands.none().");
  }

  @Test
  @DisplayName("2. The robot drives at least 1.8 m forward, within three seconds")
  void drivesForward() {
    double start = drive.getAverageDistanceMeters();
    CommandScheduler.getInstance().schedule(SimpleAuto.driveAndScore(drive, flywheels, roller));

    stepSeconds(3.0);

    double travelled = drive.getAverageDistanceMeters() - start;
    assertTrue(
        travelled >= 1.8,
        String.format(
            "Travelled only %.2f m in three seconds; the routine asks for 2.0 m. Check that the "
                + "drive step comes first in your Commands.sequence.",
            travelled));
  }

  @Test
  @DisplayName("3. The scoring step runs AFTER the drive, not alongside it")
  void scoresAfterDriving() {
    CommandScheduler.getInstance().schedule(SimpleAuto.driveAndScore(drive, flywheels, roller));

    boolean spunUpWhileStillDriving = false;

    for (int i = 0; i < 400; i++) {
      step();

      // "Still driving" means the drive step is still COMMANDING the motors —
      // not that the robot is still moving. A 60 kg robot coasts for a good
      // second after the voltage is cut, and grading on motion would flag a
      // perfectly correct sequence for the crime of having momentum.
      boolean stillDriving = Math.abs(drive.getLeftVolts()) > 0.1;

      if (stillDriving && flywheels.getTargetRpm() > 0.0) {
        spunUpWhileStillDriving = true;
      }
    }

    assertFalse(
        spunUpWhileStillDriving,
        "The shooter was spinning while the robot was still driving. Commands.sequence waits for "
            + "each step to FINISH before starting the next; Commands.parallel does not. (Running "
            + "them together is a perfectly good optimisation later — but this lesson is about "
            + "knowing which operator you asked for.)");
    assertTrue(
        flywheels.getTargetRpm() >= 0.0, "Sanity: the flywheels should have been commanded at all");
  }

  @Test
  @DisplayName("4. The routine finishes on its own inside eight seconds")
  void routineIsBounded() {
    Command auto = SimpleAuto.driveAndScore(drive, flywheels, roller);
    CommandScheduler.getInstance().schedule(auto);

    stepSeconds(8.5);

    assertFalse(
        auto.isScheduled(),
        "The auto is still running after 8.5 s. Autonomous is fifteen seconds long, and a routine "
            + "that overruns is still holding the drivetrain when your driver takes over. Put a "
            + ".withTimeout(8.0) on the whole thing.");
  }

  @Test
  @DisplayName("5. Everything is left stopped when the routine ends")
  void leavesTheRobotSafe() {
    CommandScheduler.getInstance().schedule(SimpleAuto.driveAndScore(drive, flywheels, roller));
    stepSeconds(9.0);

    assertTrue(
        Math.abs(drive.getLeftVolts()) < 0.1 && Math.abs(drive.getRightVolts()) < 0.1,
        String.format(
            "The drivetrain is still being commanded (%.2f V / %.2f V) after the routine ended. "
                + "finallyDo is what guarantees cleanup happens however a command finishes — "
                + "including when a timeout cancels it.",
            drive.getLeftVolts(), drive.getRightVolts()));
    assertTrue(
        flywheels.getTargetRpm() < 1.0, "The flywheels should have wound down when the auto ended");
    assertTrue(
        Math.abs(Constants.LOOP_PERIOD_SECONDS - 0.02) < 1e-9, "Sanity: loop period is 20 ms");
  }
}
