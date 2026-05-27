/* ═══════════════════════════════════════════════════════════
   app.js — wires editor + sim + filesystem for the
   Methods (Functions) lesson.
   ═══════════════════════════════════════════════════════════ */

const LESSON_SLUG    = 'functions-deadband';
const FILE_PATH      = 'src/main/java/frc/robot/util/MathUtils.java';

const STARTER_CODE = `package frc.robot.util;

/**
 * Reusable math helpers used throughout the robot code.
 *
 * Today we add one method: applyDeadband(value, threshold).
 * Future lessons (Tank Drive, Subsystems) will import and
 * call this same method — so once you write it, you're done
 * forever.
 */
public class MathUtils {

    /**
     * Returns 0 when |value| is below the threshold, otherwise
     * returns value unchanged. Used to ignore tiny joystick
     * readings caused by sensor noise.
     *
     * @param value     raw input, typically in [-1.0, 1.0]
     * @param threshold magnitude below which input is ignored
     */
    public static double applyDeadband(double value, double threshold) {
        // TODO: implement me!
        return value;
    }
}
`;

/* ─── Lesson manifest (drives the filesystem panel) ─── */
const MANIFEST = {
  editing:  [FILE_PATH],
  prior:    [],   // no prerequisites
  upcoming: [
    { path: 'src/main/java/frc/robot/subsystems/DriveSubsystem.java',
      label: 'Tank Drive lesson' },
    { path: 'src/main/java/frc/robot/subsystems/ElevatorSubsystem.java',
      label: 'Elevator PID lesson' },
  ],
};

/* ─── Boot: load saved file if any, otherwise starter ─ */
const initialCode = window.FS.read(FILE_PATH) || STARTER_CODE;

const editor = CodeMirror(document.getElementById('codeEditor'), {
  value:             initialCode,
  mode:              'text/x-java',
  theme:             'material-darker',
  lineNumbers:       true,
  tabSize:           4,
  indentWithTabs:    false,
  matchBrackets:     true,
  autoCloseBrackets: true,
  viewportMargin:    Infinity,
});

/* ─── Sim instances ─────────────────────────────────── */
const sim   = new JoystickSim();
const graph = new JoystickGraph(document.getElementById('graphCanvas'));

/* ─── DOM refs ──────────────────────────────────────── */
const btnRun       = document.getElementById('btnRun');
const btnReset     = document.getElementById('btnReset');
const btnResetFs   = document.getElementById('btnResetFs');
const statusBar    = document.getElementById('editorStatus');
const fsStatus     = document.getElementById('fsStatus');
const noiseToggle  = document.getElementById('noiseToggle');
const themeToggle  = document.getElementById('themeToggle');
const joystickPad  = document.getElementById('joystickPad');
const dotRaw       = document.getElementById('dotRaw');
const dotClean     = document.getElementById('dotClean');
const motorXFill   = document.getElementById('motorXFill');
const motorYFill   = document.getElementById('motorYFill');
const motorXIndic  = document.getElementById('motorIndicator');
const motorYIndic  = document.getElementById('motorIndicatorY');

/* ─── Filesystem tree render ────────────────────────── */
function refreshFsPanel() {
  window.renderFilesystemTree(document.getElementById('fsTree'), MANIFEST);
  const n = window.FS.list().length;
  fsStatus.textContent = n === 0 ? 'empty' : `${n} file${n > 1 ? 's' : ''} saved`;
}
refreshFsPanel();

/* ─── Java -> JS body translator ────────────────────── */
/* This is intentionally tiny: it handles the subset of Java
   used in early lessons (primitive math, Math.* calls). The
   Infrastructure-Analysis.md doc discusses how to scale this
   into a real Java subset interpreter. */
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
    // strip primitive type declarations: `double x = ...` -> `x = ...`
    .replace(/\b(double|int|float|long|boolean|String|var)\s+(?=[A-Za-z_])/g, '')
    // Java's Math.signum is named Math.sign in JS
    .replace(/\bMath\.signum\b/g, 'Math.sign');
}

function compileStudentFn(code) {
  const body = extractMethodBody(code, 'applyDeadband');
  if (body === null) {
    throw new Error('Could not find applyDeadband(...) — did you rename it?');
  }
  const js = javaBodyToJs(body);
  // eslint-disable-next-line no-new-func
  return new Function('value', 'threshold', 'Math', js + '\nreturn value;')
    // bind Math so the body has it in scope without a parameter
    .bind(null);
}

/* ─── Run ───────────────────────────────────────────── */
function runCode() {
  const code = editor.getValue();
  try {
    const fn = compileStudentFn(code);
    // wrap to inject Math, since `new Function` body uses it
    sim.setDeadbandFn((v, t) => fn(v, t, Math));

    // Persist to filesystem
    window.FS.write(FILE_PATH, code);
    window.FS.markComplete(LESSON_SLUG);
    refreshFsPanel();

    statusBar.className = 'editor-status success';
    statusBar.textContent =
      `✓ Deployed and saved to ${FILE_PATH}. Test it by moving the joystick.`;
  } catch (e) {
    statusBar.className = 'editor-status error';
    statusBar.textContent = `✗ Build failed: ${e.message}`;
  }
}
btnRun.addEventListener('click', runCode);

