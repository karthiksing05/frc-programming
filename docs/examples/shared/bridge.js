/* ═══════════════════════════════════════════════════════════
   bridge.js — Path A → Path B "Graduate to VS Code" export
   ───────────────────────────────────────────────────────────
   Takes the student's in-browser filesystem (see filesystem.js)
   and produces a .zip containing the full path-b-demo skeleton
   with the student's edited files merged in on top.

   Usage:
     <script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js"></script>
     <script src="/examples/shared/filesystem.js"></script>
     <script src="/examples/shared/bridge.js"></script>
     ...
     const blob = await exportProject(window.FS);
     // -> trigger download

   Or use the helper:
     exportButton(document.getElementById('downloadProject'), window.FS);

   Design choices:
     • Skeleton file list is hardcoded (SKELETON_FILES below) so the
       bridge doesn't depend on a directory-listing endpoint. Each path
       is fetched at runtime from SKELETON_BASE — by default this is
       `/examples/path-b-demo/` because serve.sh symlinks the repo's
       examples/ into the MkDocs docs/ root.
     • For each path that exists in BOTH the skeleton and the student's
       FS, the student's version wins. This is the whole point of the
       bridge: lesson 01's MathUtils.java overrides the stub.
     • The zip is rooted at `my-frc-learning/` so unzipping produces a
       single project directory rather than spraying files everywhere.
   ═══════════════════════════════════════════════════════════ */

/* All files in examples/path-b-demo/ that should ship in the zip. */
const SKELETON_FILES = [
  '.gitignore',
  'README.md',
  'build.gradle',
  'gradle.properties',
  'settings.gradle',
  '.github/workflows/ci.yml',
  'lessons/manifest.json',
  'lessons/01-methods/README.md',
  'lessons/01-methods/hints.md',
  'lessons/01-methods/lesson.json',
  'lessons/02-tank-drive/README.md',
  'lessons/02-tank-drive/hints.md',
  'lessons/02-tank-drive/lesson.json',
  'src/main/java/frc/robot/Main.java',
  'src/main/java/frc/robot/Robot.java',
  'src/main/java/frc/robot/Constants.java',
  'src/main/java/frc/robot/RobotContainer.java',
  'src/main/java/frc/robot/util/MathUtils.java',
  'src/main/java/frc/robot/subsystems/drive/Drive.java',
  'src/main/java/frc/robot/subsystems/drive/DriveIO.java',
  'src/main/java/frc/robot/subsystems/drive/DriveIOSim.java',
  'src/main/java/frc/robot/subsystems/drive/DriveIOReal.java',
  'src/main/java/frc/robot/subsystems/joystick/JoystickIO.java',
  'src/main/java/frc/robot/subsystems/joystick/JoystickIOSim.java',
  'src/test/java/frc/robot/util/MathUtilsTest.java',
  'src/test/java/frc/robot/subsystems/drive/DriveTest.java',
  'tools/frcprog.sh',
  'vendordeps/AdvantageKit.json',
  'vendordeps/WPILibNewCommands.json',
];

/* Where to fetch the skeleton from. Override via exportProject(fs, {base}). */
const DEFAULT_SKELETON_BASE = '/examples/path-b-demo/';

/* Root directory inside the zip. Single top-level folder is more user-friendly
   than a flat dump. */
const ZIP_ROOT = 'my-frc-learning';

/**
 * Build a "Getting Started" markdown file describing how to use the zip.
 * Generated dynamically so the timestamp + file-list reflect THIS export.
 */
function buildGettingStarted(studentFiles) {
  const now = new Date().toISOString().replace('T', ' ').slice(0, 16) + ' UTC';
  const studentList = studentFiles.length
    ? studentFiles.map(p => `- \`${p}\``).join('\n')
    : '- (no browser edits detected — you got the plain skeleton)';
  return `# Welcome to your FRC project!

You graduated from the browser lessons. This zip is **your project** —
the same code you wrote in the browser, now structured as a real
WPILib + AdvantageKit repository you can open in VS Code.

> Exported ${now}

## What just happened?

Your edits from the browser PoCs have been merged into a freshly-stamped
copy of the \`path-b-demo\` skeleton. Files that you edited in the
browser take precedence over the skeleton's stubs.

Files in this zip that came from **your** browser work:

${studentList}

Everything else in this zip is the WPILib project scaffolding that the
later (Path B) lessons will fill in.

## How to open this in VS Code

1. **Install WPILib** (~2.5 GB, one-time pain — sorry).
   Follow https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html
2. **Unzip** this archive somewhere sensible (your \`Documents\` folder is fine).
3. **Open the unzipped folder in WPILib's VS Code** (not your system VS Code —
   they're separate installs). \`File → Open Folder\` → pick \`my-frc-learning\`.
4. **Wait** while the WPILib extension downloads Gradle and the vendordeps.
   First load takes 2-5 minutes.

## Verify it built

From a terminal inside the project folder:

\`\`\`bash
./gradlew lesson04
\`\`\`

You should see a JUnit run; if Lesson 04 isn't done yet, that's expected
— but Gradle should at least *attempt* the build. If the command itself
fails (e.g. "command not found"), see *Troubleshooting* below.

## What to do next

Continue the curriculum from where you left off. Open
\`lessons/manifest.json\` to see what's available, or just run:

\`\`\`bash
./tools/frcprog.sh next
\`\`\`

## Troubleshooting

- **\`./gradlew: Permission denied\`** → \`chmod +x gradlew tools/frcprog.sh\`
- **\`./gradlew: command not found\`** → run from inside the project folder, not the zip.
- **AdvantageKit download fails** → see \`vendordeps/AdvantageKit.json\` for the canonical URL; re-fetch manually if needed.

Welcome to Path B. Have fun.
`;
}

