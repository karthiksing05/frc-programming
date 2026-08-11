package frc.robot.autos;

import edu.wpi.first.math.controller.LTVUnicycleController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants;
import frc.robot.subsystems.drive.Drive;
import java.util.List;

/**
 * Driving a curve instead of a straight line.
 *
 * <p>{@code driveDistanceCommand(2.0, 6.0)} is honest work and it will get you a leave-the-zone
 * auto. It will not get you around a game piece, and it cannot be adjusted by anybody who does not
 * write Java. A <em>trajectory</em> — a path through space with a velocity attached to every point
 * on it — can.
 *
 * <p>Three pieces cooperate here, and separating them is the whole idea:
 *
 * <ol>
 *   <li><strong>{@link TrajectoryGenerator}</strong> turns waypoints plus speed limits into a
 *       time-parameterised path. Pure geometry; runs once, at startup.
 *   <li><strong>{@link LTVUnicycleController}</strong> answers, every loop: "given where the path says I
 *       should be right now, and where I actually am, what chassis speeds close that gap?" This is
 *       the feedback that makes the robot recover from wheel slip instead of compounding it.
 *   <li><strong>{@link DifferentialDriveKinematics}</strong> converts chassis speeds into left and
 *       right wheel speeds, because a tank drive cannot strafe and the geometry of that constraint
 *       is worth being explicit about.
 * </ol>
 *
 * <p><strong>Why no PathPlanner or Choreo here?</strong> Both are excellent, both are what a real
 * team uses, and both are vendor libraries that have to be downloaded. This lesson is built on the
 * trajectory tools that ship inside WPILib itself so it runs on a laptop that has never been online
 * since the installer. When you do get network, {@code lessons/EXTENSIONS.md} walks you through
 * swapping in Choreo — the concepts transfer exactly, because Choreo replaces step 1 and leaves
 * steps 2 and 3 alone.
 */
public final class TrajectoryAuto {
  private TrajectoryAuto() {}

  /** Converts chassis speeds to wheel speeds for our specific track width. */
  public static final DifferentialDriveKinematics KINEMATICS =
      new DifferentialDriveKinematics(Constants.Drive.TRACK_WIDTH_METERS);

  /**
   * Builds an S-curve: forward three meters while sliding one meter to the left, ending straight.
   *
   * <p>Generated once and cached, because parameterising a trajectory takes real milliseconds and
   * doing it inside a command that runs at 50 Hz would blow the loop time.
   */
  public static Trajectory sCurve() {
    TrajectoryConfig config =
        new TrajectoryConfig(Constants.Drive.MAX_TRAJECTORY_SPEED_MPS,
                Constants.Drive.MAX_TRAJECTORY_ACCEL_MPS2)
            .setKinematics(KINEMATICS);

    return TrajectoryGenerator.generateTrajectory(
        new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0)), // start: origin, facing +X
        List.of(new Translation2d(1.0, 0.5), new Translation2d(2.0, 0.5)), // bulge left
        new Pose2d(3.0, 0.0, Rotation2d.fromDegrees(0.0)), // end: 3 m out, straight again
        config);
  }

  /**
   * Follows a trajectory to its end.
   *
   * @param drive the drivetrain to move
   * @param trajectory the path to follow, from {@link #sCurve()} or anywhere else
   * @return a command that finishes when the trajectory's duration has elapsed
   */
  public static Command follow(Drive drive, Trajectory trajectory) {
    // The controller is stateless between calls, so one per command is fine.
    LTVUnicycleController controller =
        new LTVUnicycleController(Constants.LOOP_PERIOD_SECONDS);
    Timer timer = new Timer();

    return Commands.sequence(
            // Tell odometry that we are, by definition, at the start of the path.
            // Skip this and the controller spends the first second driving to
            // wherever it thinks the path begins relative to the last reset.
            drive.runOnce(
                () -> {
                  drive.resetPose(trajectory.getInitialPose());
                  timer.restart();
                }),
            drive.run(
                () -> {
                  // TODO (LESSON 13): close the loop on the trajectory.
                  //
                  //   1. Ask the trajectory where we are SUPPOSED to be right now:
                  //          Trajectory.State goal = trajectory.sample(timer.get());
                  //
                  //   2. Ask the controller how to get from where we ARE to there.
                  //      It returns chassis speeds — forward m/s and rotation rad/s:
                  //          ChassisSpeeds speeds = controller.calculate(drive.getPose(), goal);
                  //
                  //   3. Split chassis speeds into per-side wheel speeds:
                  //          DifferentialDriveWheelSpeeds wheels = KINEMATICS.toWheelSpeeds(speeds);
                  //
                  //   4. Turn wheel speeds into volts. kV is "volts per meter per
                  //      second" — the feedforward that does the actual driving:
                  //          drive.setVoltage(
                  //              wheels.leftMetersPerSecond  * Constants.Drive.kV_LINEAR,
                  //              wheels.rightMetersPerSecond * Constants.Drive.kV_LINEAR);
                  //
                  //   Notice there is no PID on wheel velocity here. On a real
                  //   robot you would add one; in simulation the feedforward model
                  //   and the physics model are the same model, so it tracks
                  //   almost perfectly. That is worth remembering as a limitation
                  //   of simulation, not a triumph of your tuning.
                  drive.setVoltage(0.0, 0.0);
                }))
        .until(() -> timer.hasElapsed(trajectory.getTotalTimeSeconds()))
        .finallyDo(() -> drive.setVoltage(0.0, 0.0));
  }

  /** Convenience: generate the S-curve and follow it. */
  public static Command sCurveAuto(Drive drive) {
    return follow(drive, sCurve());
  }
}
// The ChassisSpeeds and DifferentialDriveWheelSpeeds imports above are unused
// until you complete the TODO. They are pre-imported on purpose: hunting for the
// right import is not what this lesson is about.
