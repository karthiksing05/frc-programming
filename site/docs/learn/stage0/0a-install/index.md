# Lesson 0A — First-run install <small>· Stage 0</small>

<span class="stage-badge">Stage 0 · Lesson 0A</span>

*The single biggest funnel killer in FRC programming is "I tried to install it once and couldn't." We're going to make that impossible.*

!!! abstract "Lesson stats"

    | | |
    |---|---|
    | **Stage** | 0 |
    | **Time** | ~60 min (40 min hands-on, 20 min waiting on downloads) |
    | **Prereqs** | None — this is lesson one. |
    | **Edits** | None — you're installing tools, not writing code. |
    | **Tests** | `./tools/frcprog.sh doctor` (exit code 0 = green). |
    | **Reference robot** | — |

---

## What you'll learn

By the end of this lesson you'll be able to:

1. Install **WPILib 2026**, including the bundled JDK and bundled VS Code.
2. Install and run **AdvantageScope**, the log/telemetry viewer you'll use forever after.
3. Install **Git** and confirm `git --version` answers from the terminal.
4. Clone the curriculum template repo into a sensible folder.
5. Run `./tools/frcprog.sh doctor` and get five green checkmarks back.

---

## The real-world problem

Every FRC programming season starts the same way: somebody downloads "Java," somebody else downloads VS Code from the marketplace, a third person installs Git Bash on Windows, and three days later the team can't agree on whose laptop builds the project and whose doesn't. The fix is to use **exactly the tools WPILib ships**, exactly once, on day one.

WPILib's installer is large because it bundles its own JDK, its own VS Code, and its own simulation toolkit. That's a feature, not a bug. It means every laptop on your team runs identical tooling.

!!! info "Friction is the enemy"

    First-time install genuinely takes about an hour, and most of it is just waiting on the download. Don't panic if it feels slow. Put on music, make a tea, and check back in 15 minutes. **You only do this once.**

---

## What you'll do

Five tasks, in order. Don't skip ahead — each one assumes the previous is done.

### 1. Install WPILib 2026

Grab the installer for your platform from the WPILib release page:

[Download WPILib 2026 :material-download:](https://github.com/wpilibsuite/allwpilib/releases){ .md-button .md-button--primary }

When the installer asks, pick **"Everything"** (the offline option). Yes, it's a 2+ GB download. Yes, that's normal.

!!! warning "Do NOT install system Java first"

    If you already have a Java JDK on your machine (Oracle, OpenJDK, Homebrew, whatever), leave it alone — but **don't** add WPILib's tools to a folder that's already on your `JAVA_HOME` path. WPILib ships its own JDK and expects to use that one. Mixing them is the #1 source of "it builds on my friend's laptop but not mine" tickets.

After install, look for **WPILib VS Code** in your applications list. That's the editor you'll use — not "Visual Studio Code," which is a *different* program. They look identical. Pin the WPILib one to your taskbar so you don't pick wrong later.

### 2. Install AdvantageScope

[AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope) is the telemetry and log-viewer made by Team 6328 (Mechanical Advantage). Every team you'll meet uses it. Grab the latest installer:

[Download AdvantageScope :material-download:](https://github.com/Mechanical-Advantage/AdvantageScope/releases){ .md-button }

Open it once after installing. You should see an empty window with a sidebar. Close it for now; we'll come back next lesson.

### 3. Install Git

=== "Windows"

    Install [Git for Windows](https://git-scm.com/download/win). When the installer asks about the default editor, pick **VS Code**. Accept all other defaults.

=== "macOS"

    Open Terminal and type:

    ```bash
    git --version
    ```

    The first time you run this on a fresh macOS, the OS will prompt to install the Command Line Tools. Click **Install** and wait it out (~10 minutes).

=== "Linux"

    Use your distro's package manager:

    ```bash
    sudo apt install git        # Debian/Ubuntu
    sudo dnf install git        # Fedora
    sudo pacman -S git          # Arch
    ```

Confirm it worked by running `git --version` from a fresh terminal. You should see something like `git version 2.43.0` (the exact number doesn't matter).

### 4. Clone the curriculum template

Decide on a folder. Use **anywhere except OneDrive, iCloud Drive, or Google Drive folders** — file-syncing services will fight Gradle and you'll lose.

!!! danger "Cloud-synced folders break builds"

    OneDrive, iCloud, and Google Drive grab file locks while syncing. Gradle expects to be the only thing touching `build/` and `.gradle/`. Mix the two and your builds will fail intermittently with messages that look like file-system corruption. **Clone somewhere local.** A folder like `~/code/frc/` (macOS/Linux) or `C:\Users\you\frc\` (Windows) works great.

In your terminal:

```bash
git clone https://github.com/karthiksing05/FRC-Programming.git
cd FRC-Programming
```

On macOS or Linux only, give the Gradle wrapper permission to run:

```bash
chmod +x gradlew
```

### 5. Run the doctor

The repo ships a script that checks your install for the seven things that bite first-timers:

```bash
./tools/frcprog.sh doctor
```

You want five green checkmarks. If any line is red, the script tells you exactly what to fix.

!!! success "Five green checks"

    When `frcprog doctor` exits 0 and prints all green, you are done. You can write code from this machine now. Pat yourself on the back — this was the hard part.

---

## What if something went wrong?

??? tip "Click to expand the troubleshooting tree"

    **`./gradlew` says "permission denied" on macOS or Linux.**
    You forgot `chmod +x gradlew`. Run it now.

    **`git: command not found`** even after you installed it.
    You need a *fresh* terminal — the one that was open when you installed Git doesn't know about it yet. Close every terminal and reopen.

    **WPILib VS Code won't open the project.**
    Make sure you opened the *folder* (`File → Open Folder…`), not a single file. WPILib's project view depends on the folder being the project root.

    **`frcprog doctor` says "system Java is shadowing WPILib's."**
    Your `JAVA_HOME` environment variable points at a non-WPILib JDK. On macOS/Linux, run `unset JAVA_HOME` in your shell config (`~/.zshrc` or `~/.bashrc`) so WPILib's bundled JDK wins. On Windows, edit your environment variables to remove `JAVA_HOME`.

    **Still stuck?** Ask in your team's Discord or open a [discussions thread](https://github.com/karthiksing05/FRC-Programming/discussions). Don't suffer in silence — install problems are the *least* interesting problem you'll have all season, and other people have hit yours before.

---

## Going further (optional, but nice)

- Install [Choreo](https://choreo.autos/) now. We won't use it until Lesson 13, but the download is small and it's nice to have.
- Open WPILib VS Code's command palette (`Ctrl/Cmd+Shift+P`) and type `WPILib`. Skim the commands available — you'll use several of them by Stage 1C.
- Read [WPILib's official install guide](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html) if you want a second perspective.

---

<div class="grid cards lesson-footer-nav" markdown>

-   :material-flag-checkered:{ .lg .middle } __Start of course__

    ---

    You're at the beginning! There's no previous lesson.

    [:octicons-home-24: Course home](../../)

-   :material-arrow-right:{ .lg .middle } __Next__

    ---

    **Lesson 0B**
    Meet Presto — the shooter robot

    [:octicons-arrow-right-24: Continue to lesson 0B](../0b-meet-presto/)

</div>
