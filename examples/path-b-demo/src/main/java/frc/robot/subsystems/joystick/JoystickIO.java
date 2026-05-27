package frc.robot.subsystems.joystick;

import org.littletonrobotics.junction.AutoLog;

/**
 * IO abstraction for the lesson-01 visualization. The sim implementation
 * emits a deterministic swept waveform + Gaussian noise so the student's
 * {@code applyDeadband} has something interesting to chew on.
 *
 * The IO pattern is overkill for one method call — that's the point.
 * Using the same pattern here as in {@code DriveIO} means lesson 02
 * doesn't introduce yet-another-abstraction; the student has already
 * seen the shape.
 */
public interface JoystickIO {

    @AutoLog
    class JoystickIOInputs {
        public double rawValue   = 0.0;   // noisy raw stick reading
        public double cleanValue = 0.0;   // value after applyDeadband
        public boolean noiseOn   = true;
    }

    default void updateInputs(JoystickIOInputs inputs) {}

    /** Feeds the student's applyDeadband output back into the inputs so
     *  AdvantageScope can plot both traces on one axis. */
    default void setCleanValue(double clean) {}
}
