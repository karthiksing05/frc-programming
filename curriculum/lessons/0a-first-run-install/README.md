# Lesson 0A — Install

**Stage 0 · 60 min · Needs: nothing**

Install once. Most of the hour is waiting on a download.

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

| Piece | Why you need it |
|---|---|
| Java 17 JDK | Everyone on your team compiles the same way |
| WPILib's own VS Code | Already configured, extension installed |
| Offline Maven repository | Every library, already on disk. This is what makes the rest work with no internet. |
| AdvantageScope | Plots. You live in this from Stage 1C. |
| Simulator | Runs your robot without a robot |

## Watch out for

**"Unsupported class file major version"**
You typed `gradle` instead of `./gradlew`. The `./` and the `w` both matter.

**Cloud-synced folders**
Gradle holds files open while OneDrive syncs them. Builds fail at random and look
corrupted. Nobody ever guesses the cause.

**Windows Firewall popup on first run**
Click **Allow**. It is the simulator opening a local port. Clicking Cancel breaks
your plots two lessons later.

**Very slow builds on Windows**
Antivirus scanning Gradle's cache. Ask IT to exclude the project folder and
`%USERPROFILE%\.gradle`. Twenty minutes becomes two.

**macOS "unidentified developer"**
Right-click the app, choose Open.

## Done

`frcprog doctor` is green and `./gradlew build` succeeds.

That proves your JDK, build system, libraries and simulator all work. From here,
any problem is in code you wrote, which is a much better problem.

```bash
./tools/frcprog next
```
