/* ═══════════════════════════════════════════════════════════
   app.js  –  Wires together editor, simulation, rendering
   ═══════════════════════════════════════════════════════════ */

// ─── Starter code ──────────────────────────────────────────
const STARTER_CODE = `package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ElevatorSubsystem extends SubsystemBase {

    // ── Hardware ─────────────────────────────
    private final TalonFX m_motor = new TalonFX(5);  // CAN ID 5
    private final VoltageOut m_voltageRequest = new VoltageOut(0);

    // ── Mechanical Constants ─────────────────
    private static final double GEAR_RATIO  = 10.0;
    private static final double SPOOL_CIRCUMFERENCE = Math.PI * 0.0381; // 1.5" spool
    private static final double METERS_PER_ROTATION =
            SPOOL_CIRCUMFERENCE / GEAR_RATIO;

    // ── Setpoint Heights (meters) ────────────
    public static final double STOW = 0.0;
    public static final double LOW  = 0.5;
    public static final double MID  = 1.0;
    public static final double HIGH = 1.5;

    // ══════════════════════════════════════════
    //  TODO: Tune these gains!
    // ══════════════════════════════════════════

    // PID gains — passed to WPILib's PIDController
    private static final double kP = 0.0;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    // Feedforward gains — passed to WPILib's ElevatorFeedforward
    //   kS = static friction compensation (V)
    //   kG = gravity compensation (V) — KEY for elevators!
    //   kV = velocity feedforward (V·s/m)
    private static final double kS = 0.0;
    private static final double kG = 0.0;
    private static final double kV = 0.0;

    // ══════════════════════════════════════════

    private final PIDController m_pid =
            new PIDController(kP, kI, kD);

    private final ElevatorFeedforward m_feedforward =
            new ElevatorFeedforward(kS, kG, kV);

    private double m_setpointMeters = MID;

    // ── Constructor ──────────────────────────
    public ElevatorSubsystem() {
        m_motor.getPosition().setUpdateFrequency(50);
        m_pid.setTolerance(0.02); // 2 cm tolerance
    }

    // ── Public API ───────────────────────────
    public void setSetpoint(double meters) {
        m_setpointMeters = MathUtil.clamp(meters, STOW, HIGH);
    }

    public double getHeightMeters() {
        double rotations = m_motor.getPosition().getValueAsDouble();
        return rotations * METERS_PER_ROTATION;
    }

    public boolean atSetpoint() {
        return m_pid.atSetpoint();
    }

    // ── Periodic Loop (called every 20 ms) ──
    @Override
    public void periodic() {
        double currentHeight = getHeightMeters();

        // PID output (volts)
        double pidVolts = m_pid.calculate(currentHeight, m_setpointMeters);

        // Feedforward output (volts) — kG fights gravity!
        double ffVolts = m_feedforward.calculate(0); // velocity = 0 for holding

        // Combine and clamp to battery voltage
        double outputVolts = MathUtil.clamp(pidVolts + ffVolts, -12.0, 12.0);

        // Send voltage command to the TalonFX
        m_motor.setControl(m_voltageRequest.withOutput(outputVolts));

        // Telemetry
        SmartDashboard.putNumber("Elevator/Height", currentHeight);
        SmartDashboard.putNumber("Elevator/Setpoint", m_setpointMeters);
        SmartDashboard.putNumber("Elevator/OutputVolts", outputVolts);
        SmartDashboard.putBoolean("Elevator/AtSetpoint", atSetpoint());
    }
}
`;

// ─── Initialize CodeMirror ─────────────────────────────────
const editorEl = document.getElementById('codeEditor');
const editor = CodeMirror(editorEl, {
  value: STARTER_CODE,
  mode: 'text/x-java',
  theme: 'material-darker',
  lineNumbers: true,
  tabSize: 4,
  indentWithTabs: false,
  lineWrapping: false,
  matchBrackets: true,
  autoCloseBrackets: true,
  viewportMargin: Infinity
});

// ─── Simulation instances ──────────────────────────────────
const sim = new ElevatorSim();
const elevatorRenderer = new ElevatorRenderer(document.getElementById('elevatorCanvas'));
const graphRenderer = new GraphRenderer(document.getElementById('graphCanvas'));

// ─── DOM refs ──────────────────────────────────────────────
const btnRun = document.getElementById('btnRun');
const btnReset = document.getElementById('btnReset');
const statusBar = document.getElementById('editorStatus');
const readPos = document.getElementById('readPos');
const readSet = document.getElementById('readSet');
const readOut = document.getElementById('readOut');
const setpointBtns = document.querySelectorAll('.setpoint-btn');
const themeToggle = document.getElementById('themeToggle');

