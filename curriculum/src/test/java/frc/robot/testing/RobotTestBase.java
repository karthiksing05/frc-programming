package frc.robot.testing;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Shared setup for every lesson rubric.
 *
 * <p>Testing robot code has three problems that this class solves once so no lesson has to solve
 * them again.
 *
 * <p><strong>1. The HAL has to exist.</strong> WPILib talks to a Hardware Abstraction Layer even in
 * simulation; without {@code HAL.initialize} every constructor that allocates a channel throws.
 * Initialising it costs about 100 ms, which is why it happens once per test rather than once per
 * assertion.
 *
 * <p><strong>2. The CommandScheduler is a singleton.</strong> It survives between tests in the same
 * JVM, so a subsystem registered by one test keeps having its {@code periodic()} called during the
 * next one, long after its motor has been closed. Worse, every {@code Trigger} ever constructed is
 * registered on the scheduler's <em>button loop</em> rather than on the subsystem that made it, so
 * it outlives that subsystem too and goes on polling a sensor whose handle has been freed. Every
 * test therefore starts by evicting all of it and ends by doing it again.
 *
 * <p><strong>3. Simulated Driver Station state is global and sticky.</strong> A button or an axis
 * left pressed by one test is still pressed in the next one, because the value lives in the HAL and
 * not in the {@code XboxControllerSim} object that set it. That produces the worst class of test
 * failure: one that passes in isolation, fails in a full run, and blames an innocent test.
 *
 * <p><strong>4. Hardware channels are a finite, shared resource.</strong> A {@code PWMSparkMax} on
 * channel 5 that is never closed makes every later test that wants channel 5 fail with "already
 * allocated" — and the failure lands in an innocent test, several files away from the one that
 * leaked. {@link #manage} registers an object for automatic close, in reverse order, after the test.
 *
 * <p>All of this is the same discipline production code needs; a test suite just surfaces the
 * consequences of skipping it within seconds instead of at a competition.
 */
public abstract class RobotTestBase {

  private final Deque<AutoCloseable> managed = new ArrayDeque<>();

  @BeforeEach
  void frcprogSetUp() {
    assert HAL.initialize(500, 0) : "HAL failed to initialise";

    // Wipe every joystick axis and button back to neutral before anything else.
    //
    // Without this, a test that left the operator's right trigger pulled hands the
    // next test a robot whose scoring sequence fires on its own — which then steals
    // the roller from whatever that test was actually checking. The symptom is a
    // rubric that passes alone and fails in `frcprog check --all`, which is a
    // genuinely horrible thing to debug.
    DriverStationSim.resetData();

    // Then pretend the Driver Station has enabled us in teleop. Without this, the
    // CommandScheduler refuses to run commands, and every rubric would fail for
    // a reason that has nothing to do with the student's code.
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    DriverStationSim.setAutonomous(false);
    DriverStationSim.setTest(false);
    DriverStationSim.notifyNewData();

    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
    CommandScheduler.getInstance().getDefaultButtonLoop().clear();

    // Freeze the simulated clock so tests advance time explicitly, in whole loop
    // periods, instead of racing the wall clock. A test that depends on real
    // elapsed time passes on a fast laptop and fails on a school Chromebook.
    SimHooks.pauseTiming();
    SimHooks.restartTiming();
  }

  @AfterEach
  void frcprogTearDown() {
    // Order matters: evict the triggers BEFORE closing the hardware they poll.
    // A Trigger lives on the scheduler's button loop, not on the subsystem that
    // created it, so it happily outlives its subsystem and keeps calling
    // beamBreak.get() on a closed DIO handle. The symptom is a
    // HalHandleException thrown from a completely unrelated later test — which
    // is a genuinely horrible afternoon if you have not seen it before.
    CommandScheduler.getInstance().getDefaultButtonLoop().clear();
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
    SimHooks.resumeTiming();

    while (!managed.isEmpty()) {
      try {
        managed.pop().close();
      } catch (Exception ignored) {
        // Closing is best-effort; a failure here must not mask the real
        // assertion failure the test is trying to report.
      }
    }
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();
  }

  /**
   * Registers a resource to be closed when the test finishes.
   *
   * @param resource anything holding a hardware channel
   * @param <T> the resource type, returned unchanged so this reads as a wrapper: {@code var drive =
   *     manage(new Drive());}
   * @return {@code resource}
   */
  protected <T extends AutoCloseable> T manage(T resource) {
    managed.push(resource);
    return resource;
  }

  /**
   * Advances the robot by one loop period: run the scheduler, then move the clock 20 ms.
   *
   * <p>This is the test-suite equivalent of one call to {@code robotPeriodic()} on a real robot.
   */
  protected static void step() {
    CommandScheduler.getInstance().run();

    // stepTimingAsync, not stepTiming. The blocking version advances the clock
    // and then WAITS for every registered Notifier to be serviced — which is
    // correct on a running robot and deadlocks here, because constructing a
    // TimedRobot registers a notifier that nothing is servicing (the test drives
    // teleopPeriodic() by hand; startCompetition() never runs). The async
    // version advances the same clock without waiting for anybody.
    SimHooks.stepTimingAsync(frc.robot.Constants.LOOP_PERIOD_SECONDS);
  }

  /**
   * Advances the robot by several loop periods.
   *
   * @param cycles how many 20 ms loops to run
   */
  protected static void step(int cycles) {
    for (int i = 0; i < cycles; i++) {
      step();
    }
  }

  /**
   * Advances the robot for a number of seconds.
   *
   * @param seconds simulated seconds; rounded up to the next whole loop period
   */
  protected static void stepSeconds(double seconds) {
    step((int) Math.ceil(seconds / frc.robot.Constants.LOOP_PERIOD_SECONDS));
  }
}
