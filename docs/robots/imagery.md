# Robot imagery — licensing, and how to add real photos

Short version: **this repository contains no third-party photographs, on purpose.**
The mechanism diagrams on the robot tour pages are original drawings. This page
records why, and exactly what to do when you want real photos.

---

## What we checked

| | Presto (6328) | Kelpie (8033) |
|---|---|---|
| Repository licence | **MIT**, explicit `LICENSE` | **NOASSERTION** — GitHub reports "Other" |
| Photographs in the repo | none | none |
| 3D model | `ascope_assets/Robot_Presto/model.glb` (31 MB) + `model_0.glb` (23 MB) | none |

Two things follow from that table.

**Kelpie has no licence grant at all.** The repo carries WPILib's BSD-3 boilerplate,
which covers the WPILib code shipped inside it — not Team 8033's own work.
`process/Reference-Robots.md` §5.1 has flagged this as an open action item from the
start: *email 8033 and get permission in writing.* Until that happens, we link to
their repository and quote short snippets under fair use, and we do not redistribute
their material.

**Neither repo has photos to take even if we wanted to.** Real photographs of these
robots live on Chief Delphi build threads, team websites, and FIRST's media channels.
Each one belongs to whoever pressed the shutter. A permissive licence on a code
repository says nothing about a photograph on a forum, and "it was on the internet"
has never been a licence.

So: no scraped images in this repo.

---

## What we did instead

Original SVG mechanism diagrams, on the two tour pages.

They are arguably the better tool for the job. Lesson 0B asks a student to *name
Presto's mechanisms and understand how a game piece moves through them*; a
photograph of a robot behind a bumper answers neither question, while a labelled
flow does. They also cost nothing to ship — a few kilobytes of vector, legible in
both themes, working offline and inside the single-file artifact.

---

## Adding real photos, properly

### 1. Ask

For Kelpie, this is the email `process/Reference-Robots.md` §5.1 already calls for.
A workable draft:

> Subject: Permission to reference Reefscape (Kelpie) in a free FRC programming curriculum
>
> Hi Team 8033,
>
> I maintain a free, open-source curriculum that teaches FRC programming using
> WPILib and the IO Layer pattern. Your Reefscape repository is one of two robots
> the lessons keep returning to, because the elevator / shoulder / wrist / roller
> split maps unusually cleanly onto how the material is sequenced.
>
> Three questions:
>
> 1. Is the Reefscape code intended to be open source under a standard licence
>    (MIT, Apache 2, BSD)? GitHub currently reports "Other", and the only licence
>    file is WPILib's boilerplate.
> 2. May we quote short snippets in curriculum material that links back to your
>    repository?
> 3. Would you be willing to share one or two photographs of the robot for use on
>    the tour page, with whatever credit line you prefer?
>
> Happy to add attribution in any form you want, and to remove anything on request.
>
> Thanks — and the Highlanders-Training repo is a genuinely good piece of work.

Presto is MIT for code, but a **photograph is not code**. Ask 6328 separately before
using an image of theirs.

### 2. Drop it in

Put the file in `site/docs/assets/robots/`, named for what it shows:

```
site/docs/assets/robots/
├── presto-2024.jpg
└── kelpie-2025.jpg
```

and record the permission in the table further down this page.

Reference it from the tour page:

```markdown
<figure markdown="span">
  ![Presto at the 2024 New England District Championship](../assets/robots/presto-2024.jpg)
  <figcaption>
    Presto, Crescendo 2024. Photo © Team 6328 Mechanical Advantage, used with permission.
  </figcaption>
</figure>
```

Keep them under about 300 KB each — they travel into the single-file artifact, and
that budget is shared with 54 pages of lessons and source.

### 3. Record it

Every image gets a row, so the next maintainer never has to re-derive whether
something is safe to ship:

| File | Subject | Source | Licence / permission | Date |
|---|---|---|---|---|
| *(none yet)* | | | | |

An image with no row here should be deleted, not published.

---

## The 3D models, which you *can* use today

Presto's repository ships `ascope_assets/Robot_Presto/model.glb` under the same MIT
licence as the rest of it. That is a legitimately reusable asset, and it is what
lesson 0B already points students at: download it, drop it into AdvantageScope, and
the 3D field view renders the actual robot.

We do not vendor it here — 54 MB of geometry for two pages is a bad trade, and it
would land in the artifact too. Students fetch it themselves, once, if they want it.
