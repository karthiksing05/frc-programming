# Set up

Three things, in order. Budget about an hour, most of it waiting on a download.

---

## 1. Install WPILib 2026

Download the installer from
[docs.wpilib.org](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html)
and run it. Choose **Everything** and **Install for this User**.

It is a 2–3 GB download. **Do this at home**, on a network you control. Thirty
students installing simultaneously on school Wi-Fi at the first meeting of the year
is a tradition, and it is a bad one.

What you get, and why each piece matters:

| Piece | Why |
|---|---|
| A Java 17 JDK | Everyone on your team compiles with the same compiler |
| Its own copy of VS Code | Pre-configured, extension already installed |
| An offline Maven repository | **Every library this curriculum needs, already on disk** |
| AdvantageScope | The plotting tool you live in from Stage 1C onward |
| Simulation tools | The robot simulator and driver station |

!!! warning "Use WPILib's VS Code, not the one you already had"

    If you already had VS Code, you now have two. The WPILib one knows where its
    Java is; a normal one does not, and the resulting errors are cryptic in a way
    that wastes afternoons. On macOS it is *WPILib VS Code 2026* in Applications; on
    Windows and Linux the installer offers a separate shortcut.

---

## 2. Get the curriculum

Copy the `curriculum` folder to somewhere short and local:

- macOS / Linux — `~/dev/frc-curriculum`
- Windows — `C:\dev\frc-curriculum`

!!! danger "Not in OneDrive, iCloud, Dropbox, or Google Drive"

    Those services sync files while Gradle is holding them open. Builds then fail at
    random with errors that look like file corruption, and nobody ever guesses that
    cloud sync is the cause.

---

## 3. Check yourself

Open the folder in WPILib VS Code, open a terminal (**Terminal → New Terminal**), and
run:

=== "macOS / Linux"

    ```bash
    ./tools/frcprog doctor
    ```

=== "Windows"

    ```bat
    tools\frcprog.cmd doctor
    ```

`doctor` checks the half-dozen things that actually go wrong — wrong JDK, missing
offline repository, cloud-synced folder — and tells you what to do about each. Work
down the list until everything is green.

Then:

```bash
./gradlew build
```

You are looking for `BUILD SUCCESSFUL`. The first run takes a couple of minutes while
Gradle unpacks itself; later ones take seconds.

That milestone is worth being clear about: it proves your JDK works, your build
system works, the libraries are present, and the simulation layer loaded. Every
problem after this is a problem with code you wrote, which is a much better kind of
problem to have.

---

## Then

```bash
./tools/frcprog next
```

[Start Lesson 0A :material-arrow-right:](../learn/stage0/0a-first-run-install/index.md){ .md-button .md-button--primary }

---

## When it goes wrong

Ranked by how often it actually happens.

**"Unsupported class file major version"**
:   Gradle is using some other Java. You typed `gradle` instead of `./gradlew`. The
    leading `./` and the `w` both matter — `gradlew` is a script in the project that
    finds the right Java for you.

**The build hangs trying to download something**
:   Something is reaching the network when it should not. Check that
    `settings.gradle` still contains the `frcHome` maven block.

**Windows Firewall popup on first run**
:   Click **Allow**. It is the JVM opening a local port so the simulator and
    AdvantageScope can talk. Clicking Cancel out of reflex means your plots never
    connect, and the symptom appears two lessons later looking like a different
    problem entirely.

**Builds are extremely slow on Windows**
:   Antivirus scanning Gradle's cache. Ask whoever administers the machine to
    exclude the project folder and `%USERPROFILE%\.gradle`. This routinely turns a
    twenty-minute build into a two-minute one.

**macOS: "app is from an unidentified developer"**
:   Right-click the app and choose **Open** rather than double-clicking.

Still stuck? Write down the *exact* error text — not a paraphrase — and ask. "It
doesn't work" is unanswerable; the real message is usually enough for someone to
recognise the problem immediately.
