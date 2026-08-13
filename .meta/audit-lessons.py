#!/usr/bin/env python3
"""Static audit of all 34 lessons — the checks Gradle cannot make.

`checkLessons` validates structure and `verify-rubrics.sh` validates that each
graded rubric actually grades. This covers the gaps between them:

  1. Every graded lesson's `edits` file really carries that lesson's TODO marker,
     so `frcprog next` sends the student to a file with something to do in it.
  2. Reference answers in hints.md agree with the generated exemplar. A hint that
     hands over code the rubric then rejects is worse than no hint.
  3. Every Java identifier the lessons name in prose actually exists in the
     project or in WPILib — catches a lesson that talks about a method somebody
     later renamed.
  4. The prerequisite graph is acyclic and complete, and stage order is sane.
  5. Every `frcprog` command named in prose is one the CLI implements.

Exit code is the number of problems found.
"""
from __future__ import annotations

import json
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
LESSONS = ROOT / "lessons"

problems: list[str] = []
checks = 0


def fail(msg: str) -> None:
    problems.append(msg)


def ok(n: int = 1) -> None:
    global checks
    checks += n


manifest = json.loads((LESSONS / "manifest.json").read_text())["lessons"]
by_dir = {l["dir"]: l for l in manifest}


def lesson_json(slug: str) -> dict:
    return json.loads((LESSONS / slug / "lesson.json").read_text())


# ── 1. TODO markers ─────────────────────────────────────────────────────────
print("1. TODO markers in the files each lesson says you edit")
for entry in manifest:
    if not entry["graded"]:
        continue
    meta = lesson_json(entry["dir"])
    marker = f"TODO (LESSON {entry['id']})"
    found_in = []
    for rel in meta.get("edits", []):
        path = ROOT / rel
        if not path.exists():
            fail(f"lesson {entry['id']}: edits names a missing file {rel}")
            continue
        if marker in path.read_text():
            found_in.append(rel)
    if not found_in and not meta.get("integration"):
        fail(
            f"lesson {entry['id']}: none of its `edits` files contain '{marker}' — "
            f"a student opening them would find nothing to do. If this lesson "
            f"deliberately has no new TODO (a capstone), set \"integration\": true "
            f"in its lesson.json."
        )
    ok()
print(f"   {len(by_dir)} lessons scanned")


# ── 2. hints vs exemplar ────────────────────────────────────────────────────
# The reference answer in hints.md must be code the exemplar actually contains.
# Compared on non-whitespace characters, so formatting differences are ignored
# but a genuinely different answer is caught.
print("2. Reference answers in hints.md match the generated exemplar")


def squash(s: str) -> str:
    """Reduce Java to the characters that change what it does.

    Strips comments and all whitespace, so a hint that presents the same logic
    with different indentation or without the surrounding commentary still
    matches the exemplar. A genuinely different answer still does not.
    """
    s = re.sub(r"//[^\n]*", "", s)
    s = re.sub(r"/\*.*?\*/", "", s, flags=re.S)
    return re.sub(r"\s+", "", s)


CODE_BLOCK = re.compile(r"```java\n(.*?)```", re.S)

for entry in manifest:
    if not entry["graded"]:
        continue
    slug = entry["dir"]
    hints = (LESSONS / slug / "hints.md").read_text()
    details = hints.split("<details>", 1)
    if len(details) < 2:
        fail(f"lesson {entry['id']}: hints.md has no <details> reference answer")
        continue

    exemplar_dir = ROOT / ".meta" / "exemplar" / slug
    if not exemplar_dir.exists():
        fail(f"lesson {entry['id']}: no exemplar directory")
        continue
    exemplar_text = squash(
        "\n".join(p.read_text() for p in sorted(exemplar_dir.rglob("*.java")))
    )

    blocks = CODE_BLOCK.findall(details[1])
    if not blocks:
        if not lesson_json(slug).get("integration"):
            fail(f"lesson {entry['id']}: reference answer contains no java code block")
        continue

    # At least one block in the reveal must appear verbatim in the exemplar.
    # (Others are deliberately-wrong "here is the bug" examples.)
    matched = False
    for b in blocks:
        body = squash(b)
        if len(body) > 40 and body in exemplar_text:
            matched = True
            break
    if not matched:
        longest = max(blocks, key=len).strip().splitlines()[0][:60]
        fail(
            f"lesson {entry['id']}: no code block in the reference answer appears in the "
            f"exemplar (first line of longest block: {longest!r}) — the hint and the "
            f"answer have drifted apart"
        )
    ok()
