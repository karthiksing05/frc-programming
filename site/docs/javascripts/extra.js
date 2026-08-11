// ─────────────────────────────────────────────────────────────────────────
// FRCProgramming.org — site-wide JS hook
//
// This file is intentionally tiny for now. Place to grow into:
//   • Analytics (GA / Plausible) — wire up in initAnalytics() below.
//   • A "lesson progress" indicator that reads localStorage from the
//     browser PoCs and decorates the nav with checkmarks.
//   • Theme-sync between the parent MkDocs page and embedded PoC iframes.
//
// Material for MkDocs uses an instant-loading SPA shell, so anything that
// touches the DOM should subscribe to `document$` if it needs to re-run on
// every page navigation (see https://squidfunk.github.io/mkdocs-material/customization/#additional-javascript).
// ─────────────────────────────────────────────────────────────────────────

(function () {
  "use strict";
  // Placeholder marker so we can confirm extra.js is being loaded.
  console.log("[FRCProgramming.org] extra.js loaded");

  // Hook for future analytics — no-op today.
  function initAnalytics() {
    // TODO: insert analytics snippet here once a provider is chosen.
  }

  initAnalytics();
})();
