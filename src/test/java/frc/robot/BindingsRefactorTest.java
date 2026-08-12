package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import frc.robot.subsystems.roller.RollerSubsystem;
import frc.robot.testing.RobotTestBase;
import frc.robot.testing.SourceInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Rubric for lesson 14 — Refactoring with {@code *Bindings} classes.
 *
 * <p>A refactor is a change that alters structure and preserves behaviour, so grading one takes two
 * kinds of assertion: the structure must have changed, and the behaviour must not have.
 *
 * <p>Tests 1–3 check the structure. Test 4 checks that the robot still does what it did — and the
 * strongest version of that check is not in this file at all. It is that lessons 07 through 13's
 * rubrics still pass, untouched, against the refactored code. Run {@code ./tools/frcprog check
 * --all} after this lesson; a green board is the real proof.
 *
 * <p>The line-count check counts <em>code</em> lines: no blanks, no comments. Counting raw lines
 * would punish exactly the habit this curriculum is trying to build.
 */
@Tag("lesson")
@Tag("lesson-14")
class BindingsRefactorTest extends RobotTestBase {

  private static final String CONTAINER = "src/main/java/frc/robot/RobotContainer.java";
  private static final String DRIVER_BINDINGS = "src/main/java/frc/robot/bindings/DriverBindings.java";
  private static final String OPERATOR_BINDINGS =
      "src/main/java/frc/robot/bindings/OperatorBindings.java";

  private static final int MAX_CONTAINER_CODE_LINES = 85;

  private RobotContainer container;
  private XboxControllerSim operator;

  @BeforeEach
  void setUp() {
    container = manage(new RobotContainer());
    operator = new XboxControllerSim(Constants.OperatorInterface.OPERATOR_PORT);
  }

  @Test
  @DisplayName("1. The bindings classes are no longer empty shells")
  void bindingClassesDoTheWork() {
    String driver = SourceInspector.read(DRIVER_BINDINGS);
    String op = SourceInspector.read(OPERATOR_BINDINGS);

    assertTrue(
        SourceInspector.mentionsOutsideComments(DRIVER_BINDINGS, "setDefaultCommand"),
        "DriverBindings should now own the drivetrain's default command");
    assertTrue(
        SourceInspector.mentionsOutsideComments(OPERATOR_BINDINGS, "whileTrue"),
        "OperatorBindings should now own the operator's button bindings");
    assertTrue(
        SourceInspector.countCodeLines(driver) > 5, "DriverBindings still looks like a stub");
    assertTrue(
        SourceInspector.countCodeLines(op) > 8, "OperatorBindings still looks like a stub");
  }

  @Test
  @DisplayName("2. RobotContainer has actually shrunk")
  void containerIsSmall() {
    int lines = SourceInspector.countCodeLines(SourceInspector.read(CONTAINER));
    assertTrue(
        lines <= MAX_CONTAINER_CODE_LINES,
        String.format(
            "RobotContainer is %d lines of code (limit %d).%n"
                + "  Moving the bindings out is only half the refactor — the old copies have to "
                + "be DELETED, not commented out. Comments do not count toward this limit, so if "
                + "you are over, there is real code still here that belongs in a bindings class.",
            lines, MAX_CONTAINER_CODE_LINES));
  }

  @Test
  @DisplayName("3. RobotContainer constructs the bindings classes")
  void containerWiresThemUp() {
    assertTrue(
        SourceInspector.mentionsOutsideComments(CONTAINER, "new DriverBindings("),
        "RobotContainer must construct DriverBindings — moving the code out and never calling it "
            + "is a robot with no controls at all.");
    assertTrue(
        SourceInspector.mentionsOutsideComments(CONTAINER, "new OperatorBindings("),
        "RobotContainer must construct OperatorBindings");
  }

  @Test
  @DisplayName("4. Behaviour is unchanged — the buttons still work")
  void behaviourIsPreserved() {
    step(2);
    assertNotNull(
        container.getDrive().getDefaultCommand(),
        "The drivetrain lost its default command in the move. Check that DriverBindings is "
            + "actually constructed, and constructed with the right subsystem.");

    operator.setAButton(true);
    operator.notifyNewData();
    step(5);
    assertTrue(
        container.getFlywheels().getTargetRpm() > 0.0,
        "A should still spin the flywheels up after the refactor");

    operator.setAButton(false);
    operator.setBButton(true);
    operator.notifyNewData();
    step(5);
    assertEquals(
        RollerSubsystem.State.INTAKING,
        container.getRoller().getMode(),
        "B should still intake after the refactor");
  }
}
