# Lesson 0A — Install

**Stage 0 · 60 min · Needs: nothing**

You install this once and then never think about it again. Most of the hour is spent
waiting for a download, so start it and go and do something else.

If the words WPILib, Gradle or AdvantageScope mean nothing to you yet, read
[The software, explained](../../../orientation/software.md) first. This lesson tells
you what to install; that page tells you what each piece actually is, which makes the
next ten lessons make a great deal more sense.

## Do this

1. **Download WPILib 2026** from [docs.wpilib.org](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html).
   Pick **Everything** and **Install for this User**.

    Do it at home. It is 2–3 GB. Thirty students on one school access point does not work.

2. **Copy the `curriculum` folder** somewhere short and local:

    - macOS / Linux: `~/dev/frc-curriculum`
    - Windows: `C:\dev\frc-curriculum`

    Not OneDrive, iCloud, Dropbox or Google Drive. See below.

3. **Open the folder** in WPILib VS Code. Not your normal VS Code. There are now two.

4. **Open a terminal** in VS Code (Terminal → New Terminal) and run:

    ```bash
    ./tools/frcprog doctor
    ```

    Fix anything it flags. It tells you what to do for each one.

5. **Build it:**

    ```bash
    ./gradlew build
    ```

    Look for `BUILD SUCCESSFUL`. First run takes a couple of minutes.

## What you just installed

That one installer put five separate things on your machine.

The **Java 17 JDK** is the compiler and runtime, shipped by WPILib so that every laptop
on your team builds with an identical version instead of whatever Java happened to be
there. **WPILib's own copy of VS Code** is the editor, already configured, with the
extension installed and pointed at that JDK.

The **offline Maven repository** is a folder holding every library the project needs,
already downloaded. This is the piece that makes the rest of the curriculum work with
no internet connection at all — Gradle looks in there rather than reaching out to a
server.

**AdvantageScope** draws graphs of what your robot is doing, and you will be using it
from Stage 1B onward. The **simulator** runs your robot code without a robot, which is
the only reason any of this is possible on a laptop.

## Watch out for

**"Unsupported class file major version"**
This means Gradle is running on the wrong Java. You almost certainly typed `gradle`
instead of `./gradlew` — the `./` and the `w` both matter, because `gradlew` is a
script inside the project that finds the correct Java for you.

**Cloud-synced folders**
Gradle holds files open while OneDrive or Dropbox tries to sync them underneath it.
Builds then fail at random with errors that look like file corruption, and almost
nobody guesses that cloud sync is the cause.

**Windows Firewall popup on first run**
Click **Allow**. It is the simulator opening a port on your own machine so that
AdvantageScope can connect to it. If you click Cancel out of reflex, your graphs will
silently fail to connect two lessons later and it will look like an unrelated problem.

**Very slow builds on Windows**
This is usually antivirus software scanning Gradle's cache every time it is touched.
Ask whoever administers the machine to exclude the project folder and
`%USERPROFILE%\.gradle`, which normally takes builds from several minutes back down to
a few seconds.

**macOS "unidentified developer"**
Right-click the app and choose Open, rather than double-clicking it.

## Done

`frcprog doctor` is green and `./gradlew build` succeeds.

That proves your JDK, build system, libraries and simulator all work. From here,
any problem is in code you wrote, which is a much better problem.

```bash
./tools/frcprog next
```
