/* ═══════════════════════════════════════════════════════════
   app.js — Tank Drive lesson.

   Demonstrates filesystem-compounding: this lesson IMPORTS
   the MathUtils.java file the student wrote in the Functions
   lesson. If they did that lesson, their code runs here. If
   they didn't, we fall back to a working stub but flag it.
   ═══════════════════════════════════════════════════════════ */

const LESSON_SLUG  = 'tank-drive-wiring';
const DRIVE_PATH   = 'src/main/java/frc/robot/subsystems/DriveSubsystem.java';
const MATH_PATH    = 'src/main/java/frc/robot/util/MathUtils.java';

const STARTER_DRIVE = `package frc.robot.subsystems;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.util.MathUtils;   // ← from your Methods lesson!

public class DriveSubsystem extends SubsystemBase {

    // Hardware
    private final PWMSparkMax m_leftMotor  = new PWMSparkMax(0);
    private final PWMSparkMax m_rightMotor = new PWMSparkMax(1);
    private final XboxController m_driver  = new XboxController(0);

    // Tunable: ignore stick readings below this magnitude
    private static final double DEADBAND = 0.10;

    @Override
    public void periodic() {
        double forward  = -m_driver.getLeftY();   // up on stick = +forward
        double rotation =  m_driver.getRightX();

        // TODO 1: pass each through MathUtils.applyDeadband
        double forwardClean  = forward;
        double rotationClean = rotation;

        // TODO 2: arcade-drive mixing
        double leftOut  = 0.0;
        double rightOut = 0.0;

        // TODO 3: send commands to the motors
        // m_leftMotor.set(leftOut);
        // m_rightMotor.set(rightOut);
    }
}
`;

/* A working fallback so this lesson still runs if the student
   skipped the Methods lesson — but we'll tell them about it. */
const FALLBACK_MATH = `package frc.robot.util;
// PLACEHOLDER — written by the website because you skipped the
// Methods lesson. Go back and write this yourself!
public class MathUtils {
    public static double applyDeadband(double v, double t) {
        return Math.abs(v) < t ? 0.0 : v;
    }
}
`;

/* ─── Manifest ─────────────────────────────────────── */
const MANIFEST = {
  editing:  [DRIVE_PATH],
  prior:    [MATH_PATH],
  upcoming: [
    { path: 'src/main/java/frc/robot/subsystems/ElevatorSubsystem.java',
      label: 'Elevator PID lesson' },
    { path: 'src/main/java/frc/robot/Robot.java',
      label: 'Robot wiring lesson' },
  ],
};

/* ─── Load files ───────────────────────────────────── */
const initialDrive = window.FS.read(DRIVE_PATH) || STARTER_DRIVE;
const initialMath  = window.FS.read(MATH_PATH)  || FALLBACK_MATH;
const mathIsPlaceholder = !window.FS.exists(MATH_PATH);

const editor = CodeMirror(document.getElementById('codeEditor'), {
  value:             initialDrive,
  mode:              'text/x-java',
  theme:             'material-darker',
  lineNumbers:       true,
  tabSize:           4,
  indentWithTabs:    false,
  matchBrackets:     true,
  autoCloseBrackets: true,
  viewportMargin:    Infinity,
});

/* Track which file is shown in the editor */
let activeTab = 'drive';
const driveBuffer = initialDrive;
let driveSnapshot = initialDrive;  // last value when on drive tab

/* ─── Sim instances ────────────────────────────────── */
const sim = new TankDriveSim();
const renderer = new RobotRenderer(document.getElementById('robotCanvas'));

/* ─── DOM refs ─────────────────────────────────────── */
const btnRun     = document.getElementById('btnRun');
const btnReset   = document.getElementById('btnReset');
const btnResetFs = document.getElementById('btnResetFs');
const statusBar  = document.getElementById('editorStatus');
const fsStatus   = document.getElementById('fsStatus');
const fileLabel  = document.getElementById('editorFileLabel');
const leftBar    = document.getElementById('leftBar');
const rightBar   = document.getElementById('rightBar');
const leftVal    = document.getElementById('leftVal');
const rightVal   = document.getElementById('rightVal');
const themeToggle= document.getElementById('themeToggle');

function refreshFsPanel() {
  window.renderFilesystemTree(document.getElementById('fsTree'), MANIFEST);
  const n = window.FS.list().length;
  fsStatus.textContent = n === 0 ? 'empty' : `${n} file${n > 1 ? 's' : ''} saved`;
}
refreshFsPanel();

