package frc.robot.subsystems.joystick;

import java.util.Random;

/** Synthesizes a swept stick value through the deadband region, with
 *  bounded Gaussian noise on top — so the student's applyDeadband
 *  sees realistic input variation. */
public class JoystickIOSim implements JoystickIO {

    private static final double SWEEP_HZ   = 0.25;   // sweeps every 4 s
    private static final double NOISE_AMP  = 0.05;   // realistic stick noise

    private final Random rng = new Random(42);       // deterministic for replay
    private double t = 0.0;
    private double clean = 0.0;

    @Override
    public void updateInputs(JoystickIOInputs inputs) {
        t += 0.02;
        double base = Math.sin(2 * Math.PI * SWEEP_HZ * t);
        inputs.rawValue   = base + (NOISE_AMP * (rng.nextDouble() * 2 - 1));
        inputs.cleanValue = clean;
        inputs.noiseOn    = true;
    }

    @Override
    public void setCleanValue(double cleanVal) {
        this.clean = cleanVal;
    }
}