// ─── Parse gains from code ─────────────────────────────────
function parseGains(code) {
  const gains = { kP: 0, kI: 0, kD: 0, kS: 0, kG: 0, kV: 0 };
  const patterns = {
    kP: /kP\s*=\s*([+-]?\d*\.?\d+)/,
    kI: /kI\s*=\s*([+-]?\d*\.?\d+)/,
    kD: /kD\s*=\s*([+-]?\d*\.?\d+)/,
    kS: /kS\s*=\s*([+-]?\d*\.?\d+)/,
    kG: /kG\s*=\s*([+-]?\d*\.?\d+)/,
    kV: /kV\s*=\s*([+-]?\d*\.?\d+)/,
  };
  for (const [key, re] of Object.entries(patterns)) {
    const m = code.match(re);
    if (m) {
      const val = parseFloat(m[1]);
      if (!isNaN(val)) gains[key] = val;
    }
  }
  return gains;
}

// ─── Run button ────────────────────────────────────────────
function runCode() {
  const code = editor.getValue();
  try {
    const gains = parseGains(code);
    // Convert WPILib voltage-domain gains to sim's normalized domain:
    //   Sim motor output is -1..1, sim MOTOR_MAX_FORCE = 120N
    //   WPILib PID outputs volts (0-12V). Scale: 1V ≈ 1/12 of full output.
    //   Feedforward kG is in volts. In sim, kF is normalized (0-1).
    const voltScale = 1.0 / 12.0;
    sim.setGains(
      gains.kP * voltScale,
      gains.kI * voltScale,
      gains.kD * voltScale,
      (gains.kG + gains.kS) * voltScale  // kG fights gravity, kS adds static friction
    );
    sim.reset();
    sim.running = true;

    statusBar.className = 'editor-status success';
    statusBar.textContent = `✓ Deployed — PID(${gains.kP}, ${gains.kI}, ${gains.kD})  FF(kS=${gains.kS}, kG=${gains.kG}, kV=${gains.kV})`;
  } catch (e) {
    statusBar.className = 'editor-status error';
    statusBar.textContent = `✗ Build failed: ${e.message}`;
  }
}

btnRun.addEventListener('click', runCode);

// ─── Reset button ──────────────────────────────────────────
btnReset.addEventListener('click', () => {
  editor.setValue(STARTER_CODE);
  sim.setGains(0, 0, 0, 0);
  sim.reset();
  statusBar.className = 'editor-status';
  statusBar.textContent = 'Code reset to starter template.';
});

// ─── Setpoint buttons ──────────────────────────────────────
setpointBtns.forEach(btn => {
  btn.addEventListener('click', () => {
    const target = parseFloat(btn.getAttribute('data-target'));
    sim.setSetpoint(target);
    setpointBtns.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// ─── Keyboard shortcuts ───────────────────────────────────
document.addEventListener('keydown', (e) => {
  // Don't intercept when typing in editor
  if (document.activeElement && editorEl.contains(document.activeElement)) {
    // Ctrl+Enter to run even from editor
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      runCode();
    }
    return;
  }

  if (e.key === '1') pressSetpoint(0);
  if (e.key === '2') pressSetpoint(1);
  if (e.key === '3') pressSetpoint(2);
  if (e.key === '4') pressSetpoint(3);
  if (e.key === ' ') {
    e.preventDefault();
    sim.running = !sim.running;
    statusBar.className = 'editor-status';
    statusBar.textContent = sim.running ? '▶ Simulation running' : '⏸ Simulation paused';
  }
});

function pressSetpoint(idx) {
  if (setpointBtns[idx]) {
    setpointBtns[idx].click();
  }
}

// ─── Theme toggle ──────────────────────────────────────────
themeToggle.addEventListener('click', () => {
  const html = document.documentElement;
  const current = html.getAttribute('data-theme');
  const next = current === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', next);
  themeToggle.querySelector('.material-icons').textContent =
    next === 'dark' ? 'lightbulb_outline' : 'lightbulb';

  // Swap CodeMirror theme
  editor.setOption('theme', next === 'dark' ? 'material-darker' : 'default');
});

// ─── Animation loop ────────────────────────────────────────
let lastTime = 0;
let accumulator = 0;

function loop(timestamp) {
  requestAnimationFrame(loop);

  if (!lastTime) { lastTime = timestamp; return; }
  const delta = (timestamp - lastTime) / 1000;  // seconds
  lastTime = timestamp;

  // Accumulate time and run physics at fixed DT
  accumulator += delta;
  const maxSteps = 4; // prevent spiral of death
  let steps = 0;
  while (accumulator >= sim.DT && steps < maxSteps) {
    sim.step();
    accumulator -= sim.DT;
    steps++;
  }

  // Render
  elevatorRenderer.draw(sim);
  graphRenderer.draw(sim.history, sim.MAX_HEIGHT);

  // Update readouts
  readPos.textContent = sim.position.toFixed(2);
  readSet.textContent = sim.setpoint.toFixed(2);
  readOut.textContent = (sim.motorOutput * 12).toFixed(1);
}

requestAnimationFrame(loop);

// ─── Initial status ────────────────────────────────────────
statusBar.textContent = 'Edit PID & feedforward gains, then press Run (or Ctrl+Enter) to deploy';

// ─── Run once with default gains on load ───────────────────
sim.setGains(0, 0, 0, 0);