/* ─── Tab switching ────────────────────────────────── */
document.querySelectorAll('.file-tab').forEach(tab => {
  tab.addEventListener('click', () => {
    const which = tab.dataset.tab;
    if (which === activeTab) return;

    if (activeTab === 'drive') {
      // remember what user has in the editor
      driveSnapshot = editor.getValue();
    }
    document.querySelectorAll('.file-tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    activeTab = which;

    if (which === 'drive') {
      editor.setValue(driveSnapshot);
      editor.setOption('readOnly', false);
      fileLabel.innerHTML = '<span class="material-icons" style="font-size:16px;color:#4caf50">description</span> src/main/java/frc/robot/subsystems/DriveSubsystem.java';
    } else {
      editor.setValue(window.FS.read(MATH_PATH) || FALLBACK_MATH);
      editor.setOption('readOnly', true);
      fileLabel.innerHTML = '<span class="material-icons" style="font-size:16px;color:#448aff">description</span> src/main/java/frc/robot/util/MathUtils.java &nbsp;<em style="color:#448aff;font-size:0.7rem">(read-only — owned by previous lesson)</em>';
    }
  });
});

/* ─── Java -> JS translator (slightly more capable
       than the Functions PoC: handles a periodic() body
       calling methods on injected objects) ─────────── */
function extractMethodBody(code, methodName) {
  const startRe = new RegExp(
    String.raw`\b` + methodName + String.raw`\s*\([^)]*\)\s*\{`
  );
  const m = code.match(startRe);
  if (!m) return null;
  const start = m.index + m[0].length;
  let depth = 1, i = start;
  while (i < code.length && depth > 0) {
    if (code[i] === '{') depth++;
    else if (code[i] === '}') depth--;
    i++;
  }
  return code.slice(start, i - 1);
}

function javaBodyToJs(body) {
  return body
    .replace(/\b(double|int|float|long|boolean|String|var)\s+(?=[A-Za-z_])/g, '')
    .replace(/\bMath\.signum\b/g, 'Math.sign')
    // strip the "final" keyword if any
    .replace(/\bfinal\s+/g, '');
}

/* Factory: returns a function periodic(driver, leftMotor, rightMotor)
   that calls the student's compiled periodic() body. */
function buildPeriodic(driveCode, mathCode) {
  const body = extractMethodBody(driveCode, 'periodic');
  if (body === null) throw new Error('periodic() not found');
  const deadbandBody = extractMethodBody(mathCode, 'applyDeadband');
  if (deadbandBody === null) {
    throw new Error('MathUtils.applyDeadband missing — finish Methods lesson');
  }
  const deadbandFn = new Function(
    'value', 'threshold', 'Math',
    javaBodyToJs(deadbandBody) + '\nreturn value;'
  );
  const MathUtils = {
    applyDeadband: (v, t) => deadbandFn(v, t, Math)
  };

  // Extract DEADBAND constant from drive code (default 0.10)
  const dbMatch = driveCode.match(/DEADBAND\s*=\s*([+-]?\d*\.?\d+)/);
  const DEADBAND = dbMatch ? parseFloat(dbMatch[1]) : 0.10;

  const fn = new Function(
    'm_driver', 'm_leftMotor', 'm_rightMotor', 'MathUtils', 'DEADBAND', 'Math',
    javaBodyToJs(body)
  );

  return (driver, leftMotor, rightMotor) =>
    fn(driver, leftMotor, rightMotor, MathUtils, DEADBAND, Math);
}

/* ─── Compiled state ───────────────────────────────── */
let periodicFn = null;

/* ─── WASD input state ─────────────────────────────── */
const heldKeys = new Set();
const joystick = { leftY: 0, rightX: 0 };

function updateJoystickFromKeys() {
  let fy = 0, rx = 0;
  if (heldKeys.has('w')) fy -= 1;   // getLeftY returns negative when pushed up
  if (heldKeys.has('s')) fy += 1;
  if (heldKeys.has('a')) rx -= 1;
  if (heldKeys.has('d')) rx += 1;
  joystick.leftY  = fy;
  joystick.rightX = rx;
  document.querySelectorAll('.wasd-pad__key').forEach(k => {
    const key = k.dataset.key;
    if (key) k.classList.toggle('held', heldKeys.has(key));
  });
}

/* ─── Mock WPILib objects ──────────────────────────── */
const mockDriver = {
  getLeftY:  () => joystick.leftY,
  getRightX: () => joystick.rightX,
  getLeftX:  () => 0,
  getRightY: () => 0,
};
const mockLeftMotor  = { _v: 0, set(v) { this._v = v; } };
const mockRightMotor = { _v: 0, set(v) { this._v = v; } };

/* ─── Run ──────────────────────────────────────────── */
function runCode() {
  const driveCode = activeTab === 'drive' ? editor.getValue() : driveSnapshot;
  const mathCode  = window.FS.read(MATH_PATH) || FALLBACK_MATH;
  try {
    periodicFn = buildPeriodic(driveCode, mathCode);
    window.FS.write(DRIVE_PATH, driveCode);
    window.FS.markComplete(LESSON_SLUG);
    refreshFsPanel();

    if (mathIsPlaceholder) {
      statusBar.className = 'editor-status';
      statusBar.textContent =
        `⚠ Deployed using a placeholder MathUtils — go back and finish the Methods lesson for real credit.`;
    } else {
      statusBar.className = 'editor-status success';
      statusBar.textContent =
        `✓ Deployed. periodic() runs every 20 ms. Drive with WASD.`;
    }
  } catch (e) {
    periodicFn = null;
    statusBar.className = 'editor-status error';
    statusBar.textContent = `✗ Build failed: ${e.message}`;
  }
}
btnRun.addEventListener('click', runCode);

btnReset.addEventListener('click', () => {
  driveSnapshot = STARTER_DRIVE;
  if (activeTab === 'drive') editor.setValue(STARTER_DRIVE);
  periodicFn = null;
  statusBar.className = 'editor-status';
  statusBar.textContent = 'Reset to starter — Run to redeploy.';
});

btnResetFs.addEventListener('click', () => {
  if (!confirm('Wipe your entire saved project (all lessons)? This cannot be undone.'))
    return;
  window.FS.clear();
  driveSnapshot = STARTER_DRIVE;
  editor.setValue(STARTER_DRIVE);
  periodicFn = null;
  refreshFsPanel();
  statusBar.className = 'editor-status';
  statusBar.textContent = 'Project wiped.';
});

/* ─── Keyboard ─────────────────────────────────────── */
document.addEventListener('keydown', e => {
  if (document.activeElement &&
      document.getElementById('codeEditor').contains(document.activeElement)) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      runCode();
    }
    return;
  }
  const k = e.key.toLowerCase();
  if ('wasd'.includes(k)) {
    e.preventDefault();
    heldKeys.add(k);
  }
  if (k === 'r') sim.reset();
});
document.addEventListener('keyup', e => {
  heldKeys.delete(e.key.toLowerCase());
});

