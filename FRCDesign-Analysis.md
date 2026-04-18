# FRCDesign.org — Deep Analysis for Replication

> **Purpose:** Understand FRCDesign.org's architecture, content strategy, pedagogy, and community structure so that a similar site can be built for FRC **programming**.

---

## 1. Technology Stack

| Layer | Technology | Notes |
|---|---|---|
| **Static Site Generator** | [MkDocs](https://www.mkdocs.org/) (Python) | Markdown-based, single `mkdocs.yml` config drives the entire site |
| **Theme** | [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | Paid "Insiders" edition used for some features (social cards, meta plugin) |
| **Content Language** | Markdown (61.6% of repo) + some HTML snippets | All lesson/reference content is plain `.md` files in `docs/` |
| **Hosting** | GitHub Pages via `ghp-import` | Built & deployed through CI or manual `./run.sh` |
| **Image Lightbox** | `mkdocs-glightbox` | Click-to-enlarge images with captions |
| **Social Cards** | Material social cards plugin | Auto-generates OG/Twitter card images per page |
| **Git Dates** | `mkdocs-git-revision-date-localized-plugin` | Shows "Created" and "Last updated" timestamps from git history |
| **Minification** | `mkdocs-minify-plugin` | HTML minification for production builds |
| **Analytics** | Google Analytics (G-KX3MNPJ4L6) | Basic usage tracking |
| **Custom CSS/JS** | `stylesheets/extra.css`, `javascripts/extra.js` | Styling overrides and custom slideshow behavior |
| **Dev Container** | `.devcontainer/` with Dockerfile | Codespaces-ready for contributors |

### Key Python Dependencies (`requirements.txt`)
```
mkdocs==1.6.1
mkdocs-material (latest)
mkdocs-material-extensions==1.3.1
mkdocs-glightbox==0.4.0
mkdocs-minify-plugin==0.8.0
mkdocs-git-revision-date-localized-plugin==1.4.5
pymdown-extensions==10.1
pillow==11.2.1
CairoSVG==2.7.1
```

### Local Development
```bash
python -m venv venv
./installdependencies.sh   # pip installs from requirements.txt
./runlocal.sh              # serves at http://127.0.0.1:8000
```

---

## 2. Repository Structure

```
FRCDesign.org/
├── docs/                          # ALL website content lives here
│   ├── index.md                   # Homepage
│   ├── website-feature-guide.md   # How to use the site
│   ├── learning-course/           # The core curriculum (stages)
│   │   ├── index.md
│   │   ├── course-setup/          # Onboarding (account, tools)
│   │   ├── stage1/                # Fundamentals
│   │   │   ├── 1A/                # Onshape Fundamentals
│   │   │   ├── 1B/                # Power Transmissions
│   │   │   ├── 1C/                # Practice Mechanisms
│   │   │   ├── 1D/                # Design Methodology
│   │   │   └── 1E/                # Subsystem Workflow
│   │   ├── stage2/                # Mechanism Design
│   │   │   ├── 2A/                # Basic Shooter
│   │   │   ├── 2B/                # Dead Axle Pivot
│   │   │   ├── 2C/                # Slapdown Intake
│   │   │   └── 2D/                # Cascade Elevator
│   │   ├── stage3/                # Full robot integration (WIP)
│   │   └── stage4.md              # Mastery (WIP)
│   ├── educators-guide/           # Parallel guide for mentors/leads
│   │   ├── introduction/
│   │   ├── stage0/
│   │   └── stage1/
│   ├── design-handbook/           # Reference wiki (not a course)
│   │   ├── structure/
│   │   ├── power-transmission/
│   │   ├── mechanisms/
│   │   └── design-writeups/
│   ├── mechanism-examples/        # Curated real-robot breakdowns
│   │   ├── drivebase/
│   │   ├── intake/
│   │   ├── shooter/
│   │   ├── elevator/
│   │   ├── pivots/
│   │   └── turret/
│   ├── best-practices/            # CAD conventions reference
│   ├── resources/                 # Glossary, tools, challenges
│   │   ├── glossary.md
│   │   ├── featurescripts.md
│   │   └── design-challenges/     # Weekly competitive challenges
│   └── contribution/              # How to contribute
│       ├── methodsOfContributing.md
│       ├── styleguide.md
│       └── contributors.md
├── includes/                      # Shared snippets (abbreviations.md)
├── layouts/                       # Custom social card layouts
├── overrides/                     # Material theme overrides
├── mkdocs.yml                     # Master configuration (nav, plugins, theme)
├── requirements.txt
├── runlocal.sh / run.sh / setup.sh
└── .devcontainer/                 # Docker dev environment
```

---

## 3. Navigation & Information Architecture

### Top-Level Tabs (sticky navigation bar)
The site uses **Material's `navigation.tabs`** feature with sticky tabs:

1. **Home** — Landing page with card grid linking to major sections
2. **Learning Course** — The structured curriculum (the main product)
3. **Educator's Guide** — Parallel companion for mentors/leads
4. **Design Handbook** — Reference wiki organized by topic
5. **Mechanism Examples** — Curated case studies
6. **Best Practices** — CAD conventions
7. **Other Resources** — Tools, glossary, design challenges
8. **Changelog** — Site update log
9. **Contribution** — How to help

### Key Navigation Features (from `mkdocs.yml`)
```yaml
features:
  - navigation.sections      # Group items under section headers
  - navigation.indexes       # Section index pages (index.md)
  - navigation.tabs          # Top-level tabs
  - navigation.tabs.sticky   # Tabs stay visible on scroll
  - navigation.instant        # SPA-like instant page loads
  - navigation.instant.prefetch  # Prefetch links on hover
  - navigation.instant.progress  # Progress bar during loads
  - navigation.footer        # Previous/Next buttons at bottom
  - navigation.prune         # Remove non-visible nav items from DOM
  - navigation.top           # "Back to top" button
  - navigation.path          # Breadcrumbs
  - toc.follow               # TOC follows scroll position
  - search.suggest           # Search suggestions
  - search.highlight         # Highlight search terms
  - content.tabs.link        # Sync content tabs across page
  - content.code.copy        # Copy button on code blocks
  - content.tooltips         # Hover tooltips
  - content.action.edit      # "Edit this page" link → GitHub
  - content.action.view      # "View source" link → GitHub
  - header.autohide          # Header hides on scroll down
```

---

## 4. Theme & Visual Design

### Color Scheme
- **Primary color:** Green
- **Default mode:** Dark (slate scheme) — inverted from typical; "light mode" uses `scheme: default`
- **Accent:** White (dark mode), Black (light mode)
- **Links:** Green text

### Branding
- **Logo:** `fontawesome/solid/book-open`
- **Favicon:** Custom `img/favicon.png`
- **Footer:** Sponsor banner (WCP/Fabworks), "Made with Material for MkDocs", Discord link

### Custom Admonition Icons
| Type | Icon |
|---|---|
| Note | `fontawesome/solid/note-sticky` |
| Abstract | `fontawesome/solid/book` |
| Tip | `fontawesome/solid/fire` |
| Warning | `fontawesome/solid/triangle-exclamation` |
| Example | `fontawesome/solid/flask` |
| Bug | `fontawesome/solid/robot` |
| Quote | `fontawesome/solid/quote-left` |

### Custom CSS & JS
- `stylesheets/extra.css` — Custom styling overrides
- `javascripts/extra.js` — Slideshow functionality and other interactive elements

---

## 5. Content Types & Markdown Features

### Markdown Extensions Used
```yaml
markdown_extensions:
  - abbr                    # Abbreviations (glossary tooltips)
  - attr_list               # Add HTML attributes to elements
  - md_in_html              # Markdown inside HTML blocks
  - def_list                # Definition lists
  - admonition              # Call-out boxes (tip, warning, note, etc.)
  - pymdownx.details        # Collapsible admonitions (dropdowns)
  - pymdownx.superfences    # Enhanced code blocks
  - pymdownx.snippets       # Include files (auto-appends abbreviations.md)
  - pymdownx.tabbed         # Content tabs
  - pymdownx.emoji          # Emoji support with twemoji
```

### Content Element Patterns

#### 1. Admonitions (Call-out Boxes)
Used extensively for pedagogical signaling:
```markdown
!!! tip
    Quick tips are shown in a "Tip" box.

!!! warning
    Pay attention to anything in a "Warning" box.

!!! note
    Extra notes and context.

!!! example
    Examples for concepts.
```

#### 2. Collapsible Dropdowns
Hidden content for optional deeper dives:
```markdown
??? info "Why Self-Paced?"
    Different people will have different starting points...
```

#### 3. Slideshows
Custom HTML/JS-powered image slideshows for step-by-step CAD instructions. Navigate with arrows. This is a key teaching mechanism — students follow along with visual steps.

#### 4. Embedded Videos
- Short clips: `.webm` files embedded directly
- Longer videos: YouTube embeds
- Note: Some users need to disable adblockers for video loading

#### 5. Buttons (for external resources)
Large centered buttons link to Onshape documents:
```markdown
<center markdown>
[Document Name](onshape_url){:target="_blank" .md-button .md-button--primary}
</center>
```

#### 6. Glossary / Abbreviations
- A shared `includes/abbreviations.md` file defines terms
- Auto-appended to every page via `pymdownx.snippets`
- Glossary terms appear with **underlines and hover tooltips** throughout all content
- Dedicated glossary page at `resources/glossary.md`

#### 7. Images
- Format: `.webp` (compressed via [Squoosh](https://squoosh.app/))
- Lightbox: Click to enlarge, `Esc` to close (via `mkdocs-glightbox`)
- Centered with captions: `<center><img src="..." width="x%"></center>`

---

## 6. Pedagogy: How the Learning Course Works

### Core Philosophy
1. **Self-paced learning** — Students progress at their own speed
2. **Competitive focus** — Everything taught from the perspective of building winning robots
3. **Depth over breadth** — Teach underlying fundamentals, not just "how to do X"
4. **Single tool focus** — Only teaches Onshape (no SolidWorks/Fusion split)
5. **Practical small details** — Include the experience-gap knowledge that most guides forget
6. **Decreasing guidance** — Exercises start highly guided, progressively become more independent

### Stage Progression Model

```
Course Setup → Stage 1 → Stage 2 → Stage 3 → Stage 4
  (onboard)    (fundamentals)  (mechanisms)  (integration)  (mastery)
```

#### Course Setup
Three entry points depending on background:
- **New to CAD** — Brief intro to what CAD is
- **New to Onshape** — Account setup, performance tuning, UI tour
- **Required Course Tools** — Part library & featurescripts (everyone does this)

#### Stage 1: Fundamentals (5 sub-stages, ~20+ hours)
| Sub-stage | Topic | Format |
|---|---|---|
| **1A** | Onshape Fundamentals | 3 sections × ~6 exercises each. Video tutorials + reference images + "mess around" time |
| **1B** | Power Transmissions | Concept pages (motors, shafts, gears, belts, chains) + 3 exercises |
| **1C** | Practice Mechanisms | 8 exercises with decreasing guidance. Shooters, intakes, indexers |
| **1D** | Design Methodology | Top-down design, layout sketches, origin placement. Full swerve drivebase project |
| **1E** | Subsystem Workflow | Battery, electronics, bellypan, bumpers. Detailing the 1D drivebase |

#### Stage 2: Mechanism Design (4 sub-stages)
Each sub-stage follows an **identical structure**:
1. **Introduction** — What you'll learn
2. **Project Overview** — What you'll build
3. **Engineering Concepts** — 3-6 concept pages teaching the theory
4. **Layout Sketch** — Design the mechanism top-down
5. **Part Studio** — Model the parts
6. **Assembly** — Assemble everything
7. **Summary** — Review what was learned

| Sub-stage | Mechanism | Key Concepts |
|---|---|---|
| **2A** | Basic Shooter | Structure rigidity, ball trajectory, exit velocity, compression, spin control, friction |
| **2B** | Dead Axle Pivot | Strength, friction, power transmission, tensioning, backlash |
| **2C** | Slapdown Intake | Intake golden rules, robustness, pivot design, rollers, zombie axles |
| **2D** | Cascade Elevator | Elevator blocks, chain attachment, rigging, cable management, gearbox |

#### Stage 3: Integration (Under Construction)
- Full robot layout sketches
- Replicate simple robots
- Practice top-down workflow

#### Stage 4: Mastery (Under Construction)
- Master mechanism design
- Study game history
- Learn strategic design
- Make more robots, get review

### Exercise Page Pattern
A typical exercise page contains:
1. **Learning objectives** — Numbered list of what you'll learn
2. **Concept introduction** — Brief text + images explaining the concept
3. **Video tutorial / Slideshow** — Step-by-step visual walkthrough
4. **Reference images** — Screenshots of expected results
5. **Detailed tool info** — Expandable sections about specific CAD tools
6. **"Done?" prompt** — Encouragement to experiment, then move on
7. **Navigation buttons** — Previous/Next at bottom

### Key: There Are No Formal "Homework Assignments"
The exercises **are** the homework. The site is designed for self-paced use where:
- Students work through exercises sequentially
- Each exercise builds on the previous one
- Solutions/reference files are provided (Onshape documents linked via buttons)
- Educators review student work for quality (fully constrained sketches, proper organization, etc.)
- No automated grading — assessment is via manual review against criteria

---

## 7. Educator's Guide: Parallel Companion

The Educator's Guide mirrors the learning course structure but targets mentors/leads:

### Structure per Stage
Each stage in the educator's guide provides:
1. **Overview** — What the stage covers
2. **Learning Objectives** — Bullet list of expected outcomes
3. **Teaching Structure** — Numbered steps for how to run the stage
4. **Time Estimates** — Expected completion times (e.g., "3-5 hours for 1A")
5. **Teaching Tips** — Specific advice (e.g., "Emphasize keyboard shortcuts early")
6. **Common Student Challenges** — Known pain points
7. **Resources** — Solution files, Discord, documentation links
8. **Assessment Criteria** — What good work looks like (e.g., "Fully constrained sketches")

### Pedagogical Approach: "Self-Paced Blended Learning"
- Offload curriculum (concepts, exercises) to the website
- Free up educator time for **individualized help and review**
- Meeting/class time = students working at own pace + educator reviewing work
- Benefits: increased motivation, reduced knowledge gaps, personalized support

---

## 8. Design Handbook (Reference Wiki)

Unlike the learning course, the handbook is **not sequential**. It's organized by topic:

### Categories
1. **Hardware** — Materials, structure, fasteners, sheet metal, 3D printing, tolerances, weight saving
2. **Power Transmission** — Motion components, rotation, linear extension, motors, wheels, pneumatics, electronics
3. **Mechanisms** — Drivetrains, elevators, arms, linkages, intakes, shooters, turrets, bumpers
4. **Design Write-ups** — In-depth articles (controllability, chain tensioning, bumper mounting, springs)

Each page teaches **concepts and fundamentals** rather than step-by-step build instructions.

---

## 9. Mechanism Examples (Case Studies)

Hand-picked real-robot designs with detailed breakdowns. Organized by mechanism type:

| Category | Sub-categories |
|---|---|
| Drivebases | Swerve (6 examples), Tank |
| Intakes | Pivoting/Slapdown (5), Linkage (4), UTB |
| Shooters | 5 examples |
| End Effectors | Index page |
| Indexers | Index page |
| Elevators | Continuous (2), Cascade (3) |
| Telescopes | Index page |
| Pivots | 4 examples |
| Turrets | Index page |

Each example references a specific team and season (e.g., "1678's Crescendo Intake") with CAD links and analysis of design decisions.

---

## 10. Design Challenges (Competitive Practice)

Weekly challenges run through their Discord community:

### Structure
- **Cadence:** Weekly (5 weeks documented: Swerve Drivebase, Gearboxes, Ball Shooter, Intake, Tilt Shift)
- **Scoring:** 7 points max per challenge
  - 4 points for completion
  - 3 bonus points: Simplicity, Quality, Special (challenge-specific)
- **Late submissions:** Up to 2 completion points + all 3 bonus points
- **Leaderboards:** Separate Beginner vs. Intermediate/Advanced tracks (honor system)
- **Submission:** Via Discord

---

## 11. Community & Contribution Model

### Discord Server
- Central community hub (`discord.gg/qdx7pdZKx4`)
- Design reviews, challenges, events, feedback
- Channels: `#website-feedback`, `#public-website-contribution` (forum)

### Contribution Workflow
Two paths for contributors:

#### Public Contributors
1. Propose contribution in `#website-feedback` with template (issue, solution, timeline, platform)
2. Get approval from internal contributor
3. Either:
   - Fork repo on GitHub → submit PRs
   - Work in Google Docs/Notion → internal contributor ports to site
   
#### Internal Contributors
- Direct repo access, branch-based workflow
- GitHub Desktop recommended for version control
- Branch naming convention: descriptive of changes (e.g., `3A-cleanup`)

### Style Guide Principles
1. **Futureproofing** — Teach fundamentals, not current meta
   - "Pros and cons are context dependent; fundamentals are universal"
   - Don't recommend specific products without explaining why
2. **Don't deal in absolutes** — Use pros/cons, explain WHY things are good/bad
3. **Competitive standpoint** — Leave out unpopular/non-functional approaches
4. **Minimize opinions** — Don't speak authoritatively without first-hand experience
5. **Trends are temporary** — Think about underlying benefits, not popularity

### Content Standards
- **Images:** Compress to `.webp` via Squoosh
- **Videos:** Short → `.webm`, Long → YouTube embed
- **External links:** Open in new tab with `{:target="_blank"}`
- **CAD doc links:** Centered button style with `.md-button .md-button--primary`
- **Internal links:** Relative paths, same tab
- **Brand:** Follow FIRST® trademark guidelines, check The Blue Alliance for team names

---

## 12. What Makes FRCDesign.org Work (Key Takeaways for Replication)

### 1. MkDocs + Material is the Right Choice for This Kind of Site
- Extremely low barrier for content contributors (just write Markdown)
- Rich feature set out of the box (search, tabs, admonitions, code blocks)
- Fast, SEO-friendly static site
- Active development and community

### 2. Content Architecture = Course + Reference + Examples
The three-pillar model is powerful:
- **Learning Course** (sequential, guided) — for beginners learning skills
- **Design Handbook** (non-sequential wiki) — for reference during projects
- **Mechanism Examples** (curated case studies) — for inspiration and analysis

### 3. The Educator's Guide is a Multiplier
A parallel companion guide for teachers/mentors makes the site useful for teams, not just individuals. It provides structure, time estimates, and assessment criteria.

### 4. Self-Paced with External Review
The "blended learning" model where:
- Website handles curriculum delivery
- Humans handle review, feedback, and individualized support
- Discord bridges the gap

### 5. Consistent Stage Structure
Stage 2 demonstrates the ideal repeatable pattern:
```
Introduction → Project Overview → Engineering Concepts → Layout → Build → Summary
```
This consistency helps both content creators and learners.

### 6. Progressive Difficulty with Decreasing Guidance
- Stage 1: Very guided (video tutorials, step-by-step)
- Stage 2: Concepts taught, then guided project
- Stage 3-4: Replicate real designs, minimal hand-holding

### 7. Community-Driven Content
- Open-source with clear contribution guidelines
- Style guide ensures consistency
- Discord for feedback and coordination

---

## 13. Adapting This for FRC Programming

### Direct Parallels to Plan For

| FRCDesign.org | FRC Programming Equivalent |
|---|---|
| Learning Course (CAD skills) | Learning Course (programming skills — Java/WPILib, command-based, etc.) |
| Course Setup (Onshape account) | Course Setup (VS Code, WPILib, Git, GitHub) |
| Stage 1: Fundamentals (sketches, parts, assemblies) | Stage 1: Fundamentals (syntax, OOP, robot project structure, basic commands) |
| Stage 2: Mechanisms (shooter, intake, elevator) | Stage 2: Subsystems (drivetrain, shooter, intake, elevator code) |
| Stage 3: Full robot integration | Stage 3: Full robot code (autonomous routines, state machines, integration) |
| Stage 4: Mastery | Stage 4: Advanced (vision, path planning, custom control loops, simulation) |
| Design Handbook (reference wiki) | Programming Handbook (WPILib reference, control theory, networking, etc.) |
| Mechanism Examples (real robot CADs) | Code Examples (real robot code repos with analysis) |
| Best Practices (CAD conventions) | Best Practices (code style, project structure, Git workflow, testing) |
| Design Challenges (weekly) | Programming Challenges (weekly coding tasks) |
| Educator's Guide | Educator's Guide (for programming mentors/leads) |
| Glossary (CAD terms) | Glossary (programming/FRC terms) |
| Exercises with Onshape documents | Exercises with GitHub template repos or starter code |

### Technical Recommendations
1. **Use MkDocs + Material** — Same stack, proven for this exact use case
2. **Add code syntax highlighting** — Material supports it natively with Pygments
3. **Consider `pymdownx.highlight` + `pymdownx.inlinehilite`** — For inline code highlighting
4. **Add `content.code.annotate`** — Already in their config; lets you add numbered annotations to code blocks
5. **Consider adding tabs for language variants** — If supporting Java + Python (WPILib supports both)
6. **Use GitHub template repos** — Instead of Onshape document links, link to GitHub template repos students can fork
7. **Embed a code runner** — Consider tools like Replit embeds or custom sandboxes for interactive exercises

---

## 14. Quick-Start: Minimum Viable Setup

```bash
# 1. Create project
mkdir FRC-Programming-Site && cd FRC-Programming-Site

# 2. Set up Python environment  
python -m venv venv
source venv/bin/activate

# 3. Install dependencies
pip install mkdocs-material mkdocs-glightbox pymdown-extensions

# 4. Initialize MkDocs
mkdocs new .

# 5. Configure mkdocs.yml (copy/adapt FRCDesign's config)
# 6. Create docs/ structure mirroring their architecture
# 7. Start writing content
mkdocs serve  # Preview at http://127.0.0.1:8000
```

### Minimum `mkdocs.yml` to Match FRCDesign's Feature Set
```yaml
site_name: FRCProgramming.org
site_description: A comprehensive learning guide for FRC programming.

theme:
  name: material
  features:
    - navigation.sections
    - navigation.indexes
    - navigation.tabs
    - navigation.tabs.sticky
    - navigation.instant
    - navigation.footer
    - navigation.top
    - navigation.path
    - toc.follow
    - search.suggest
    - search.highlight
    - content.tabs.link
    - content.code.annotate
    - content.code.copy
    - content.tooltips
    - header.autohide
  palette:
    - scheme: slate
      primary: deep purple   # Pick your color
      accent: white
      toggle:
        icon: material/lightbulb-outline
        name: Switch to Light Mode
    - scheme: default
      primary: deep purple
      accent: black
      toggle:
        icon: material/lightbulb
        name: Switch to Dark Mode

markdown_extensions:
  - abbr
  - attr_list
  - md_in_html
  - def_list
  - admonition
  - pymdownx.details
  - pymdownx.superfences
  - pymdownx.snippets
  - pymdownx.tabbed:
      alternate_style: true
  - pymdownx.highlight:
      anchor_linenums: true
  - pymdownx.inlinehilite
  - pymdownx.emoji:
      emoji_index: !!python/name:material.extensions.emoji.twemoji
      emoji_generator: !!python/name:material.extensions.emoji.to_svg

plugins:
  - search
  - glightbox

nav:
  - Home: index.md
  - Learning Course:
      - learning-course/index.md
      - Course Setup:
          - learning-course/setup/environment.md
          - learning-course/setup/wpilib.md
          - learning-course/setup/git.md
      - Stage 1: ...
      - Stage 2: ...
  - Programming Handbook:
      - handbook/index.md
  - Code Examples:
      - examples/index.md
  - Best Practices:
      - best-practices/index.md
  - Resources:
      - resources/index.md
  - Educator's Guide:
      - educators/index.md
```
