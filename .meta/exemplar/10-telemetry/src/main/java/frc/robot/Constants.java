package frc.robot;

/**
 * Every number the robot cares about, in one place, with a name attached.
 *
 * <p>The rule this file exists to enforce: <strong>a number that means something should never appear
 * as a bare literal in robot logic.</strong> The gear ratio is not "8.45", it is {@code
 * Drive.GEAR_RATIO}. The day you change gearboxes you change one line here instead of hunting
 * through six files and missing one.
 *
 * <p>Constants are grouped into nested classes named after the thing they configure. That means call
 * sites read like sentences — {@code Constants.Elevator.MAX_HEIGHT_METERS} — and it means adding a
 * subsystem never forces you to reshuffle anybody else's imports. This is the layout every WPILib
 * project template ships with, and the layout Kelpie and Presto both use (they push it one step
 * further and put each group next to its subsystem; either is fine).
 *
 * <p>All of these are {@code public static final}:
 *
 * <ul>
 *   <li>{@code public} — anyone may read them
 *   <li>{@code static} — they belong to the class, not to an instance; you never {@code new
 *       Constants()}
 *   <li>{@code final} — nobody, including you at 2am during a competition, can reassign them
 * </ul>
 */
public final class Constants {
  private Constants() {} // utility class — never instantiated

  /** Loop period, in seconds. 50 Hz. Everything WPILib does is timed against this. */
  public static final double LOOP_PERIOD_SECONDS = 0.020;

  /** Nominal battery voltage. A fresh FRC battery is about this; under load it sags. */
  public static final double NOMINAL_BATTERY_VOLTS = 12.0;

  /** Drivetrain: two motors per side, one encoder per side. */
  public static final class Drive {
    private Drive() {}

    public static final int LEFT_MOTOR_PWM = 0;
    public static final int RIGHT_MOTOR_PWM = 1;

    public static final int LEFT_ENCODER_A_DIO = 0;
    public static final int LEFT_ENCODER_B_DIO = 1;
    public static final int RIGHT_ENCODER_A_DIO = 2;
    public static final int RIGHT_ENCODER_B_DIO = 3;

    /** Wheel radius in meters (6 inch wheels). */
    public static final double WHEEL_RADIUS_METERS = 0.0762;

    /** Distance between the left and right wheel contact patches, in meters. */
    public static final double TRACK_WIDTH_METERS = 0.62;

    /** Encoder ticks per full revolution of the wheel shaft. */
    public static final int ENCODER_CPR = 2048;

    /** Joystick readings smaller than this are treated as zero. */
    public static final double DEADBAND = 0.10;

    /** Ceiling on commanded voltage, so a runaway PID can't ask for 40 volts. */
    public static final double MAX_VOLTS = 12.0;

    /** Motor rotations per wheel rotation. */
    public static final double GEAR_RATIO = 8.45;

    // ─── Trajectory following (lesson 13) ───────────────────────────────────

    /**
     * Volts per meter per second. The feedforward gain that does the actual driving during a
     * trajectory: to travel at 2 m/s, ask for {@code 2.0 * kV_LINEAR} volts.
     *
     * <p>On a real robot you measure this with SysId (lesson 28). Here it is derived from the
     * simulated kitbot's free speed of roughly 5 m/s at 12 V.
     */
    public static final double kV_LINEAR = 2.4;

    /** Speed cap when generating trajectories. Comfortably below the drivetrain's free speed. */
    public static final double MAX_TRAJECTORY_SPEED_MPS = 2.0;

    /** Acceleration cap when generating trajectories. */
    public static final double MAX_TRAJECTORY_ACCEL_MPS2 = 1.5;
  }

  /** Intake roller — one motor, one beam-break sensor looking across the throat. */
  public static final class Roller {
    private Roller() {}

    public static final int MOTOR_PWM = 5;

    /** Beam-break digital input. Reads {@code false} when a game piece breaks the beam. */
    public static final int BEAM_BREAK_DIO = 4;

    /** Duty cycle used when pulling a game piece in. */
    public static final double INTAKE_SPEED = 0.6;

    /** Duty cycle used when spitting a game piece out. Negative: the other way. */
    public static final double EJECT_SPEED = -0.6;
  }

  /** Elevator — a carriage that travels straight up and down. */
  public static final class Elevator {
    private Elevator() {}

    public static final int MOTOR_PWM = 6;
    public static final int ENCODER_A_DIO = 5;
    public static final int ENCODER_B_DIO = 6;