btnReset.addEventListener('click', () => {
  editor.setValue(STARTER_CODE);
  sim.setDeadbandFn((v, _) => v);
  statusBar.className = 'editor-status';
  statusBar.textContent = 'Reset to starter — Run to redeploy.';
});

btnResetFs.addEventListener('click', () => {
  if (!confirm('Wipe your entire saved project (all lessons)? This cannot be undone.'))
    return;
  window.FS.clear();
  editor.setValue(STARTER_CODE);
  refreshFsPanel();
  statusBar.className = 'editor-status';
  statusBar.textContent = 'Project wiped. You can start fresh.';
});

/* ─── Noise toggle ──────────────────────────────────── */
noiseToggle.addEventListener('change', () => {
  sim.noiseEnabled = noiseToggle.checked;
});

/* ─── Joystick input: pointer drag ──────────────────── */
let dragging = false;

function setJoystickFromEvent(e) {
  const r = joystickPad.getBoundingClientRect();
  const cx = r.left + r.width  / 2;
  const cy = r.top  + r.height / 2;
  const dx = (e.clientX - cx) / (r.width  / 2);
  const dy = (e.clientY - cy) / (r.height / 2);
  // Clamp to unit circle for niceness
  const mag = Math.hypot(dx, dy);
  if (mag > 1) {
    sim.setTrue(dx / mag, -dy / mag);  // invert Y so up is positive
  } else {
    sim.setTrue(dx, -dy);
  }
}

joystickPad.addEventListener('pointerdown', e => {
  dragging = true;
  joystickPad.setPointerCapture(e.pointerId);
  setJoystickFromEvent(e);
});
joystickPad.addEventListener('pointermove', e => { if (dragging) setJoystickFromEvent(e); });
joystickPad.addEventListener('pointerup',   e => {
  dragging = false;
  joystickPad.releasePointerCapture(e.pointerId);
  sim.setTrue(0, 0);  // release => center
});

/* ─── Joystick input: WASD ──────────────────────────── */
const heldKeys = new Set();

document.addEventListener('keydown', e => {
  // Don't capture keys while typing in the editor
  if (document.activeElement &&
      document.getElementById('codeEditor').contains(document.activeElement)) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      runCode();
    }
    return;
  }
  if ('wasd'.includes(e.key.toLowerCase())) {
    e.preventDefault();
    heldKeys.add(e.key.toLowerCase());
  }
});
document.addEventListener('keyup', e => {
  heldKeys.delete(e.key.toLowerCase());
});

function updateFromKeys() {
  let x = 0, y = 0;
  if (heldKeys.has('a')) x -= 1;
  if (heldKeys.has('d')) x += 1;
  if (heldKeys.has('w')) y += 1;
  if (heldKeys.has('s')) y -= 1;
  if (x !== 0 || y !== 0) {
    const mag = Math.hypot(x, y);
    sim.setTrue(x / mag, y / mag);
  } else if (!dragging) {
    sim.setTrue(0, 0);
  }
}

/* ─── Theme toggle ──────────────────────────────────── */
themeToggle.addEventListener('click', () => {
  const html = document.documentElement;
  const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', next);
  themeToggle.querySelector('.material-icons').textContent =
    next === 'dark' ? 'lightbulb_outline' : 'lightbulb';
  editor.setOption('theme', next === 'dark' ? 'material-darker' : 'default');
});

/* ─── Animation loop ────────────────────────────────── */
let lastT = 0, accum = 0;
function loop(t) {
  requestAnimationFrame(loop);
  if (!lastT) { lastT = t; return; }
  const dt = (t - lastT) / 1000;
  lastT = t;

  updateFromKeys();

  accum += dt;
  let steps = 0;
  while (accum >= sim.DT && steps < 4) {
    sim.step();
    accum -= sim.DT;
    steps++;
  }

  // Draw graph
  graph.draw(sim.history, sim.THRESHOLD);

  // Update joystick dots
  const padPx = joystickPad.getBoundingClientRect();
  const w = padPx.width, h = padPx.height;
  const place = (dot, x, y) => {
    dot.style.left = (50 + x * 48) + '%';
    dot.style.top  = (50 - y * 48) + '%';
  };
  place(dotRaw,   sim.noisyX, sim.noisyY);
  place(dotClean, sim.cleanX, sim.cleanY);

  // Motor bars (centered, fill grows L or R)
  const drawBar = (fill, value) => {
    const pct = Math.abs(value) * 50;
    if (value >= 0) {
      fill.style.left = '50%';
      fill.style.width = pct + '%';
    } else {
      fill.style.left = (50 - pct) + '%';
      fill.style.width = pct + '%';
    }
  };
  drawBar(motorXFill, sim.cleanX);
  drawBar(motorYFill, sim.cleanY);

  // Buzz indicator
  motorXIndic.classList.toggle('buzzing', Math.abs(sim.cleanX) > 0.01);
  motorYIndic.classList.toggle('buzzing', Math.abs(sim.cleanY) > 0.01);
}
requestAnimationFrame(loop);

/* ─── Initial status ────────────────────────────────── */
if (window.FS.exists(FILE_PATH)) {
  statusBar.textContent =
    `Loaded your saved ${FILE_PATH} from your project. Press Run to re-deploy.`;
  try {
    sim.setDeadbandFn(compileStudentFn(initialCode));
  } catch {/* fall back to identity */}
} else {
  statusBar.textContent =
    'Edit the method body, then press Run (or Ctrl+Enter) to deploy.';
}
