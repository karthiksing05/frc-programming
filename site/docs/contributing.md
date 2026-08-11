# Contributing

FRCProgramming.org is open-source, lives on GitHub, and survives only because contributors keep showing up. We want your help — whether that's a one-line typo fix or a brand-new handbook page.

The contribution model is borrowed directly from [FRCDesign.org's two-path approach](https://frcdesign.org): a low-friction PR workflow for outside contributors, and direct-access branch work for core maintainers. Both paths produce the same end result; pick whichever fits how often you plan to contribute.

## The two paths

### Public contributor (most people, most of the time)

1. **Float the idea first.** Drop a note in the project Discord's `#website-feedback` channel (link below) describing what you want to change. For typos and obvious fixes, skip this and go straight to step 2.
2. **Fork on GitHub.** Branch off `main` with a descriptive name (`lesson-04-cleanup`, `handbook-pid-page`, `fix-typo-stage1a`).
3. **Edit.** Read [`site/AUTHORING.md`](https://github.com/karthiksing05/FRC-Programming/blob/main/site/AUTHORING.md) before touching a lesson; it covers the template, the admonition vocabulary, and the per-stage tone guide.
4. **Preview locally.** Run `./serve.sh` from the repo root and confirm your changes render without warnings.
5. **Open a PR.** Describe the *why* in the first paragraph, the *what* below. Link the related Lesson-Plan.md entry or issue if there is one.

A maintainer reviews; you iterate; we merge.

### Internal contributor (regular core maintainers)

Same workflow, plus direct push access to feature branches. GitHub Desktop is recommended over the CLI for anyone newer to Git. Branch naming follows the same convention as above. Open a PR against `main` when ready — even internal contributors merge via review, never direct-to-`main`.

If you're contributing regularly (a lesson a week, say), ask for internal status — saves the fork-and-sync overhead.

## Where to find work

- **Open issues** on the [GitHub repo](https://github.com/karthiksing05/FRC-Programming/issues) — the easiest entry. Look for `good-first-issue`.
- **[`process/Lesson-Plan.md`](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Lesson-Plan.md)** — the canonical spec for all 34 lesson blocks. Any lesson whose page is still a stub is fair game; claim it in `#website-feedback` first so two people don't duplicate work.
- **Handbook stubs** — every page under [`site/docs/handbook/`](https://github.com/karthiksing05/FRC-Programming/tree/main/site/docs/handbook) currently carries a "Coming in Phase 2" marker. Pick one and write it. These are the lowest-overhead way to start because they don't have the prereq-graph constraints lessons do.
- **Interactive PoCs** — the three browser widgets in [`examples/`](https://github.com/karthiksing05/FRC-Programming/tree/main/examples) are the model. New PoCs for under-served lessons are always welcome; follow the conventions in `examples/shared/`.
- **Translation** — Spanish, French, and Mandarin are all on the wishlist. Reach out before starting; we need to set up the i18n plugin first.

## Style guide

The full lesson author handbook lives in [`site/AUTHORING.md`](https://github.com/karthiksing05/FRC-Programming/blob/main/site/AUTHORING.md). It covers:

- The seven-section lesson template (and the rule against reordering)
- The Material admonition vocabulary (`!!! tip`, `!!! warning`, etc. — each carries pedagogical signal)
- Per-stage tone (Stage 0 friendly → Stage 1 patient → Stage 2 direct)
- How to cite the two reference robots without paraphrasing comments

For non-lesson pages (this one, About, the handbook): match the existing voice — declarative, concrete, low on adjectives, honest about what doesn't exist yet. Phase 0 status notes belong everywhere they're true.

## Discord

The project Discord is the central community hub — design reviews, lesson feedback, mentor coordination, weekly challenges. *(Link to be added — TBD as the server is stood up; see [Implementation-Plan.md §6 Workstream C](https://github.com/karthiksing05/FRC-Programming/blob/main/process/Implementation-Plan.md) for the rollout plan.)*

## Code of conduct

Be kind. Assume good faith. Disagree with ideas, not people. We follow the [Contributor Covenant](https://www.contributor-covenant.org/) — a formal `CODE_OF_CONDUCT.md` will land in Phase 1 with the explicit reporting flow.
