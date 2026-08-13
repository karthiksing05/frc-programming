#!/usr/bin/env bash
# Build the static site.
#
# WHY A VIRTUALENV AND NOT A BARE `pip install`
#
# Hosted build images increasingly ship a Python marked "externally managed"
# (PEP 668) — Vercel's is managed by uv. Installing into it fails with:
#
#     error: externally-managed-environment
#     × This environment is externally managed
#
# A virtualenv is unmanaged by definition, so this works on those images and on
# an ordinary laptop without needing to detect which one it is running on.
# It also avoids --break-system-packages, which does what its name says.
set -euo pipefail

python3 -m venv .venv
.venv/bin/python -m pip install --upgrade pip >/dev/null
.venv/bin/python -m pip install -r requirements.txt

# --strict so a missing snippet include fails the deploy rather than quietly
# publishing a page with an empty code block.
.venv/bin/mkdocs build --strict

echo "Built $(find _site -name '*.html' | wc -l | tr -d ' ') pages into _site/"