/* On-screen pad: click & hold buttons */
document.querySelectorAll('.wasd-pad__key').forEach(k => {
  const key = k.dataset.key;
  if (!key) return;
  k.addEventListener('pointerdown', e => {
    k.setPointerCapture(e.pointerId);
    heldKeys.add(key);
  });
  k.addEventListener('pointerup',   () => heldKeys.delete(key));
  k.addEventListener('pointerleave', () => heldKeys.delete(key));
});

/* ─── Theme ────────────────────────────────────────── */
themeToggle.addEventListener('click', () => {
  const html = document.documentElement;
  const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', next);
  themeToggle.querySelector('.material-icons').textContent =
    next === 'dark' ? 'lightbulb_outline' : 'lightbulb';
  editor.setOption('theme', next === 'dark' ? 'material-darker' : 'default');
});

/* ─── Animation loop ───────────────────────────────── */
let lastT = 0, accum = 0;
function loop(t) {
  requestAnimationFrame(loop);
  if (!lastT) { lastT = t; return; }
  const dt = (t - lastT) / 1000;
  lastT = t;

  updateJoystickFromKeys();

  accum += dt;
  let steps = 0;
  while (accum >= sim.DT && steps < 4) {
    // Run student periodic() if compiled, else default to zero
    if (periodicFn) {
      try {
        periodicFn(mockDriver, mockLeftMotor, mockRightMotor);
      } catch (e) {
        statusBar.className = 'editor-status error';
        statusBar.textContent = `✗ Runtime error in periodic(): ${e.message}`;
        periodicFn = null;
      }
    } else {
      mockLeftMotor._v = 0;
      mockRightMotor._v = 0;
    }
    // Clamp motor demands
    sim.leftDemand  = Math.max(-1, Math.min(1, mockLeftMotor._v || 0));
    sim.rightDemand = Math.max(-1, Math.min(1, mockRightMotor._v || 0));
    sim.step();
    accum -= sim.DT;
    steps++;
  }

  renderer.draw(sim);

  // Motor bars
  const place = (fill, v) => {
    const pct = Math.abs(v) * 50;
    if (v >= 0) { fill.style.left = '50%'; fill.style.width = pct + '%'; }
    else        { fill.style.left = (50 - pct) + '%'; fill.style.width = pct + '%'; }
  };
  place(leftBar,  sim.leftDemand);
  place(rightBar, sim.rightDemand);
  leftVal.textContent  = sim.leftDemand.toFixed(2);
  rightVal.textContent = sim.rightDemand.toFixed(2);
}
requestAnimationFrame(loop);

/* ─── Initial status ───────────────────────────────── */
if (mathIsPlaceholder) {
  statusBar.className = 'editor-status';
  statusBar.textContent =
    '⚠ MathUtils.java not in your project — using a placeholder. Finish the Methods lesson for full credit.';
} else if (window.FS.exists(DRIVE_PATH)) {
  statusBar.textContent = 'Loaded your saved DriveSubsystem.java. Press Run to re-deploy.';
} else {
  statusBar.textContent = 'Fill in the periodic() body, then Run (or Ctrl+Enter).';
}
