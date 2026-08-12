#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  Prove every rubric is worth something.
#
#  A rubric that passes with the exemplar applied is only half a rubric. If it
#  ALSO passes on the untouched starter, it grades nothing, and a student will
#  sail through the lesson without learning it. This script checks both halves:
#
#      starter  -> the rubric must FAIL
#      exemplar -> the rubric must PASS
#
#  Curriculum authors run this before shipping a lesson, and after any change to
#  a starter file. Students never need it.
#
#  Usage:
#      .meta/verify-rubrics.sh            # every graded lesson
#      .meta/verify-rubrics.sh 05 06      # only these
# ─────────────────────────────────────────────────────────────────────────────
set -uo pipefail

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$PROJECT_DIR"

WORK="${FRCPROG_VERIFY_DIR:-$(mktemp -d)}"
export JAVA_HOME="${JAVA_HOME:-$HOME/wpilib/2026/jdk}"

# Which lessons to check: the graded ones from the manifest, or the CLI args.
if [[ $# -gt 0 ]]; then
  IDS=("$@")
else
  mapfile -t IDS < <(python3 -c "
import json
m = json.load(open('lessons/manifest.json'))
print('\n'.join(l['id'] for l in m['lessons'] if l['graded']))
")
fi

slug_for() {
  python3 -c "
import json,sys
m = json.load(open('lessons/manifest.json'))
print(next(l['dir'] for l in m['lessons'] if l['id'] == sys.argv[1]))
" "$1"
}

echo "Workspace: $WORK"
rm -rf "$WORK/starter" "$WORK/solved"
mkdir -p "$WORK/starter" "$WORK/solved"
rsync -a --exclude build --exclude .gradle --exclude .meta/exemplar ./ "$WORK/starter/"
rsync -a --exclude build --exclude .gradle --exclude .meta/exemplar ./ "$WORK/solved/"

fails=0

for id in "${IDS[@]}"; do
  slug="$(slug_for "$id")"
  printf '\n──────── lesson %s (%s) ────────\n' "$id" "$slug"

  # ── half 1: the starter must fail ──────────────────────────────────────────
  # Restore pristine sources, then run just this lesson's rubric.
  rsync -a --delete src/ "$WORK/starter/src/"
  if (cd "$WORK/starter" && ./gradlew "lesson$id" -q >/dev/null 2>&1); then
    echo "  ✗ PASSES ON THE STARTER — this rubric grades nothing"
    fails=$((fails + 1))
  else
    echo "  ✓ fails on the starter (as it must)"
  fi

  # ── half 2: the exemplar must pass ─────────────────────────────────────────
  rsync -a --delete src/ "$WORK/solved/src/"
  rsync -a ".meta/exemplar/$slug/src/" "$WORK/solved/src/"
  if (cd "$WORK/solved" && ./gradlew "lesson$id" -q >/dev/null 2>&1); then
    echo "  ✓ passes with the exemplar"
  else
    echo "  ✗ FAILS WITH THE EXEMPLAR — the reference answer is wrong"
    (cd "$WORK/solved" && ./gradlew "lesson$id" 2>&1 | grep -E "FAILED|expected|Asked for|allowed|^\s+[A-Za-z].*Error" | head -20)
    fails=$((fails + 1))
  fi
done

printf '\n'
if [[ $fails -eq 0 ]]; then
  echo "✓ all ${#IDS[@]} rubric(s) verified: fail on starter, pass on exemplar"
else
  echo "✗ $fails problem(s) across ${#IDS[@]} lesson(s)"
fi
exit $fails