    /** Meters of carriage travel per encoder pulse. */
    public static final double METERS_PER_PULSE = 2.0 * Math.PI * 0.02 / 4096.0;

    public static final double MIN_HEIGHT_METERS = 0.0;
    public static final double MAX_HEIGHT_METERS = 1.60;

    /** Physical model: gearbox reduction between motor and drum. */
    public static final double GEARING = 6.0;

    /** Mass of everything the elevator lifts, in kilograms. */
    public static final double CARRIAGE_MASS_KG = 5.0;

    /** Radius of the drum the belt wraps around, in meters. */
    public static final double DRUM_RADIUS_METERS = 0.02;

    /** The four heights the operator can ask for, in meters. */
    public static final double STOW_METERS = 0.05;

    public static final double LOW_METERS = 0.45;
    public static final double MID_METERS = 0.90;
    public static final double HIGH_METERS = 1.45;

    /** How close counts as "there". */
    public static final double TOLERANCE_METERS = 0.02;

    /**
     * Volts required to hold the carriage still against gravity.
     *
     * <p>Constant, because an elevator weighs the same at every height. Supplied for you in lesson
     * 05 so the lesson can be about the feedback loop; lesson 06 makes you compute the equivalent
     * number for an arm, where it is <em>not</em> constant.
     */
    public static final double kG_HOLD = 0.30;

    /**
     * Tuned by the recipe in lesson 05: raise kP until it rings, add kD until the ringing stops,
     * add kI only if it parks short. These numbers are for THIS mechanism at THIS mass; copying
     * them onto a different elevator is how tuning lore starts.
     */
    public static final double kP = 40.0;

    public static final double kI = 0.0;
    public static final double kD = 6.0;
  }

  /** Shoulder — a single-jointed arm that pivots the end effector. */
  public static final class Shoulder {
    private Shoulder() {}

    public static final int MOTOR_PWM = 7;
    public static final int ENCODER_A_DIO = 7;
    public static final int ENCODER_B_DIO = 8;

    /** Radians of arm rotation per encoder pulse. */
    public static final double RADIANS_PER_PULSE = 2.0 * Math.PI / 4096.0;

    public static final double GEARING = 100.0;
    public static final double ARM_LENGTH_METERS = 0.60;
    public static final double ARM_MASS_KG = 3.0;

    public static final double MIN_ANGLE_RADIANS = -Math.PI / 2.0; // straight down
    public static final double MAX_ANGLE_RADIANS = Math.PI / 2.0; // straight up

    public static final double DOWN_RADIANS = -Math.PI / 4.0;
    public static final double LEVEL_RADIANS = 0.0;
    public static final double UP_RADIANS = Math.PI / 3.0;

    public static final double TOLERANCE_RADIANS = Math.toRadians(2.0);

    public static final double kP = 12.0;
    public static final double kI = 0.0;
    public static final double kD = 0.5;

    /**
     * Volts needed to hold the arm horizontal against gravity. Measured, not guessed: command the
     * arm to sit level, raise the voltage until it stops falling, write that number down.
     */
    public static final double kG = 0.12;
  }

  /** Shooter flywheels — spun up to a target speed, then a game piece is fed in. */
  public static final class Flywheels {
    private Flywheels() {}

    public static final int MOTOR_PWM = 8;
    public static final int ENCODER_A_DIO = 9;
    public static final int ENCODER_B_DIO = 10;

    public static final double RPM_PER_PULSE = 60.0 / 2048.0;

    /** Gearbox reduction between motor and wheel. 1.0 = direct drive. */
    public static final double GEARING = 1.0;

    /** Moment of inertia of the spinning mass, kg·m². */
    public static final double MOI_KG_M2 = 0.004;

    /** Volts per RPM. Measured with SysId on a real robot; guessed here. */
    public static final double kV = 12.0 / 5800.0;

    public static final double kP = 0.0015;

    /** Target speed when shooting. */
    public static final double SHOOT_RPM = 3000.0;

    /** Fraction of target speed that counts as "ready to shoot". */
    public static final double READY_FRACTION = 0.70;

    /** Free speed of the shooter motor, in RPM. */
    public static final double MAX_RPM = 5800.0;
  }

  /** Which USB port each controller is plugged into on the Driver Station. */
  public static final class OperatorInterface {
    private OperatorInterface() {}

    public static final int DRIVER_PORT = 0;
    public static final int OPERATOR_PORT = 1;
  }
}
