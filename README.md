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
vercel.json         build command and output directory
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
| Build command | `pip install -r requirements.txt && mkdocs build` |
| Output directory | `_site` |
| Install command | *(leave empty — the build command installs)* |

Set `site_url` in `mkdocs.yml` to the deployed URL once you have it, so search
and canonical links point at the right place.
