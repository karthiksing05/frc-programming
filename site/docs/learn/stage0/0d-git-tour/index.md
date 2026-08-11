# Lesson 0D — Git + project tour <small>· Stage 0</small>

<span class="stage-badge">Stage 0 · Lesson 0D</span>

*Three commands. One file edit. One successful push. That's all Git is on day one — and the only Git you need until something forces more.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 0 |
    | **Time** | ~30 min |
    | **Prereqs** | [Lesson 0A](../0a-install/) · [Lesson 0B](../0b-meet-presto/) · [Lesson 0C](../0c-meet-kelpie/) |
    | **Edits** | `README.md` — one line, just to confirm the push works. |
    | **Tests** | None — success is the change showing up on GitHub. |
    | **Reference robot** | — |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Edit a file, **stage** it, **commit** it with a message, and **push** to GitHub — all from VS Code's source-control panel.
2. Confirm your change made it to the remote repo by reloading GitHub.com.
3. Navigate the curriculum project layout (`src/main/java/frc/robot/`, `lessons/`, `tools/`) and explain what each folder is for.
4. Find the `README.md` for any upcoming lesson.

---

## The real-world problem

Git is taught two ways. The wrong way: a 90-minute lecture on Merkle DAGs, commit hashes, the staging area, the index, and the difference between `HEAD~1` and `HEAD^`. Students glaze over and then panic the first time they see a merge conflict.

The right way — for now — is **ritual**: a three-step routine you run every time you finish a lesson. Edit. Commit. Push. That's it. Branching, merging, rebasing, the staging area as a concept — all of that is real, but **none of it bites you on day one**. We'll teach each piece the lesson where its absence first hurts.

!!! warning "Resist the Git rabbit hole"

    If a senior team member tells you "you have to learn `git rebase -i` before you can contribute" — politely smile and ignore them this week. You don't. You need *add, commit, push.* The rest comes when the rest is needed.

---

## What you'll do

### 1. Project tour (5 min)

Open the cloned `FRC-Programming` folder in WPILib VS Code (`File → Open Folder…`). Take 30 seconds to orient yourself in the file tree:

```text
FRC-Programming/
├── README.md                       ← you'll edit this in a minute
├── build.gradle                    ← Gradle build script; rarely touched
├── gradlew, gradlew.bat            ← the Gradle wrappers; never edit
├── src/
│   ├── main/java/frc/robot/        ← THE ROBOT CODE LIVES HERE
│   │   ├── Robot.java              ← entry point
│   │   ├── RobotContainer.java     ← where you wire subsystems + buttons
│   │   ├── Constants.java          ← constants live here
│   │   ├── subsystems/             ← one folder per subsystem (Lesson 04+)
│   │   └── util/                   ← shared helpers (Lesson 01+)
│   └── test/java/frc/robot/        ← the lesson rubrics (JUnit 5)
├── lessons/                        ← per-lesson README + hints (mirrors the site)
├── tools/
│   └── frcprog.sh                  ← the doctor script + helpers
├── process/                        ← curriculum design docs (read for context)
└── site/                           ← what you're reading right now (MkDocs)
```

!!! info "The most important folder"

    `src/main/java/frc/robot/` is where you'll spend 95% of your time after Stage 0. Memorize that path. Every lesson from Lesson 01 onward points you at a file inside it.

### 2. Make a change (3 min)

Open `README.md` at the repo root. Find the line near the top that looks like:

```markdown
Welcome! This is the FRCProgramming.org curriculum project.
```

