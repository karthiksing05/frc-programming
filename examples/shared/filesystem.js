/* ═══════════════════════════════════════════════════════════
   filesystem.js — Cross-lesson persistent student filesystem
   ───────────────────────────────────────────────────────────
   Every PoC lesson reads & writes files through this module.
   Backing store is window.localStorage, so a student's work
   persists across lessons (and across browser sessions) as
   long as they stay on the same origin.

   File paths mirror real WPILib project layout:
       src/main/java/frc/robot/...
   This way the same data shape moves cleanly to a real
   project export (zip / GitHub) later, and the student gets
   a head start on understanding the project structure they
   will see in VS Code.

   IMPORTANT: open the PoCs from a single http origin
   (`python -m http.server` from the repo root) so that all
   lessons share one localStorage namespace. file:// origins
   are partitioned per-file in many browsers.
   ═══════════════════════════════════════════════════════════ */

const FS_KEY      = 'frcprogramming.fs.v1';
const LESSONS_KEY = 'frcprogramming.lessons.v1';

class StudentFilesystem {
  constructor() {
    this._load();
  }

  _load() {
    try {
      this.files = JSON.parse(localStorage.getItem(FS_KEY))      ?? {};
    } catch { this.files = {}; }
    try {
      this.lessons = JSON.parse(localStorage.getItem(LESSONS_KEY)) ?? {};
    } catch { this.lessons = {}; }
  }

  _save() {
    localStorage.setItem(FS_KEY,      JSON.stringify(this.files));
    localStorage.setItem(LESSONS_KEY, JSON.stringify(this.lessons));
  }

  /* ── File ops ───────────────────────────────────────── */
  read  (path)            { return this.files[path] ?? null; }
  write (path, contents)  { this.files[path] = contents; this._save(); }
  exists(path)            { return Object.prototype.hasOwnProperty.call(this.files, path); }
  list  ()                { return Object.keys(this.files).sort(); }

  /* ── Lesson tracking ────────────────────────────────── */
  /** Mark a lesson complete (records timestamp). */
  markComplete(slug) {
    this.lessons[slug] = { completedAt: Date.now() };
    this._save();
  }
  isComplete(slug) { return slug in this.lessons; }

  /* ── Maintenance ────────────────────────────────────── */
  clear() {
    this.files = {};
    this.lessons = {};
    localStorage.removeItem(FS_KEY);
    localStorage.removeItem(LESSONS_KEY);
  }
}

/* Singleton: every lesson on this origin sees the same FS. */
window.FS = window.FS || new StudentFilesystem();

/* ═══════════════════════════════════════════════════════════
   Project-tree renderer — drop a <div id="fsTree"></div> in
   your lesson and call renderFilesystemTree(...) to populate.

   Each lesson declares its "manifest": which file is being
   edited now, which files come from earlier lessons (read-
   only), and which files are coming up in future lessons
   (locked preview).
   ═══════════════════════════════════════════════════════════ */

function renderFilesystemTree(containerEl, manifest) {
  // manifest = {
  //   editing:  ['src/main/java/.../Foo.java'],
  //   prior:    ['src/main/java/.../Bar.java'],  // shown ✓ if FS has it
  //   upcoming: [{ path: '...', label: 'Tank Drive lesson' }],
  // }
  containerEl.innerHTML = '';

  const root = document.createElement('div');
  root.className = 'fs-tree';

  // Group all paths by their directory for tree display
  const all = [
    ...manifest.editing.map (p => ({ path: p, kind: 'editing' })),
    ...manifest.prior.map   (p => ({ path: p, kind: window.FS.exists(p) ? 'done' : 'missing' })),
    ...manifest.upcoming.map(u => ({ path: u.path, kind: 'locked', label: u.label })),
  ];

  // Build a nested map
  const tree = {};
  for (const item of all) {
    const parts = item.path.split('/');
    let node = tree;
    for (let i = 0; i < parts.length - 1; i++) {
      node[parts[i]] = node[parts[i]] || { __dir: true, __children: {} };
      node = node[parts[i]].__children;
    }
    node[parts[parts.length - 1]] = { __file: true, ...item };
  }

  const renderNode = (node, depth) => {
    const wrap = document.createElement('div');
    const names = Object.keys(node).sort((a, b) => {
      const aIsDir = node[a].__dir, bIsDir = node[b].__dir;
      if (aIsDir !== bIsDir) return aIsDir ? -1 : 1;
      return a.localeCompare(b);
    });
    for (const name of names) {
      const entry = node[name];
      const row = document.createElement('div');
      row.className = 'fs-row ' + (entry.__dir ? 'fs-dir' : `fs-file fs-${entry.kind}`);
      row.style.paddingLeft = (depth * 14 + 8) + 'px';

      const icon = document.createElement('span');
      icon.className = 'fs-icon';
      if (entry.__dir) icon.textContent = '▾';
      else if (entry.kind === 'editing') icon.textContent = '✎';
      else if (entry.kind === 'done')    icon.textContent = '✓';
      else if (entry.kind === 'missing') icon.textContent = '!';
      else if (entry.kind === 'locked')  icon.textContent = '🔒';
      row.appendChild(icon);

      const label = document.createElement('span');
      label.className = 'fs-name';
      label.textContent = name;
      row.appendChild(label);

      if (!entry.__dir) {
        const tag = document.createElement('span');
        tag.className = 'fs-tag fs-tag-' + entry.kind;
        tag.textContent =
          entry.kind === 'editing' ? 'editing'
          : entry.kind === 'done'    ? 'from previous lesson'
          : entry.kind === 'missing' ? 'do prior lesson!'
          : entry.label || 'next lesson';
        row.appendChild(tag);
      }
      wrap.appendChild(row);

      if (entry.__dir) {
        wrap.appendChild(renderNode(entry.__children, depth + 1));
      }
    }
    return wrap;
  };

  root.appendChild(renderNode(tree, 0));
  containerEl.appendChild(root);
}

window.renderFilesystemTree = renderFilesystemTree;
