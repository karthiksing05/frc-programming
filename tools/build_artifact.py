#!/usr/bin/env python3
"""Compile the whole MkDocs site into one self-contained data blob.

Produces artifact_data.json:
  {
    "sections": [{title, pages:[{id,title,...}]}],
    "pages":    {id: {title, section, html, toc:[...]}},
    "files":    {path: highlighted_html}      # each source file stored ONCE
  }

Code includes inside the markdown are replaced with a placeholder that the page
fills in from `files` at render time. The same RobotContainer.java is included by
six different lessons; storing it once instead of six times is the difference
between a page that fits in the artifact budget and one that does not.
"""
from __future__ import annotations

import html as html_mod
import json
import pathlib
import re

import markdown
import yaml
from pygments import highlight as pyg_highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import get_lexer_by_name

REPO = pathlib.Path("/Users/karthiksing05/Documents/Metal-Crusaders/FRC-Programming")
DOCS = REPO / "site" / "docs"
OUT = pathlib.Path(__file__).parent / "artifact_data.json"

SNIPPET = re.compile(r'^(?P<indent>[ \t]*)--8<--\s+"(?P<path>[^"]+)"\s*$', re.M)
FENCE_OPEN = re.compile(r'^[ \t]*```')

FILES: dict[str, str] = {}


def resolve_path(rel: str) -> pathlib.Path:
    """Snippets resolve against the repo root first, then docs/ — same as mkdocs.yml."""
    for base in (REPO, DOCS):
        p = base / rel
        if p.exists():
            return p
    raise FileNotFoundError(rel)


def highlight_source(text: str, lang: str) -> str:
    try:
        lexer = get_lexer_by_name(lang, stripall=False)
    except Exception:
        lexer = get_lexer_by_name("text")
    fmt = HtmlFormatter(nowrap=True)
    return pyg_highlight(text, lexer, fmt)


def register_file(rel: str, lang: str) -> str:
    """Store a source file once; return the key the page will look it up by."""
    if rel not in FILES:
        FILES[rel] = highlight_source(resolve_path(rel).read_text(), lang)
    return rel


CODE_INCLUDE = re.compile(
    r'^(?P<indent>[ \t]*)```(?P<lang>[a-zA-Z0-9]*)(?P<meta>[^\n]*)\n'
    r'(?P=indent)--8<--\s+"(?P<path>[^"]+)"\s*\n'
    r'(?P=indent)```[ \t]*$',
    re.M,
)
TITLE_ATTR = re.compile(r'title="([^"]+)"')


def expand(text: str, depth: int = 0) -> str:
    """Resolve --8<-- includes.

    A fenced block whose entire body is one include is a SOURCE FILE. Those are
    replaced with a raw-HTML placeholder and the file is stored once in FILES —
    RobotContainer.java is included by six lessons, and six copies of it is the
    difference between fitting in the artifact budget and not.

    Any other include is prose, and is spliced in so it renders as markdown.
    """
    if depth > 6:
        return text

    def swap_code(m: re.Match) -> str:
        indent = m.group("indent")
        lang = m.group("lang") or "text"
        rel = m.group("path")
        title_m = TITLE_ATTR.search(m.group("meta") or "")
        title = title_m.group(1) if title_m else rel.rsplit("/", 1)[-1]
        register_file(rel, lang)
        # A raw HTML block passes through python-markdown untouched, which a
        # marker inside a fence does not — pygments tokenises it into spans.
        return (
            f'{indent}<div class="srcfile" data-src="{html_mod.escape(rel, quote=True)}" '
            f'data-lang="{lang}" data-title="{html_mod.escape(title, quote=True)}"></div>'
        )

    text = CODE_INCLUDE.sub(swap_code, text)

    out: list[str] = []
    for line in text.split("\n"):
        m = SNIPPET.match(line)
        if not m:
            out.append(line)
            continue
        indent = m.group("indent")
        included = expand(resolve_path(m.group("path")).read_text(), depth + 1)
        for sub in included.split("\n"):
            out.append(indent + sub if sub.strip() else sub)

    return "\n".join(out)


MD_EXTENSIONS = [
    "abbr", "attr_list", "md_in_html", "def_list", "admonition", "tables",
    "footnotes", "toc",
    "pymdownx.details", "pymdownx.superfences", "pymdownx.inlinehilite",
    "pymdownx.tabbed", "pymdownx.highlight",
]
MD_CONFIG = {
    "pymdownx.highlight": {"anchor_linenums": False, "use_pygments": True},
    "pymdownx.tabbed": {"alternate_style": True},
    "toc": {"permalink": False},
}


