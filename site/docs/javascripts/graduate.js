/* ═══════════════════════════════════════════════════════════
   graduate.js — wires the "Graduate to VS Code" download button
   on /docs/graduate/.

   Loads in order:
     1. JSZip (CDN)
     2. /examples/shared/filesystem.js  (gives us window.FS)
     3. /examples/shared/bridge.js      (gives us exportButton)

   We load these dynamically (rather than via <script src=>) so the
   site's Material-for-MkDocs instant-navigation SPA shell doesn't
   double-load them, and so we can wire the button only AFTER all
   three deps are ready.
   ═══════════════════════════════════════════════════════════ */

(function () {
  "use strict";

  const JSZIP_URL  = 'https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js';
  const FS_URL     = '/examples/shared/filesystem.js';
  const BRIDGE_URL = '/examples/shared/bridge.js';

  function loadScript(src) {
    return new Promise((resolve, reject) => {
      // Already loaded? Resolve immediately.
      if (document.querySelector(`script[data-graduate-src="${src}"]`)) {
        return resolve();
      }
      const s = document.createElement('script');
      s.src = src;
      s.async = false;  // preserve execution order
      s.dataset.graduateSrc = src;
      s.onload  = () => resolve();
      s.onerror = () => reject(new Error('Failed to load ' + src));
      document.head.appendChild(s);
    });
  }

  async function wireButton() {
    const btn = document.getElementById('downloadProject');
    const status = document.getElementById('graduateStatus');
    if (!btn) return;  // not on the graduate page

    // Don't double-wire if Material's SPA re-runs us on the same page.
    if (btn.dataset.wired === 'yes') return;
    btn.dataset.wired = 'yes';

    if (status) status.textContent = 'Loading export tools…';

    try {
      await loadScript(JSZIP_URL);
      await loadScript(FS_URL);
      await loadScript(BRIDGE_URL);
    } catch (e) {
      console.error(e);
      if (status) status.textContent =
        'Could not load the export tools. Check your internet connection and reload.';
      btn.disabled = true;
      return;
    }

    if (!window.FS || !window.exportButton) {
      if (status) status.textContent =
        'Export tools loaded but did not initialise — open the browser console.';
      btn.disabled = true;
      return;
    }

    // Show a small "we found N files in your project" hint.
    const n = window.FS.list().length;
    if (status) {
      if (n === 0) {
        status.innerHTML =
          'No browser-edited files detected. You can still download the plain ' +
          'skeleton, but you will get the most out of this after completing ' +
          'at least Lessons 01–03.';
      } else {
        status.textContent =
          `Found ${n} file${n === 1 ? '' : 's'} from your browser lessons. ` +
          `They will be merged into the project zip.`;
      }
    }

    // exportButton attaches the click handler.
    window.exportButton(btn, window.FS, {
      label: 'Download my project (.zip)',
      filename: 'my-frc-learning.zip',
    });
  }

  // Material for MkDocs uses instant navigation — subscribe to document$
  // so we re-run on every SPA route change. Fall back to DOMContentLoaded
  // if the subscription primitive isn't available.
  if (typeof document$ !== 'undefined' && document$.subscribe) {
    document$.subscribe(() => { wireButton(); });
  } else if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', wireButton);
  } else {
    wireButton();
  }
})();
