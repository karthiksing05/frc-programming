package frc.robot.composition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.RobotContainer;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import frc.robot.testing.SourceInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 09 — Command composition.
 *
 * <p>"Spin the shooter up, wait for it to reach speed, then feed a game piece in" is one sentence
 * and three commands. The composition operators exist so that the code can be one sentence too.
 *
 * <p>The assertion worth reading twice is test 2: it checks the <em>ordering</em>, not just the end
 * state. A composition that runs the roller immediately also ends with everything spun up and fed —
 * it just spits the game piece out at 400 RPM and dribbles it onto the floor. Getting the sequencing
 * right is the entire difference, and the only way to grade it is to watch the run unfold.
 */
@Tag("lesson")
@Tag("lesson-09")
class ScoreSequenceTest extends RobotTestBase {

  private RobotContainer container;

  @BeforeEach
  void setUp() {
    container = manage(new RobotContainer());
  }

  @Test
  @DisplayName("1. The sequence is actually built (not still a placeholder)")
  void sequenceExists() {
    Command score = container.scoreCommand();
    CommandScheduler.getInstance().schedule(score);
    step(5);

    assertTrue(
        container.getFlywheels().getTargetRpm() > 0.0,
        "Scheduling the score command should immediately start spinning the flywheels up. If "
            + "nothing happens, scoreCommand() is still returning Commands.none().");
  }

  @Test
  @DisplayName("2. The roller does not fire until the flywheels are up to speed")
  void rollerWaitsForFlywheels() {
    CommandScheduler.getInstance().schedule(container.scoreCommand());

    boolean sawRollerBeforeReady = false;
    boolean sawRollerAfterReady = false;

    // Watch a second and a half of the run, one loop at a time.
    for (int i = 0; i < 75; i++) {
      step();
      boolean rollerRunning = container.getRoller().getMode() == RollerSubsystem.State.EJECTING;
      boolean ready = container.getFlywheels().isReadyToShoot();

      if (rollerRunning && !ready) {
        sawRollerBeforeReady = true;
      }
      if (rollerRunning && ready) {
        sawRollerAfterReady = true;
      }
    }

    assertFalse(
        sawRollerBeforeReady,
        "The roller fed a game piece while the flywheels were still spinning up. On a real robot "
            + "that is a shot that dribbles off the front bumper.%n"
                .formatted()
            + "  Use Commands.waitUntil(flywheels::isReadyToShoot) before the roller step — not "
            + "a fixed delay, which is only correct at one battery voltage.");
    assertTrue(
        sawRollerAfterReady,
        "The roller never ran at all. After the flywheels report ready, the sequence should feed "
            + "for about 0.4 s.");
  }

  @Test
  @DisplayName("3. The whole sequence gives up on its own")
  void sequenceIsBounded() {
    Command score = container.scoreCommand();
    CommandScheduler.getInstance().schedule(score);

    stepSeconds(3.0);

    assertFalse(
        score.isScheduled(),
        "The score command is still running after three seconds. Every composition that waits on "
            + "a sensor needs a .withTimeout(...): a beam-break that comes unplugged mid-match "
            + "should cost you one scoring cycle, not the rest of the match. The lesson asks for "
            + "1.5 s.");
  }

  @Test
  @DisplayName("4. Nothing blocks the scheduler thread")
  void noThreadSleep() {
    for (String file :
        new String[] {
          "src/main/java/frc/robot/RobotContainer.java",
          "src/main/java/frc/robot/autos/SimpleAuto.java"
        }) {
      assertFalse(
          SourceInspector.mentionsOutsideComments(file, "Thread.sleep"),
          file
              + " calls Thread.sleep. Robot code runs on ONE thread: sleeping stops every "
              + "subsystem's periodic(), stops odometry, and stops the watchdog from being fed. "
              + "Commands.waitSeconds does the same thing without freezing the robot.");
      assertFalse(
          SourceInspector.mentionsOutsideComments(file, "Timer.delay"),
          file + " calls Timer.delay, which blocks the scheduler exactly like Thread.sleep does.");
    }
  }
}
