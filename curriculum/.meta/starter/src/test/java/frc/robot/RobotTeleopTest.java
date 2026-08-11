package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.wpilibj.simulation.DIOSim;
import edu.wpi.first.wpilibj.simulation.PWMSim;
import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import frc.robot.testing.RobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 03 — Conditionals in {@code teleopPeriodic}.
 *
 * <p>Four scenarios, straight from what the drivers asked for. Notice that this test grades
 * <em>behaviour at the motor</em>, not the shape of your code: it presses buttons, breaks a beam,
 * and reads the PWM output. That is deliberate, and it is why this same test still passes after
 * lesson 04 moves all of the logic into a subsystem. A good rubric outlives the implementation it
 * was written against.
 *
 * <p>The pieces:
 *
 * <ul>
 *   <li>{@link XboxControllerSim} injects button presses into the Driver Station's data, exactly as
 *       a real controller would.
 *   <li>{@link DIOSim} sets what the beam-break sensor reads. {@code setValue(false)} means the beam
 *       is broken — a game piece is in the way.
 *   <li>{@link PWMSim} reads back what the roller motor was actually told to do.
 * </ul>
 */
@Tag("lesson")
@Tag("lesson-03")
class RobotTeleopTest extends RobotTestBase {

  /**
   * How close a PWM readback has to be.
   *
   * <p>Not 1e-6, and the reason is worth knowing. A PWM output is a pulse width, quantised to
   * microseconds by the roboRIO's FPGA. Asking for 0.6 produces a pulse that reads back as
   * 0.59960..., because 0.6 is not exactly representable in that grid. Real hardware has the same
   * granularity — this is not a simulation artefact — so a rubric that demanded exact equality
   * would be demanding something the electronics cannot deliver.
   */
  private static final double PWM_TOLERANCE = 0.005;


  private Robot robot;
  private XboxControllerSim operator;
  private DIOSim beamBreak;
  private PWMSim rollerMotor;

  @BeforeEach
  void setUp() {
    robot = manage(new Robot());
    operator = new XboxControllerSim(Constants.OperatorInterface.OPERATOR_PORT);
    beamBreak = new DIOSim(Constants.Roller.BEAM_BREAK_DIO);
    rollerMotor = new PWMSim(Constants.Roller.MOTOR_PWM);

    setGamePiecePresent(false);
    releaseAllButtons();
  }

  @Test
  @DisplayName("1. Hold B with an empty intake — the roller pulls in")
  void bButtonIntakes() {
    operator.setBButton(true);
    operator.notifyNewData();
    setGamePiecePresent(false);

    tick();

    assertEquals(
        Constants.Roller.INTAKE_SPEED,
        rollerMotor.getSpeed(),
        PWM_TOLERANCE,
        "Holding B with nothing in the intake should run the roller inward");
  }

  @Test
  @DisplayName("2. Hold B with a game piece already there — the roller stops")
  void bButtonStopsWhenLoaded() {
    operator.setBButton(true);
    operator.notifyNewData();
    setGamePiecePresent(true);

    tick();

    assertEquals(
        0.0,
        rollerMotor.getSpeed(),
        PWM_TOLERANCE,
        "Once the beam is broken we already have a game piece. Continuing to run the roller just "
            + "grinds it against a hard stop.");
  }

  @Test
  @DisplayName("3. Hold X — eject wins, even over B, even with a piece loaded")
  void xButtonEjectsAndOverridesB() {
    operator.setBButton(true);
    operator.setXButton(true);
    operator.notifyNewData();
    setGamePiecePresent(true);

    tick();

    assertEquals(
        Constants.Roller.EJECT_SPEED,
        rollerMotor.getSpeed(),
        PWM_TOLERANCE,
        "X must win. Check X FIRST in your if/else chain — the branch you test first is the "
            + "branch that has priority.");
  }

  @Test
  @DisplayName("4. Nothing held — the roller is off")
  void nothingHeldMeansStopped() {
    releaseAllButtons();
    setGamePiecePresent(false);

    tick();

    assertEquals(
        0.0,
        rollerMotor.getSpeed(),
        PWM_TOLERANCE,
        "Every if/else chain needs a final else. Without one the motor keeps whatever it was last "
            + "told, forever.");
  }

  // ─── helpers ───────────────────────────────────────────────────────────────

  /**
   * Runs one robot loop, the way the Driver Station would.
   *
   * <p>Both calls matter, and which one does the work depends on how far through the curriculum you
   * are. Before lesson 04, {@code teleopPeriodic()} writes the motor itself. After lesson 04 it only
   * sets a mode, and the scheduler's call to {@code RollerSubsystem.periodic()} is what actually
   * moves the motor. Doing both keeps this rubric honest across the refactor.
   */
  private void tick() {
    robot.teleopPeriodic();
    step();
  }

  /**
   * @param present true to break the beam, i.e. to put a game piece in the intake
   */
  private void setGamePiecePresent(boolean present) {
    // A beam-break reads HIGH when the beam is intact, so "piece present" is a
    // LOW reading. Inverting it here mirrors what the robot code does.
    beamBreak.setValue(!present);
  }

  private void releaseAllButtons() {
    operator.setBButton(false);
    operator.setXButton(false);
    operator.notifyNewData();
  }
}
