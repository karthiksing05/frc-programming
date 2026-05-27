#!/usr/bin/env bash
# frcprog — minimal lesson-runner CLI (the seed of a VS Code extension)
#
# Subcommands:
#   list                  show all lessons + status
#   next                  show next unfinished lesson
#   read <slug>           print a lesson README to the terminal
#   check <slug>          run that lesson's tagged tests
#   sim <slug>            launch ./gradlew simulateJava with this lesson active
#   doctor                check JDK / Gradle / network / WPILib install health

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="$ROOT/lessons/manifest.json"

require_jq() {
    if ! command -v jq >/dev/null 2>&1; then
        echo "frcprog: needs 'jq' installed (brew install jq | apt install jq)" >&2
        exit 1
    fi
}

lesson_field() {
    # $1 = slug, $2 = field name in lesson.json
    jq -r ".${2}" "$ROOT/lessons/$1/lesson.json"
}

cmd_list() {
    require_jq
    printf "%-20s %-8s %s\n" "LESSON" "STAGE" "TITLE"
    jq -r '.lessons[] | "\(.slug)|\(.stage)|\(.title)"' "$MANIFEST" |
    while IFS='|' read -r slug stage title; do
        # crude completeness check: lesson tests pass?
        status="○"   # not started / unknown
        result_xml="$ROOT/build/test-results/test/TEST-frc.robot.*Lesson$(printf '%02d' "${slug%%-*}")*.xml"
        if ls $result_xml 2>/dev/null | head -1 >/dev/null; then
            if grep -q 'failures="0"' $result_xml 2>/dev/null; then
                status="✓"
            else
                status="✗"
            fi
        fi
        printf "%s %-18s %-8s %s\n" "$status" "$slug" "$stage" "$title"
    done
}

cmd_next() {
    require_jq
    # next = first lesson without a passing test result
    jq -r '.lessons[].slug' "$MANIFEST" | while read -r slug; do
        num="${slug%%-*}"
        if ! grep -q 'failures="0"' "$ROOT/build/test-results/test/TEST-frc.robot.*Lesson${num}"*.xml 2>/dev/null; then
            title=$(lesson_field "$slug" "title")
            edits=$(jq -r '.edits | join("\n  ")' "$ROOT/lessons/$slug/lesson.json")
            echo "Next: ${slug} — ${title}"
            echo "  edit:"
            echo "  $edits"
            echo "  read: lessons/$slug/README.md"
            echo "  check: ./tools/frcprog.sh check $slug"
            exit 0
        fi
    done
    echo "🎉 All lessons in manifest are complete."
}

cmd_read() {
    require_jq
    slug="${1:-}"
    [[ -z "$slug" ]] && { echo "usage: frcprog read <slug>"; exit 2; }
    if command -v glow >/dev/null 2>&1; then
        glow "$ROOT/lessons/$slug/README.md"
    else
        cat "$ROOT/lessons/$slug/README.md"
    fi
}

cmd_check() {
    require_jq
    slug="${1:-}"
    [[ -z "$slug" ]] && { echo "usage: frcprog check <slug>"; exit 2; }
    num="${slug%%-*}"
    echo "→ ./gradlew test --tests '*Test' -DincludeTags='lesson-${num}'"
    cd "$ROOT"
    ./gradlew test --tests '*Test' -DincludeTags="lesson-${num}"
}

cmd_sim() {
    slug="${1:-}"
    [[ -z "$slug" ]] && { echo "usage: frcprog sim <slug>"; exit 2; }
    cd "$ROOT"
    FRCPROG_LESSON="$slug" ./gradlew simulateJava
}

cmd_doctor() {
    echo "frcprog doctor — checking your environment"
    echo
    # JDK
    if java -version 2>&1 | grep -q '17\.'; then
        echo "✓ Java 17 found"
    else
        echo "✗ Java 17 not found — install WPILib's bundled JDK"
    fi
    # Gradle wrapper
    if [[ -x "$ROOT/gradlew" ]]; then echo "✓ ./gradlew wrapper is executable"
    else echo "✗ ./gradlew missing or not executable"; fi
    # Network
    if curl -fsSL --head https://frcmaven.wpi.edu >/dev/null 2>&1; then
        echo "✓ frcmaven.wpi.edu reachable"
    else
        echo "✗ frcmaven.wpi.edu unreachable — school firewall? Try ./gradlew --offline"
    fi
    if curl -fsSL --head https://maven.advantagekit.org >/dev/null 2>&1; then
        echo "✓ maven.advantagekit.org reachable"
    else
        echo "✗ maven.advantagekit.org unreachable"
    fi
    # WPILib install (best-effort, varies by OS)
    if [[ -d "$HOME/wpilib/2026" ]] || [[ -d "/Users/Public/wpilib/2026" ]] || [[ -d "/usr/local/wpilib/2026" ]]; then
        echo "✓ WPILib 2026 install found"
    else
        echo "△ no WPILib 2026 install found at usual paths — fine if you use Gradle only"
    fi
    echo
    echo "Run './tools/frcprog.sh next' for what to do next."
}

case "${1:-}" in
    list)   shift; cmd_list   "$@" ;;
    next)   shift; cmd_next   "$@" ;;
    read)   shift; cmd_read   "$@" ;;
    check)  shift; cmd_check  "$@" ;;
    sim)    shift; cmd_sim    "$@" ;;
    doctor) shift; cmd_doctor "$@" ;;
    *) cat <<EOF
usage: frcprog <command> [args]

  list                show all lessons + status
  next                show next unfinished lesson
  read <slug>         print a lesson README
  check <slug>        run that lesson's tagged tests
  sim <slug>          launch simulateJava with this lesson active
  doctor              check your environment

EOF
       exit 2 ;;
esac
