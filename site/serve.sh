#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  FRCProgramming.org — local dev server
#
#  This script:
#    1. Creates / activates a Python virtualenv at site/.venv
#    2. Installs (or refreshes) MkDocs + Material + plugins from requirements.txt
#    3. Symlinks ../examples/ into docs/examples so iframe-embedded PoCs
#       served at  /examples/<poc>/index.html  resolve correctly
#    4. Launches `mkdocs serve` bound to 0.0.0.0:8000
#
#  NOTE: process/ is deliberately NOT linked in. Those are the project's own
#  design documents, not student-facing material, and their relative links point
#  outside the docs tree.
#
#  Run from anywhere — the script resolves its own absolute path.
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# Resolve the directory this script lives in (the `site/` directory).
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

VENV_DIR="$SCRIPT_DIR/.venv"
REQ_FILE="$SCRIPT_DIR/requirements.txt"
EXAMPLES_SRC="$SCRIPT_DIR/../examples"
EXAMPLES_LINK="$SCRIPT_DIR/docs/examples"

# ─── 1. venv ────────────────────────────────────────────────────────────────
# Use the project-local venv so we don't pollute the user's system Python.
if [[ ! -d "$VENV_DIR" ]]; then
  echo "[serve.sh] Creating virtualenv at $VENV_DIR"
  python3 -m venv "$VENV_DIR"
fi

# Activate the venv for the remainder of the script.
# shellcheck disable=SC1091
source "$VENV_DIR/bin/activate"

# ─── 2. dependencies ────────────────────────────────────────────────────────
# pip-install is idempotent and fast on warm caches; safe to run every time.
echo "[serve.sh] Installing/refreshing dependencies"
python -m pip install --upgrade pip >/dev/null
python -m pip install -r "$REQ_FILE"

# ─── 3. examples symlink ────────────────────────────────────────────────────
# MkDocs only serves files under docs/. The interactive PoCs live in
# ../examples/ and are intentionally NOT copied into docs/ (so the source of
# truth stays in examples/). A symlink makes the PoCs reachable at
# /examples/<poc>/index.html during `mkdocs serve` without duplicating files.
# Note: we delete and recreate so re-runs don't accumulate stale links.
if [[ -L "$EXAMPLES_LINK" ]]; then
  rm "$EXAMPLES_LINK"
fi
if [[ -e "$EXAMPLES_LINK" && ! -L "$EXAMPLES_LINK" ]]; then
  # Something other than a symlink lives there (e.g. the docs/examples/index.md
  # directory). We don't want to clobber it — but we DO want the symlink to win.
  # If it's a directory that contains only the placeholder index.md, move that
  # file aside and replace with the symlink.
  if [[ -d "$EXAMPLES_LINK" && -f "$EXAMPLES_LINK/index.md" ]]; then
    mkdir -p "$SCRIPT_DIR/docs/.examples-stub"
    mv "$EXAMPLES_LINK/index.md" "$SCRIPT_DIR/docs/.examples-stub/index.md"
    rmdir "$EXAMPLES_LINK"
  else
    echo "[serve.sh] WARN: $EXAMPLES_LINK exists and is not a symlink — leaving alone"
  fi
fi
if [[ ! -e "$EXAMPLES_LINK" ]]; then
  echo "[serve.sh] Symlinking $EXAMPLES_SRC -> $EXAMPLES_LINK"
  ln -s "$EXAMPLES_SRC" "$EXAMPLES_LINK"
fi

# ─── 4. serve ───────────────────────────────────────────────────────────────
# Bind to 0.0.0.0 so the site is reachable from other devices on the LAN
# (handy when previewing on a phone or tablet).
echo "[serve.sh] Starting MkDocs at http://localhost:8000"
exec mkdocs serve --dev-addr=0.0.0.0:8000
