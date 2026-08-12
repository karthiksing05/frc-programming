#!/usr/bin/env python3
"""Inject the compiled data blob and syntax CSS into the artifact shell.

Run build_artifact.py first; this step is pure assembly.
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
OUT = HERE.parent / "frcprogramming-artifact.html"


def main() -> None:
    shell = (HERE / "artifact_shell.html").read_text()
    syntax = (HERE / "artifact_syntax.css").read_text()
    data = json.loads((HERE / "artifact_data.json").read_text())
    data.pop("pygments", None)

    blob = json.dumps(data, separators=(",", ":"))
    # Escaping "<" stops a literal </script> inside an embedded source file from
    # closing the tag early. < is valid JSON and parses back identically.
    blob = blob.replace("<", "\\u003c")

    page = shell.replace("/*__PYGMENTS__*/", syntax).replace("/*__DATA__*/", blob)
    page = page.replace(
        'placeholder="Search 53 pages…"',
        f'placeholder="Search {len(data["pages"])} pages…"',
    )

    assert "__DATA__" not in page and "__PYGMENTS__" not in page, "placeholder left unfilled"
    OUT.write_text(page)
    print(f"wrote {OUT}  ({OUT.stat().st_size / 1024 / 1024:.2f} MB, {len(data['pages'])} pages)")


if __name__ == "__main__":
    main()