def strip_material_icons(text: str) -> str:
    """Drop Material's icon shortcodes — they need the theme's icon set.

    The trailing `{ .lg .middle }` sizing block has to go with them. attr_list
    only consumes such a block when it is attached to something; orphaned by the
    removed icon, it would render as literal text in the middle of a heading.
    """
    return re.sub(
        r':(material|octicons|fontawesome)-[a-z0-9-]+:(\s*\{[^{}\n]*\})?', "", text
    )


def slugify(rel: str) -> str:
    s = rel[:-3] if rel.endswith(".md") else rel
    if s.endswith("/index"):
        s = s[: -len("/index")]
    return s or "home"


def lesson_meta() -> dict[str, dict]:
    m = json.loads((REPO / "curriculum" / "lessons" / "manifest.json").read_text())
    return {l["dir"]: l for l in m["lessons"]}


def main() -> None:
    LESSONS = lesson_meta()
    cfg_text = (REPO / "site" / "mkdocs.yml").read_text()
    # The config uses !!python/name: tags for the emoji extension; ignore them.
    cfg_text = re.sub(r'!!python/name:\S+', "null", cfg_text)
    cfg = yaml.safe_load(cfg_text)

    sections: list[dict] = []
    pages: dict[str, dict] = {}
    order: list[str] = []

    def add_page(rel: str, title: str, section: str, group: str | None):
        src = DOCS / rel
        if not src.exists():
            raise FileNotFoundError(f"nav references missing {rel}")
        raw = src.read_text()
        # Drop YAML front matter — the nav supplies the title.
        raw = re.sub(r"^---\n.*?\n---\n", "", raw, count=1, flags=re.S)
        raw = strip_material_icons(expand(raw))

        md = markdown.Markdown(extensions=MD_EXTENSIONS, extension_configs=MD_CONFIG)
        body = md.convert(raw)

        # Walk the real heading tree and keep h2/h3 only: h1 is the page title
        # the header already shows, and h4+ is too fine for a rail.
        flat = []

        def walk_toc(tokens):
            for t in tokens:
                if t["level"] in (2, 3):
                    flat.append({
                        "id": t["id"],
                        "name": re.sub(r"<[^>]+>", "", t["name"]),
                        "level": t["level"],
                    })
                walk_toc(t.get("children", []))

        walk_toc(getattr(md, "toc_tokens", []))

        pid = slugify(rel)
        info = LESSONS.get(pid.rsplit("/", 1)[-1], {})
        pages[pid] = {
            "num": info.get("id"),
            "stage": info.get("stage"),
            "minutes": info.get("estimatedMinutes"),
            "status": (
                "extension" if info.get("track") == "extension"
                else "graded" if info.get("graded")
                else "guided" if info else None
            ),
            "title": title,
            "section": section,
            "group": group,
            "html": body,
            "toc": flat,
            "text": re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", body))[:6000],
        }
        order.append(pid)
        return pid

    def walk(nav_items, section_title, group_title=None, collector=None):
        for item in nav_items:
            # A bare string entry is an unlabelled page — mkdocs takes its title
            # from the document's own first heading. We call it "Overview",
            # which is what it always is in this nav.
            if isinstance(item, str):
                pid = add_page(item, "Overview", section_title, group_title)
                collector.append({"id": pid, "title": "Overview", "group": group_title})
                continue
            for label, value in item.items():
                if isinstance(value, str):
                    pid = add_page(value, label if label else "Overview", section_title, group_title)
                    collector.append({"id": pid, "title": label, "group": group_title})
                else:
                    walk(value, section_title, label, collector)

    for top in cfg["nav"]:
        for label, value in top.items():
            if isinstance(value, str):
                collector = []
                pid = add_page(value, label, label, None)
                collector.append({"id": pid, "title": label, "group": None})
                sections.append({"title": label, "pages": collector})
            else:
                collector = []
                walk(value, label, None, collector)
                sections.append({"title": label, "pages": collector})

    data = {
        "sections": sections,
        "order": order,
        "pages": pages,
        "files": FILES,
        "pygments": HtmlFormatter().get_style_defs(".hl"),
    }
    OUT.write_text(json.dumps(data, separators=(",", ":")))

    total = OUT.stat().st_size
    print(f"pages: {len(pages)}   unique source files: {len(FILES)}")
    print(f"json:  {total/1024/1024:.2f} MB")
    biggest = sorted(((len(v), k) for k, v in FILES.items()), reverse=True)[:5]
    for n, k in biggest:
        print(f"   {n/1024:6.1f} KB  {k}")


if __name__ == "__main__":
    main()