/**
 * Fetch a single skeleton file. Returns null on 404 so a missing file
 * doesn't tank the whole export — the user can still get something usable.
 */
async function fetchSkeletonFile(base, relPath) {
  try {
    const res = await fetch(base + relPath);
    if (!res.ok) {
      console.warn(`[bridge] skeleton fetch failed (${res.status}): ${relPath}`);
      return null;
    }
    return await res.text();
  } catch (e) {
    console.warn(`[bridge] skeleton fetch error: ${relPath}`, e);
    return null;
  }
}

/**
 * Build a Blob (.zip) containing the path-b-demo skeleton merged with
 * the student's edited files.
 *
 * @param studentFs  the StudentFilesystem instance (window.FS)
 * @param options    { base: skeleton URL base (default '/examples/path-b-demo/') }
 * @returns Promise<Blob>
 */
async function exportProject(studentFs, options = {}) {
  if (typeof JSZip === 'undefined') {
    throw new Error('JSZip is not loaded. Include the JSZip <script> tag before bridge.js.');
  }
  const base = options.base || DEFAULT_SKELETON_BASE;
  const zip  = new JSZip();
  const root = zip.folder(ZIP_ROOT);

  const studentPaths = studentFs ? studentFs.list() : [];
  const studentSet   = new Set(studentPaths);
  const overridden   = [];

  // 1. Walk the skeleton; for each file, prefer the student's edit if present.
  await Promise.all(SKELETON_FILES.map(async (relPath) => {
    if (studentSet.has(relPath)) {
      // Student edited this file in the browser — use their version.
      root.file(relPath, studentFs.read(relPath));
      overridden.push(relPath);
    } else {
      // Fall back to the skeleton's stub.
      const content = await fetchSkeletonFile(base, relPath);
      if (content !== null) root.file(relPath, content);
    }
  }));

  // 2. Add student-only files (paths that don't appear in the skeleton).
  //    This handles future lessons that introduce new files the skeleton
  //    doesn't ship yet.
  for (const p of studentPaths) {
    if (!SKELETON_FILES.includes(p)) {
      root.file(p, studentFs.read(p));
      overridden.push(p);
    }
  }

  // 3. Generated welcome file at the top of the zip.
  root.file('GETTING-STARTED.md', buildGettingStarted(overridden));

  return zip.generateAsync({ type: 'blob', compression: 'DEFLATE' });
}

/**
 * Trigger a browser download of `blob` with the given filename.
 */
function triggerDownload(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a   = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  // Revoke after a short delay so Safari has time to actually start the download.
  setTimeout(() => URL.revokeObjectURL(url), 5000);
}

/**
 * Render a "Download project" button into `targetEl`. Wires the click
 * handler to exportProject(fs, options) and triggers a download.
 *
 * @param targetEl  an existing DOM element or a HTMLButtonElement to attach to
 * @param fs        the student filesystem (defaults to window.FS)
 * @param options   { label, filename, base }
 */
function exportButton(targetEl, fs, options = {}) {
  if (!targetEl) throw new Error('exportButton: targetEl is required');
  const filesystem = fs || window.FS;
  const label      = options.label    || 'Download project (.zip)';
  const filename   = options.filename || `my-frc-learning-${Date.now()}.zip`;

  // If the target IS a button, reuse it; otherwise create one inside.
  let btn;
  if (targetEl.tagName === 'BUTTON') {
    btn = targetEl;
    if (!btn.textContent.trim()) btn.textContent = label;
  } else {
    btn = document.createElement('button');
    btn.type = 'button';
    btn.textContent = label;
    btn.className = 'md-button md-button--primary';
    targetEl.appendChild(btn);
  }

  btn.addEventListener('click', async () => {
    const original = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Packing your project…';
    try {
      const blob = await exportProject(filesystem, { base: options.base });
      triggerDownload(blob, filename);
      btn.textContent = 'Done — check your downloads';
      setTimeout(() => { btn.textContent = original; btn.disabled = false; }, 4000);
    } catch (e) {
      console.error('[bridge] export failed', e);
      btn.textContent = 'Export failed (see console)';
      setTimeout(() => { btn.textContent = original; btn.disabled = false; }, 4000);
    }
  });

  return btn;
}

/* Browser-global exports. (No ES modules — keeps parity with filesystem.js.) */
window.exportProject = exportProject;
window.exportButton  = exportButton;
