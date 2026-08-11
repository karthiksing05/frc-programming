# Graduate to VS Code

<span class="stage-badge">Stage 1B · Lesson 03.5</span>

You did the browser part. Now here come the real tools.

## Why we make you switch

This curriculum has **two paths** by design — Path A (browser) and Path B (VS Code) — and you've just finished Path A.

The browser PoCs were a deliberately small surface. They let you write *real* WPILib-shaped code (the same package layout, the same method signatures, the same file paths) without first negotiating a 2.5 GB install. That trade-off is great for the first hour and counterproductive by hour ten. Real FRC code lives in VS Code with the WPILib extension, real autocomplete, real Git, real AdvantageScope, and real Gradle tests. From here on, every lesson assumes you're in that environment.

The good news: **you don't lose your work**. The button below downloads a fully-structured WPILib project that contains the files *you wrote in the browser*, dropped into the right places. The `applyDeadband` method you authored in Lesson 01 will be in `src/main/java/frc/robot/util/MathUtils.java` — exactly where Lesson 04 expects to import it.

## Download your project

<button id="downloadProject" class="md-button md-button--primary" type="button" disabled>Loading export tools…</button>

<p id="graduateStatus" style="margin-top: 0.5rem; color: var(--md-default-fg-color--light); font-size: 0.9rem;"></p>

!!! tip "First time? Read this first."
    Don't click the button until you've done at least Lessons 01–03 in the browser. The zip will still work if you haven't, but the point of the bridge is to carry forward *your* code — not to give you an empty skeleton you could have cloned from GitHub.

## After you download

Follow these in order. None of them are hard individually, but each one waits on the previous.

### 1. Install WPILib (one-time pain)

If you haven't already, install the WPILib release for the current FRC season:

- macOS / Linux / Windows: <https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html>

This installs **WPILib's own copy of VS Code** (separate from any VS Code you already have). Use *that* one, not your system VS Code — it bundles the WPILib extension and the right JDK out of the box. If you're rushed, the install runs ~15–30 minutes mostly idle in the background.

### 2. Unzip the project

Unzip the downloaded `my-frc-learning.zip` somewhere stable — your `Documents/` folder is fine. Don't unzip into a synced cloud folder (Dropbox/iCloud); Gradle's build cache fights with file sync.

You should see a folder called `my-frc-learning` containing `build.gradle`, `src/`, `lessons/`, and so on.

### 3. Open in VS Code

Launch **WPILib VS Code** (not system VS Code), then `File → Open Folder…` and pick the `my-frc-learning` folder.

The first time you open the project, the WPILib extension will:

- download a Gradle wrapper distribution (~80 MB)
- download AdvantageKit's vendor library (~30 MB)
- index your Java source tree

This takes 2–5 minutes. The status bar at the bottom tells you what's happening. Don't worry if the editor shows red squigglies during this phase — they go away once indexing finishes.

### 4. Verify the build

Open a terminal inside VS Code (`Terminal → New Terminal`) and run:

```bash
./gradlew lesson04
```

On Windows, use `.\gradlew.bat lesson04` instead.

If you see something like `BUILD SUCCESSFUL` and a JUnit-style test report — congratulations, you're set up. If you see `Lesson 04 ✓ (… passed)` your bridge crossing is complete.

If you see a build error, that's normal for Lesson 04 because you haven't *done* Lesson 04 yet. Look for `tests FAILED` rather than `BUILD FAILED` — the former means Gradle worked and is correctly telling you to write code; the latter means the toolchain isn't installed yet (re-do step 1 or check `frcprog doctor`).

### 5. Find your work

Open `src/main/java/frc/robot/util/MathUtils.java`. The `applyDeadband` body should match what you wrote in the browser. If it's still `// TODO: implement me!`, the bridge missed your file — go back to the browser tab, hit *Run* on Lesson 01 to re-save, and re-download.

## What changes about the curriculum

| Browser (Path A)                                | VS Code (Path B)                          |
| ----------------------------------------------- | ----------------------------------------- |
| Click *Run*, watch the canvas                   | `./gradlew lesson04`, read the JUnit log  |
| Files persist in `localStorage`                 | Files live on disk, tracked by Git        |
| One file per lesson                             | Whole subsystems per lesson               |
| Friendly errors with hints inline               | Real compiler errors (Niwiden says good!) |
| Java sim built in JavaScript                    | Real WPILib HALSim + AdvantageScope       |

From Lesson 04 onward, **every** lesson lives in the project you just downloaded. Open the *Lessons* tree in the WPILib extension to see what's next, or read `lessons/manifest.json` directly.

## If something goes wrong

- **The button stays disabled** → your browser blocked one of the three scripts. Disable adblockers for this page and reload.
- **The zip downloads but is empty / 1 KB** → your browser's `localStorage` for this site is empty. You're not signed in to the same origin you used for the lessons. Go back, do at least Lesson 01, and re-export.
- **VS Code says "no Java found"** → you opened it in system VS Code, not WPILib VS Code. Launch the one called `WPILib VS Code <year>` from your applications menu.
- **Stuck somewhere not on this list** → check the project's `GETTING-STARTED.md` (at the top of the zip), or open a thread in `#help` on Discord.

---

*Welcome to Path B. The training wheels come off; the real fun begins.*

<script src="../javascripts/graduate.js" defer></script>
