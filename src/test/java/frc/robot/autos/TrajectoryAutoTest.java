package frc.robot.autos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.drive.Drive;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 13 — Trajectory-following auto.
 *
 * <p>The tolerances here deserve a word. Ten centimetres and five degrees sound generous until you
 * remember that a differential drive cannot move sideways: the only way to fix a lateral error is to
 * turn, drive, and turn back, and a controller trying to do that at the end of a path will chatter.
 * Real teams reset their pose from vision precisely because path-following alone drifts.
 *
 * <p>So this rubric asks for what an honest tank-drive trajectory follower can actually deliver: end
 * up near the right place, facing roughly the right way, in roughly the planned time.
 */
@Tag("lesson")
@Tag("lesson-13")
class TrajectoryAutoTest extends RobotTestBase {

  private Drive drive;

  @BeforeEach
  void setUp() {
    drive = manage(new Drive());
  }

  @Test
  @DisplayName("1. The trajectory generates, and it is the shape we asked for")
  void trajectoryGenerates() {
    Trajectory trajectory = TrajectoryAuto.sCurve();

    assertTrue(
        trajectory.getTotalTimeSeconds() > 0.5,
        "An empty or instantaneous trajectory means generation failed");

    Pose2d end = trajectory.sample(trajectory.getTotalTimeSeconds()).poseMeters;
    assertTrue(
        Math.abs(end.getX() - 3.0) < 0.05 && Math.abs(end.getY()) < 0.05,
        "The S-curve should end at (3.0, 0.0); the generator produced " + end);
  }

  @Test
  @DisplayName("2. The follower actually commands the drivetrain")
  void followerDrives() {
    CommandScheduler.getInstance().schedule(TrajectoryAuto.sCurveAuto(drive));
    step(10);

    assertTrue(
        Math.abs(drive.getLeftVolts()) > 0.1 || Math.abs(drive.getRightVolts()) > 0.1,
        "Nothing is reaching the motors. The TODO inside follow()'s run(...) lambda is probably "
            + "still calling setVoltage(0, 0).");
  }

  @Test
  @DisplayName("3. The robot ends up where the path said it would")
  void robotReachesTheEndPose() {
    Trajectory trajectory = TrajectoryAuto.sCurve();
    Command auto = TrajectoryAuto.follow(drive, trajectory);
    CommandScheduler.getInstance().schedule(auto);

    stepSeconds(trajectory.getTotalTimeSeconds() + 0.5);

    Pose2d expected = trajectory.sample(trajectory.getTotalTimeSeconds()).poseMeters;
    Pose2d actual = drive.getPose();
    double distanceError = expected.getTranslation().getDistance(actual.getTranslation());
    double headingErrorDeg =
        Math.abs(expected.getRotation().minus(actual.getRotation()).getDegrees());

    assertTrue(
        distanceError < 0.20,
        String.format(
            "Ended %.3f m from the planned end pose (allowed 0.20 m).%n"
                + "  Planned %s%n  Actual  %s%n"
                + "  A large error usually means the wheel speeds are not being converted to "
                + "volts with kV_LINEAR, or the pose was never reset to the start of the path.",
            distanceError, expected, actual));
    assertTrue(
        headingErrorDeg < 15.0,
        String.format("Ended %.1f° off the planned heading (allowed 15°)", headingErrorDeg));
  }

  @Test
  @DisplayName("4. The robot went the long way round — it followed the curve, not the chord")
  void robotFollowsTheCurveNotTheChord() {
    // The S-curve bulges half a metre to the left before returning to centre. A
    // follower that simply drove straight at the end pose would arrive with the
    // right answer having never left y = 0, and test 3 would not notice.
    Trajectory trajectory = TrajectoryAuto.sCurve();
    CommandScheduler.getInstance().schedule(TrajectoryAuto.follow(drive, trajectory));

    double maxLateral = 0.0;
    int cycles = (int) Math.ceil(trajectory.getTotalTimeSeconds() / 0.02) + 25;
    for (int i = 0; i < cycles; i++) {
      step();
      maxLateral = Math.max(maxLateral, Math.abs(drive.getPose().getY()));
    }

    assertTrue(
        maxLateral > 0.25,
        String.format(
            "The robot never deviated more than %.2f m from the straight line, but the path bulges "
                + "0.5 m sideways. It is driving to the destination rather than along the "
                + "trajectory — check that you sample the trajectory at timer.get() each loop "
                + "instead of always sampling the end.",
            maxLateral));
  }

  @Test
  @DisplayName("5. The routine ends and leaves the drivetrain stopped")
  void followerStopsCleanly() {
    Trajectory trajectory = TrajectoryAuto.sCurve();
    Command auto = TrajectoryAuto.follow(drive, trajectory);
    CommandScheduler.getInstance().schedule(auto);

    stepSeconds(trajectory.getTotalTimeSeconds() + 1.0);

    assertFalse(auto.isScheduled(), "The follower should finish when the trajectory's time is up");
    assertTrue(
        Math.abs(drive.getLeftVolts()) < 0.1 && Math.abs(drive.getRightVolts()) < 0.1,
        "The drivetrain should be left at zero volts — that is what finallyDo is for");
  }
}