print(f"   {sum(1 for e in manifest if e['graded'])} graded lessons checked")


# ── 3. Identifiers named in prose exist ─────────────────────────────────────
print("3. Java identifiers named in lesson prose exist somewhere real")
project_text = "\n".join(
    p.read_text() for p in (ROOT / "src").rglob("*.java")
)

wpilib_classes: set[str] = set()
jars = list(pathlib.Path.home().glob("wpilib/2026/maven/edu/wpi/first/**/*.jar"))
for jar in jars:
    if "sources" in jar.name or "javadoc" in jar.name:
        continue
    try:
        out = subprocess.run(
            ["unzip", "-l", str(jar)], capture_output=True, text=True, timeout=30
        ).stdout
    except Exception:
        continue
    for m in re.finditer(r"([A-Za-z0-9_$/]+)\.class", out):
        wpilib_classes.add(m.group(1).rsplit("/", 1)[-1].split("$")[0])

# Class-shaped names the lessons reference in backticks.
CLASSNAME = re.compile(r"`([A-Z][A-Za-z0-9]{3,})`")
# Names that are correctly absent from this project:
#   · JDK classes — indexed from WPILib jars only, so these look missing
#   · vendor classes — the extension lessons' libraries are not installed
#   · files the student is asked to CREATE in a guided lesson
#   · files that live in Kelpie's or Presto's repositories, not ours
JDK_CLASSES = {
    "BooleanSupplier", "DoubleSupplier", "Supplier", "Runnable", "Optional",
    "ArrayList", "List", "Math", "String", "Override", "Deprecated",
}
VENDOR_CLASSES = {
    "LoggedRobot", "WPILOGWriter", "WPILOGReader", "NT4Publisher", "Logger",
    "LogFileUtil", "DriveIOInputsAutoLogged", "PhotonPoseEstimator",
    "PhotonUtils", "VisionSystemSim", "PhotonCamera", "SimulatedArena",
    "CrescendoNoteOnField", "FollowPathCommand",
}
STUDENT_CREATES = {
    "Superstructure", "ClimberStateMachine", "ModuleIO", "ModuleIOSim",
    "ModuleIOReal", "ModuleIOMapleSim", "GyroIO", "GyroIOSim", "VisionIO",
    "SwerveSubsystem", "Module",
}
REFERENCE_ROBOT_FILES = {
    "ElevatorIOReal", "ElevatorIOSim", "ElevatorIO", "ElevatorSubsystem",
    "FlywheelsIO", "FlywheelsIOSim", "FlywheelsIOKrakenFOC", "FlywheelsIOSparkFlex",
    "Flywheels", "RollerSubsystem",
}
# Naming schemes a lesson names in order to REJECT them.
COUNTEREXAMPLES = {"ElevatorBindings", "ShooterBindings", "IntakeNoteCommand"}
# Names used in a hypothetical: "suppose next season you add a JAMMED state".
HYPOTHETICALS = {"JAMMED", "EXTENDING", "HOOKED", "RETRACTING", "CLIMBED", "INTAKING", "EJECTING"}

