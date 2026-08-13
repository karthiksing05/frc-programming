# FRCProgramming — website

The MkDocs site, laid out for a static host. **This branch is generated** by
`tools/publish-branches.sh` on the `dev` branch; edits made here are overwritten
on the next publish.

## Layout

```
mkdocs.yml          site config (snippets resolve against this directory)
docs/               the pages
docs/examples/      the browser playgrounds, served at /examples/
curriculum/         lesson text and robot source, included by the lesson pages
requirements.txt    mkdocs + material + pymdown-extensions
build.sh            creates a virtualenv, installs, runs mkdocs build --strict
vercel.json         points the host at build.sh
```

`curriculum/` is here because every lesson page **includes** the canonical lesson
text and the real Java source rather than copying it. That is what stops the site
and the project a student edits from drifting apart, and it means the site cannot
build without those files.

## Build it locally

```bash
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
mkdocs serve          # http://localhost:8000
mkdocs build --strict # -> _site/
```

`--strict` turns a missing include into a build failure, which is what you want:
a moved source file should break the build rather than silently produce an empty
code block.

## Deploy

Any static host works. `vercel.json` is pre-configured:

| Setting | Value |
|---|---|
| Build command | `bash build.sh` |
| Output directory | `_site` |
| Install command | `true` (a no-op — `build.sh` installs into a virtualenv) |

`build.sh` builds inside a virtualenv on purpose. Vercel's build image ships a
Python that is "externally managed" (PEP 668), so a bare
`pip install -r requirements.txt` fails with `externally-managed-environment`.
A virtualenv is unmanaged, so the same script works on the host and on a laptop.

Set `site_url` in `mkdocs.yml` to the deployed URL once you have it, so search
and canonical links point at the right place.
