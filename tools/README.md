# Artifact builder

Compiles the whole MkDocs site into **one self-contained HTML file** — every page,
every lesson, every source file and rubric, with client-side navigation and search.
Useful for handing the curriculum to someone as a single file, or publishing it as a
Claude artifact.

```bash
cd site && source .venv/bin/activate
python3 tools/build_artifact.py          # -> tools/artifact_data.json
python3 tools/assemble_artifact.py       # -> frcprogramming-artifact.html
```

## How it works

`build_artifact.py` walks `mkdocs.yml`'s nav, resolves every `--8<--` include, and
renders each page's markdown to HTML.

The one non-obvious part is how source files are handled. A fenced block whose entire
body is an include is a *source file*, and it is replaced with a placeholder div while
the file itself is stored once in a shared map. `RobotContainer.java` is included by
six different lessons; six copies of it is the difference between fitting in the
artifact size budget and not. The page fills the placeholders at render time.

Lesson metadata (number, stage, minutes, graded / guided / extension) is pulled from
`curriculum/lessons/manifest.json` so the navigation shows real status rather than an
invented hierarchy.

## Files

| File | What it is |
|---|---|
| `build_artifact.py` | markdown → data blob compiler |
| `assemble_artifact.py` | injects the blob and the syntax CSS into the shell |
| `artifact_shell.html` | the page itself — CSS, markup, and the router/search JS |
| `artifact_syntax.css` | Pygments token colours, as theme-aware CSS variables |

## Keeping it honest

The compiler fails loudly on a missing include, so a moved file breaks the build
rather than producing a silently empty page — the same guarantee `mkdocs build
--strict` gives the real site.