KNOWN_PROSE = {
    # Words that look like class names but are plain English or our own labels.
    "TODO", "README", "AdvantageScope", "AdvantageKit", "PathPlanner", "Choreo",
    "PhotonVision", "NetworkTables", "WPILib", "WPILOG", "Gradle", "GradleRIO",
    "JUnit", "Java", "Reefscape", "Crescendo", "Kelpie", "Presto", "OFFLINE",
    "EXTENSIONS", "MENTOR", "GUIDE", "Stage", "Lesson", "Overview", "IOReal",
    "IOSim", "IOKrakenFOC", "IOMapleSim", "Bindings", "AutoLog", "AutoLogOutput",
    "Windows", "OneDrive", "Dropbox", "MkDocs", "Python", "Xbox", "AprilTag",
    "AprilTags", "Maven", "GitHub", "Kraken", "SparkFlex", "SparkMax", "TalonFX",
    "Pigeon2", "Real", "Sim", "Sequential", "Sometimes",
}
unknown: dict[str, list[str]] = {}
for entry in manifest:
    slug = entry["dir"]
    text = (LESSONS / slug / "README.md").read_text() + (LESSONS / slug / "hints.md").read_text()
    for name in set(CLASSNAME.findall(text)):
        if (name in KNOWN_PROSE or name in wpilib_classes or name in JDK_CLASSES
                or name in VENDOR_CLASSES or name in STUDENT_CREATES
                or name in REFERENCE_ROBOT_FILES or name in COUNTEREXAMPLES or name in HYPOTHETICALS):
            continue
        if name in project_text:
            continue
        unknown.setdefault(name, []).append(entry["id"])
    ok()
for name, ids in sorted(unknown.items()):
    fail(f"identifier `{name}` (lessons {', '.join(ids)}) is not in the project or WPILib")
print(f"   {len(wpilib_classes)} WPILib classes indexed from {len(jars)} jars")


# ── 4. Prerequisite graph ───────────────────────────────────────────────────
print("4. Prerequisite graph is complete and acyclic")
slugs = {e["dir"] for e in manifest}
order = {e["dir"]: i for i, e in enumerate(manifest)}
for entry in manifest:
    meta = lesson_json(entry["dir"])
    for pre in meta.get("prerequisites", []):
        if pre not in slugs:
            fail(f"lesson {entry['id']}: prerequisite '{pre}' does not exist")
        elif order[pre] >= order[entry["dir"]]:
            fail(
                f"lesson {entry['id']}: prerequisite '{pre}' comes later in the "
                f"manifest — that is a cycle or a mis-ordering"
            )
    ok()
print(f"   {len(manifest)} lessons ordered")


# ── 5. frcprog commands named in prose exist ────────────────────────────────
print("5. Every frcprog command mentioned in prose is implemented")
cli = (ROOT / "tools" / "Frcprog.java").read_text()
implemented = set(re.findall(r'case "([a-z-]+)"', cli))
implemented |= {"--all", "--online"}
mentioned: dict[str, list[str]] = {}
for entry in manifest:
    slug = entry["dir"]
    text = (LESSONS / slug / "README.md").read_text() + (LESSONS / slug / "hints.md").read_text()
    # Require the backticked/CLI form so English like "the frcprog command
    # line" is not mistaken for an invocation of a subcommand named `command`.
    for cmd in set(re.findall(r"(?:\./)?tools/frcprog(?:\.sh|\.cmd)?\s+([a-z-]+)", text)
                   + re.findall(r"`frcprog\s+([a-z-]+)", text)):
        if cmd not in implemented:
            mentioned.setdefault(cmd, []).append(entry["id"])
    ok()
for cmd, ids in sorted(mentioned.items()):
    fail(f"lessons {', '.join(ids)} tell the student to run `frcprog {cmd}`, which does not exist")
print(f"   CLI implements: {' '.join(sorted(c for c in implemented if not c.startswith('-')))}")


# ── report ──────────────────────────────────────────────────────────────────
print()
if problems:
    for p in problems:
        print(f"  ✗ {p}")
    print(f"\n✗ {len(problems)} problem(s) across {checks} checks")
else:
    print(f"✓ all {checks} checks passed")
sys.exit(len(problems))