Add your name (or your team's name, or a robot pun) on the next line, like this:

```diff
 Welcome! This is the FRCProgramming.org curriculum project.
+
+— first commit by Alex, Team 9999.
```

Save the file (`Ctrl/Cmd+S`).

!!! tip "VS Code knows you changed something"

    Look at the left sidebar. The **Source Control** icon (it looks like a Y-shaped branch) now has a little blue circle with a `1` in it — that's the count of files that have changed since your last commit. Click it.

### 3. Stage, commit, push from VS Code (5 min)

The Source Control panel has three things to learn, all in one place.

**(a) Stage your change.** Find `README.md` in the "Changes" list. Hover over it and click the **`+`** icon that appears on the right. The file moves from "Changes" up to "Staged Changes." That's the **staging area** — files queued to be included in the next commit.

!!! note "You can ignore staging for now"

    The staging area exists so you can choose *which* changes go into a commit when you have many. With one file, "stage" is just a button you click. We'll revisit it the day it matters.

**(b) Commit.** In the message box at the top of the Source Control panel, type:

```text
Lesson 0D: first push from my machine
```

Then click the big **Commit** button. The "Staged Changes" list empties — your change is now saved *locally* in Git's history.

**(c) Push.** A small **Sync Changes** button appears at the top. Click it. VS Code will:

1. Push your local commit up to GitHub.
2. Pull anything new from GitHub (there won't be).
3. Show a status notification.

If this is the first time you've pushed from this machine, VS Code will prompt you to **authenticate with GitHub**. Use the browser flow — click through, log in, click "Authorize Visual Studio Code." It's a one-time pain.

### 4. Verify on GitHub.com (2 min)

Open [github.com/karthiksing05/FRC-Programming](https://github.com/karthiksing05/FRC-Programming) (or your fork, if you forked it) in a browser. Look at the `README.md` at the top of the page. **Your line should be there.**

!!! success "If you see your line on GitHub, you're done."

    That's the Git ritual. Edit. Stage. Commit. Push. Verify. You'll do it after every single lesson from here on.

---

## The terminal equivalent (learn it once)

VS Code's source-control buttons run these commands under the hood:

```bash
git add README.md
git commit -m "Lesson 0D: first push from my machine"
git push
```

That's the **three-command ritual**. Memorize the words even if you click the buttons.

!!! warning "Deliberately not taught this lesson"

    Branching, merge conflicts, `.gitignore`, rebasing, pull requests. Every concept you absorb before you need it is a concept you'll re-absorb later. We'll teach each piece the lesson where its absence first hurts.

---

## Finding your next lesson

You're about to leave Stage 0. Two places hold the lesson content from here:

- **This site** (`site/docs/learn/...`) — what you're reading. The polished, hyperlinked version.
- **`lessons/<NN-slug>/README.md`** in the repo — the same content beside the code you edit. Handy when you're already in VS Code and don't want to alt-tab.

They're the same words. Use whichever is closer to your hands.

!!! tip "The next lesson"

    Lesson 01 — Methods (Functions) — lives at:

    - On this site: [Stage 1A · Lesson 01](../../stage1a/01-methods/)
    - In the repo: `lessons/01-methods/README.md`

    Open one of those when you're ready. Stage 1A is where you finally start writing code.

---

## What if something went wrong?

??? tip "Click for troubleshooting"

    **`git push` rejected — "non-fast-forward."**
    Somebody pushed before you. Run `git pull` first, then push again. If `git pull` complains about merges, ask your mentor.

    **"Author identity unknown."**
    One time, in the terminal:

    ```bash
    git config --global user.name "Your Name"
    git config --global user.email "you@example.com"
    ```

    Use the email on your GitHub account — your commits will then show up with your avatar.

    **The change isn't showing up on GitHub.**
    Did the push finish (no errors in the VS Code status bar)? Refresh the GitHub tab — your browser might be showing a cached page.

---

## Going further (optional)

- Bookmark [GitHub's "About Git" intro](https://docs.github.com/en/get-started/using-git/about-git) for a quiet 10 minutes later.
- If you want SSH-based Git auth (no password prompts ever), follow [GitHub's SSH key setup](https://docs.github.com/en/authentication/connecting-to-github-with-ssh).

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-arrow-left:{ .lg .middle } __Previous__

    ---

    **Lesson 0C**
    Meet Kelpie

    [:octicons-arrow-left-24: Back to lesson 0C](../0c-meet-kelpie/)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 01**
    Methods (Functions) — Stage 1A begins

    [:octicons-arrow-right-24: Continue to lesson 01](../../stage1a/01-methods/)

</div>
