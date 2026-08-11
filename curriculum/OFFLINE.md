# Working offline

This project is built to run on a laptop that has not been online since the WPILib
installer finished. Not "works offline if you prepared carefully" — offline is the
default, and going online is the special case.

This page explains how that works, so that when something does need a network you
know exactly what and why.

---

## Why bother

Because the alternative fails at exactly the wrong moment.

A first team meeting is thirty students opening laptops on one school access point.
A build-season Saturday is a district Wi-Fi captive portal that blocks Maven Central.
A competition pit has no useful internet at all. In every one of those situations a
toolchain that reaches out to the internet on every build is a toolchain that does
not work.

The friction list in `process/Infrastructure-Analysis.md` §3.10 ranks school network filters as one of the top causes of lost onboarding time in
FRC. This project's answer is to not depend on the network at all.

---

## How it works

### 1. WPILib ships a complete Maven repository

Your WPILib install contains every artifact this project needs:

| Platform | Location |
|---|---|
| macOS / Linux | `~/wpilib/2026/maven` |
| Windows | `C:\Users\Public\wpilib\2026\maven` |

GradleRIO, every WPILib library, the native simulation binaries, and JUnit 5 are all
in there. It is a few hundred megabytes, and it arrived when you ran the installer.

### 2. `settings.gradle` points Gradle at it

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { name = 'frcHome'; url = frcHomeMaven }
    }
}
```

That `frcHome` entry is what lets Gradle resolve `edu.wpi.first.GradleRIO` and
everything downstream of it without a network. Delete it and the whole offline story
collapses.

### 3. Offline mode is forced, for every invocation

Also in `settings.gradle`:

```groovy
if (System.getenv('FRCPROG_ONLINE') == null) {
    startParameter.offline = true
}
```

This matters more than passing `--offline` by hand, because it also applies to the
builds VS Code's WPILib extension fires when you click **Build Robot Code** or
**Simulate Robot Code**. Those go through Gradle's API rather than your terminal, and
a flag you type is a flag they never see.

> **Why not `gradle.properties`?** Gradle silently ignores an `org.gradle.offline`
> property there. Offline mode is only settable from the command line or from
> `settings.gradle`. This is worth knowing because setting it in the obvious place
> appears to work and does nothing.

### 4. Nothing here is a vendordep

The project depends on WPILib and `WPILibNewCommands`, both of which ship in the
offline repository. It deliberately does not depend on AdvantageKit, PathPlanner,
PhotonVision, or maple-sim — all excellent, all downloads.

That constraint shapes a few lessons. Lesson 13 follows a trajectory using WPILib's
own `TrajectoryGenerator` rather than Choreo. Lesson 16 teaches the IO Layer pattern
with a hand-written inputs class rather than AdvantageKit's `@AutoLog`.

Neither is a watered-down version. The concepts are identical, and writing the inputs
class by hand once is arguably the better way to understand what the annotation
generates. See `lessons/EXTENSIONS.md` for how to add the
real thing when you have network.

---

## Checking that it works

```bash
./tools/frcprog doctor
```

Among other things, this verifies your WPILib install, confirms the offline Maven
repository is present, and runs a real compile with the network disabled.

To satisfy yourself directly, disconnect from Wi-Fi and run:

```bash
./gradlew build
```

It should succeed. If it does not, `doctor` will tell you why.

---

## When you do need the network

Exactly one situation: adding a vendor library.

```bash
./tools/frcprog build --online
```

or equivalently:

```bash
FRCPROG_ONLINE=1 ./gradlew build --refresh-dependencies
```

Do this once, with a working connection, after adding the vendordep JSON. Gradle
caches everything it downloads in `~/.gradle/caches`, so every subsequent build is
offline again with no further action.

---

## Setting up a machine that has never been online

The realistic case: a team laptop, a shared drive, and no expectation of internet at
the meeting.

**Everything needed is in two places** — the WPILib install, and this folder.

1. Put the WPILib installer for each platform on a USB stick. Run it on each machine.
   This is the only step that ever needed a network, and you did it once, at home.
2. Copy the `curriculum` folder onto each machine — USB stick, shared drive, zip
   file, anything.
3. Copy your `~/.gradle` folder too, if you want to skip even the Gradle
   distribution unpack. Optional; the WPILib installer places the Gradle
   distribution in the right place already.
4. `./tools/frcprog doctor` on each machine.

No accounts, no cloning, no server, nothing to sign into.

---

## What "offline" deliberately does not mean

This curriculum is offline in the sense of **not depending on a network or a cloud
service**. A few things are worth being explicit about:

**No GitHub, no CI, no pull requests.** The online version of this curriculum would
run every rubric on a server and post results to a PR. Here, `frcprog check --all` is
that server, and it runs on your laptop in about a minute. If your team does use Git,
use it — lesson 0D shows the three commands that matter — but nothing here requires
it.

**No accounts and no telemetry.** Your progress lives in `.frcprog/progress.json`,
in this folder, and goes nowhere. Delete it and you have reset your progress; nothing
else notices.

**The reference robots are still on GitHub.** Lessons 0B and 0C link to Presto and
Kelpie, and reading real competition code is genuinely valuable. If you cannot reach
GitHub, those two lessons lose their browsing exercise and nothing else breaks.

**The site is served locally.** `./tools/frcprog site` runs MkDocs on
`localhost:8000`. The one thing that needs a network is the initial `pip install` of
MkDocs itself; after that it serves offline. If you would rather not, every lesson
reads perfectly well in the terminal via `frcprog read`.

---

## Troubleshooting

**"Could not resolve edu.wpi.first..." or "No cached version available for offline
mode"**

Gradle cannot find something in the offline repository. Either WPILib is not
installed where it expects, or you added a vendordep and have not done the one online
build. Run `./tools/frcprog doctor` — it distinguishes these cases.

**Builds hang for thirty seconds and then work**

Something is still trying to reach the network and timing out. Check that
`settings.gradle` still has the offline block, and that `FRCPROG_ONLINE` is not set
in your shell.

**Builds are slow on Windows even offline**

Antivirus scanning Gradle's cache. Exclude your project folder and `~/.gradle` /
`%USERPROFILE%\.gradle`. This routinely turns a twenty-minute build into a
two-minute one and is the highest-value thing you can ask an IT department for.

**Everything worked yesterday and now nothing resolves**

Check whether the project moved into OneDrive, iCloud Drive, or a Dropbox folder.
Cloud sync and Gradle's file locking interact badly, and the errors look like
corruption rather than like sync. `frcprog doctor` checks for this.
